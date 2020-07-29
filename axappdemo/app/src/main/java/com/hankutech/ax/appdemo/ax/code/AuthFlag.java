package com.hankutech.ax.appdemo.ax.code;

/**
 * X4标示授权信息
 */
public enum AuthFlag {
    FAILURE(0, "授权不成功"),
    RFID(1, "RFID授权"),
    AI_FACE(2, "人脸识别授权"),
    QRCODE(3, "二维码授权");

    public static AuthFlag valueOf(int value) {
        switch (value) {

            case 1:
                return RFID;
            case 2:
                return AI_FACE;
            case 3:
                return QRCODE;
            default:
                return FAILURE;
        }
    }

    int value;
    String description;

    AuthFlag(int v, String desc) {
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
