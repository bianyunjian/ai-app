package com.hankutech.ax.appdemo.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.penfeizhou.animation.apng.APNGDrawable;
import com.github.penfeizhou.animation.loader.AssetStreamLoader;
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

import java.util.Random;

public class GateFragment extends Fragment implements IFragmentOperation {

    private static final String TAG = "GateFragment";
    private static final String Desc_Gate_Pending_Open = "正在打开投递口的门!";
    private static final String Desc_Gate_Not_Close = "请按照垃圾分类，并打开垃圾袋投放。\n全程会视频录制和评分.\n感谢您的配合!";
    private static final String Desc_Gate_Not_Close_Timeout = "系统故障,请联系管理员处理";
    private View view;
    private TickTimer tickTimer = new TickTimer();
    private TextView textViewGateStateProcessDescription;
    private GateState gateState = GateState.CLOSED;


    private TickTimer gateMessageTimer = new TickTimer();
    private ImageView imageView;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        this.view = inflater.inflate(R.layout.fragment_gate, container, false);

        return this.view;

    }

    @Override
    public void onResume() {
        super.onResume();

        tickTimer.start(Common.GateWaitMillis, Common.TickInterval, (t) -> {
            TextView tv = this.view.findViewById(R.id.gateStateTiktokTimeDesc);
            tv.setText(Common.getTimeTickDesc(t));
        }, (t) -> {
            if (this.gateState == GateState.NOT_CLOSE) {
                //gate open, play error tips audio
                playAudio(AudioScene.GATE_NOT_CLOSE_TIMEOUT);
                this.textViewGateStateProcessDescription.setText(Desc_Gate_Not_Close_Timeout);

            } else if (this.gateState == GateState.CLOSED) {
                //gate not open , return to home
                EventBus.getDefault().post(new MessageEvent(MessageCode.HOME, null));
            }

        });

        imageView = this.view.findViewById(R.id.img_gateState_icon);
        AssetStreamLoader assetLoader = new AssetStreamLoader(getContext(), "icon_tf_01.apng");
        APNGDrawable apngDrawable = new APNGDrawable(assetLoader);
        imageView.setImageDrawable(apngDrawable);

        textViewGateStateProcessDescription = this.view.findViewById(R.id.gateStateProcessDescription);
        this.textViewGateStateProcessDescription.setText(Desc_Gate_Pending_Open);


        //ONLINE-FIX 只发一次开门请求， 不再重复发送,  如果发送多次开门请求， PLC可能会执行多次开门动作， 导致关门后门又被打开了。
        MessageExchange.sendRequireOpenGate();

//        gateMessageTimer.start(Common.GateWaitMillis, Common.MessageRequestLoopInterval, (t) -> {
//            MessageExchange.sendRequireOpenGate();
//        }, (f) -> {
//            LogExt.d(TAG, "在限定时间内未等到开门请求的响应数据");
//        });
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
            gateState = GateState.NOT_CLOSE;

            EventBus.getDefault().post(new MessageEvent(MessageCode.AUDIO_PLAY_LOOP, AudioScene.GATE_NOT_CLOSE));
            textViewGateStateProcessDescription = this.view.findViewById(R.id.gateStateProcessDescription);
            this.textViewGateStateProcessDescription.setText(Desc_Gate_Not_Close);
        }

        if (dataEvent.getMessageType() == AppMessageType.GATE_CLOSED_EVENT_REQ) {

            if (dataEvent.getPayload() == AppMessageValue.GATE_CLOSED_EVENT_REQ) {
                LogExt.d(TAG, "关门到位");
                gateState = GateState.CLOSED;

                Random rand = new Random();
                int score = rand.nextInt(10);
                if (score <= 0) {
                    score = 1;
                }
                String desc = "投递完成\n您本次荣获" + score + "分\n感谢您为环境保护的付出";
                this.textViewGateStateProcessDescription.setText(desc);
                AssetStreamLoader assetLoader = new AssetStreamLoader(getContext(), "icon_tf_02.apng");
                APNGDrawable apngDrawable = new APNGDrawable(assetLoader);
                imageView.setImageDrawable(apngDrawable);
                playAudio(AudioScene.GATE_CLOSE);

                this.tickTimer.cancel();
                tickTimer.start(Common.GateClosedMillis, Common.TickInterval, (t) -> {
                    TextView tv = this.view.findViewById(R.id.gateStateTiktokTimeDesc);
                    tv.setText(Common.getTimeTickDesc(t));
                }, (t) -> {
                    //倒计时结束后,返回首页
                    EventBus.getDefault().post(new MessageEvent(MessageCode.HOME, null));
                });

            }
        }

    }
}

