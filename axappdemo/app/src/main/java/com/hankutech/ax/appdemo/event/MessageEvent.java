package com.hankutech.ax.appdemo.event;

import com.hankutech.ax.appdemo.code.MessageCode;

import lombok.Data;
import lombok.ToString;

/**
 * 流程控制的消息事件,主要用于UI行为控制, 传递给订阅者处理
 */
@Data
@ToString
public class MessageEvent {

    private Object object;
    private MessageCode msgCode;

    public MessageEvent(MessageCode msgCode, Object object) {
        this.msgCode = msgCode;
        this.object = object;
    }

}
