package com.hankutech.ax.appdemo.event;

import lombok.Data;
import lombok.ToString;

/**
 * 日志事件
 */
@Data
@ToString
public class LogEvent {

    private String data;

    public LogEvent(String data) {
        this.data = data;
    }

}
