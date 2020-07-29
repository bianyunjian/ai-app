package com.hankutech.ax.appdemo.ax.code;

/**
 * 垃圾分类检测任务应答
 */
public enum AIGarbageResultType implements AIResult{
    EMPTY(0,"UNKNOWN"),
    DRY(1, "干垃圾（灰色垃圾袋）"),
    WET(2, "湿垃圾（黑色垃圾袋）"),
    RECYCLABLE(3, "可回收垃圾（绿色垃圾袋）"),
    HAZARDOUS(4, "有害垃圾（红色垃圾袋）"),
    EXCEPTION(99, "异常") ;
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
            case 99:
                return EXCEPTION;
            default:
                return EMPTY;
        }
    }
    int value;
    String description;

    AIGarbageResultType(int v, String desc) {
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
