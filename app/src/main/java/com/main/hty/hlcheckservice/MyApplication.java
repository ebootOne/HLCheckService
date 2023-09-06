package com.main.hty.hlcheckservice;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.WindowManager;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

/**
 * @author ASUS
 */
public class MyApplication extends Application {


    public static final int MAX_FLOATING_ICONS = 5;
    public static  int MIN_VALUE = 0;
    public static final int MAX_VALUE = 10000;

    public static final int StatusOK = 1;
    public static final int StatusNONE = 0;
    public static final int StatusNO = -1;

    public static final int StatusERR = -2;
    public static final boolean STATUS_TRUE = true;
    public static final boolean STATUS_FALSE = false;

    private static MyApplication myApplication;

    public static Context context;

    //Action

    public static final String ACTION_FEATURE_SETTING_STOPPED = "com.example.yu.ACTION_FEATURE_SETTING_STOPPED";
    public static final String ACTION_ICON_SETTING_START = "com.example.yu.ICON__SETTING_START";

    //广播传递标识符

    public static final String MESSAGE_HAD_SET_VIEW = "HAD_SETTING";
    public static final String MESSAGE_ICON_SETTING_START = "ICON_SETTING_START";

    private static final String SHARED_PREFERENCES_NAME = "config";

    public static int SELECT_DATA = 1;
    

    public static int WHICH_ICON = 0;

    private static final String TAG = "MyApplication";

    public static  WindowManager windowManager;
    public static  boolean is_delete_node = false;




    @Override
    public void onCreate() {
        super.onCreate();
        myApplication = this;


        // Initialize the UserData instance here
    }
    public static MyApplication getMyApplication(){
        return myApplication;
    }


    public static void pritfLine(){
        Log.d(TAG,"===========================================================\n");
    }



}
