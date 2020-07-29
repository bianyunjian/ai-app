package com.hankutech.ax.appdemo.socket;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Netty服务端异常
 *
 * @author ZhangXi
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class NettyServerException extends Exception {

    private SocketError socketError;

    public NettyServerException(String message, SocketError socketError) {
        super(message);
        this.socketError = socketError;
    }

    public NettyServerException(Throwable cause, SocketError socketError) {
        super(cause);
        this.socketError = socketError;
    }

    public NettyServerException(String message, Throwable cause, SocketError socketError) {
        super(message, cause);
        this.socketError = socketError;
    }
}
