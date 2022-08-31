package com.halfpastnein.telefication;


import android.app.Notification;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import static com.halfpastnein.telefication.NotificationSenderService.ACTION_SEND_DATA;

public class MyNotificationService extends NotificationListenerService
{
    NotificationServiceReceiver notificationServiceReceiver;

    String NotificationServiceTag = "NotificationService";

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        Log.i(NotificationServiceTag,"Notification posted");
        Intent i = new  Intent("android.service.notification.NotificationListenerService");
        i.putExtra("notification_event","onNotificationPosted: " + sbn.getPackageName() + "\n");
        sendBroadcast(i);

        final PackageManager pm = getApplicationContext().getPackageManager();
        ApplicationInfo ai;
        try {
            ai = pm.getApplicationInfo( sbn.getPackageName(), 0);
        } catch (final PackageManager.NameNotFoundException e) {
            ai = null;
        }
        final String applicationName = (String) (ai != null ? pm.getApplicationLabel(ai) : "(unknown)");

        i = new Intent(this,NotificationSenderService.class);
        i.setAction(ACTION_SEND_DATA);
        i.putExtra("result",applicationName+"\n"+sbn.getNotification().extras.getString(Notification.EXTRA_TITLE)+"\n"+sbn.getNotification().extras.getString(Notification.EXTRA_TEXT));
        startService(i);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(NotificationServiceTag,"NotificationService destroyed");
    }

    @Override
    public void onCreate() {
        Log.i(NotificationServiceTag,"NotificationService created");
        super.onCreate();
        notificationServiceReceiver = new NotificationServiceReceiver();

        IntentFilter filter = new IntentFilter();
        filter.addAction("android.service.notification.NotificationServiceReceiver");
        registerReceiver(notificationServiceReceiver,filter);

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }


    class NotificationServiceReceiver extends BroadcastReceiver
    {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(NotificationServiceTag,"Broadcast received");
            if (intent.getStringExtra("command").equals("clear"))
            {
                MyNotificationService.this.cancelAllNotifications();
            }
        }
    }
}