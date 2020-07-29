package com.hankutech.ax.appdemo.ax.code;

/**
 * 字节X1标示艾信控制器系统状态
 */
public enum SysRunFlag {
    EMPTY(0, "UNKNOWN"),
    STOP(5, "停止"),
    RUN(8, "运行");

    public static SysRunFlag valueOf(int value) {
        switch (value) {

            case 5:
                return STOP;
            case 8:
                return RUN;

            default:
                return EMPTY;
        }
    }

    int value;
    String description;

    SysRunFlag(int v, String desc) {
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
