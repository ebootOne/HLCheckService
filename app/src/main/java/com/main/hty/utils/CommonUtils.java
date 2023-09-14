package com.main.hty.utils;

import android.os.Build;

public class CommonUtils {
    //判断手机版本是否为android 0以前的版本
    public static boolean isPreAndroidO() {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.O;
    }
}
