package com.main.hty.hlcheckservice;

import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.IBinder;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

import androidx.annotation.Nullable;

public class BackService extends Service {
    private View mView;
    private WindowManager mSystemService;


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        initWindow();
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * 初始化一个悬浮窗
     */
    private void initWindow() {
        // 获取WindowManager
        mSystemService = (WindowManager) getSystemService(WINDOW_SERVICE);
        // 创建布局参数
        WindowManager.LayoutParams params = new WindowManager.LayoutParams();
        //这里需要进行不同的设置
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            params.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            params.type = WindowManager.LayoutParams.TYPE_PHONE;
        }
        //设置透明度
        params.alpha = 1.0f;
        //设置内部视图对齐方式
        params.gravity = Gravity.RIGHT | Gravity.BOTTOM;
        //窗口的右上角角坐标
        params.x = 20;
        params.y = 20;
        //是指定窗口的像素格式为 RGBA_8888。
        //使用 RGBA_8888 像素格式的窗口可以在保持高质量图像的同时实现透明度效果。
        params.format = PixelFormat.RGBA_8888;
        //设置窗口的宽高,这里为自动
        params.width = WindowManager.LayoutParams.WRAP_CONTENT;
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        //这段非常重要，是后续是否穿透点击的关键
        params.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE  //表示悬浮窗口不需要获取焦点，这样用户点击悬浮窗口以外的区域，就不需要关闭悬浮窗口。
                |WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;//表示悬浮窗口不会阻塞事件传递，即用户点击悬浮窗口以外的区域时，事件会传递给后面的窗口处理。
        //这里的引入布局文件的方式，也可以动态添加控件
        mView = View.inflate(getApplicationContext(), R.layout.float_window, null);
        mSystemService.addView(mView,params);
    }
}
