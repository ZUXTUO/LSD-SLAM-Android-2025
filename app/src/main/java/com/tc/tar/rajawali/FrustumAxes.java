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

import android.graphics.Color;
import android.util.Log;

import org.rajawali3d.materials.Material;
import org.rajawali3d.math.vector.Vector3;
import org.rajawali3d.primitives.Line3D;

import java.util.Arrays;
import java.util.Collections;
import java.util.Stack;

/**
 * 该类为视锥体与坐标轴的组合体（线段形式）
 */
public class FrustumAxes extends Line3D {
    // 视锥体宽度
    private static final float FRUSTUM_WIDTH = 0.8f;
    // 视锥体高度
    private static final float FRUSTUM_HEIGHT = 0.6f;
    // 视锥体深度
    private static final float FRUSTUM_DEPTH = 0.5f;

    /**
     * 构造函数，初始化视锥体与坐标轴
     * @param thickness 线宽
     */
    public FrustumAxes(float thickness) {
        super(makePoints(), thickness, makeColors());
        Material material = new Material();
        material.useVertexColors(true);
        setMaterial(material);
        // 日志：初始化FrustumAxes成功
        Log.d("FrustumAxes", "初始化FrustumAxes成功，线宽: " + thickness);
    }

    /**
     * 构建视锥体和坐标轴的所有顶点
     * @return 顶点栈
     */
    private static Stack<Vector3> makePoints() {
        // 原点
        Vector3 o = new Vector3(0, 0, 0);
        // 视锥体四个角点
        Vector3 a = new Vector3(-FRUSTUM_WIDTH / 2f, FRUSTUM_HEIGHT / 2f, -FRUSTUM_DEPTH);
        Vector3 b = new Vector3(FRUSTUM_WIDTH / 2f, FRUSTUM_HEIGHT / 2f, -FRUSTUM_DEPTH);
        Vector3 c = new Vector3(FRUSTUM_WIDTH / 2f, -FRUSTUM_HEIGHT / 2f, -FRUSTUM_DEPTH);
        Vector3 d = new Vector3(-FRUSTUM_WIDTH / 2f, -FRUSTUM_HEIGHT / 2f, -FRUSTUM_DEPTH);

        // 坐标轴方向向量
        Vector3 x = new Vector3(1, 0, 0);
        Vector3 y = new Vector3(0, 1, 0);
        Vector3 z = new Vector3(0, 0, 1);

        Stack<Vector3> points = new Stack<Vector3>();
        // 添加坐标轴线段
        Collections.addAll(points, o, x, o, y, o, z,
                // 添加视锥体线段
                o, a, b, o, b, c, o, c, d, o, d, a);

        // 日志：顶点数量
        Log.d("FrustumAxes", "顶点数量: " + points.size());
        return points;
    }

    /**
     * 构建每条线段的颜色数组
     * @return 颜色数组
     */
    private static int[] makeColors() {
        int[] colors = new int[18];
        // 默认全部填充为黑色
        Arrays.fill(colors, Color.BLACK);
        // X轴为红色
        colors[0] = Color.RED;
        colors[1] = Color.RED;
        // Y轴为绿色
        colors[2] = Color.GREEN;
        colors[3] = Color.GREEN;
        // Z轴为蓝色
        colors[4] = Color.BLUE;
        colors[5] = Color.BLUE;
        // 其余为黑色
        // 日志：颜色数组已设置
        Log.d("FrustumAxes", "颜色数组已设置");
        return colors;
    }
}
