package com.main.hty.hlcheckservice;

import android.annotation.SuppressLint;
import android.app.Application;
import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.WindowManager;

import androidx.annotation.NonNull;

import com.main.hty.utils.ForegroundNotificationUtils;
import com.main.hty.utils.NotificationUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author ASUS
 */
public class MyApplication extends Application {


    public static final boolean STATUS_TRUE = true;
    public static final boolean STATUS_FALSE = false;

    private static MyApplication myApplication;

    public static Context context;

    //Action

    private static final String TAG = "MyApplication";

    @Override
    public void onCreate() {
        super.onCreate();
        myApplication = this;
        try {
            Notification notification = ForegroundNotificationUtils.showFastEntranceNotify(getApplicationContext());
            NotificationUtils.notify(this,Constants.NOTIFY_ID_FAST_ENTRANCE,notification);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Initialize the UserData instance here
    }
    public static MyApplication getMyApplication(){
        return myApplication;
    }


    public static void pritfLine(){
        Log.d(TAG,"===========================================================\n");
    }



}
