package com.hankutech.ax.appdemo.util;

import android.util.Log;

import com.hankutech.ax.appdemo.constant.Common;
import com.hankutech.ax.appdemo.event.LogEvent;

import org.greenrobot.eventbus.EventBus;

import java.text.SimpleDateFormat;
import java.util.Date;

public class LogExt {

    public static void d(String tag, String msg) {

        Log.d(tag, msg);

        postLogEvent(tag, msg);
    }

    private static void postLogEvent(String tag, String msg) {
        String formatMsg = String.format("%s-%s: %s", getNowTimeString(), tag, msg);
        EventBus.getDefault().post(new LogEvent(formatMsg));
    }

    private static String getNowTimeString() {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String nowTimeText = format.format(new Date());
        return nowTimeText;
    }

    public static void i(String tag, String msg) {
        Log.i(String.format("%s-%s", getNowTimeString(), tag), msg);
        postLogEvent(tag, msg);
    }


    public static void e(String tag, String msg) {
        Log.e(String.format("%s-%s", getNowTimeString(), tag), msg);
        postLogEvent(tag, msg);
    }
}
