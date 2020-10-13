package com.hankutech.ax.appdemo.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;

import com.hankutech.ax.appdemo.code.AudioScene;
import com.hankutech.ax.appdemo.constant.Common;
import com.hankutech.ax.appdemo.constant.SocketConst;
import com.hankutech.ax.appdemo.util.LogExt;

import java.util.HashMap;

import lombok.Data;
import lombok.ToString;

import static android.content.Context.MODE_PRIVATE;

@Data
@ToString
public class ConfigData {

    private static final String TAG = "ConfigData";
    private Uri logoUri;
    private String logoTitle;
    private Uri videoUri;
    private String aiFaceRTSPUrl;
    private Integer appNumber;
    private String serverIp;
    private Integer serverPort;
    private HashMap<AudioScene, Integer> audioMap = new HashMap<>();
    private String qrcodeUrl;

    public void update(Context ctx) {
        LogExt.d(TAG, "update:" + this.toString());

        SharedPreferences sp = ctx.getSharedPreferences("AXAPP", MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString("logoTitle", logoTitle == null ? "" : logoTitle);
        editor.putString("aiFaceRTSPUrl", aiFaceRTSPUrl == null ? "" : aiFaceRTSPUrl);
        editor.putString("videoUri", videoUri == null ? "" : videoUri.toString());
        editor.putString("logoUri", logoUri == null ? "" : logoUri.toString());
        editor.putString("appNumber", appNumber == null ? String.valueOf(Common.APP_NUMBER) : String.valueOf(appNumber));
        editor.putString("serverIp", serverIp == null ? SocketConst.CENTRAL_SERVER_LISTENING_IP : serverIp);
        editor.putString("serverPort", serverPort == null ? String.valueOf(SocketConst.CENTRAL_SERVER_LISTENING_PORT) : String.valueOf(serverPort));
        // 保存二维码地址
        editor.putString("qrcodeUrl", qrcodeUrl == null ? Common.DEFAULT_QRCODE_URL : qrcodeUrl);
        editor.commit();
    }

    public void loadData(Context ctx) {
        LogExt.d(TAG, "loadData");
        SharedPreferences sp = ctx.getSharedPreferences("AXAPP", MODE_PRIVATE);
        this.logoTitle = sp.getString("logoTitle", "");
        this.aiFaceRTSPUrl = sp.getString("aiFaceRTSPUrl", "");

        String videoUriStr = sp.getString("videoUri", "");
        if (videoUriStr.length() > 0) {
            this.videoUri = Uri.parse(videoUriStr);
        }
        String logoUriStr = sp.getString("logoUri", "");
        if (logoUriStr.length() > 0) {
            this.logoUri = Uri.parse(logoUriStr);
        }

        String appNumberStr = sp.getString("appNumber", String.valueOf(Common.APP_NUMBER));
        if (appNumberStr.length() > 0) {
            this.appNumber = Integer.parseInt(appNumberStr);
        }

        this.serverIp = sp.getString("serverIp", SocketConst.CENTRAL_SERVER_LISTENING_IP);


        String serverPortStr = sp.getString("serverPort", String.valueOf(SocketConst.CENTRAL_SERVER_LISTENING_PORT));
        if (serverPortStr.length() > 0) {
            this.serverPort = Integer.parseInt(serverPortStr);
        }

        // 设置默认二维码url地址
        this.qrcodeUrl = sp.getString("qrcodeUrl", Common.DEFAULT_QRCODE_URL);

    }
}
