package com.hankutech.ax.appdemo.code;

public enum AuthType {
    UNKNOWN(0, "UNKNOWN"),
    RFID(1, "RFID"),
    AI_FACE(2, "人脸识别"),
    QRCODE(3, "二维码");

    public static AuthType valueOf(int value) {
        switch (value) {
            case 1:
                return RFID;
            case 2:
                return AI_FACE;
            case 3:
                return QRCODE;
            default:
                return UNKNOWN;
        }
    }

    int value;
    String description;

    AuthType(int v, String desc) {
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
