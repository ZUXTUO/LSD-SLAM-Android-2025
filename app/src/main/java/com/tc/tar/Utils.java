package com.tc.tar;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.os.Environment;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * 工具类，包含帧数据保存等功能
 * 创建者：aarontang，日期：2017/4/18
 */

public class Utils {

    /**
     * 将NV21格式的帧数据保存为PNG图片到本地
     * @param data       NV21格式的字节数组
     * @param width      图像宽度
     * @param height     图像高度
     * @param frameIndex 帧序号
     */
    public static void dumpFrame(byte[] data, int width, int height, int frameIndex) {
        // 将NV21数据转换为YuvImage对象
        YuvImage yuvImage = new YuvImage(data, ImageFormat.NV21, width, height, null);
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        // 压缩为JPEG格式
        yuvImage.compressToJpeg(new Rect(0, 0, width, height), 100, os);
        byte[] jpegByteArray = os.toByteArray();
        // 解码为Bitmap对象
        Bitmap bitmap = BitmapFactory.decodeByteArray(jpegByteArray, 0, jpegByteArray.length);
        FileOutputStream fos;
        try {
            // 构造输出文件路径
            String filePath = Environment.getExternalStorageDirectory() + "/LSD/dump/" +
                    String.format("%05d", frameIndex) + ".png";
            fos = new FileOutputStream(filePath);
            // 保存为PNG格式
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.flush();
            fos.close();
            // 中文日志输出
            Log.i("Utils", "帧已成功保存到: " + filePath);
        } catch (FileNotFoundException e) {
            // 中文日志输出
            Log.e("Utils", "保存帧时未找到文件: " + e.getMessage());
            e.printStackTrace();
        } catch (IOException e) {
            // 中文日志输出
            Log.e("Utils", "保存帧时发生IO异常: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
