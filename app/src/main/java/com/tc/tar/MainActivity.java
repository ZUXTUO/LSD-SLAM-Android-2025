package com.tc.tar;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import org.rajawali3d.renderer.Renderer;
import org.rajawali3d.view.ISurface;
import org.rajawali3d.view.SurfaceView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;

/**
 * 主界面Activity，负责初始化、渲染和资源管理
 */
public class MainActivity extends AppCompatActivity implements LSDRenderer.RenderListener {

    public static final String TAG = MainActivity.class.getSimpleName();

    // 静态视频源对象
    private static VideoSource sVideoSource;
    // 文件目录
    private String          mfileDir;
    // 主布局
    private RelativeLayout  mLayout;
    // Rajawali3D渲染Surface
    private SurfaceView     mRajawaliSurface;
    // 渲染器
    private Renderer        mRenderer;
    // 用于显示图像的ImageView
    private ImageView       mImageView;
    // 分辨率
    private int[]           mResolution;
    // 是否已开始
    private boolean         mStarted = false;

    // 加载所需的本地库
    static {
        System.loadLibrary("g2o_core");
        System.loadLibrary("g2o_csparse_extension");
        System.loadLibrary("g2o_ext_csparse");
        System.loadLibrary("g2o_solver_csparse");
        System.loadLibrary("g2o_stuff");
        System.loadLibrary("g2o_types_sba");
        System.loadLibrary("g2o_types_sim3");
        System.loadLibrary("g2o_types_slam3d");
        System.loadLibrary("LSD");
        System.loadLibrary("opencv_java3");
        System.loadLibrary("z");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 获取应用外部文件目录
        mfileDir = getExternalFilesDir(null).getAbsolutePath();
        // 拷贝assets中的配置文件到外部目录
        copyAssets(this, mfileDir);
        // 初始化本地接口
        TARNativeInterface.nativeInit(mfileDir + File.separator + "cameraCalibration.cfg");
        // 创建渲染Surface
        mRajawaliSurface = createSurfaceView();
        // 创建渲染器
        mRenderer = createRenderer();
        // 应用渲染器
        applyRenderer();

        // 创建主布局
        mLayout = new RelativeLayout(this);
        FrameLayout.LayoutParams childParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams
                .MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        mLayout.addView(mRajawaliSurface, childParams);

        // 创建用于显示图像的ImageView
        mImageView = new ImageView(this);
        RelativeLayout.LayoutParams imageParams = new RelativeLayout.LayoutParams(480, 320);
        imageParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        imageParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        mLayout.addView(mImageView, imageParams);

        // 获取分辨率
        mResolution = TARNativeInterface.nativeGetResolution();

        // 初始化视频源
        sVideoSource = new VideoSource(this, mResolution[0], mResolution[1]);
        sVideoSource.start();

        // 设置主布局为内容视图
        setContentView(mLayout);
        // 提示用户按音量+键开始
        Toast.makeText(this, "请按音量(+)键开始！", Toast.LENGTH_LONG).show();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // 处理按键事件
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            // 按返回键，销毁本地接口并退出
            TARNativeInterface.nativeDestroy();
            finish();
            System.exit(0);
        } else if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            // 按音量+键，开始
            Toast.makeText(this, "开始", Toast.LENGTH_SHORT).show();
            mStarted = true;
            TARNativeInterface.nativeStart();
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            // 按音量-键，重置（待实现）
            Toast.makeText(this, "重置！", Toast.LENGTH_SHORT).show();
            return true;
        }
        return true;
    }

    /**
     * 创建Rajawali3D的SurfaceView
     */
    protected SurfaceView createSurfaceView() {
        SurfaceView view = new SurfaceView(this);
        view.setFrameRate(60);
        view.setRenderMode(ISurface.RENDERMODE_WHEN_DIRTY);
        return view;
    }

    /**
     * 创建渲染器
     */
    protected Renderer createRenderer() {
        LSDRenderer renderer = new LSDRenderer(this);
        renderer.setRenderListener(this);
        return renderer;
    }

    /**
     * 应用渲染器到SurfaceView
     */
    protected void applyRenderer() {
        mRajawaliSurface.setSurfaceRenderer(mRenderer);
    }

    /**
     * 获取主布局View
     */
    public View getView() {
        return mLayout;
    }

    /**
     * 渲染回调，每帧调用
     */
    @Override
    public void onRender() {
        if (mImageView == null)
            return;

        byte[] imgData;
        if (!mStarted) {
            // 未开始时，显示摄像头YUV数据
            byte[] frameData = sVideoSource.getFrame();     // YUV数据
            if (frameData == null)
                return;
            imgData = new byte[mResolution[0] * mResolution[1] * 4];
            // 灰度转ARGB
            for (int i = 0; i < imgData.length / 4; ++i) {
                imgData[i * 4] = frameData[i];
                imgData[i * 4 + 1] = frameData[i];
                imgData[i * 4 + 2] = frameData[i];
                imgData[i * 4 + 3] = (byte) 0xff;
            }
        } else {
            // 已开始时，获取本地处理后的图像
            imgData = TARNativeInterface.nativeGetCurrentImage(0);
        }

        if (imgData == null)
            return;

        // 创建Bitmap并显示到ImageView
        final Bitmap bm = Bitmap.createBitmap(mResolution[0], mResolution[1], Bitmap.Config.ARGB_8888);
        bm.copyPixelsFromBuffer(ByteBuffer.wrap(imgData));
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mImageView.setImageBitmap(bm);
            }
        });
    }

    /**
     * JNI调用，获取视频源
     */
    public static VideoSource getVideoSource() {
        return sVideoSource;
    }

    /**
     * 拷贝assets目录下的.cfg配置文件到指定目录
     */
    public static void copyAssets(Context context, String dir) {
        AssetManager assetManager = context.getAssets();
        String[] files = null;
        try {
            files = assetManager.list("");
        } catch (IOException e) {
            Log.e(TAG, "copyAssets: 获取asset文件列表失败。", e);
        }
        for(String filename : files) {
            // 只拷贝以.cfg结尾的文件
            if(!filename.endsWith(".cfg"))
                continue;
            InputStream in = null;
            OutputStream out = null;
            try {
                in = assetManager.open(filename);
                File outFile = new File(dir, filename);
                if(outFile.exists())
                {
                    Log.d(TAG, "copyAssets: 文件已存在: " + filename);
                }
                else
                {
                    out = new FileOutputStream(outFile);
                    copyFile(in, out);
                    Log.d(TAG, "copyAssets: 文件已拷贝: " + filename);
                }
            } catch(IOException e) {
                Log.e(TAG, "copyAssets: 拷贝asset文件失败: " + filename, e);
            }
            finally {
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e) {
                        Log.e(TAG, "copyAssets: 关闭输入流失败: " + e.toString());
                    }
                }
                if (out != null) {
                    try {
                        out.close();
                    } catch (IOException e) {
                        Log.e(TAG, "copyAssets: 关闭输出流失败: " + e.toString());
                    }
                }
            }
        }
    }

    /**
     * 拷贝输入流到输出流
     */
    public static void copyFile(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while((read = in.read(buffer)) != -1){
            out.write(buffer, 0, read);
        }
    }
}
