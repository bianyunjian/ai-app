package com.hankutech.ax.appdemo.socket;

import com.hankutech.ax.appdemo.event.AXDataEvent;
import com.hankutech.ax.appdemo.util.LogExt;
import com.hankutech.ax.message.protocol.MessageSource;
import com.hankutech.ax.message.protocol.app.AppDataConverter;
import com.hankutech.ax.message.protocol.app.AppMessage;

import org.greenrobot.eventbus.EventBus;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;


/**
 * 字节数据处理器
 *
 * @author ZhangXi
 */

public class ByteMessageHandler extends ChannelInboundHandlerAdapter {

    private static final String TAG = "ByteMessageHandler";

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        super.channelRegistered(ctx);
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        super.channelUnregistered(ctx);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {

        super.channelActive(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof ByteBuf) {
            ByteBuf buf = (ByteBuf) msg;

            byte[] bytes = new byte[buf.readableBytes()];
            buf.getBytes(0, bytes);
            LogExt.d(TAG, "接收原始字节：" + formatString(bytes));

            int[] convertedData = ByteConverter.fromByte(bytes);
            LogExt.d(TAG, "转换后的字节：" + formatString(convertedData));

            AppMessage response = AppDataConverter.parse(convertedData);
            if (response.getMessageSource() != MessageSource.CENTRAL_SERVER) {
                LogExt.e(TAG + "消息头不正确，丢弃该消息", response.toString());
                return;
            }
            if (response != null && response.isValid()) {
                LogExt.d(TAG, "解析后的请求数据：" + response.toString());

                EventBus.getDefault().post(response);
            } else {
                LogExt.d(TAG, "未能正确解析请求数据：" + response.toString());
            }
        }
    }

    private String formatString(int[] d) {

        if (d != null) {
            StringBuilder builder = new StringBuilder();
            builder.append("[");
            for (int obj : d
            ) {
                builder.append(String.valueOf(obj)).append(",");
            }

            builder.append("]");
            return builder.toString();
        } else {
            return "";
        }
    }

    private String formatString(byte[] d) {

        if (d != null) {
            StringBuilder builder = new StringBuilder();
            builder.append("[");
            for (byte obj : d
            ) {
                builder.append(String.valueOf(obj)).append(",");
            }

            builder.append("]");
            return builder.toString();
        } else {
            return "";
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        super.channelReadComplete(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
    }
}
