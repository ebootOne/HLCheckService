package com.main.hty.utils;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.text.TextUtils;
import android.widget.RemoteViews;

import com.main.hty.hlcheckservice.Constants;
import com.main.hty.hlcheckservice.R;

/**
 * 适配Android O
 */
public class NotificationUtils {

    private static final String CHANNEL_ID_FAST_ENTRANCE = "com.adfuture.clean.CHANNEL_ID_NOTIFY";

    public static Notification.Builder getSafeNotificationBuilder(Context context, String channelId, int channelNameResId) {
        return getSafeNotificationBuilder(context, channelId, channelNameResId, false, false);
    }

    public static Notification.Builder getSafeNotificationBuilder(Context context, String channelId, int channelNameResId, boolean needSound, boolean isFloating) {
        Notification.Builder builder = new Notification.Builder(context);
        if (!CommonUtils.isPreAndroidO()) {
            builder.setChannelId(channelId);
        }
        builder.setOnlyAlertOnce(true);
        int importance;
        if (isFloating) {
            builder.setPriority(Notification.PRIORITY_HIGH);
            builder.setDefaults(Notification.DEFAULT_VIBRATE);
            importance = NotificationManager.IMPORTANCE_HIGH;
        } else {
            builder.setPriority(Notification.PRIORITY_DEFAULT);
            importance = NotificationManager.IMPORTANCE_DEFAULT;
        }
        createNotificationChannel(context, channelId, context.getString(channelNameResId), needSound, importance);
        return builder;
    }

    private static void createNotificationChannel(Context context, String channelId, String channelName, boolean needSound, int importance) {
        if (CommonUtils.isPreAndroidO()) {
            return;
        }
        if (TextUtils.isEmpty(channelId) || TextUtils.isEmpty(channelName) || context == null) {
            return;
        }
        NotificationChannel notificationChannel = new NotificationChannel(
                channelId, channelName, importance);
        notificationChannel.enableLights(false);
        notificationChannel.setShowBadge(false);
        if (!needSound) {
            notificationChannel.setSound(null, null);
        }
        notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_SECRET);
        NotificationManager manager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager == null) {
            return;
        }
        manager.createNotificationChannel(notificationChannel);
    }

    public static void notify(Context context, int id, Notification notification) {
        getManager(context).notify(id, notification);
    }

    public static void notify(NotificationManager notificationManager, int id, Notification notification) {
        notificationManager.notify(id, notification);
    }

    public static void cancel(Context context, int id) {
        NotificationManager notificationManager = getManager(context);
        notificationManager.cancel(id);
    }

    private static NotificationManager getManager(Context context) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        return notificationManager;
    }


    public static  boolean isNotificationListenersEnabled(Context context) {
        String pkgName = context.getPackageName();
        final String flat = Settings.Secure.getString(context.getContentResolver(), "enabled_notification_listeners");
        if (!TextUtils.isEmpty(flat)) {
            final String[] names = flat.split(":");
            for (int i = 0; i < names.length; i++) {
                final ComponentName cn = ComponentName.unflattenFromString(names[i]);
                if (cn != null) {
                    if (TextUtils.equals(pkgName, cn.getPackageName())) {
                        return true;
                    }
                }
            }
        }
        return false;

    }

    }
