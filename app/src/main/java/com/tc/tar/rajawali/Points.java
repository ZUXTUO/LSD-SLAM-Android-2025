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

import android.opengl.GLES10;
import android.opengl.GLES20;
import android.util.Log;

import org.rajawali3d.Object3D;

import java.nio.FloatBuffer;

/**
 * Rajawali的点云（Points）基元类。
 * 可用于渲染点云数据，便于后续贡献给Rajawali主库。
 */
public class Points extends Object3D {
    private static final int BYTES_PER_FLOAT = 4; // 每个float占用4字节

    private int mMaxNumberOfVertices; // 最大顶点数
    // 每个点包含的float数量，XYZ格式为3，XYZC格式为4
    protected int mFloatsPerPoint = 3;
    // 每个颜色包含的float数量（RGBA）
    protected int mFloatsPerColor = 4;

    /**
     * 构造函数
     * @param numberOfPoints 最大点数
     * @param floatsPerPoint 每个点的float数量
     * @param isCreateColors 是否创建颜色缓冲区
     */
    public Points(int numberOfPoints, int floatsPerPoint, boolean isCreateColors) {
        super();
        mMaxNumberOfVertices = numberOfPoints;
        mFloatsPerPoint = floatsPerPoint;
        init(true, isCreateColors);
        Log.d("Points", "初始化Points对象，最大点数: " + numberOfPoints + "，每点float数: " + floatsPerPoint + "，是否创建颜色: " + isCreateColors);
    }

    /**
     * 初始化Points基元的缓冲区。
     * 只初始化顶点、索引和颜色缓冲区，通过setData方法设置。
     * @param createVBOs 是否创建VBO
     * @param createColors 是否创建颜色缓冲区
     */
    protected void init(boolean createVBOs, boolean createColors) {
        float[] vertices = new float[mMaxNumberOfVertices * mFloatsPerPoint]; // 顶点数组
        int[] indices = new int[mMaxNumberOfVertices]; // 索引数组
        for (int i = 0; i < indices.length; ++i) {
            indices[i] = i;
        }
        float[] colors = null;
        if (createColors) {
            colors = new float[mMaxNumberOfVertices * mFloatsPerColor]; // 颜色数组
        }
        mGeometry.getVertexBufferInfo().stride = mFloatsPerPoint * BYTES_PER_FLOAT;
        setData(vertices, null, null, colors, indices, true);
        Log.d("Points", "完成缓冲区初始化，顶点数: " + mMaxNumberOfVertices + "，是否有颜色: " + createColors);
    }

    /**
     * 根据提供的点缓冲区更新点的几何信息。
     * @param pointCount 点的数量
     * @param pointCloudBuffer 点数据缓冲区
     */
    public void updatePoints(int pointCount, FloatBuffer pointCloudBuffer) {
        // mGeometry.setNumIndices(pointCount);
        mGeometry.setNumVertices(pointCount);
        mGeometry.setVertices(pointCloudBuffer);
        mGeometry.changeBufferData(mGeometry.getVertexBufferInfo(), mGeometry.getVertices(), 0,
                pointCount * mFloatsPerPoint);
        Log.d("Points", "更新点数据，点数: " + pointCount);
    }

    /**
     * 根据提供的点缓冲区和颜色数组更新点的几何信息和颜色。
     * @param pointCount 点的数量
     * @param points 点数据缓冲区
     * @param colors 颜色数组
     */
    public void updatePoints(int pointCount, FloatBuffer points, float[] colors) {
        if (pointCount > mMaxNumberOfVertices) {
            throw new RuntimeException(
                    String.format("点数pointCount = %d 超过最大允许点数 = %d",
                            pointCount, mMaxNumberOfVertices));
        }
        // mGeometry.setNumIndices(pointCount);
        mGeometry.setNumVertices(pointCount);
        mGeometry.setVertices(points);
        mGeometry.changeBufferData(mGeometry.getVertexBufferInfo(), mGeometry.getVertices(), 0,
                pointCount * mFloatsPerPoint);
        mGeometry.setColors(colors);
        mGeometry.changeBufferData(mGeometry.getColorBufferInfo(), mGeometry.getColors(), 0,
                pointCount * mFloatsPerColor);
        Log.d("Points", "更新点和颜色数据，点数: " + pointCount);
    }

    /**
     * 渲染前设置绘制模式为GL_POINTS，并设置点大小。
     */
    @Override
    public void preRender() {
        super.preRender();
        setDrawingMode(GLES20.GL_POINTS);
        GLES10.glPointSize(5.0f);
        Log.d("Points", "设置绘制模式为GL_POINTS，点大小为5.0f");
    }
}
