package com.tc.tar;

/**
 * TARNativeInterface：与C++原生层交互的接口类
 * 创建者：aarontang，日期：2017/3/30
 * 所有方法均为native方法，需对应JNI实现
 */

public class TARNativeInterface {
    // 日志标签（中文）
    public static final String TAG = "TARNativeInterface（原生接口）";

    /**
     * 初始化原生层，加载相机标定文件
     * @param calibPath 标定文件路径
     */
    public static native void nativeInit(String calibPath);

    /**
     * 销毁原生层，释放资源
     */
    public static native void nativeDestroy();

    /**
     * 启动SLAM主流程
     */
    public static native void nativeStart();

    /**
     * 传递按键信息到原生层
     * @param keycode 按键码
     */
    public static native void nativeKey(int keycode);

    /**
     * 获取相机内参（返回float数组，长度4）
     * @return 相机内参数组
     */
    public static native float[] nativeGetIntrinsics();

    /**
     * 获取相机分辨率（返回int数组，长度2）
     * @return 分辨率数组
     */
    public static native int[] nativeGetResolution();

    /**
     * 获取当前相机位姿（返回float数组，长度16，4x4矩阵）
     * @return 当前位姿矩阵
     */
    public static native float[] nativeGetCurrentPose();

    /**
     * 获取所有关键帧（返回LSDKeyFrame数组）
     * @return 关键帧数组
     */
    public static native LSDKeyFrame[] nativeGetAllKeyFrames();

    /**
     * 获取关键帧数量
     * @return 关键帧数量
     */
    public static native int nativeGetKeyFrameCount();

    /**
     * 获取当前帧图像数据
     * @param format 图像格式（目前仅支持0，ARGB格式）
     * @return 图像字节数组
     */
    public static native byte[] nativeGetCurrentImage(int format);  // 仅支持format = 0（ARGB）
}
