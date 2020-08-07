package com.hankutech.ax.appdemo.fragment;

import android.app.Fragment;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.hankutech.ax.appdemo.R;
import com.hankutech.ax.appdemo.ax.SocketConst;
import com.hankutech.ax.appdemo.ax.code.AIGarbageTypeDetectResult;
import com.hankutech.ax.appdemo.ax.code.GateState;
import com.hankutech.ax.appdemo.ax.code.SysRunFlag;
import com.hankutech.ax.appdemo.ax.protocol.AXDataConverter;
import com.hankutech.ax.appdemo.ax.protocol.AXRequest;
import com.hankutech.ax.appdemo.ax.protocol.AXResponse;
import com.hankutech.ax.appdemo.code.AudioScene;
import com.hankutech.ax.appdemo.code.MessageCode;
import com.hankutech.ax.appdemo.constant.Common;
import com.hankutech.ax.appdemo.constant.RuntimeContext;
import com.hankutech.ax.appdemo.event.AXDataEvent;
import com.hankutech.ax.appdemo.event.MessageEvent;
import com.hankutech.ax.appdemo.socket.ByteConverter;
import com.hankutech.ax.appdemo.socket.SocketServer;
import com.hankutech.ax.appdemo.util.LogExt;
import com.hankutech.ax.appdemo.util.TickTimer;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class GarbageFragment extends Fragment implements IFragmentOperation {

    private static final String TAG = "GarbageFragment";
    private static final String Desc_Default = "正在检测垃圾分类...";
    private static final String Desc_Success = "现在是" + RuntimeContext.CurrentGarbageType.getDescription() + "投放时间.\n请按照垃圾分类要求进行投放.\n感谢您的配合!";
    private static final String Desc_Failure = "您本次投放的垃圾不符合垃圾分类要求.\n请按照要求分类好之后再来投放.\n感谢您的配合!";
    private View view;
    private TickTimer tickTimer = new TickTimer();
    private TickTimer sendGarbageDetectRequestTickTimer = new TickTimer();
    private TextView textViewGarbageDetectProcessDescription;
    /**
     * 是否在等待投递
     */
    private boolean waitGarbageDeliver;

    /**
     * 是否已经收到了垃圾检测的结果
     */
    private boolean receiveGarbageResult;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        this.view = inflater.inflate(R.layout.fragment_garbage, container, false);

        return this.view;

    }

    @Override
    public void onResume() {
        super.onResume();

        playAudio(AudioScene.GARBAGE_DETECT);

        tickTimer.start(Common.GarbageWaitDetectMillis, Common.TickInterval, (t) -> {
            TextView tv = this.view.findViewById(R.id.garbageDetectTiktokTimeDesc);
            tv.setText(Common.getTickDesc(t));
        }, (t) -> {
            backToHome();
        });


        textViewGarbageDetectProcessDescription = this.view.findViewById(R.id.garbageDetectProcessDescription);
        this.textViewGarbageDetectProcessDescription.setText(Desc_Default);

        LogExt.d(TAG, "正在检测垃圾分类");

        if (Common.DebugMode) {
            this.view.findViewById(R.id.btnSendMessage2PLC).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    sendGarbageDetectMessage2PLC();
                }
            });
        } else {
            this.view.findViewById(R.id.btnSendMessage2PLC).setVisibility(View.GONE);
        }


        sendGarbageDetectRequestTickTimer.start(Common.SendGarbageDetectRequestMillis, Common.TickInterval, (t) -> {
            if (this.receiveGarbageResult == false) {
                sendGarbageDetectMessage2PLC();
            } else {
                sendGarbageDetectRequestTickTimer.cancel();
            }
        }, (t) -> {
        });
    }

    private void sendGarbageDetectMessage2PLC() {
        AXResponse response = new AXResponse();
        response.setSysRunFlag(SysRunFlag.RUN);
        response.setAuthFlag(RuntimeContext.CurrentAuthFlag);
        response.setStartGarbageDetectRequestFlag(1);
        LogExt.i(TAG, "发送请求PLC开始检测垃圾分类：" + response.toString());

        int[] respData = AXDataConverter.convertResponse(response);
        byte[] respByteData = ByteConverter.toByte(respData);
        ByteBuf responseByteBuf = Unpooled.buffer(respByteData.length);
        responseByteBuf.writeBytes(respByteData);
        SocketServer.getServer(SocketConst.LISTENING_PORT).sendData(responseByteBuf);
    }

    private void playAudio(AudioScene audioScene) {
        EventBus.getDefault().post(new MessageEvent(MessageCode.AUDIO_PLAY, audioScene));
    }


    @Override
    public void init() {
        EventBus.getDefault().register(this);
    }

    public void release() {
        this.tickTimer.cancel();
        EventBus.getDefault().post(new MessageEvent(MessageCode.AUDIO_STOP, null));
        EventBus.getDefault().unregister(this);
    }


    private void backToHome() {
        EventBus.getDefault().post(new MessageEvent(MessageCode.HOME, null));
    }

    /**
     * ThreadMode设置为MAIN，事件的处理会在UI线程中执行，用TextView来展示收到的事件消息
     *
     * @param dataEvent
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void OnEventMessage(AXDataEvent dataEvent) {

        LogExt.d(TAG, "OnEventMessage: " + dataEvent.toString());

        AXRequest axData = dataEvent.getData();
        if (axData.isSysException()) {
            EventBus.getDefault().post(new MessageEvent(MessageCode.HOME, axData));
            return;
        }

        AIGarbageTypeDetectResult garbageDetectResult = axData.getGarbageTypeDetectResult();

        boolean gateOpen = axData.getGateState().getValue() == GateState.NOT_CLOSE.getValue();
        if (this.waitGarbageDeliver == false && this.receiveGarbageResult == false) {
            if (garbageDetectResult.getValue() == AIGarbageTypeDetectResult.SUCCESS.getValue()) {
                LogExt.d(TAG, "垃圾分类检测结果成功");

                playAudio(AudioScene.GARBAGE_DETECT_SUCCESS);
                this.textViewGarbageDetectProcessDescription.setText(Desc_Success);
                this.textViewGarbageDetectProcessDescription.setTextColor(Color.BLACK);

                this.tickTimer.cancel();
                tickTimer.start(Common.GarbageWaitDeliverMillis, Common.TickInterval, (t) -> {
                    TextView tv = this.view.findViewById(R.id.garbageDetectTiktokTimeDesc);
                    tv.setText(Common.getTickDesc(t));
                }, (t) -> {
                    //倒计时结束后,认为投递动作完成
                    EventBus.getDefault().post(new MessageEvent(MessageCode.GARBAGE_PASS, null));
                });
                this.waitGarbageDeliver = true;
                this.receiveGarbageResult = true;
                return;
            } else if (garbageDetectResult.getValue() == AIGarbageTypeDetectResult.FAILURE.getValue()) {
                LogExt.d(TAG, "垃圾分类检测结果失败");
                playAudio(AudioScene.GARBAGE_DETECT_FAILURE);
                this.textViewGarbageDetectProcessDescription.setText(Desc_Failure);
                this.textViewGarbageDetectProcessDescription.setTextColor(Color.RED);
                this.tickTimer.cancel();
                tickTimer.start(Common.GarbageDetectFailureMillis, Common.TickInterval, (t) -> {
                    TextView tv = this.view.findViewById(R.id.garbageDetectTiktokTimeDesc);
                    tv.setText(Common.getTickDesc(t));
                }, (t) -> {
                    //倒计时结束后,回到主界面
                    EventBus.getDefault().post(new MessageEvent(MessageCode.HOME, null));
                });
                this.receiveGarbageResult = true;
            }
        }
        if (this.waitGarbageDeliver && gateOpen) {
            LogExt.d(TAG, "门已经打开,可以投递垃圾");

            this.tickTimer.cancel();
            //投递动作完成
            EventBus.getDefault().post(new MessageEvent(MessageCode.GARBAGE_PASS, null));

        }
    }
}

