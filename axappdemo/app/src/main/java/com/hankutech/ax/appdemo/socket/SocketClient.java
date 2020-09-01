package com.hankutech.ax.appdemo.socket;

import com.hankutech.ax.message.protocol.app.AppDataConverter;
import com.hankutech.ax.message.protocol.app.AppRequest;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.Getter;

import java.util.concurrent.ConcurrentHashMap;

/**
 * SOCKET 通用客户端
 *
 * @author ZhangXi
 */

public class SocketClient {


    private static ConcurrentHashMap<String, SocketClient> SOCKET_CLIENT_MAP = new ConcurrentHashMap<>();


    private final String host;
    private final int port;

    private int timeoutMillis = 10000;

    @Getter
    private ChannelFuture future;
    private final ChannelInitializer initializer;


    public SocketClient(String host, int port, ChannelInitializer initializer) {
        this.host = host;
        this.port = port;
        this.initializer = initializer;
    }

    /**
     * @throws NettyClientException
     */
    public void startConnect() throws NettyClientException {
        EventLoopGroup clientGroup = new NioEventLoopGroup();

        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(clientGroup)
                // 设置非延迟连接
                .option(ChannelOption.TCP_NODELAY, true)
                // 保持连接，默认每2小时检测一次
                .option(ChannelOption.SO_KEEPALIVE, true)
                /* ===========================================
                 * 注意：这里必须设置连接超时时间，否则握手会失败!!!
                 * =========================================*/
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, this.timeoutMillis)
                .channel(NioSocketChannel.class);
        bootstrap.handler(this.initializer);

        try {
            this.future = bootstrap.connect(this.host, this.port).sync();
            if (this.future.isSuccess()) {
                System.out.println("服务端：" + this.host + ":" + this.port + " 连接成功！");
                this.addClient(this.host + ":" + this.port, this);
            }
        } catch (InterruptedException e) {
            throw new NettyClientException(e, SocketError.START_INTERRUPTED);
        }

        // 监听连接关闭动作
        this.future.channel().closeFuture().addListener((ChannelFutureListener) channelFuture -> {
            clientGroup.shutdownGracefully();
//            log.info("socket连接：{}:{} 已关闭", host, port);
            System.out.println("socket连接：" + this.host + ":" + this.port + " 已关闭！");
        });
    }

    /**
     * 关闭socket连接
     */
    public void close() {
        if (null != future) {
            this.future.channel().close();
        }
    }

    /**
     * 发送数据
     *
     * @param request
     */
    public void sendData(AppRequest request) {

        int[] respData = AppDataConverter.convertRequest(request);
        byte[] respByteData = ByteConverter.toByte(respData);
        ByteBuf responseByteBuf = Unpooled.buffer(respByteData.length);
        responseByteBuf.writeBytes(respByteData);
        this.future.channel().writeAndFlush(responseByteBuf);
    }

    public static void addClient(String key, SocketClient client) {
        SOCKET_CLIENT_MAP.put(key, client);
    }


    public static SocketClient getClient(String key) {
        return SOCKET_CLIENT_MAP.get(key);
    }


    public SocketClient withTimeoutMillis(int millis) {
        this.timeoutMillis = millis;
        return this;
    }

}
