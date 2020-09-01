package com.hankutech.ax.appdemo.event;

import com.hankutech.ax.message.protocol.app.AppRequest;

import lombok.Data;
import lombok.ToString;

/**
 * 当接收到艾信的数据时触发该事件, 传递给订阅者处理
 */
@Data
@ToString
public class AXDataEvent {

    private AppRequest data;

    public AXDataEvent(AppRequest data) {
        this.data = data;
    }

}
