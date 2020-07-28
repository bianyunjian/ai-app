package com.hankutech.ax.appdemo.constant;

public class Common {
    public static boolean DebugMode = true;

    public static long TikTokSeconds = 15 * 1000;
    public static long TikTokInterval = 1 * 1000;

    public static String getTikTokDesc(Long t) {
        return "倒计时 " + t + " 秒";
    }
}
