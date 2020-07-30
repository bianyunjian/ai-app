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
            showIpAndPort();
            server = new SocketServer(SocketConst.LISTENING_PORT, new ByteSocketServerInitializer(SocketConst.REQUEST_DATA_LENGTH));
            try {
                server.start();
            } catch (NettyServerException e) {
                e.printStackTrace();
            }
        });
        this.serverThread.start();

    }

    private void showIpAndPort() {
        //get wifi service
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        //判断wifi是否开启
        if (!wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(true);
        }
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        int ipAddress = wifiInfo.getIpAddress();
        String ip = intToIp(ipAddress);
        LogExt.d(TAG, "IP=" + ip);

    }

    private String intToIp(int i) {
        return (i & 0xFF) + "." +
                ((i >> 8) & 0xFF) + "." +
                ((i >> 16) & 0xFF) + "." +
                (i >> 24 & 0xFF);
    }
}