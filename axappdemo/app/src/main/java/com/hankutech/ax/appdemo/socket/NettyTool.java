package com.hankutech.ax.appdemo.socket;

import io.netty.channel.ChannelHandlerContext;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * Netty工具类
 *
 * @author ZhangXi
 */
public class NettyTool {


    public static String getClientAddress(ChannelHandlerContext ctx) {
        return ctx.channel().remoteAddress().toString();
    }

    /**
     * 本地端口是否被占用
     *
     * @param port 端口号
     * @return true/false
     */
    public static boolean isLocalPortUsing(int port) {
        boolean flag = true;
        try {
            flag = isPortUsing("127.0.0.1", port);
        } catch (UnknownHostException e) {
            // 不做任何处理
        }
        return flag;
    }

    /**
     * 指定主机端口是否被占用
     *
     * @param host 主机名称,包括ip或者域名
     * @param port 端口号
     * @return true/false
     * @throws UnknownHostException 未知主机名异常
     */
    public static boolean isPortUsing(String host, int port) throws UnknownHostException {
        InetAddress address = InetAddress.getByName(host);
        Socket socket = null;
        try {
            socket = new Socket(address, port);
            return true;
        } catch (Exception e) {
            return false;
        } finally {
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }


}
