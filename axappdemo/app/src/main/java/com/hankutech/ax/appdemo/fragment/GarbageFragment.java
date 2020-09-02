package com.hankutech.ax.appdemo.fragment;

import android.app.Fragment;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.hankutech.ax.appdemo.MessageExchange;
import com.hankutech.ax.appdemo.R;
import com.hankutech.ax.appdemo.constant.SocketConst;
import com.hankutech.ax.appdemo.code.AudioScene;
import com.hankutech.ax.appdemo.code.MessageCode;
import com.hankutech.ax.appdemo.constant.Common;
import com.hankutech.ax.appdemo.constant.RuntimeContext;
import com.hankutech.ax.appdemo.event.AXDataEvent;
import com.hankutech.ax.appdemo.event.MessageEvent;
import com.hankutech.ax.appdemo.socket.ByteConverter;
import com.hankutech.ax.appdemo.socket.SocketClient;
import com.hankutech.ax.appdemo.util.LogExt;
import com.hankutech.ax.appdemo.util.TickTimer;
import com.hankutech.ax.message.code.AIGarbageTypeDetectResult;
import com.hankutech.ax.message.protocol.app.AppMessage;
import com.hankutech.ax.message.protocol.app.AppMessageType;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class GarbageFragment extends Fragment implements IFragmentOperation {

    private static final String TAG = "GarbageFragment";
    private static final String Desc_Default = AudioScene.GARBAGE_DETECT.getDescription();
    private static final String Desc_Success = "现在是" + RuntimeContext.CurrentGarbageType.getDescription() + "投放时间.\n请按照垃圾分类要求进行投放.\n感谢您的配合!";
    private static final String Desc_Failure = AudioScene.GARBAGE_DETECT_FAILURE.getDescription();
    private View view;
    private TickTimer tickTimer = new TickTimer();
    private TickTimer sendGarbageDetectMessageTickTimer = new TickTimer();
    private TextView textViewGarbageDetectProcessDescription;


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
                    sendGarbageDetectMessage2CentralServer();
                }
            });
        } else {
            this.view.findViewById(R.id.btnSendMessage2PLC).setVisibility(View.GONE);
        }


        sendGarbageDetectMessageTickTimer.start(Common.GarbageWaitDetectMillis, Common.MessageLoopInterval, (t) -> {
            if (this.receiveGarbageResult == false) {
                sendGarbageDetectMessage2CentralServer();
            } else {
                sendGarbageDetectMessageTickTimer.cancel();
            }
        }, (t) -> {
            LogExt.d(TAG, "在限定时间内未等到垃圾检测的响应数据");
        });
    }

    private void sendGarbageDetectMessage2CentralServer() {
        MessageExchange.sendGarbageDetect(RuntimeContext.CurrentGarbageType);
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
    public void OnEventMessage(AppMessage dataEvent) {

        LogExt.d(TAG, "OnEventMessage: " + dataEvent.toString());

        if (dataEvent.getMessageType() == AppMessageType.GARBAGE_DETECT_RESP) {

            AIGarbageTypeDetectResult garbageDetectResult = AIGarbageTypeDetectResult.valueOf(dataEvent.getPayload());


            if (this.receiveGarbageResult == false) {
                if (garbageDetectResult.getValue() == AIGarbageTypeDetectResult.SUCCESS.getValue()) {
                    LogExt.d(TAG, "垃圾分类检测结果成功");

                    playAudio(AudioScene.GARBAGE_DETECT_SUCCESS);
                    this.textViewGarbageDetectProcessDescription.setText(Desc_Success);
                    this.textViewGarbageDetectProcessDescription.setTextColor(Color.BLACK);

                    this.tickTimer.cancel();
                    this.receiveGarbageResult = true;
                    this.sendGarbageDetectMessageTickTimer.cancel();

                    //垃圾分类检测完成， 进入开门流程
                    EventBus.getDefault().post(new MessageEvent(MessageCode.GARBAGE_PASS, null));
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
                    this.sendGarbageDetectMessageTickTimer.cancel();
                }
            }
        }
    }
}

