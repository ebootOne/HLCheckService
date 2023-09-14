package com.xbase.tool;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.provider.Settings;
import android.text.TextUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.UUID;


public class DeviceUtils {
    private DeviceUtils() {
        throw new UnsupportedOperationException("u can't instantiate me...");
    }

    public static String getSDKVersionName() {
        return Build.VERSION.RELEASE;
    }


    /**
     * Return version code of device's system.
     *
     * @return version code of device's system
     */
    public static int getSDKVersionCode() {
        return Build.VERSION.SDK_INT;
    }

    /**
     * 设备型号
     * */
    public static String getModel() {
        String model = Build.MODEL;
        if (model != null) {
            model = model.trim().replaceAll("\\s*", "");
        } else {
            model = "";
        }
        return model;
    }
    /**
     * Return the android id of device.
     *
     * @return the android id of device
     */
    @SuppressLint("HardwareIds")
    public static String getAndroidID(Context context) {
        String id = Settings.Secure.getString(
                context.getContentResolver(),
                Settings.Secure.ANDROID_ID
        );
        if ("9774d56d682e549c".equals(id)) return "";
        return id == null ? "" : id;
    }



    private static final    String KEY_UDID = "KEY_UDID";
    private volatile static String udid;


    /**
     * 设备id
     * */
    @SuppressLint({"MissingPermission", "HardwareIds"})
    public static String getUniqueDeviceId(Context context) {
        return getUniqueDeviceId(context,"", true);
    }
    public JSONObject getJsonDeviceId(Context context){
        JSONObject data = new JSONObject();
        try {
            data.put("device_id", getUniqueDeviceId(context,"", true));
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        return data;
    }

    /**
     * @param prefix   The prefix of the unique device id.
     * @param useCache True to use cache, false otherwise.
     * @return the unique device id
     */
    @SuppressLint({"MissingPermission", "HardwareIds"})
    public static String getUniqueDeviceId(Context context,String prefix, boolean useCache) {
        if (!useCache) {
            return getUniqueDeviceIdReal(context,prefix);
        }
        if (udid == null) {
            synchronized (DeviceUtils.class) {
                if (udid == null) {
                    final String id = UtilsBridge.getSpUtils4Utils(context).getString(KEY_UDID, null);
                    if (id != null) {
                        udid = id;
                        return udid;
                    }
                    return getUniqueDeviceIdReal(context,prefix);
                }
            }
        }
        return udid;
    }

    private static String getUniqueDeviceIdReal(Context context,String prefix) {
        try {
            final String androidId = getAndroidID(context);
            if (!TextUtils.isEmpty(androidId)) {
                return saveUdid(context,prefix + 2, androidId);
            }

        } catch (Exception ignore) {/**/}
        return saveUdid(context,prefix + 9, "");
    }

    private static String saveUdid(Context context,String prefix, String id) {
        udid = getUdid(prefix, id);
        UtilsBridge.getSpUtils4Utils(context).put(KEY_UDID, udid);
        return udid;
    }

    private static String getUdid(String prefix, String id) {
        if (id.equals("")) {
            return prefix + UUID.randomUUID().toString().replace("-", "");
        }
        return prefix + UUID.nameUUIDFromBytes(id.getBytes()).toString().replace("-", "");
    }
    /**
     * 获取手机型号
     *
     * @return  手机型号
     */
    public static String getSystemModel() {
        return android.os.Build.MODEL;
    }

    /**
     * 获取手机厂商
     *
     * @return  手机厂商
     */
    public static String getDeviceBrand() {
        return android.os.Build.BRAND;
    }


    /**
     * 获取版本号
     * */
    public static String getAppVersionName(Context context) {
        return getAppVersionName(context,context.getPackageName());
    }

    public static String getAppVersionName(Context context,String packageName) {
        if (isSpace(packageName)) return "";
        try {
            PackageManager pm = context.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(packageName, 0);
            return pi == null ? null : pi.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return "";
        }
    }

    public static boolean isSpace(final String s) {
        if (s == null) return true;
        for (int i = 0, len = s.length(); i < len; ++i) {
            if (!Character.isWhitespace(s.charAt(i))) {
                return false;
            }
        }
        return true;
    }

}
