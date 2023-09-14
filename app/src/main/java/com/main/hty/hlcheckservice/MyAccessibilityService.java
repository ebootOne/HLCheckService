package com.main.hty.hlcheckservice;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.accessibilityservice.GestureDescription;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Path;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.util.Log;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import com.main.hty.MyThreadFactory;
import com.main.hty.utils.HttpUtils;
import com.xbase.tool.DeviceUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author ASUS
 */


/*资源id
 * 141,1271 939,1395  编辑框
 * 945，1265 1080，1400
 *
 * */
public class MyAccessibilityService extends AccessibilityService{
    private static final String TAG = "AccessibilityService";
    private final String PACKAGENAME = "com.example.yu";

    private volatile boolean isStopClick = false;
    private ThreadPoolExecutor clickThreadPool;
    PowerManager.WakeLock mWakeLock;
    Context context;

    List<List<String>> clickPointMessages = new ArrayList<>();
    Runnable runnable = new Runnable() {
        /**
         * @channel 渠道
         * @app_version 应用版本号
         * @device_id 设备id
         * @number 设备型号
         * @os 系统版本号
         * */
        @Override
        public void run() {
            if(!serviceOn){
                if(mapIds != null){
                    mapIds.clear();
                }
                return;
            }
            if(isUpdateIng ){//正在更新中 是否关闭服务
                return;
            }
            Map<String,String> map = new HashMap<>();
            map.put("factory", DeviceUtils.getDeviceBrand());
            map.put("device_id", DeviceUtils.getUniqueDeviceId(context));
            map.put("number", DeviceUtils.getModel());
            map.put("version", DeviceUtils.getSDKVersionCode() + "");
            map.put("remark",remark);
            String result = HttpUtils.requestByGet("get_xiazhu.html?",System.currentTimeMillis()/1000,map);
            Log.i(TAG,"onAccessibilityEvent---:runnable"+result);
            if(result != null) {
                sendMsgBroadcast(result);

            }
        }
    };

    ConcurrentHashMap <String,String> mapIds = new ConcurrentHashMap <>();
    public synchronized void sendMsgBroadcast(String result){
        Intent intent = new Intent(MyAccessibilityService.ACTION_AIUI_UPDATE);
        try {
            JSONObject resultJson = new JSONObject(result);
            JSONArray jsonArray = resultJson.getJSONArray("data");
            if(jsonArray == null){
                return;
            }
            for (int i = 0, length = jsonArray.length(); i < length; i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                String msg = jsonObject.getString("msg");
                String id = jsonObject.getString("id");
                intent.putExtra("input",msg);
                intent.putExtra("id",id);
                if(i == length-1) {
                    intent.putExtra("isSendComplete", true);//是否发送完成
                }
                if(msg != null && msg.length() > 0) {
                    sendBroadcast(intent);
                }
                Log.i(TAG,"onAccessibilityEvent---:runnable："+id+"---:"+"--："+msg);

            }
        } catch (Exception e) {
            e.printStackTrace();
        }


    }
    @SuppressLint("InvalidWakeLockTag")
    @Override
    public void onCreate() {
        super.onCreate();
        context = this;
        regReceiver();
        // 1. 创建 定时线程池对象 & 设置线程池线程数量固定为5
        ScheduledExecutorService scheduledThreadPool = Executors.newScheduledThreadPool(10);// 2. 创建好Runnable类线程对象 & 需执行的任务
        // 3. 向线程池提交任务：schedule（）
        //scheduledThreadPool.schedule(runnable, 0, TimeUnit.SECONDS); // 延迟1s后执行任务
        scheduledThreadPool.scheduleAtFixedRate(runnable,10,10000,TimeUnit.MILLISECONDS);// 延迟10ms后、每隔5000ms执行任务
        ThreadFactory threadFactory = new MyThreadFactory();
        // Replace with your custom ThreadFactory implementation
        int corePoolSize = 1;
        // 初始核心线程数
        int maximumPoolSize = 5;
        // 最大线程数
        long keepAliveTime = 0;
        // 线程空闲时间
        TimeUnit unit = TimeUnit.MILLISECONDS;
        // 时间单位
        BlockingQueue<Runnable> workQueue = new LinkedBlockingQueue<>();
        // 任务队列
        clickThreadPool = new ThreadPoolExecutor(
                corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory
        );
    }

