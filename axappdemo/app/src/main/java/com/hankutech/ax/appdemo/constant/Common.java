package com.hankutech.ax.appdemo.constant;

import com.hankutech.ax.appdemo.ax.code.AIGarbageResultType;

public class Common {
    /**
     * 是否Debug模式
     */
    public static boolean DebugMode = true;

    /**
     * 视频是否静音
     */
    public static boolean VideoMute = false;

    /**
     * 倒计时间隔
     */
    public static long TickInterval = 1 * 1000;

    /**
     * 正常流程的倒计时
     */
    public static long TickMillis = 120 * 1000; //30

    /**
     * 发送垃圾分类检测请求的时间
     */
    public static long SendGarbageDetectRequestMillis = 10 * 1000;

    /**
     * 垃圾分类检测的倒计时
     */
    public static long GarbageWaitDetectMillis = 120 * 1000; //30
    /**
     * 垃圾分类检测失败的倒计时
     */
    public static long GarbageDetectFailureMillis = 120 * 1000; //10
    /**
     * 等待垃圾投递的倒计时
     */
    public static long GarbageWaitDeliverMillis = 120 * 1000; //60

    /**
     * 当前垃圾投递类型
     */
    public static AIGarbageResultType CurrentGarbageType = AIGarbageResultType.DRY;

    /**
     * 关门到位后的倒计时
     */
    public static long GateClosedMillis = 5 * 1000;  //5
    /**
     * 等待关门的倒计时
     */
    public static long GateWaitMillis = 120 * 1000;  //30

    public static String getTickDesc(Long t) {
        return "倒计时 " + t + " 秒";
    }

    /**
     * 人脸摄像头rtsp地址
     */
    public static String AIFace_RTSP_URI = "rtsp://admin:UEDMAJ@192.168.0.28/";


    public static String LogoTitle = "垃圾分类投递显示系统";
}
