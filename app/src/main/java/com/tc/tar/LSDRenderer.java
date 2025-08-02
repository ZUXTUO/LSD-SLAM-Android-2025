package com.tc.tar;

import android.content.Context;
import android.opengl.GLES20;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.Toast;

import com.tc.tar.rajawali.PointCloud;

import org.rajawali3d.Object3D;
import org.rajawali3d.cameras.ArcballCamera;
import org.rajawali3d.materials.Material;
import org.rajawali3d.math.Matrix4;
import org.rajawali3d.math.Quaternion;
import org.rajawali3d.math.vector.Vector3;
import org.rajawali3d.primitives.Line3D;
import org.rajawali3d.renderer.Renderer;
import org.rajawali3d.util.ArrayUtils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Stack;

/**
 * LSD渲染器，负责Rajawali3D场景的初始化与渲染
 * Created by aarontang on 2017/4/6.
 */

public class LSDRenderer extends Renderer {
    public static final String TAG = LSDRenderer.class.getSimpleName();
    private static final float CAMERA_NEAR = 0.01f; // 相机近平面
    private static final float CAMERA_FAR = 200f;   // 相机远平面
    private static final int MAX_POINTS = 500000;   // 最大点云数量

    private float intrinsics[]; // 相机内参
    private int resolution[];   // 分辨率
    private Object3D mCurrentCameraFrame; // 当前相机帧对象
    private ArrayList<Object3D> mCameraFrames = new ArrayList<>(); // 所有关键帧相机对象
    private int mLastKeyFrameCount; // 上一次关键帧数量
    private PointCloud mPointCloud; // 点云对象
    private boolean mHasPointCloudAdded; // 是否已添加点云
    private RenderListener mRenderListener; // 渲染监听器

    private ArrayList<LSDKeyFrame> mAllKeyFramesForPoints = new ArrayList<>();
    private int mCurrentTotalPointsCount = 0;

    /**
     * 渲染监听接口
     */
    public interface RenderListener {
        void onRender();
    }

    public LSDRenderer(Context context) {
        super(context);
    }

    /**
     * 初始化场景
     */
    @Override
    protected void initScene() {
        // 获取相机内参和分辨率
        intrinsics = TARNativeInterface.nativeGetIntrinsics();
        resolution = TARNativeInterface.nativeGetResolution();

        // // 将分辨率和内参调整为原分辨率的三分之一
        // resolution[0] /= 3; // width
        // resolution[1] /= 3; // height
        // intrinsics[0] /= 3; // cx
        // intrinsics[1] /= 3; // cy
        // intrinsics[2] /= 3; // fx
        // intrinsics[3] /= 3; // fy

        // 创建轨迹球相机
        ArcballCamera arcball = new ArcballCamera(mContext, ((MainActivity)mContext).getView());
        arcball.setPosition(0, -4, 0);
        getCurrentScene().replaceAndSwitchCamera(getCurrentCamera(), arcball);
        getCurrentCamera().setNearPlane(CAMERA_NEAR);
        getCurrentCamera().setFarPlane(CAMERA_FAR);
        getCurrentCamera().setFieldOfView(37.5);

        // 绘制地面网格
        drawGrid();
    }

    @Override
    public void onOffsetsChanged(float xOffset, float yOffset, float xOffsetStep, float yOffsetStep, int xPixelOffset, int yPixelOffset) {
        // 保留空实现
    }

    @Override
    public void onTouchEvent(MotionEvent event) {
        // 保留空实现
    }

    /**
     * 每帧渲染回调
     */
    @Override
    protected void onRender(long ellapsedRealtime, double deltaTime) {
        super.onRender(ellapsedRealtime, deltaTime);
        // 绘制当前相机视锥
        drawFrustum();
        // 绘制关键帧及点云
        drawKeyframes();
        // 通知监听器
        if (mRenderListener != null) {
            mRenderListener.onRender();
        }
    }

    /**
     * 设置渲染监听器
     */
    public void setRenderListener(RenderListener listener) {
        mRenderListener = listener;
    }

    /**
     * 绘制地面网格
     */
    private void drawGrid() {
        getCurrentScene().addChildren(new LSDGridFloor().createGridFloor());
    }

