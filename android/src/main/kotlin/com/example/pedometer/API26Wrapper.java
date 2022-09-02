package com.example.pedometer;


import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.provider.Settings;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

@TargetApi(Build.VERSION_CODES.O)
public class API26Wrapper {

    public final static String NOTIFICATION_CHANNEL_ID = "Notification";

    public static NotificationCompat.Builder getNotificationBuilder(final Context context) {
        if (Build.VERSION.SDK_INT >= 26 ) {
            NotificationManager manager =
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            NotificationChannel channel =
                    new NotificationChannel(NOTIFICATION_CHANNEL_ID, NOTIFICATION_CHANNEL_ID,
                            NotificationManager.IMPORTANCE_NONE);
            channel.setImportance(NotificationManager.IMPORTANCE_MIN); // ignored by Android O ...
            channel.enableLights(false);
            channel.enableVibration(false);
            channel.setBypassDnd(false);
            channel.setSound(null, null);
            manager.createNotificationChannel(channel);
        }
        return new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID);
    }


}
