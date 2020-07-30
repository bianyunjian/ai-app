package com.hankutech.ax.appdemo.util;

import android.util.Log;

import com.hankutech.ax.appdemo.constant.Common;

public class LogExt {
    public static void d(String tag, String msg) {
        if (Common.DebugMode) {
            Log.d(tag, msg);
        }
    }

    public static void i(String tag, String msg) {
        Log.i(tag, msg);
    }

    public static void e(String tag, String msg) {
        Log.e(tag, msg);
    }
}
