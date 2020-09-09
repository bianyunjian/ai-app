package com.hankutech.ax.appdemo.fragment;

import android.app.Fragment;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import com.github.penfeizhou.animation.apng.APNGDrawable;
import com.github.penfeizhou.animation.loader.AssetStreamLoader;
import com.hankutech.ax.appdemo.MessageExchange;
import com.hankutech.ax.appdemo.R;
import com.hankutech.ax.appdemo.code.AudioScene;
import com.hankutech.ax.appdemo.code.MessageCode;
import com.hankutech.ax.appdemo.constant.Common;
import com.hankutech.ax.appdemo.constant.RuntimeContext;
import com.hankutech.ax.appdemo.event.MessageEvent;
import com.hankutech.ax.appdemo.util.LogExt;
import com.hankutech.ax.appdemo.util.TickTimer;
import com.hankutech.ax.message.code.AIGarbageTypeDetectResult;
import com.hankutech.ax.message.protocol.app.AppMessage;
import com.hankutech.ax.message.protocol.app.AppMessageType;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class GarbageFragment extends Fragment implements IFragmentOperation {

    private static final String TAG = "GarbageFragment";
    private static final String Desc_Default = AudioScene.GARBAGE_DETECT.getDescription();
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
            tv.setText(Common.getTimeTickDesc(t));
        }, (t) -> {
            backToHome();
        });


        textViewGarbageDetectProcessDescription = this.view.findViewById(R.id.garbageDetectProcessDescription);
        this.textViewGarbageDetectProcessDescription.setText(Desc_Default);

        LogExt.d(TAG, "正在检测垃圾分类");


        AssetStreamLoader assetLoader = new AssetStreamLoader(getContext(), "icon_garbage_detect.apng");
        ImageView imageView = this.view.findViewById(R.id.img_garbageDetect_icon);
        APNGDrawable apngDrawable = new APNGDrawable(assetLoader);
        imageView.setImageDrawable(apngDrawable);

        sendGarbageDetectMessageTickTimer.start(Common.GarbageWaitDetectMillis, Common.MessageRequestLoopInterval, (t) -> {
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

                    this.tickTimer.cancel();
                    this.receiveGarbageResult = true;
                    this.sendGarbageDetectMessageTickTimer.cancel();

                    //垃圾分类检测完成， 进入开门流程
                    EventBus.getDefault().post(new MessageEvent(MessageCode.GARBAGE_PASS, null));
                    return;
                } else if (garbageDetectResult.getValue() == AIGarbageTypeDetectResult.FAILURE.getValue()) {
                    LogExt.d(TAG, "垃圾分类检测结果失败");

                    playFailureAudio();

//                    this.textViewGarbageDetectProcessDescription.setTextColor(Color.RED);
                    this.tickTimer.cancel();
                    tickTimer.start(Common.GarbageDetectFailureMillis, Common.TickInterval, (t) -> {
                        TextView tv = this.view.findViewById(R.id.garbageDetectTiktokTimeDesc);
                        tv.setText(Common.getTimeTickDesc(t));
                    }, (t) -> {
                        //倒计时结束后,回到主界面
                        EventBus.getDefault().post(new MessageEvent(MessageCode.HOME, null));
                    });
                    this.receiveGarbageResult = true;
                    this.sendGarbageDetectMessageTickTimer.cancel();

                    AssetStreamLoader assetLoader = new AssetStreamLoader(getContext(), "icon_garbage_detect_failure.apng");
                    ImageView imageView = this.view.findViewById(R.id.img_garbageDetect_icon);
                    APNGDrawable apngDrawable = new APNGDrawable(assetLoader);
                    imageView.setImageDrawable(apngDrawable);

                }
            }
        }
    }

    private void playFailureAudio() {
        AudioScene scene = AudioScene.GARBAGE_DETECT_FAILURE_DRY;
        switch (RuntimeContext.CurrentGarbageType) {
            case DRY:
                scene = AudioScene.GARBAGE_DETECT_FAILURE_DRY;
                break;
            case WET:
                scene = AudioScene.GARBAGE_DETECT_FAILURE_WET;
                break;
            case RECYCLABLE:
                scene = AudioScene.GARBAGE_DETECT_FAILURE_RECYCLE;
                break;
            case HAZARDOUS:
                scene = AudioScene.GARBAGE_DETECT_FAILURE_HA;
                break;
            case WHITE_QUILT:
                scene = AudioScene.GARBAGE_DETECT_FAILURE_WHITE_QUILT;
                break;
        }

        playAudio(scene);
        this.textViewGarbageDetectProcessDescription.setText(scene.getDescription());
    }
}

