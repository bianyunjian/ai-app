package com.hankutech.ax.appdemo.socket;

import io.netty.channel.ChannelId;

/**
 * 广播接口
 * 用于服务端主动向客户端推送数据
 *
 * @author ZhangXi
 */
public interface Broadcast {

    /**
     * 给所有终端发送消息
     * @param msg {@link Object}
     */
    void sendAll(Object msg);

    /**
     * 给终端发送消息
     * @param msg {@link Object}
     * @param id 客户端ID
     */
    void send(Object msg, ChannelId id);

}
