package com.tc.tar;

import android.content.Context;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.util.Log;

import java.io.IOException;
import java.util.Arrays;

/**
 * 视频源类，负责相机的初始化、帧获取与管理
 * 创建者：aarontang，日期：2017/4/17
 */

public class VideoSource implements Camera.PreviewCallback {

    public static final String TAG = VideoSource.class.getSimpleName();
    private static final int MAGIC_TEX_ID = 10; // SurfaceTexture的魔法ID
    private int mWidth;    // 预览宽度
    private int mHeight;   // 预览高度

    private Camera mCamera;                // 相机对象
    private SurfaceTexture mSurfaceTexture;// SurfaceTexture对象
    private byte[] mCurrentFrame;          // 当前帧数据
    private Object mFrameLock = new Object(); // 帧数据锁

    /**
     * 构造函数，初始化相机
     * @param context 上下文
     * @param width 预览宽度
     * @param height 预览高度
     */
    public VideoSource(Context context, int width, int height) {
        mCamera = getCameraInstance(); // 获取相机实例
        mWidth = width;
        mHeight = height;
        Camera.Parameters params = mCamera.getParameters();
        params.setPreviewSize(mWidth, mHeight); // 设置预览分辨率
        params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE); // 连续自动对焦
        params.setPreviewFormat(ImageFormat.NV21); // 设置预览格式为NV21
        mCamera.setParameters(params);
        int format = params.getPreviewFormat();
        // 添加回调缓冲区
        mCamera.addCallbackBuffer(new byte[mWidth * mHeight * ImageFormat.getBitsPerPixel(format) / 8]);
        mSurfaceTexture = new SurfaceTexture(MAGIC_TEX_ID);
    }

    /**
     * 启动相机预览
     */
    public void start() {
        try {
            mCamera.setPreviewCallbackWithBuffer(this); // 设置带缓冲区的回调
            mCamera.setPreviewTexture(mSurfaceTexture); // 绑定SurfaceTexture
            mCamera.startPreview(); // 启动预览
        } catch (IOException e) {
            Log.d(TAG, "设置相机预览出错: " + e.getMessage()); // 中文日志
        }
    }

    /**
     * 停止相机预览并释放资源
     */
    public void stop() {
        try {
            mCamera.stopPreview(); // 停止预览
            mCamera.release();     // 释放相机
        } catch (Exception e) {
            // 忽略：尝试停止一个不存在的预览
        }
    }

    /**
     * 获取当前帧数据（深拷贝）
     * @return 当前帧的字节数组
     */
    public byte[] getFrame() {
        if (mCurrentFrame == null)
            return null;

        byte[] copyData;
        synchronized (mFrameLock) {
            copyData = Arrays.copyOf(mCurrentFrame, mCurrentFrame.length);
        }
        return copyData;
    }

    /**
     * 预览帧回调
     * @param data 当前帧数据
     * @param camera 相机对象
     */
    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        camera.addCallbackBuffer(data); // 重新添加缓冲区，避免内存抖动
        synchronized (mFrameLock) {
            mCurrentFrame = data;
        }
    }

    /**
     * 安全获取相机实例的方法
     * @return Camera对象，若不可用则返回null
     */
    public static Camera getCameraInstance(){
        Camera c = null;
        try {
            c = Camera.open(); // 尝试获取相机实例
        }
        catch (Exception e){
            // 相机不可用（被占用或不存在）
        }
        return c; // 如果不可用则返回null
    }
}
