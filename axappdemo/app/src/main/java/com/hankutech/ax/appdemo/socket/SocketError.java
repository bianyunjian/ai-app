package com.hankutech.ax.appdemo.socket;

/**
 * Socket出错类型
 *
 * @author ZhangXi
 */
public enum SocketError {
    /**
     *
     */
    START_INTERRUPTED(1, "启动时被打断"),
    PORT_IN_USED(2, "端口被占用");


    private int code;
    private String desc;

    SocketError(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public int getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }
}
