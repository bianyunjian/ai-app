package com.hankutech.ax.appdemo.socket;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.FixedLengthFrameDecoder;
import io.netty.handler.codec.bytes.ByteArrayEncoder;

/**
 * Byte Socket客户端初始化配置
 *
 * @author ZhangXi
 */
public class ByteSocketClientInitializer extends ChannelInitializer<SocketChannel> {

    private int fixedLengthFrame = 10;

    public ByteSocketClientInitializer(int fixedLengthFrame) {
        this.fixedLengthFrame = fixedLengthFrame;
    }

    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        ChannelPipeline pipeline = socketChannel.pipeline();
        pipeline.addLast(new FixedLengthFrameDecoder(fixedLengthFrame));

        pipeline.addLast(new ByteMessageHandler());
    }
}
