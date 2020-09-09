package com.hankutech.ax.appdemo.code;

public enum AudioScene {
    UNKNOWN(0, "UNKNOWN"),
    AUTH_CHOOSE_TYPE(11, "请选择身份验证方式"),
    AUTH_RFID(12, "请在左侧如图位置刷卡投递"),
    AUTH_AI_FACE(13, "请将面部正对摄像头"),
    AUTH_QRCODE(14, "微信扫描二维码授权开门"),
    AUTH_PASS(15, "欢迎您"),
    AUTH_FAIL(16, "用户您好，您的身份验证没有通过 请联系管理员"),

    GARBAGE_DETECT(21, "正在检测垃圾中\n请稍等..."),
    //    GARBAGE_DETECT_SUCCESS(22, "请按照垃圾分类,\n并打开垃圾袋投\n放全程会视频录\n制和评分"),
    GARBAGE_DETECT_FAILURE_DRY(23, "当前是干垃圾\n投放窗口您投递\n垃圾不符合要求"),
    GARBAGE_DETECT_FAILURE_WET(24, "当前是湿垃圾\n投放窗口您投递\n垃圾不符合要求"),
    GARBAGE_DETECT_FAILURE_RECYCLE(25, "当前是可回收垃圾\n投放窗口您投递\n垃圾不符合要求"),
    GARBAGE_DETECT_FAILURE_HA(26, "当前是有害垃圾\n投放窗口您投递\n垃圾不符合要求"),
    GARBAGE_DETECT_FAILURE_WHITE_QUILT(27, "当前是被服垃圾\n投放窗口您投递\n垃圾不符合要求"),

    GATE_CLOSE(31, "投递完成"),
    GATE_NOT_CLOSE(32, "请按照垃圾分类,\n并打开垃圾袋投\n放全程会视频录\n制和评分"),
    GATE_NOT_CLOSE_TIMEOUT(33, "系统故障,请联系管理人员处理");

    public static AudioScene valueOf(int value) {
        switch (value) {
            case 11:
                return AUTH_CHOOSE_TYPE;
            case 12:
                return AUTH_RFID;
            case 13:
                return AUTH_AI_FACE;
            case 14:
                return AUTH_QRCODE;
            case 15:
                return AUTH_PASS;
            case 16:
                return AUTH_FAIL;

            case 21:
                return GARBAGE_DETECT;
//            case 22:
//                return GARBAGE_DETECT_SUCCESS;
            case 23:
                return GARBAGE_DETECT_FAILURE_DRY;
            case 24:
                return GARBAGE_DETECT_FAILURE_WET;
            case 25:
                return GARBAGE_DETECT_FAILURE_RECYCLE;
            case 26:
                return GARBAGE_DETECT_FAILURE_HA;
            case 27:
                return GARBAGE_DETECT_FAILURE_WHITE_QUILT;

            case 31:
                return GATE_CLOSE;
            case 32:
                return GATE_NOT_CLOSE;
            case 33:
                return GATE_NOT_CLOSE_TIMEOUT;
            default:
                return UNKNOWN;
        }
    }

    int value;
    String description;

    AudioScene(int v, String desc) {
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
