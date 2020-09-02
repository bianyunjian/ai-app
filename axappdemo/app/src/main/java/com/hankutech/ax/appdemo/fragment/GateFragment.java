package com.hankutech.ax.appdemo.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.hankutech.ax.appdemo.MessageExchange;
import com.hankutech.ax.appdemo.R;
import com.hankutech.ax.appdemo.code.AudioScene;
import com.hankutech.ax.appdemo.code.MessageCode;
import com.hankutech.ax.appdemo.constant.Common;
import com.hankutech.ax.appdemo.event.AXDataEvent;
import com.hankutech.ax.appdemo.event.MessageEvent;
import com.hankutech.ax.appdemo.util.LogExt;
import com.hankutech.ax.appdemo.util.TickTimer;
import com.hankutech.ax.message.code.GateState;
import com.hankutech.ax.message.protocol.app.AppMessage;
import com.hankutech.ax.message.protocol.app.AppMessageType;
import com.hankutech.ax.message.protocol.app.AppMessageValue;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class GateFragment extends Fragment implements IFragmentOperation {

    private static final String TAG = "GateFragment";
    private static final String Desc_Gate_Pending_Open = "亲,正在打开投递口的门!";
    private static final String Desc_Gate_Not_Close = "亲,您可以开始垃圾投递了,投递后请关好门!";
    private static final String Desc_Gate_Not_Close_Timeout = "系统故障,请联系管理员处理";
    private static final String Desc_Gate_Closed = "本次投递完成.\n亲,感谢您对保护环境的付出.\n小艾,在这里等待您的再次到来.";
    private View view;
    private TickTimer tickTimer = new TickTimer();
    private TextView textViewGateStateProcessDescription;
    private boolean gateClosed;

    private TickTimer gateMessageTimer = new TickTimer();


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        this.view = inflater.inflate(R.layout.fragment_gate, container, false);

        return this.view;

    }

    @Override
    public void onResume() {
        super.onResume();

        EventBus.getDefault().post(new MessageEvent(MessageCode.AUDIO_PLAY_LOOP, AudioScene.GATE_NOT_CLOSE));

        tickTimer.start(Common.GateWaitMillis, Common.TickInterval, (t) -> {
            TextView tv = this.view.findViewById(R.id.gateStateTiktokTimeDesc);
            tv.setText(Common.getTickDesc(t));
        }, (t) -> {
            playAudio(AudioScene.GATE_NOT_CLOSE_TIMEOUT);
            this.textViewGateStateProcessDescription.setText(Desc_Gate_Not_Close_Timeout);
        });


        textViewGateStateProcessDescription = this.view.findViewById(R.id.gateStateProcessDescription);
        this.textViewGateStateProcessDescription.setText(Desc_Gate_Pending_Open);
        gateMessageTimer.start(Common.GateWaitMillis, Common.MessageLoopInterval, (t) -> {
            MessageExchange.sendRequireOpenGate();
        }, (f) -> {
            LogExt.d(TAG, "在限定时间内未等到开门请求的响应数据");
        });
        LogExt.d(TAG, "等待开门");
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

    private void playAudio(AudioScene audioScene) {
        EventBus.getDefault().post(new MessageEvent(MessageCode.AUDIO_PLAY, audioScene));
    }

    /**
     * ThreadMode设置为MAIN，事件的处理会在UI线程中执行，用TextView来展示收到的事件消息
     *
     * @param dataEvent
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void OnEventMessage(AppMessage dataEvent) {

        LogExt.d(TAG, "OnEventMessage: " + dataEvent.toString());

        if (dataEvent.getMessageType() == AppMessageType.APP_REQUIRE_OPEN_GATE_RESP) {
            LogExt.d(TAG, "门已打开: " + dataEvent.toString());
            gateMessageTimer.cancel();

            textViewGateStateProcessDescription = this.view.findViewById(R.id.gateStateProcessDescription);
            this.textViewGateStateProcessDescription.setText(Desc_Gate_Not_Close);
        }

        if (dataEvent.getMessageType() == AppMessageType.GATE_CLOSED_EVENT_REQ) {

            if (dataEvent.getPayload() == AppMessageValue.GATE_CLOSED_EVENT_REQ) {
                LogExt.d(TAG, "关门到位");

                this.textViewGateStateProcessDescription.setText(Desc_Gate_Closed);
                playAudio(AudioScene.GATE_CLOSE);

                this.tickTimer.cancel();
                tickTimer.start(Common.GateClosedMillis, Common.TickInterval, (t) -> {
                    TextView tv = this.view.findViewById(R.id.gateStateTiktokTimeDesc);
                    tv.setText(Common.getTickDesc(t));
                }, (t) -> {
                    //倒计时结束后,返回首页
                    EventBus.getDefault().post(new MessageEvent(MessageCode.HOME, null));
                });
                this.gateClosed = true;
            }
        }

    }
}

