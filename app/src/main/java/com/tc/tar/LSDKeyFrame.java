package com.tc.tar;

/**
 * LSD关键帧数据结构
 * 创建者：aarontang，日期：2017/4/10
 */

public class LSDKeyFrame {
    // 相机位姿（4x4矩阵，16个float，世界坐标系下）
    float[] pose;
    // 该关键帧包含的点的数量
    int pointCount;
    // 世界坐标系下的三维点坐标，每个点3个float（x, y, z）
    float[] worldPoints;    // 每个点3个float，世界坐标系（注意！！）
    // 每个点的颜色，1个int表示一个点的颜色（ARGB格式）
    int[] colors;           // 每个点1个int，表示颜色
}
