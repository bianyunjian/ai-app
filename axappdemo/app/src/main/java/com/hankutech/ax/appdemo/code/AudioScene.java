package com.hankutech.ax.appdemo.code;

public enum AudioScene {
    UNKNOWN(0, "UNKNOWN"),
    AUTH_CHOOSE_TYPE(11, "欢迎使用艾信智慧投递系统,请选择身份验证方式"),
    AUTH_RFID(12, "请在图示位置刷卡"),
    AUTH_AI_FACE(13, "请将面部正对摄像头,即将开始识别您的身份"),
    AUTH_QRCODE(14, "请使用手机扫描屏幕下方的二维码"),
    AUTH_PASS(15, "欢迎您"),

    GARBAGE_DETECT(21, "正在检测垃圾分类..."),
    GARBAGE_DETECT_SUCCESS(22, "请按照垃圾分类要求进行投放,感谢您的配合"),
    GARBAGE_DETECT_FAILURE(23, "本次投放的垃圾不符合垃圾分类要求.\r\n请按照垃圾分类要求进行投放,感谢您的配合"),

    GATE_CLOSE(31, "本次投递结束,小艾在这里等待您的再次到来"),
    GATE_NOT_CLOSE(32, "亲,您可以开始垃圾投递了,投递后请关好门,感谢您的配合"),
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

            case 21:
                return GARBAGE_DETECT;
            case 22:
                return GARBAGE_DETECT_SUCCESS;
            case 23:
                return GARBAGE_DETECT_FAILURE;

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
