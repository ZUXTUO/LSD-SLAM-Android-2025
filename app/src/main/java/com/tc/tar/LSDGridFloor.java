package com.tc.tar;

import android.opengl.GLES20;
import android.util.Log;

import org.rajawali3d.Object3D;
import org.rajawali3d.materials.Material;
import org.rajawali3d.math.Matrix4;
import org.rajawali3d.math.Quaternion;
import org.rajawali3d.math.vector.Vector3;
import org.rajawali3d.primitives.Line3D;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Stack;

/**
 * 网格地板生成类
 * 创建者：aarontang，日期：2017/4/6
 */
public class LSDGridFloor {

    public LSDGridFloor() {
    }

    /**
     * 创建网格地板，包括大网格、小网格和坐标轴
     * @return 返回包含所有网格线和坐标轴的Object3D集合
     */
    public Collection<Object3D> createGridFloor() {
        ArrayList<Object3D> result = new ArrayList<>();

        Stack<Vector3> points = new Stack<>();
        double dGridInterval = 0.1; // 网格间隔

        // 创建大网格
        double dMin = -100.0 * dGridInterval;
        double dMax = 100.0 * dGridInterval;
        double height = -4; // 网格高度（z轴）
        int color = 0x4c4c4c; // 默认灰色
        for(int x = -10; x <= 10; x += 1)
        {
            if(x == 0)
                color = 0xffffff; // 中心线为白色
            else
                color = 0x4c4c4c; // 其他为灰色
            points.add(new Vector3((double) x * 10 * dGridInterval, dMin, height));
            points.add(new Vector3((double) x * 10 * dGridInterval, dMax, height));
        }

        for(int y = -10; y <= 10; y += 1)
        {
            if(y == 0)
                color = 0xffffff; // 中心线为白色
            else
                color = 0x4c4c4c; // 其他为灰色
            points.add(new Vector3(dMin, (double) y * 10 * dGridInterval, height));
            points.add(new Vector3(dMax, (double) y * 10 * dGridInterval, height));
        }
        // 添加大网格线到结果集合
        result.add(createLine(points, color, 1));

        // 创建小网格
        points.clear();
        dMin = -10.0 * dGridInterval;
        dMax = 10.0 * dGridInterval;
        color = 0x808080; // 小网格为浅灰色
        for(int x = -10; x <= 10; x++)
        {
            if(x == 0)
                color = 0xffffff; // 中心线为白色
            else
                color = 0x808080; // 其他为浅灰色
            points.add(new Vector3((double) x * dGridInterval, dMin, height));
            points.add(new Vector3((double) x * dGridInterval, dMax, height));
        }

        for(int y = -10; y <= 10; y++)
        {
            if(y == 0)
                color = 0xffffff; // 中心线为白色
            else
                color = 0x808080; // 其他为浅灰色
            points.add(new Vector3(dMin, (double) y * dGridInterval, height));
            points.add(new Vector3(dMax, (double) y * dGridInterval, height));
        }
        // 添加小网格线到结果集合
        result.add(createLine(points, color, 1));

        // 创建坐标轴
        // X轴（红色）
        points.clear();
        points.add(new Vector3(0, 0, height));
        points.add(new Vector3(1, 0, height));
        result.add(createLine(points, 0xff0000, 2));

        // Y轴（绿色）
        points.clear();
        points.add(new Vector3(0, 0, height));
        points.add(new Vector3(0, 1, height));
        result.add(createLine(points, 0x00ff00, 2));

        // Z轴（蓝色）
        points.clear();
        points.add(new Vector3(0, 0, height));
        points.add(new Vector3(0, 0, height + 1));
        result.add(createLine(points, 0x0000ff, 2));

        // 日志输出网格创建完成（中文）
        Log.i("LSDGridFloor", "网格地板和坐标轴创建完成，共有对象数：" + result.size());

        return result;
    }

    /**
     * 创建一条线段
     * @param points 线段的点集合
     * @param color 线段颜色
     * @param thickness 线宽
     * @return 返回Line3D对象
     */
    private Line3D createLine(Stack<Vector3> points, int color, int thickness) {
        // 创建线段对象
        Line3D line = new Line3D(points, thickness, color);
        line.setMaterial(new Material());
        line.setDrawingMode(GLES20.GL_LINES);

        // 设置线段的位姿（旋转和平移）
        float[] pose = {0, 0, 1, 0, -1, 0, 0, 0, 0, -1, 0, 0, 0, 0, 0, 1};
        Matrix4 poseMatrix = new Matrix4(pose);
        line.setPosition(poseMatrix.getTranslation());
        line.setOrientation(new Quaternion().fromMatrix(poseMatrix));

        // 日志输出线段创建信息（中文）
        Log.d("LSDGridFloor", "创建线段，颜色: 0x" + Integer.toHexString(color) + "，点数: " + points.size());

        return line;
    }
}
