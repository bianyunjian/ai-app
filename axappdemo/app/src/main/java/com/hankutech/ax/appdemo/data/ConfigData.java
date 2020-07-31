package com.hankutech.ax.appdemo.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;

import com.hankutech.ax.appdemo.code.AudioScene;
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

    private HashMap<AudioScene, Integer> audioMap = new HashMap<>();

    public void update(Context ctx) {
        LogExt.d(TAG, "update:" + this.toString());


        SharedPreferences sp = ctx.getSharedPreferences("AXAPP", MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString("logoTitle", logoTitle == null ? "" : logoTitle);
        editor.putString("aiFaceRTSPUrl", aiFaceRTSPUrl == null ? "" : aiFaceRTSPUrl);
        editor.putString("videoUri", videoUri == null ? "" : videoUri.toString());
        editor.putString("logoUri", logoUri == null ? "" : logoUri.toString());
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

    }
}
