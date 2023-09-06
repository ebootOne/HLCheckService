package com.main.hty.hlcheckservice;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityManager;
import android.widget.Button;
import android.widget.EditText;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import java.util.List;
import java.util.Objects;

/**
 * @author XiYU
 */
public class MainActivity extends AppCompatActivity {

    @SuppressLint("StaticFieldLeak")
    private final String TAG = "MainActivity";
    private Context context;
    private final String PACKAGE_NAME = "com.main.hty.hlcheckservice";



    @SuppressLint({"ResourceType", "MissingInflatedId"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = this;
        onSetButtonClicked();
    }


    /**
     * 开启无障碍服务
     */
    public void onSetButtonClicked() {
        if (!hasOverlayPermission()){
            openOverlayPermission();
        }
        if (!hasAccessibilityServiceEnabled()){
            openAccessibilitySettings();
        }
    }

    private boolean hasOverlayPermission() {
        return Settings.canDrawOverlays(this);
    }
    private boolean hasAccessibilityServiceEnabled() {
        //检测当前无障碍服务已开启的应用列表信息
        AccessibilityManager am = (AccessibilityManager) getSystemService
                (Context.ACCESSIBILITY_SERVICE);
        List<AccessibilityServiceInfo> accessibilityServiceList =
                am.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_GENERIC);
        for (AccessibilityServiceInfo info : accessibilityServiceList) {
            String packageName = info.getResolveInfo().serviceInfo.packageName;
//            MyApplication.pritfLine();
//            Log.e(TAG, "当前已开启的无障碍服务的信息: " + info.getResolveInfo().toString());
            if(Objects.equals(packageName, PACKAGE_NAME)){
                return MyApplication.STATUS_TRUE;
            }
        }
        return MyApplication.STATUS_FALSE;
    }


    public void openOverlayPermission(){
        // 若未授予权限，则向用户请求权限
        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
        requestOverlayPermissionLauncher.launch(intent);

    }
    private void openAccessibilitySettings() {
        Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
        requestAccessibilityPermissionLauncher.launch(intent);

        //跳转到系统设置页面，由用户手动点击确认是否开启对应的无障碍服务
        //Intent intent = new Intent();
        //intent.setAction(Settings.ACTION_ACCESSIBILITY_SETTINGS);
        ////intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        //startActivity(intent);
    }

    ActivityResultLauncher<Intent> requestAccessibilityPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                // 用户处理权限请求后，再次检查是否已授予权限
                if (!hasAccessibilityServiceEnabled()) {
                    // 用户拒绝了权限请求，显示提示信息
                    ShowToast.show(MainActivity.this, "授予无障碍权限失败");
                    MyApplication.pritfLine();
                    Log.e(TAG, PACKAGE_NAME+"未开启无障碍服务");

                }else {
                    ShowToast.show(MainActivity.this, "成功获取权限");
                    MyApplication.pritfLine();
                    Log.e(TAG, TAG +PACKAGE_NAME+ "开启无障碍服务");
                }
            }
    );

    ActivityResultLauncher<Intent> requestOverlayPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                // 用户处理权限请求后，再次检查是否已授予权限
                if (!Settings.canDrawOverlays(this)) {
                    // 用户拒绝了权限请求，显示提示信息
                    ShowToast.show(MainActivity.this, "授予悬浮权限失败");
                }
            }
    );




    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

}






