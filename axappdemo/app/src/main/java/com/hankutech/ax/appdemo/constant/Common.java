package com.hankutech.ax.appdemo.constant;

public class Common {
    public static boolean DebugMode = true;


    public static long TikTokInterval = 1 * 1000;
    public static long TikTokSeconds = 15 * 1000 + TikTokInterval;

    public static String getTikTokDesc(Long t) {
        return "倒计时 " + t + " 秒";
    }

    public  static  String AIFace_RTSP_URI="rtsp://admin:NYCQJS@192.168.123.115/";

}
