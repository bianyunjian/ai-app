package com.hankutech.ax.appdemo.socket;

import com.hankutech.ax.message.protocol.app.AppDataConverter;
import com.hankutech.ax.message.protocol.app.AppMessage;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.Getter;
import lombok.Setter;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * SOCKET 通用客户端
 *
 * @author ZhangXi
 */

public class SocketClient {


    private static ConcurrentHashMap<String, SocketClient> SOCKET_CLIENT_MAP = new ConcurrentHashMap<>();

    @Getter
    @Setter
    private String host;
    @Getter
    @Setter
    private int port;

    private int timeoutMillis = 10000;

    @Getter
    private ChannelFuture future;
    private final ChannelInitializer initializer;
    private long reconnectSleepSeconds = 5;
    private boolean retryConnectFlag = false;
    private boolean disposed = false;


    public SocketClient(String host, int port, ChannelInitializer initializer) {
        this.host = host;
        this.port = port;
        this.initializer = initializer;

    }

    /**
     * @throws NettyClientException
     */
    public void startConnect() {
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
                System.out.println("服务端：" + getKey() + " 连接成功！");
                this.retryConnectFlag = false;
                this.addClient(getKey(), this);
            }
        } catch (Exception e) {
            this.retryConnectFlag = true;
            if (disposed == false) {
                retryConnect();
            } else {
                return;
            }
        }

        // 监听连接关闭动作
        this.future.channel().closeFuture().addListener((ChannelFutureListener) channelFuture -> {
            this.removeClient(getKey());
            clientGroup.shutdownGracefully();
            System.out.println("socket连接：" + getKey() + " 已关闭！");
            this.retryConnectFlag = true;
            retryConnect();
        });
    }

    private void retryConnect() {
        if (this.retryConnectFlag == true) {
            try {
                TimeUnit.SECONDS.sleep(reconnectSleepSeconds);
                System.out.println("socket等待" + reconnectSleepSeconds + "s后断线重连：" + getKey());
                startConnect(); // 断线重连
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private String getKey() {
        return this.host + ":" + this.port;
    }

    /**
     * 关闭socket连接
     */
    public void close() {
        if (null != future) {
            this.future.channel().close();
        }
        this.retryConnectFlag = false;
        this.disposed = true;
    }

    /**
     * 发送数据
     *
     * @param request
     */
    public void sendData(AppMessage request) {

        int[] respData = AppDataConverter.convert(request);
        byte[] respByteData = ByteConverter.toByte(respData);
        ByteBuf responseByteBuf = Unpooled.buffer(respByteData.length);
        responseByteBuf.writeBytes(respByteData);
        this.future.channel().writeAndFlush(responseByteBuf);
    }

    public static void addClient(String key, SocketClient client) {
        SOCKET_CLIENT_MAP.put(key, client);
    }

    public static void removeClient(String key) {
        SOCKET_CLIENT_MAP.remove(key);
    }

    public static SocketClient getClient(String key) {
        return SOCKET_CLIENT_MAP.get(key);
    }


}
