package com.hankutech.ax.appdemo.ax.code;

/**
 * 垃圾分类检测任务应答
 */
public enum AIGarbageResultType implements AIResult {
    EMPTY(0, "UNKNOWN", "UNKNOWN"),
    DRY(1, "干垃圾（灰色垃圾袋）", "干垃圾"),
    WET(2, "湿垃圾（黑色垃圾袋）", "湿垃圾"),
    RECYCLABLE(3, "可回收垃圾（绿色垃圾袋）", "可回收垃圾"),
    HAZARDOUS(4, "有害垃圾（红色垃圾袋）", "有害垃圾"),
    BLUE(90, "其他垃圾（蓝色色垃圾袋）", "其他垃圾"),
    EXCEPTION(99, "异常", "异常");

    public static AIGarbageResultType valueOf(int value) {
        switch (value) {

            case 1:
                return DRY;
            case 2:
                return WET;
            case 3:
                return RECYCLABLE;
            case 4:
                return HAZARDOUS;
            case 90:
                return BLUE;
            case 99:
                return EXCEPTION;
            default:
                return EMPTY;
        }
    }

    int value;
    String description;
    String name;

    AIGarbageResultType(int v, String desc, String name) {
        value = v;
        description = desc;
        this.name = name;
    }

    public int getValue() {
        return value;
    }

    public String getDescription() {
        return description;
    }

    public String getName() {
        return name;
    }
}
