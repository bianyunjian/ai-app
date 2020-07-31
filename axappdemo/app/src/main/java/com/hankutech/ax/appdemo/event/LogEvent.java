package com.hankutech.ax.appdemo.event;

import com.hankutech.ax.appdemo.ax.protocol.AXRequest;

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
