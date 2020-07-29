package com.hankutech.ax.appdemo.socket;

import com.hankutech.ax.appdemo.ax.protocol.AXDataConverter;
import com.hankutech.ax.appdemo.ax.protocol.AXRequest;
import com.hankutech.ax.appdemo.ax.protocol.AXResponse;
import com.hankutech.ax.appdemo.util.StringExtUtil;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;


/**
 * 字节数据处理器
 *
 * @author ZhangXi
 */

public class ByteMessageHandler extends ChannelInboundHandlerAdapter {

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
            System.out.println("接收的原始字节数据：" + formatString(bytes));

            int[] convertedData = ByteConverter.fromByte(bytes);
            System.out.println("转换后的数据：" + formatString(convertedData));

            AXRequest request = AXDataConverter.parseRequest(convertedData);
            if (request != null && request.isValid()) {
                System.out.println("解析后的请求数据：" + request.toString());
            } else {
                System.out.println("未能正确解析请求数据：" + request.toString());
            }

//            if (response.isValid() == false) {
//                log.error("未能正确处理请求数据，request={},response={}", request, response);
//            }
//            int[] respData = AXDataConverter.convertResponse(response);
//            byte[] respByteData = ByteConverter.toByte(respData);
//            log.info("转换后的响应数据：{}", response.toString());

//            ByteBuf responseByteBuf = Unpooled.buffer(respByteData.length);
//            responseByteBuf.writeBytes(respByteData);
//            ctx.channel().writeAndFlush(responseByteBuf);
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