    /**
     * 绘制当前相机视锥
     */
    private void drawFrustum() {
        float pose[] = TARNativeInterface.nativeGetCurrentPose();
        Matrix4 poseMatrix = new Matrix4();
        poseMatrix.setAll(pose);
        if (mCurrentCameraFrame == null) {
            mCurrentCameraFrame = createCameraFrame(0xff0000, 1);
            getCurrentScene().addChild(mCurrentCameraFrame);
        }
        mCurrentCameraFrame.setPosition(poseMatrix.getTranslation());
        mCurrentCameraFrame.setOrientation(new Quaternion().fromMatrix(poseMatrix));
    }

    /**
     * 绘制所有关键帧及点云
     */
    private void drawKeyframes() {
        int currentKeyFrameCount = TARNativeInterface.nativeGetKeyFrameCount();
        if (mLastKeyFrameCount < currentKeyFrameCount) {
            LSDKeyFrame[] keyFrames = TARNativeInterface.nativeGetAllKeyFrames();
            if (keyFrames == null || keyFrames.length == 0) {
                //Log.d(TAG, "未获取到关键帧数据");
                return;
            }

            // 绘制点云
            drawPoints(keyFrames);
            // 绘制关键帧相机
            drawCamera(keyFrames);

            mLastKeyFrameCount = currentKeyFrameCount;
        }
    }

