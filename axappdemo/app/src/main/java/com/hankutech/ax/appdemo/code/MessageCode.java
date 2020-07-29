package com.hankutech.ax.appdemo.code;

public enum MessageCode {
    UNKNOWN(0, "UNKNOWN"),
    HOME(10, "首页_正常状态"),
    HOME_SLEEP(11, "首页_休眠状态"),

    TIME_UPDATE(20, "时间更新"),
    APP_STATUS_UPDATE(21, "系统运行状态更新"),

    VIDEO_PLAY(30, "视频播放"),
    VIDEO_STOP(31, "视频停止"),
    AUDIO_PLAY(32, "音频播放"),
    AUDIO_STOP(33, "音频停止"),

    TICKTOCK_START(40, "倒计时开始"),
    TICKTOCK_STOP(41, "倒计时停止"),
    TICKTOCK_UPDATE(42, "倒计时更新");

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
            case 30:
                return VIDEO_PLAY;
            case 31:
                return VIDEO_STOP;
            case 32:
                return AUDIO_PLAY;
            case 33:
                return AUDIO_STOP;
            case 40:
                return TICKTOCK_START;
            case 41:
                return TICKTOCK_STOP;
            case 42:
                return TICKTOCK_UPDATE;
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
