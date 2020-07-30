package com.hankutech.ax.appdemo.event;

import com.hankutech.ax.appdemo.ax.protocol.AXRequest;
import com.hankutech.ax.appdemo.code.MessageCode;

import lombok.Data;
import lombok.ToString;

/**
 * 当接收到艾信的数据时触发该事件, 传递给订阅者处理
 */
@Data
@ToString
public class AXDataEvent {

    private AXRequest data;

    public AXDataEvent(AXRequest data) {
        this.data = data;
    }

}
