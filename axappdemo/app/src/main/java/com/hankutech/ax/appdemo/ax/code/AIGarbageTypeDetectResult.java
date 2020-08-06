package com.hankutech.ax.appdemo.ax.code;

/**
 * 垃圾分类检测结果成功与否
 */
public enum AIGarbageTypeDetectResult implements AIResult {
    EMPTY(0, "UNKNOWN"),
    SUCCESS(1, "成功"),
    FAILURE(2, "失败"),
    ;

    public static AIGarbageTypeDetectResult valueOf(int value) {
        switch (value) {

            case 1:
                return SUCCESS;
            case 2:
                return FAILURE;

            default:
                return EMPTY;
        }
    }

    int value;
    String description;

    AIGarbageTypeDetectResult(int v, String desc) {
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
