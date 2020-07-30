package com.hankutech.ax.appdemo.code;

public enum MessageCode {
    UNKNOWN(0, "UNKNOWN"),
    HOME(10, "首页_正常状态"),
    HOME_SLEEP(11, "首页_休眠状态"),

    TIME_UPDATE(20, "时间更新"),
    APP_STATUS_UPDATE(21, "系统运行状态更新"),

    //    VIDEO_PLAY(30, "视频播放"),
//    VIDEO_STOP(31, "视频停止"),
    AUDIO_PLAY(32, "音频播放"),

    AUDIO_STOP(33, "音频停止"),
    AUDIO_PLAY_LOOP(34, "音频循环播放"),

//    TICKTOCK_START(40, "倒计时开始"),
//    TICKTOCK_STOP(41, "倒计时停止"),
//    TICKTOCK_UPDATE(42, "倒计时更新"),

    AUTH_PASS(51, "用户身份验证通过"),

    GARBAGE_PASS(61, "垃圾分类验证通过"),


    GATE_PASS(71, "投递箱门正常关闭"),
    ;

    public static MessageCode valueOf(int value) {
        switch (value) {
            case 10:
                return HOME;
            case 11:
                return HOME_SLEEP;
            case 20:
                return TIME_UPDATE;
            case 21:
                return APP_STATUS_UPDATE;
//            case 30:
//                return VIDEO_PLAY;
//            case 31:
//                return VIDEO_STOP;
            case 32:
                return AUDIO_PLAY;
            case 33:
                return AUDIO_STOP;
            case 34:
                return AUDIO_PLAY_LOOP;

            case 51:
                return AUTH_PASS;
            case 61:
                return GARBAGE_PASS;
            case 71:
                return GATE_PASS;
            default:
                return UNKNOWN;
        }
    }

    int value;
    String description;

    MessageCode(int v, String desc) {
        value = v;
        description = desc;
    }

    public int getValue() {
        return value;
    }

    public String getDescription() {
        return description;
    }
}
