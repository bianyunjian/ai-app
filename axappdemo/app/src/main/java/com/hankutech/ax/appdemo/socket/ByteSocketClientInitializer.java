package com.hankutech.ax.appdemo.socket;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.bytes.ByteArrayEncoder;

/**
 * Byte Socket客户端初始化配置
 *
 * @author ZhangXi
 */
public class ByteSocketClientInitializer extends ChannelInitializer<SocketChannel> {
    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        ChannelPipeline pipeline = socketChannel.pipeline();
//        pipeline.addLast(new LengthFieldPrepender(1));
        pipeline.addLast(new ByteArrayEncoder());

        pipeline.addLast(new ByteMessageHandler());
    }
}
