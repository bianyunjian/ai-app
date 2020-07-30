package com.hankutech.ax.appdemo.socket;

import com.hankutech.ax.appdemo.util.LogExt;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;


import java.util.concurrent.ConcurrentHashMap;

/**
 * Socket 通用服务端
 *
 * @author ZhangXi
 */

public class SocketServer {

    private static final String TAG = "SocketServer";
    /**
     * socket server字典
     * key: 服务端口
     * value: {@link SocketServer}
     */
    private static ConcurrentHashMap<Integer, SocketServer> SOCKET_SERVER_MAP = new ConcurrentHashMap<>();

    private final int port;
    private int maxClientSize = 1024;
    private int timeoutMillis = 10000;

    private ChannelFuture channelFuture;
    private final ChannelInitializer initializer;


    public SocketServer(int port, ChannelInitializer initializer) {
        this.port = port;
        this.initializer = initializer;
    }

    /**
     * 启动socket服务器
     *
     * @throws NettyServerException Netty服务端异常
     */
    public void start() throws NettyServerException {
        NioEventLoopGroup bossGroup = new NioEventLoopGroup();
        NioEventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    // 重用端口
                    .option(ChannelOption.SO_REUSEADDR, true)
                    // 服务端接收连接的队列长度
                    .option(ChannelOption.SO_BACKLOG, this.maxClientSize)
                    // 连接超时时间10秒
                    .childOption(ChannelOption.CONNECT_TIMEOUT_MILLIS, this.timeoutMillis)
                    // 保持连接
                    .childOption(ChannelOption.SO_KEEPALIVE, true);
            bootstrap.childHandler(this.initializer);

            // 绑定端口并且开始接收连接请求

            this.channelFuture = bootstrap.bind(port).sync();
            if (this.channelFuture.isSuccess()) {
//                log.info("socket服务端：{} 启动成功！", this.port);
                LogExt.d(TAG, "socket服务端：" + this.port + " 启动成功！");
                // 保存到Map
                SOCKET_SERVER_MAP.put(this.port, this);
            }
            Channel channel = channelFuture.channel();
            /**CloseFuture异步方式关闭*/
            channel.closeFuture().sync();

            SOCKET_SERVER_MAP.remove(port);
//            log.info("socket服务端：{} 已关闭！", port);
            LogExt.d(TAG, "socket服务端：" + this.port + " 已关闭！");

        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    /**
     * 关闭socket服务器
     */
    public void shutdown() {
        if (null != channelFuture) {
            channelFuture.channel().close();
        }
    }

    public void sendData(Object msg, ChannelId... channelIds) {
        ChannelGroups.broadcast(msg, channelIds);
    }

    /**
     * 获取{@link SocketServer}指定实例
     *
     * @param port 服务端口
     * @return {@link SocketServer}
     */
    public static SocketServer getServer(int port) {

        return SOCKET_SERVER_MAP.get(port);
    }


    public SocketServer maxClientSize(int size) {
        this.maxClientSize = size;
        return this;
    }

    public SocketServer timeoutMillis(int millis) {
        this.timeoutMillis = millis;
        return this;
    }

}
