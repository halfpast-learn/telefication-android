package com.halfpastnein.telefication;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.util.Log;

import java.io.IOException;
import java.net.Socket;

public class NotificationSenderService extends Service {

    static final String ACTION_CONNECT = "com.hplearn.telefication.NotificationSenderService.ACTION_CONNECT";
    static final String ACTION_DISCONNECT = "com.hplearn.telefication.NotificationSenderService.ACTION_DISCONNECT";
    static final String ACTION_SEND_DATA = "com.hplearn.telefication.NotificationSenderService.ACTION_SEND_DATA";
    static final String ACTION_STOP_SERVICE = "com.hplearn.telefication.NotificationSenderService.ACTION_STOP";

    private Looper serviceLooper;
    private ServiceHandler serviceHandler;
    private Socket socket;
    boolean neverConnected, autoReconnect;

    private String SenderServiceTag = "SenderService";

    private final class ServiceHandler extends Handler {
        public ServiceHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            Log.i(SenderServiceTag,"Message received by handler");

            switch (String.valueOf(msg.obj)) {
                case ACTION_CONNECT:
                    connectToServer();
                    break;
                case ACTION_SEND_DATA:
                    sendData(msg.getData());
                    break;
                case ACTION_STOP_SERVICE:
                    onDestroy();
                    break;
                case ACTION_DISCONNECT:
                    disconnect();
                    break;
            }

        }
    }

    @Override
    public void onCreate() {
        HandlerThread thread = new HandlerThread("ServiceStartArguments",
                Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();

        socket = new Socket();
        neverConnected = true;
        autoReconnect = true;

        serviceLooper = thread.getLooper();
        serviceHandler = new ServiceHandler(serviceLooper);

        Log.i(SenderServiceTag, "Service created");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Message msg = serviceHandler.obtainMessage();
        msg.arg1 = startId;
        msg.obj = intent.getAction();
        if (msg.obj == ACTION_SEND_DATA) {
            String result = intent.getStringExtra("result");
            Bundle b = new Bundle();
            b.putString("result", result);
            msg.setData(b);
        }
        serviceHandler.sendMessage(msg);

        return START_STICKY;
    }

    public void connectToServer() {
        Log.i(SenderServiceTag, "Connecting to server");

        try {
            socket = new Socket("", 9090); // IP HAS TO BE HARDCODED
            if (socket.isConnected())
                Log.i(SenderServiceTag, "Connected");
            neverConnected = false;
            autoReconnect = true;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendData(Bundle bundle) {
        if (neverConnected) {
            Log.i(SenderServiceTag,"First connection");
            connectToServer();
        }
        if (socket==null && autoReconnect) {
            Log.i(SenderServiceTag,"Reconnecting");
            connectToServer();
        }
        if (socket==null) {
            Log.i(SenderServiceTag,"Autoreconnection disabled");
            Log.i(SenderServiceTag,"Send data failed");
            return;
        }
        String result = bundle.getString("result");

        //sending happens here
        try {
            socket.getOutputStream().write(result.getBytes());
            socket.getOutputStream().flush();
        } catch (IOException e) {
            Log.i(SenderServiceTag,"Error sending data " + e.getMessage());
        }
    }

    public void disconnect() {
        if (socket != null && !socket.isClosed()) {
            try {
                socket.close();
            } catch (IOException e) {
                Log.e(SenderServiceTag, "Error closing socket " + e.getMessage());
            } finally {
                socket = null;
                autoReconnect = false;
                Log.i(SenderServiceTag,"Disconnected");
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        Log.i(SenderServiceTag, "SenderService destroyed");

        if (socket != null && !socket.isClosed()) {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        socket = null;
    }
}