    Handler mHandler = new Handler();
    // 初始化 clickPointMessages 列表
    @SuppressLint("InflateParams")
    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        setAccessibilityNodeInfos();

    }

    AIUIBroadcast broadcast;
    public void regReceiver(){
        //注册广播实例
        IntentFilter intentFilter=new IntentFilter(INTENT_FILTER);
        intentFilter.addAction(ACTION_AIUI_UPDATE);
        intentFilter.addAction(OFF_OR_ON);
        broadcast=new AIUIBroadcast();
        registerReceiver(broadcast,intentFilter);
    }

    String input;
    public static final String INTENT_FILTER="accessibility";
    public static final String ACTION_AIUI_UPDATE="input_";
    public static final String OFF_OR_ON="off_or_on";//关闭开光
    public boolean serviceOn = true;
    boolean isUpdateIng;//是否更新中
    String remark;
    public class AIUIBroadcast extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(action == OFF_OR_ON){
                remark = intent.getStringExtra(MainActivity.REMARK_EDIT);
                Log.i("","onAccessibilityEvent---BroadcastReceiver:"+remark );
                serviceOn = intent.getBooleanExtra("off",false);
            }
            if (action == ACTION_AIUI_UPDATE) {
                //更新UI的操作
                input = intent.getStringExtra("input");
                String id = intent.getStringExtra("id");
                Log.i("","onAccessibilityEvent---onReceive"+id+"--:"+input);
                if(mapIds != null && mapIds.get(id) == null){//如果id已经发送过也要取消
                    Log.i("","onAccessibilityEvent---mapIds"+id);
                    fillEditText(input);
                    clickViewByContentDescription("Send",id);
                    try {
                        Thread.currentThread().sleep(200);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }

                boolean isSend = intent.getBooleanExtra("isSendComplete", false);//发送了一组之后
                if(isSend && mapIds != null && mapIds.size() > 0){
                    clickThreadPool.execute(new Runnable() {
                        @Override
                        public void run() {
                            StringBuilder ids = new StringBuilder();
                            for(Map.Entry<String,String>  entry : mapIds.entrySet()) {
                                ids.append(entry.getValue()).append(",");
                            }
                            if(ids != null){
                                HashMap<String,String> map = new HashMap<>();
                                map.put("ids",ids.toString());
                                map.put("factory", DeviceUtils.getDeviceBrand());
                                map.put("device_id", DeviceUtils.getUniqueDeviceId(context));
                                map.put("number", DeviceUtils.getModel());
                                map.put("version", DeviceUtils.getSDKVersionCode() + "");
                                map.put("remark",remark);
                                isUpdateIng = true;
                                String result = HttpUtils.requestByGet("update_xiazhu.html?",System.currentTimeMillis()/1000,map);
                                Log.i("","onAccessibilityEvent---:runnable update"+result);
                                if(result != null){
                                    isUpdateIng = false;
                                    try {
                                        JSONObject resultJson = new JSONObject(result);
                                        String code = resultJson.getString("code");
                                        if(code.equals("200")){
                                            mapIds.clear();
                                        }
                                    } catch (JSONException e) {
                                        throw new RuntimeException(e);
                                    }
                                }else {
                                    isUpdateIng = false;
                                }
                            }

                        }
                    });
                }
            }
        }
    }

    AccessibilityNodeInfo rootNode = null;
    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        String s = event.toString();
        if (/*event != null && event.getPackageName() != null && event.getPackageName().equals("org.telegram.messenger.web") && */event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            rootNode = getRootInActiveWindow();
            //fillEditText(ipunt);
        }

    }
    private synchronized void fillEditText(String input) {
        Log.i(TAG,"onAccessibilityEvent---:onReceive：1"+input);
        //if (rootNode == null || input == null) return;
        Log.i(TAG,"onAccessibilityEvent---:onReceive：2"+input);
        // 这里我们查找 EditText 控件。通常 EditText 控件的类名是 "android.widget.EditText"。
        AccessibilityNodeInfo rootNode = getRootInActiveWindow();
        List<AccessibilityNodeInfo> editTextList = findNodesByClassName(rootNode,"android.widget.EditText");

        for (AccessibilityNodeInfo editText : editTextList) {
            Log.i(TAG,"onAccessibilityEvent---:onReceive：3"+input);
            // 输入自定义文字
            Bundle arguments = new Bundle();
            arguments.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, input);
            editText.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments);
        }
    }

    private synchronized List<AccessibilityNodeInfo> findNodesByClassName(AccessibilityNodeInfo rootNode, String className) {
        List<AccessibilityNodeInfo> matchingNodes = new ArrayList<>();

        recursiveSearchByClassName(rootNode, className, matchingNodes);

        return matchingNodes;
    }

    private synchronized void recursiveSearchByClassName(AccessibilityNodeInfo node, String className, List<AccessibilityNodeInfo> resultList) {
        if (node == null) {
            return;
        }

        if (className.equals(node.getClassName())) {
            resultList.add(node);
        }

        for (int i = 0; i < node.getChildCount(); i++) {
            recursiveSearchByClassName(node.getChild(i), className, resultList);
        }
    }


    private synchronized void clickViewByContentDescription(String description,String id) {
        AccessibilityNodeInfo rootNode = getRootInActiveWindow();
        /*if (rootNode == null || id.equals("1") || id.equals("2")) {
            return;
        }*/

        List<AccessibilityNodeInfo> matchingNodes = findNodesByContentDescription(rootNode, description);
        for (AccessibilityNodeInfo node : matchingNodes) {
            boolean isSend = node.performAction(AccessibilityNodeInfo.ACTION_CLICK);
            Log.i(TAG,"onAccessibilityEvent---:onReceive：4"+id+"-input:"+input);
            if(isSend){//发送成功了添加进去
                mapIds.put(id,id);
            }
        }
    }

    private List<AccessibilityNodeInfo> findNodesByContentDescription(AccessibilityNodeInfo rootNode, String description) {
        List<AccessibilityNodeInfo> matchingNodes = new ArrayList<>();
        recursiveSearchByContentDescription(rootNode, description, matchingNodes);
        return matchingNodes;
    }

    private void recursiveSearchByContentDescription(AccessibilityNodeInfo node, String description, List<AccessibilityNodeInfo> resultList) {
        if (node == null) {
            return;
        }

        if (node.getContentDescription() != null && description.equals(node.getContentDescription().toString())) {
            resultList.add(node);
        }
        for (int i = 0; i < node.getChildCount(); i++) {
            recursiveSearchByContentDescription(node.getChild(i), description, resultList);
        }
    }


    @Override
    public void onInterrupt() {
        Log.e(TAG, "Something went wrong");
    }


    //TODO 动态配置，有时候onAccessibilityEvent无反应则需要配置以下
    //TODO 动态配置，有时候onAccessibilityEvent无反应则需要配置以下
    //TODO 动态配置，有时候onAccessibilityEvent无反应则需要配置以下

    private void setAccessibilityNodeInfos(){
        AccessibilityServiceInfo serviceInfo = new AccessibilityServiceInfo();
//        serviceInfo.eventTypes = AccessibilityEvent.TYPE_WINDOWS_CHANGED;
        serviceInfo.eventTypes = AccessibilityEvent.TYPES_ALL_MASK;
        serviceInfo.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC;
        serviceInfo.flags |= AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS;

        serviceInfo.flags |= AccessibilityServiceInfo.FLAG_REQUEST_TOUCH_EXPLORATION_MODE;

        serviceInfo.flags |= AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS;

        setServiceInfo(serviceInfo);
    }



}
