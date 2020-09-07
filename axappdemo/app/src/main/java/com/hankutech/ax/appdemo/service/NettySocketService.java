package com.hankutech.ax.appdemo.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.Nullable;

import com.hankutech.ax.appdemo.constant.SocketConst;
import com.hankutech.ax.appdemo.socket.ByteSocketClientInitializer;
import com.hankutech.ax.appdemo.socket.SocketClient;
import com.hankutech.ax.appdemo.util.LogExt;

public class NettySocketService extends Service {
    private static NettySocketService instance;
    private static final String TAG = "SocketService";
    private SocketClient socketClient;
    private Thread clientThread;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        LogExt.d(TAG, "onCreate");
        super.onCreate();

        startSocketServer();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        LogExt.d(TAG, "onStartCommand");
        return super.onStartCommand(intent, flags, startId);
    }


    @Override
    public void onDestroy() {
        LogExt.d(TAG, "onDestroy");
        stopSocketServer();
        super.onDestroy();

    }

    public void stopSocketServer() {
        LogExt.d(TAG, "stopSocketServer");
        if (socketClient != null) {
            socketClient.close();
            socketClient=null;
            this.clientThread.interrupt();
        }
    }

    public void startSocketServer() {
        LogExt.d(TAG, "startSocketServer");


        this.clientThread = new Thread(() -> {
            socketClient = new SocketClient(SocketConst.CENTRAL_SERVER_LISTENING_IP,
                    SocketConst.CENTRAL_SERVER_LISTENING_PORT,
                    new ByteSocketClientInitializer(SocketConst.FIXED_LENGTH_FRAME));
            try {
                socketClient.startConnect();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        this.clientThread.setName("socketClientThread");
        this.clientThread.start();

    }


}
