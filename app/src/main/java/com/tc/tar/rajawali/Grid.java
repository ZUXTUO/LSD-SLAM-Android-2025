/*
 * Copyright 2015 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.tc.tar.rajawali;

import android.opengl.GLES20;
import android.util.Log;

import org.rajawali3d.materials.Material;
import org.rajawali3d.math.vector.Vector3;
import org.rajawali3d.primitives.Line3D;

import java.util.Stack;

/**
 * Rajawali对象，表示当前场景的“地面”网格。
 * 这是一个静态网格，用于在不同视图中提供透视参考。
 */
public class Grid extends Line3D {
    /**
     * 构造函数，初始化地面网格
     * @param size 网格总尺寸
     * @param step 网格间隔
     * @param thickness 线宽
     * @param color 网格颜色
     */
    public Grid(int size, int step, float thickness, int color) {
        super(calculatePoints(size, step), thickness, color);
        Material material = new Material();
        material.setColor(color);
        this.setMaterial(material);
        // 日志：初始化地面网格成功
        Log.d("Grid", "初始化地面网格成功，尺寸: " + size + "，步长: " + step + "，线宽: " + thickness + "，颜色: " + color);
    }

    /**
     * 计算网格所有顶点
     * @param size 网格总尺寸
     * @param step 网格间隔
     * @return 顶点栈
     */
    private static Stack<Vector3> calculatePoints(int size, int step) {
        Stack<Vector3> points = new Stack<Vector3>();

        // 生成行（沿Z轴方向的线）
        for (float i = -size / 2f; i <= size / 2f; i += step) {
            points.add(new Vector3(i, 0, -size / 2f)); // 起点
            points.add(new Vector3(i, 0, size / 2f));  // 终点
        }

        // 生成列（沿X轴方向的线）
        for (float i = -size / 2f; i <= size / 2f; i += step) {
            points.add(new Vector3(-size / 2f, 0, i)); // 起点
            points.add(new Vector3(size / 2f, 0, i));  // 终点
        }

        return points;
    }

    /**
     * 初始化方法，设置绘制模式为线段
     * @param createVBOs 是否创建VBO
     */
    @Override
    protected void init(boolean createVBOs) {
        super.init(createVBOs);
        setDrawingMode(GLES20.GL_LINES);
        // 日志：设置网格绘制模式为GL_LINES
        Log.d("Grid", "设置网格绘制模式为GL_LINES");
    }
}
