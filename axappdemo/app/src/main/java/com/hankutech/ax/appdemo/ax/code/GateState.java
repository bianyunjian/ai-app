package com.hankutech.ax.appdemo.ax.code;

/**
 * 安全防护检测任务应答
 */
public enum GateState implements AIResult {

    NOT_CLOSE(0, "没关门"),
    CLOSED(1, "关门到位"),
    NOT_CLOSE_TIMEOUT(5, "关门超时报警");

    public static GateState valueOf(int value) {
        switch (value) {

            case 0:
                return NOT_CLOSE;
            case 1:
                return CLOSED;
            case 5:
                return NOT_CLOSE_TIMEOUT;

            default:
                return NOT_CLOSE;
        }
    }

    int value;
    String description;

    GateState(int v, String desc) {
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
