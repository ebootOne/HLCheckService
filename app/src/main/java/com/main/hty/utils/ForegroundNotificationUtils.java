package com.main.hty.utils;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import com.main.hty.hlcheckservice.R;


/**
 * 适配Android O
 */
public class ForegroundNotificationUtils {

    private static final String CHANNEL_ID_FAST_ENTRANCE = "com.adfuture.clean.CHANNEL_ID_FAST_SERVICE";

    public static  Notification showFastEntranceNotify(Context context) {
        Notification.Builder builder = NotificationUtils.getSafeNotificationBuilder(context,CHANNEL_ID_FAST_ENTRANCE, R.string.app_name);
        builder.setContent(getView(context));
        builder.setSmallIcon(R.mipmap.ic_launcher);
        builder.setOngoing(true);
        return builder.build();
        //notify(context, notifyID, builder.build());
    }

    public static RemoteViews getView(Context context){
        RemoteViews switchRemoteViews = new RemoteViews(context.getPackageName(), R.layout.notify_fast_entrance);

        return  switchRemoteViews;
    }


}
