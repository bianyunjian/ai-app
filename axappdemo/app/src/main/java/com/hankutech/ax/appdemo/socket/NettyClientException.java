package com.hankutech.ax.appdemo.socket;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Netty客户端异常
 *
 * @author ZhangXi
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class NettyClientException extends Exception {

    private SocketError socketError;

    public NettyClientException(String message, SocketError socketError) {
        super(message);
        this.socketError = socketError;
    }

    public NettyClientException(String message, Throwable cause, SocketError socketError) {
        super(message, cause);
        this.socketError = socketError;
    }

    public NettyClientException(Throwable cause, SocketError socketError) {
        super(cause);
        this.socketError = socketError;
    }

}
