package com.hankutech.ax.appdemo.code;

public enum AppStatus {

    NORMAL(1, "开始投递"),
    BUSY(2, "繁忙中"),
    MAINTAIN(3, "维护中"),
    ERROR(4, "系统故障");


    public static AppStatus valueOf(int value) {
        switch (value) {
            case 1:
                return NORMAL;
            case 2:
                return BUSY;
            case 3:
                return MAINTAIN;
            default:
                return ERROR;
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
