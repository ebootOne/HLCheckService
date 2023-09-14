package com.main.hty.hlcheckservice;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Build;
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
import android.widget.TextView;
import android.widget.Toast;

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

    boolean isService = false;
    TextView btn_wza;
    EditText remark_edit;
    public static final String REMARK_EDIT = "remark";
    @SuppressLint({"ResourceType", "MissingInflatedId"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = this;
        //onSetButtonClicked();
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        remark_edit = findViewById(R.id.remark_edit);
        findViewById(R.id.btn).setOnClickListener((view)->{
            if (!hasOverlayPermission()){
                openOverlayPermission();
            }
        });
        btn_wza = findViewById(R.id.btn_wza);
        btn_wza.setOnClickListener((view)->{
            onSetButtonClicked();
        });

    }


    @Override
    protected void onResume() {
        super.onResume();
        if(isService){
            btn_wza.setText("关闭自动投注");
        }else {
            btn_wza.setText("打开自动投注");
        }
    }

    /**
     * 开启无障碍服务
     */
    public void onSetButtonClicked() {
        String remark = remark_edit.getText().toString();
        if(remark.isEmpty()){
            ShowToast.show(MainActivity.this, "请输入备注");
            return;
        }
        if (!hasAccessibilityServiceEnabled()){//没有打开权限
            openAccessibilitySettings();
        }else {
            isService = !isService;
            setOffService();
        }
    }

    public void setOffService(){
        if(isService){
            btn_wza.setText("关闭自动投注");
        }else {
            btn_wza.setText("打开自动投注");
        }
        Intent intent = new Intent(MyAccessibilityService.INTENT_FILTER);
        intent.setAction(MyAccessibilityService.OFF_OR_ON);
        intent.putExtra("off",isService);
        intent.putExtra(REMARK_EDIT,remark_edit.getText().toString());
        sendBroadcast(intent);
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
        //requestOverlayPermissionLauncher.launch(intent);

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
                    isService = true;
                    setOffService();
                    btn_wza.setText("关闭自动投注");
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
                    ShowToast.show(MainActivity.this, "屏幕常量需要悬浮权限");
                }else {
                    showFloat();
                }
            }
    );

    public void  showFloat(){
        WindowManager windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        int type ;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
                    // 其他参数
        } else {
            type =
                    WindowManager.LayoutParams.TYPE_PHONE;
        }
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                // 宽度设置为自适应内容
                WindowManager.LayoutParams.WRAP_CONTENT,
                // 高度设置为自适应内容
                type,
                // 设置类型为应用程序悬浮窗口
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                        | WindowManager.LayoutParams.FLAG_SPLIT_TOUCH,
                PixelFormat.TRANSLUCENT
                // 设置窗口背景为透明
        );


        LayoutInflater inflater = LayoutInflater.from(this);
        View floatBarView = inflater.inflate(R.layout.float_window, null);
        params.gravity = Gravity.CENTER_VERTICAL | Gravity.LEFT;
        windowManager.addView(floatBarView, params);
    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

}






