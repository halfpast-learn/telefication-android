package com.halfpastnein.telefication;

import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import com.google.android.material.bottomappbar.BottomAppBar;

import static android.provider.Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS;
import static com.halfpastnein.telefication.NotificationSenderService.ACTION_CONNECT;
import static com.halfpastnein.telefication.NotificationSenderService.ACTION_DISCONNECT;

public class MainActivity extends AppCompatActivity {
    private TextView notificationOutputTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        if (!isNotificationServiceEnabled()) {
            showNotificationServiceAlertDialog();
        }

        BottomAppBar bottomAppBar = findViewById(R.id.bottomAppBar);
        setSupportActionBar(bottomAppBar);

        createNotificationChannel();

        Intent intent = new Intent(this, NotificationSenderService.class);
        startService(intent);

        notificationOutputTextView = findViewById(R.id.notificationsOutputTextview);
    }

    private boolean isNotificationServiceEnabled() {
        String[] listenerNames = Settings.Secure.getString(getContentResolver(),
                "enabled_notification_listeners").split(":");
        for (int i = 0; i < listenerNames.length; i++) {
            final ComponentName componentName = ComponentName.unflattenFromString(listenerNames[i]);
            if (componentName != null) {
                if (TextUtils.equals(this.getPackageName(), componentName.getPackageName())) {
                    return true;
                }
            }
        }
        return false;
    }

    private void showNotificationServiceAlertDialog() {
        (new AlertDialog.Builder(this))
                .setTitle("Notification access")
                .setMessage("Please grant access to notifications")
                .setPositiveButton("Grant",
                        (dialog, id) -> startActivity(new Intent(ACTION_NOTIFICATION_LISTENER_SETTINGS)))
                .setNegativeButton("Not now",
                        (dialog, id) -> Toast.makeText(
                                getApplicationContext(),
                                "App won't work properly",
                                Toast.LENGTH_SHORT).show()
                ).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.appbar_menu, menu);
        return true;
    }

    private void createNotificationChannel() {
        NotificationChannel channel = new NotificationChannel("teleficationChannel", "Telefication channel", NotificationManager.IMPORTANCE_DEFAULT);
        getSystemService(NotificationManager.class).createNotificationChannel(channel);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.clear:
                Intent intent = new Intent("android.service.notification.NotificationServiceReceiver");
                intent.putExtra("command", "clear");
                sendBroadcast(intent);
                notificationOutputTextView.setText("");
                break;
            case R.id.create:
                createNotification();
                break;
            case R.id.connect:
                Intent connectionIntent = new Intent(this, NotificationSenderService.class);
                connectionIntent.setAction(ACTION_CONNECT);
                startService(connectionIntent);
                break;
            case R.id.disconnect:
                Intent sendDataIntent = new Intent(this, NotificationSenderService.class);
                sendDataIntent.setAction(ACTION_DISCONNECT);
                startService(sendDataIntent);
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    void createNotification() {
        NotificationCompat.Builder notificationCompatBuilder = new NotificationCompat.Builder(this, "teleficationChannel")
                .setContentTitle("Example notification")
                .setContentText("Test notification created")
                .setTicker("Test notification created")
                .setSmallIcon(R.drawable.ic_baseline_info_24)
                .setAutoCancel(true);

        ((NotificationManager) getSystemService(NOTIFICATION_SERVICE)).notify((int) System.currentTimeMillis(), notificationCompatBuilder.build());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopService(new Intent(this, MyNotificationService.class));
        stopService(new Intent(this, NotificationSenderService.class));
    }
}