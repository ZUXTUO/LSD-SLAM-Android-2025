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

import org.rajawali3d.materials.Material;

import java.nio.FloatBuffer;

/**
 * 使用颜色渲染点云，以指示到深度传感器的距离。
 * 颜色基于可见光谱：最近的点为红色，最远的点为紫色。
 */
public class PointCloud extends Points {
    // 用于计算颜色的最大深度范围（最小值为0）
    public static final float CLOUD_MAX_Z = 5;

    // 存储每个点的颜色数组（RGBA）
    private float[] mColorArray;
    // 颜色调色板
    private final int[] mPalette;
    // 调色板大小
    public static final int PALETTE_SIZE = 360;
    // 色相起始值
    public static final float HUE_BEGIN = 0;
    // 色相结束值
    public static final float HUE_END = 320;

    /**
     * 构造函数，初始化点云
     * @param maxPoints 最大点数
     * @param floatsPerPoint 每个点的浮点数数量
     */
    public PointCloud(int maxPoints, int floatsPerPoint) {
        super(maxPoints, floatsPerPoint, true);
        mPalette = createPalette(); // 创建颜色调色板
        mColorArray = new float[maxPoints * 4]; // 每个点4个颜色分量
        Material m = new Material();
        m.useVertexColors(true); // 启用顶点颜色
        setMaterial(m);
    }

    /**
     * 更新点云中的点和颜色
     * @param pointCount 点的数量
     * @param pointBuffer 点数据缓冲区
     */
    public void updateCloud(int pointCount, FloatBuffer pointBuffer) {
        // 计算每个点的颜色
        calculateColors(pointCount, pointBuffer);
        // 更新点和颜色
        updatePoints(pointCount, pointBuffer, mColorArray);
    }

    /**
     * 使用外部颜色数组更新点云
     * @param pointCount 点的数量
     * @param pointBuffer 点数据缓冲区
     * @param colors 每个点的颜色数组
     */
    public void updateCloud(int pointCount, FloatBuffer pointBuffer, int[] colors) {
        int color;
        for (int i = 0; i < pointCount; i++) {
            color = colors[i];
            mColorArray[i * 4] = Color.red(color) / 255f;      // 红色分量
            mColorArray[i * 4 + 1] = Color.green(color) / 255f; // 绿色分量
            mColorArray[i * 4 + 2] = Color.blue(color) / 255f;  // 蓝色分量
            mColorArray[i * 4 + 3] = Color.alpha(color) / 255f; // 透明度分量
        }
        updatePoints(pointCount, pointBuffer, mColorArray);
    }

    /**
     * 预先计算调色板，用于将点的距离转换为RGB颜色
     * @return 颜色调色板数组
     */
    private int[] createPalette() {
        int[] palette = new int[PALETTE_SIZE];
        float[] hsv = new float[3];
        hsv[1] = hsv[2] = 1; // 饱和度和亮度都为1
        for (int i = 0; i < PALETTE_SIZE; i++) {
            hsv[0] = (HUE_END - HUE_BEGIN) * i / PALETTE_SIZE + HUE_BEGIN; // 计算色相
            palette[i] = Color.HSVToColor(hsv);
        }
        return palette;
    }

    /**
     * 计算点云中每个点的颜色
     * @param pointCount 点的数量
     * @param pointCloudBuffer 点数据缓冲区
     */
    private void calculateColors(int pointCount, FloatBuffer pointCloudBuffer) {
        float[] points = new float[pointCount * mFloatsPerPoint];
        pointCloudBuffer.rewind(); // 重置缓冲区指针
        pointCloudBuffer.get(points); // 读取所有点数据
        pointCloudBuffer.rewind(); // 再次重置缓冲区指针

        int color;
        int colorIndex;
        float z;
        for (int i = 0; i < pointCount; i++) {
            z = points[i * mFloatsPerPoint + 2]; // 获取z坐标（深度）
            // 根据z值计算颜色索引，防止越界
            colorIndex = (int) Math.min(z / CLOUD_MAX_Z * mPalette.length, mPalette.length - 1);
            colorIndex = Math.max(colorIndex, 0);
            color = mPalette[colorIndex];
            mColorArray[i * 4] = Color.red(color) / 255f;      // 红色分量
            mColorArray[i * 4 + 1] = Color.green(color) / 255f; // 绿色分量
            mColorArray[i * 4 + 2] = Color.blue(color) / 255f;  // 蓝色分量
            mColorArray[i * 4 + 3] = Color.alpha(color) / 255f; // 透明度分量
        }
    }
}
