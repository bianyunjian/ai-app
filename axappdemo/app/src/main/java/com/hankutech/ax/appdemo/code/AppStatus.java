package com.hankutech.ax.appdemo.code;

public enum AppStatus {
    UNKNOWN(0, "UNKNOWN"),
    NORMAL(1, "运行中"),
    WAIT(2, "待机"),
    ERROR(3, "故障");

    public static AppStatus valueOf(int value) {
        switch (value) {
            case 1:
                return NORMAL;
            case 2:
                return WAIT;
            case 3:
                return ERROR;
            default:
                return UNKNOWN;
        }
    }

    int value;
    String description;

    AppStatus(int v, String desc) {
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