    /**
     * 绘制点云
     */
    private void drawPoints(LSDKeyFrame[] keyFrames) {
        // 遍历新关键帧，添加到总列表并更新总点数
        for (LSDKeyFrame keyFrame : keyFrames) {
            mAllKeyFramesForPoints.add(keyFrame);
            mCurrentTotalPointsCount += keyFrame.pointCount;
        }

        // 如果点云数量超过上限，则移除最旧的关键帧
        while (mCurrentTotalPointsCount > MAX_POINTS && !mAllKeyFramesForPoints.isEmpty()) {
            LSDKeyFrame oldestKeyFrame = mAllKeyFramesForPoints.remove(0);
            mCurrentTotalPointsCount -= oldestKeyFrame.pointCount;
        }

        // 准备点云数据
        if (mCurrentTotalPointsCount == 0) {
            if (mPointCloud != null) {
                mPointCloud.updateCloud(0, null, null); // 清空点云
            }
            return;
        }

        float[] vertices = new float[mCurrentTotalPointsCount * 3]; // 每个点3个坐标分量
        int[] colors = new int[mCurrentTotalPointsCount];
        int vertexOffset = 0;
        int colorOffset = 0;

        for (LSDKeyFrame keyFrame : mAllKeyFramesForPoints) {
            System.arraycopy(keyFrame.worldPoints, 0, vertices, vertexOffset, keyFrame.worldPoints.length);
            System.arraycopy(keyFrame.colors, 0, colors, colorOffset, keyFrame.colors.length);
            vertexOffset += keyFrame.worldPoints.length;
            colorOffset += keyFrame.colors.length;
        }

        // 首次添加点云对象
        if (!mHasPointCloudAdded) {
            mPointCloud = new PointCloud(MAX_POINTS, 3); // 1+ 手机最大点数
            getCurrentScene().addChild(mPointCloud);
            mHasPointCloudAdded = true;
        }

        // 更新点云数据
        ByteBuffer byteBuf = ByteBuffer.allocateDirect(vertices.length * 4); // 每个float 4字节
        byteBuf.order(ByteOrder.nativeOrder());
        FloatBuffer buffer = byteBuf.asFloatBuffer();
        buffer.put(vertices);
        buffer.position(0);
        mPointCloud.updateCloud(mCurrentTotalPointsCount, buffer, colors);

        // 提示用户点云数量上限
        if (mCurrentTotalPointsCount >= MAX_POINTS) {
            Log.w(TAG, "点云数量已达上限(" + MAX_POINTS + ")，不再添加新点。");
            ((MainActivity)mContext).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(mContext, "点云数量已达上限 (" + MAX_POINTS + ")!!", Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    /**
     * 绘制所有关键帧相机
     */
    private void drawCamera(LSDKeyFrame[] keyFrames) {
        float allPose[] = getAllPose(keyFrames);
        int currentCameraFrameCount = keyFrames.length;

        // 移除多余的相机帧对象
        while (mCameraFrames.size() > currentCameraFrameCount) {
            Object3D obj = mCameraFrames.remove(mCameraFrames.size() - 1);
            getCurrentScene().removeChild(obj);
        }

        // 更新或添加相机帧对象
        for (int i = 0; i < currentCameraFrameCount; ++i) {
            float pose[] = Arrays.copyOfRange(allPose, i * 16, i * 16 + 16);
            Matrix4 poseMatrix = new Matrix4();
            poseMatrix.setAll(pose);

            Line3D line;
            if (i < mCameraFrames.size()) {
                // 重用现有对象
                line = (Line3D) mCameraFrames.get(i);
            } else {
                // 添加新对象
                line = createCameraFrame(0xff0000, 2);
                mCameraFrames.add(line);
                getCurrentScene().addChild(line); // 新添加的对象需要加入场景
            }
            line.setPosition(poseMatrix.getTranslation());
            line.setOrientation(new Quaternion().fromMatrix(poseMatrix));
        }
    }

    /**
     * 获取所有关键帧的位姿矩阵
     */
    private float[] getAllPose(LSDKeyFrame[] keyframes) {
        float allPose[] = new float[keyframes.length * 16];
        int offset = 0;
        for (LSDKeyFrame keyFrame : keyframes) {
            System.arraycopy(keyFrame.pose, 0, allPose, offset, keyFrame.pose.length);
            offset += keyFrame.pose.length;
        }
        return allPose;
    }

    /**
     * 创建相机视锥线框
     * @param color 线框颜色
     * @param thickness 线宽
     * @return 线框对象
     */
    private Line3D createCameraFrame(int color, int thickness) {
        float cx = intrinsics[0];
        float cy = intrinsics[1];
        float fx = intrinsics[2];
        float fy = intrinsics[3];
        int width = resolution[0];
        int height = resolution[1];

        // 构造相机视锥的顶点
        Stack<Vector3> points = new Stack<>();
        points.add(new Vector3(0, 0, 0));
        points.add(new Vector3(0.05 * (0 - cx) / fx, 0.05 * (0 - cy) / fy, 0.05));
        points.add(new Vector3(0, 0, 0));
        points.add(new Vector3(0.05 * (0 - cx) / fx, 0.05 * (height - 1 - cy) / fy, 0.05));
        points.add(new Vector3(0, 0, 0));
        points.add(new Vector3(0.05 * (width - 1 - cx) / fx, 0.05 * (height - 1 - cy) / fy, 0.05));
        points.add(new Vector3(0, 0, 0));
        points.add(new Vector3(0.05 * (width - 1 - cx) / fx, 0.05 * (0 - cy) / fy, 0.05));
        points.add(new Vector3(0.05 * (width - 1 - cx) / fx, 0.05 * (0 - cy) / fy, 0.05));
        points.add(new Vector3(0.05 * (width - 1 - cx) / fx, 0.05 * (height - 1 - cy) / fy, 0.05));
        points.add(new Vector3(0.05 * (width - 1 - cx) / fx, 0.05 * (height - 1 - cy) / fy, 0.05));
        points.add(new Vector3(0.05 * (0 - cx) / fx, 0.05 * (height - 1 - cy) / fy, 0.05));
        points.add(new Vector3(0.05 * (0 - cx) / fx, 0.05 * (height - 1 - cy) / fy, 0.05));
        points.add(new Vector3(0.05 * (0 - cx) / fx, 0.05 * (0 - cy) / fy, 0.05));
        points.add(new Vector3(0.05 * (0 - cx) / fx, 0.05 * (0 - cy) / fy, 0.05));
        points.add(new Vector3(0.05 * (width - 1 - cx) / fx, 0.05 * (0 - cy) / fy, 0.05));

        Line3D frame = new Line3D(points, thickness, color);
        frame.setMaterial(new Material());
        frame.setDrawingMode(GLES20.GL_LINES);
        return frame;
    }
}
