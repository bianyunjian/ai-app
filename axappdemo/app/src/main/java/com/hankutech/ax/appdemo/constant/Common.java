package com.hankutech.ax.appdemo.constant;

public class Common {
    public static int APP_NUMBER = 2;
    /**
     * 是否Debug模式
     */
    public static boolean DebugMode = true;

    /**
     * 视频是否静音
     */
    public static boolean VideoMute = false;

    /**
     * 基础倒计时间隔
     */
    public static long BaseInterval = 30 * 1000;
    public static long DebugRate = 5;
    /**
     * 倒计时间隔
     */
    public static long TickInterval = 1 * 1000;

    /**
     * 正常流程的倒计时
     */
    public static long TickMillis = (DebugMode ? DebugRate : 1) * BaseInterval; //30


    /**
     * 垃圾分类检测的倒计时
     */
    public static long GarbageWaitDetectMillis = (DebugMode ? DebugRate : 1) * BaseInterval; //30
    /**
     * 垃圾分类检测失败的倒计时
     */
    public static long GarbageDetectFailureMillis = (DebugMode ? DebugRate : 1) * 10 * 1000; //10
    /**
     * 等待垃圾投递的倒计时
     */
    public static long GarbageWaitDeliverMillis = (DebugMode ? DebugRate : 1) * BaseInterval; //60


    /**
     * 关门到位后的倒计时
     */
    public static long GateClosedMillis = 5 * 1000;  //5
    /**
     * 等待门的倒计时
     */
    public static long GateWaitMillis = (DebugMode ? DebugRate : 1) * BaseInterval;  //30


    public static String getTickDesc(Long t) {
        return "倒计时 " + t + " 秒";
    }

    /**
     * 人脸摄像头rtsp地址
     */
    public static String AIFace_RTSP_URI = "rtsp://admin:1qaz1qaz@172.16.114.34:554/";


    public static String LogoTitle = "垃圾分类投递显示系统";

    /**
     * 轮询发送消息的时间间隔
     */
    public static long MessageLoopInterval = 3 * 1000;
}
