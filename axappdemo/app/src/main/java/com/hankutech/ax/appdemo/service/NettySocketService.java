package com.hankutech.ax.appdemo.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import com.hankutech.ax.appdemo.ax.SocketConst;
import com.hankutech.ax.appdemo.socket.ByteSocketServerInitializer;
import com.hankutech.ax.appdemo.socket.NettyServerException;
import com.hankutech.ax.appdemo.socket.SocketServer;
import com.hankutech.ax.appdemo.util.LogExt;

public class NettySocketService extends Service {
    private static final String TAG = "SocketService";
    private SocketServer server;
    private Thread serverThread;

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

    private void stopSocketServer() {
        LogExt.d(TAG, "stopSocketServer");
        if (server != null) {
            server.shutdown();
            this.serverThread.interrupt();
        }
    }

    private void startSocketServer() {
        LogExt.d(TAG, "startSocketServer");

        this.serverThread = new Thread(() -> {
            server = new SocketServer(SocketConst.LISTENING_PORT, new ByteSocketServerInitializer(SocketConst.REQUEST_DATA_LENGTH));
            try {
                server.start();
            } catch (NettyServerException e) {
                e.printStackTrace();
            }
        });
        this.serverThread.start();

    }


}
