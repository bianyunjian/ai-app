package com.hankutech.ax.appdemo;

import com.hankutech.ax.appdemo.constant.Common;
import com.hankutech.ax.appdemo.constant.RuntimeContext;
import com.hankutech.ax.appdemo.constant.SocketConst;
import com.hankutech.ax.appdemo.socket.SocketClient;
import com.hankutech.ax.appdemo.util.LogExt;
import com.hankutech.ax.message.code.AIAuthFlag;
import com.hankutech.ax.message.code.AIGarbageResultType;
import com.hankutech.ax.message.protocol.MessageSource;
import com.hankutech.ax.message.protocol.app.AppMessageType;
import com.hankutech.ax.message.protocol.app.AppMessageValue;
import com.hankutech.ax.message.protocol.app.AppRequest;

public class MessageExchange {
    private static final String TAG = "MessageExchange";

    private static void send(AppRequest request) {
        SocketClient client = getSocketClient();
        if (client != null) {
            client.sendData(request);
        } else {
            LogExt.i(TAG, "socket连接还未准备好");
        }
    }

    private static SocketClient getSocketClient() {

        String key = SocketConst.SERVER_LISTENING_IP + ":" + SocketConst.SERVER_LISTENING_PORT;
        return SocketClient.getClient(key);

    }

    public static void sendHandShake() {

        AppRequest request = new AppRequest();
        request.setMessageSource(MessageSource.APP);
        request.setAppNumber(Common.APP_NUMBER);
        request.setMessageType(AppMessageType.HAND_SHAKE_REQ);
        request.setPayload(AppMessageValue.HAND_SHAKE_REQ);
        LogExt.i(TAG, "发送握手请求：" + request.toString());

        send(request);
    }

    public static void sendAuth(AIAuthFlag aiAuthFlag) {

        AppRequest request = new AppRequest();
        request.setMessageSource(MessageSource.APP);
        request.setAppNumber(Common.APP_NUMBER);
        request.setMessageType(AppMessageType.AUTH_REQ);
        request.setPayload(aiAuthFlag.getValue());
        LogExt.i(TAG, "发送身份验证请求：" + request.toString());

        send(request);
    }

    public static void sendGarbageDetect(AIGarbageResultType garbageType) {

        AppRequest request = new AppRequest();
        request.setMessageSource(MessageSource.APP);
        request.setAppNumber(Common.APP_NUMBER);
        request.setMessageType(AppMessageType.GARBAGE_DETECT_REQ);
        request.setPayload(garbageType.getValue());
        LogExt.i(TAG, "发送垃圾检测请求：" + request.toString());

        send(request);
    }

    public static void sendRequireOpenGate() {

        AppRequest request = new AppRequest();
        request.setMessageSource(MessageSource.APP);
        request.setAppNumber(Common.APP_NUMBER);
        request.setMessageType(AppMessageType.APP_REQUIRE_OPEN_GATE_REQ);
        request.setPayload(AppMessageValue.APP_OPEN_GATE_REQ);
        LogExt.i(TAG, "发送开门请求：" + request.toString());

        send(request);
    }

    public static void sendGateClosedEventResp() {

        AppRequest request = new AppRequest();
        request.setMessageSource(MessageSource.APP);
        request.setAppNumber(Common.APP_NUMBER);
        request.setMessageType(AppMessageType.GATE_CLOSED_EVENT_RESP);
        request.setPayload(AppMessageValue.GATE_CLOSED_EVENT_RESP_SUCCESS);
        LogExt.i(TAG, "发送门已关闭响应：" + request.toString());

        send(request);
    }


    public static void sendSysStatusChangeEventResp() {

        AppRequest request = new AppRequest();
        request.setMessageSource(MessageSource.APP);
        request.setAppNumber(Common.APP_NUMBER);
        request.setMessageType(AppMessageType.SYS_STATUS_RESP);
        request.setPayload(AppMessageValue.SYS_STATUS_RESP_SUCCESS);
        LogExt.i(TAG, "发送系统状态事件响应：" + request.toString());

        send(request);
    }
}
