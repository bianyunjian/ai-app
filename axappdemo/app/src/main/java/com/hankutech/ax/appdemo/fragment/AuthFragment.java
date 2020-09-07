package com.hankutech.ax.appdemo.fragment;

import android.app.Fragment;
import android.content.Context;
import android.graphics.Outline;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.EventLog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


import com.hankutech.ax.appdemo.MessageExchange;
import com.hankutech.ax.appdemo.constant.RuntimeContext;
import com.hankutech.ax.appdemo.event.AXDataEvent;
import com.hankutech.ax.appdemo.event.AuthChooseEvent;
import com.hankutech.ax.appdemo.event.MessageEvent;
import com.hankutech.ax.appdemo.R;

import com.hankutech.ax.appdemo.code.AudioScene;
import com.hankutech.ax.appdemo.code.MessageCode;
import com.hankutech.ax.appdemo.constant.Common;
import com.hankutech.ax.appdemo.util.LogExt;
import com.hankutech.ax.appdemo.util.TickTimer;
import com.daniulive.smartplayer.EventHandeV2;
import com.daniulive.smartplayer.SmartPlayerJniV2;
import com.hankutech.ax.message.code.AIAuthFlag;
import com.hankutech.ax.message.protocol.app.AppMessage;
import com.hankutech.ax.message.protocol.app.AppMessageType;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class AuthFragment extends Fragment implements IFragmentOperation {

    private static final String TAG = "AuthFragment";
    private View view;
    private TickTimer tickTimer = new TickTimer();
    private TickTimer authMessageLoopTimer = new TickTimer();
    private View layoutRfid;
    private View layoutAiFace;
    private View layoutQrCode;
    private View layoutChooseAuthType;
    private TextView textViewGuidDescription;
    private View backChooseButton;
    private View layoutAuthError;

    private String rtspUrl;
    private SurfaceView sSurfaceView = null;
    private long playerHandle = 0;
    private SmartPlayerJniV2 libPlayer = null;
    private Context myContext;
    private AIAuthFlag currentAuthFlag;
    private boolean authPassed;
    private View backHomeButton;

    public void setRTSPVideoUrl(String rtspUrl) {
        this.rtspUrl = rtspUrl;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        this.view = inflater.inflate(R.layout.fragment_auth, container, false);

        return this.view;

    }

    @Override
    public void onResume() {
        super.onResume();

        playAudio(AudioScene.AUTH_CHOOSE_TYPE);

        tickTimer.start(Common.TickMillis, Common.TickInterval, (t) -> {
            TextView tv = this.view.findViewById(R.id.authTiktokTimeDesc);
            tv.setText(Common.getTimeTickDesc(t));
        }, (t) -> {
            stopRTSPVideo();

            if (!authPassed) {
                handleAuthFail();
            }

//            backToHome();
        });

        this.libPlayer = new SmartPlayerJniV2();
        myContext = getActivity().getApplicationContext();
        sSurfaceView = this.view.findViewById(R.id.surfaceView_ai_face_rtsp);

        textViewGuidDescription = this.view.findViewById(R.id.textViewGuidDescription);
        textViewGuidDescription.setText(AudioScene.AUTH_CHOOSE_TYPE.getDescription());
        layoutChooseAuthType = this.view.findViewById(R.id.layout_choose_auth_type);
        layoutRfid = this.view.findViewById(R.id.layout_choose_auth_rfid);
        layoutRfid = this.view.findViewById(R.id.layout_choose_auth_rfid);
        layoutAiFace = this.view.findViewById(R.id.layout_choose_auth_ai_face);
        layoutQrCode = this.view.findViewById(R.id.layout_choose_auth_qrcode);
        layoutChooseAuthType.setVisibility(View.VISIBLE);
        backChooseButton = this.view.findViewById(R.id.button_back_choose_auth_type);
        backChooseButton.setVisibility(View.INVISIBLE);

        layoutAuthError = this.view.findViewById(R.id.layout_auth_error);
        layoutAuthError.setVisibility(View.INVISIBLE);

        backHomeButton = this.view.findViewById(R.id.button_back_choose_auth_type_back_home);
        backHomeButton.setVisibility(View.VISIBLE);
        backHomeButton.setOnClickListener((t) -> {
            EventBus.getDefault().post(new MessageEvent(MessageCode.HOME, null));
        });

        backChooseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopRTSPVideo();

                currentAuthFlag = AIAuthFlag.FAILURE;
                RuntimeContext.CurrentAuthFlag = currentAuthFlag;
                layoutChooseAuthType.setVisibility(View.VISIBLE);
                layoutRfid.setVisibility(View.GONE);
                layoutAiFace.setVisibility(View.GONE);
                layoutQrCode.setVisibility(View.GONE);
                backChooseButton.setVisibility(View.INVISIBLE);
                backHomeButton.setVisibility(View.VISIBLE);

                playAudio(AudioScene.AUTH_CHOOSE_TYPE);
                textViewGuidDescription.setText(AudioScene.AUTH_CHOOSE_TYPE.getDescription());
                tickTimer.reset();
                authMessageLoopTimer.cancel();

                LogExt.d(TAG, "重新选择身份验证方式");
            }
        });
        this.view.findViewById(R.id.button_rfid).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopRTSPVideo();

                currentAuthFlag = AIAuthFlag.RFID;
                RuntimeContext.CurrentAuthFlag = currentAuthFlag;
                layoutChooseAuthType.setVisibility(View.GONE);
                layoutRfid.setVisibility(View.VISIBLE);
                layoutAiFace.setVisibility(View.GONE);
                layoutQrCode.setVisibility(View.GONE);
                backChooseButton.setVisibility(View.VISIBLE);
                backHomeButton.setVisibility(View.INVISIBLE);
                playAudio(AudioScene.AUTH_RFID);
                textViewGuidDescription.setText(AudioScene.AUTH_RFID.getDescription());
                tickTimer.reset();


                LogExt.d(TAG, "身份验证方式=RFID");
                EventBus.getDefault().post(new AuthChooseEvent(AIAuthFlag.RFID));
            }
        });

        this.view.findViewById(R.id.button_ai_face).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playRTSPVideo();

                currentAuthFlag = AIAuthFlag.AI_FACE;
                RuntimeContext.CurrentAuthFlag = currentAuthFlag;
                layoutChooseAuthType.setVisibility(View.GONE);
                layoutRfid.setVisibility(View.GONE);
                layoutAiFace.setVisibility(View.VISIBLE);
                layoutQrCode.setVisibility(View.GONE);
                backChooseButton.setVisibility(View.VISIBLE);
                backHomeButton.setVisibility(View.INVISIBLE);

                playAudio(AudioScene.AUTH_AI_FACE);
                textViewGuidDescription.setText(AudioScene.AUTH_AI_FACE.getDescription());
                tickTimer.reset();
                LogExt.d(TAG, "身份验证方式=AI FACE");
                EventBus.getDefault().post(new AuthChooseEvent(AIAuthFlag.AI_FACE));
            }
        });

        this.view.findViewById(R.id.button_qrcode).setOnClickListener((t) -> {
            Toast.makeText(view.getContext(), "正在开发中，敬请期待", Toast.LENGTH_LONG).show();
        });
//        this.view.findViewById(R.id.button_qrcode).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                stopRTSPVideo();
//
//                currentAuthFlag = AIAuthFlag.QRCODE;
//                RuntimeContext.CurrentAuthFlag = currentAuthFlag;
//                layoutChooseAuthType.setVisibility(View.GONE);
//                layoutRfid.setVisibility(View.GONE);
//                layoutAiFace.setVisibility(View.GONE);
//                layoutQrCode.setVisibility(View.VISIBLE);
//                backChooseButton.setVisibility(View.VISIBLE);
//                backHomeButton.setVisibility(View.INVISIBLE);
//
//                playAudio(AudioScene.AUTH_QRCODE);
//                textViewGuidDescription.setText(AudioScene.AUTH_QRCODE.getDescription());
//                tickTimer.reset();
//                LogExt.d(TAG, "身份验证方式==QRCODE");
//                EventBus.getDefault().post(new AuthChooseEvent(AIAuthFlag.QRCODE));
//            }
//        });

        LogExt.d(TAG, "等待选择身份验证方式");
    }

    private void playAudio(AudioScene chooseAuthType) {

        EventBus.getDefault().post(new MessageEvent(MessageCode.AUDIO_PLAY, chooseAuthType));
    }

    private void InitAndSetConfig4RTSP() {
        LogExt.d(TAG, "InitAndSetConfig4RTSP:" + rtspUrl);
        playerHandle = libPlayer.SmartPlayerOpen(myContext);

        if (playerHandle == 0) {
            Log.e(TAG, "surfaceHandle with nil..");
            return;
        }

        libPlayer.SetSmartPlayerEventCallbackV2(playerHandle,
                new EventHandeV2());
        int playBuffer = 200;// 默认200ms
        libPlayer.SmartPlayerSetBuffer(playerHandle, playBuffer);

        // set report download speed(默认2秒一次回调 用户可自行调整report间隔)
        libPlayer.SmartPlayerSetReportDownloadSpeed(playerHandle, 1, 2);

        boolean isFastStartup = true;
        libPlayer.SmartPlayerSetFastStartup(playerHandle, isFastStartup ? 1 : 0);

        //设置RTSP超时时间
        int rtsp_timeout = 10;
        libPlayer.SmartPlayerSetRTSPTimeout(playerHandle, rtsp_timeout);

        //设置RTSP TCP/UDP模式自动切换
        int is_auto_switch_tcp_udp = 1;
        libPlayer.SmartPlayerSetRTSPAutoSwitchTcpUdp(playerHandle, is_auto_switch_tcp_udp);

        libPlayer.SmartPlayerSaveImageFlag(playerHandle, 1);

        // It only used when playback RTSP stream..
        libPlayer.SmartPlayerSetRTSPTcpMode(playerHandle, 1);


        if (this.rtspUrl == null) {
            Log.e(TAG, "playback URL with NULL...");
            return;
        }

        libPlayer.SmartPlayerSetUrl(playerHandle, this.rtspUrl);

    }

    private void stopRTSPVideo() {
        Log.i(TAG, "Stop playback stream++");

        int iRet = libPlayer.SmartPlayerStopPlay(playerHandle);

        if (iRet != 0) {
            Log.e(TAG, "Call SmartPlayerStopPlay failed..");
            return;
        }
        libPlayer.SmartPlayerClose(playerHandle);
        playerHandle = 0;
    }

    private void playRTSPVideo() {
        InitAndSetConfig4RTSP();
        Log.i(TAG, "Start playback stream++");
        // 如果第二个参数设置为null，则播放纯音频
        this.libPlayer.SmartPlayerSetSurface(playerHandle, sSurfaceView);

        this.libPlayer.SmartPlayerSetRenderScaleMode(playerHandle, 1);

        boolean isLowLatency = true;
        this.libPlayer.SmartPlayerSetLowLatencyMode(playerHandle, isLowLatency ? 1
                : 0);

        int iPlaybackRet = this.libPlayer
                .SmartPlayerStartPlay(playerHandle);

        if (iPlaybackRet != 0) {
            Log.e(TAG, "Call SmartPlayerStartPlay failed..");
            return;
        }
    }

    @Override
    public void init() {
        EventBus.getDefault().register(this);
    }

    public void release() {
        this.tickTimer.cancel();
        stopRTSPVideo();
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

        if (dataEvent.getMessageType() == AppMessageType.AUTH_RESP) {

            int authResult = dataEvent.getPayload();
            if (this.authPassed == false && this.currentAuthFlag != null && authResult == this.currentAuthFlag.getValue()) {
                LogExt.d(TAG, "授权成功:" + this.currentAuthFlag.getDescription());
                if (this.authMessageLoopTimer != null) {
                    this.authMessageLoopTimer.cancel();
                }

                this.tickTimer.cancel();

                //至少显示一下视频画面3秒， 避免消息接收快了后， 人脸视频画面一闪而过。
                tickTimer.start(3000, Common.TickInterval, (t) -> {
                }, (t) -> {
                    stopRTSPVideo();
                    this.layoutAiFace.setVisibility(View.GONE);
                    this.layoutRfid.setVisibility(View.GONE);
                    this.layoutQrCode.setVisibility(View.GONE);
                    this.layoutChooseAuthType.setVisibility(View.GONE);
                    this.backChooseButton.setVisibility(View.GONE);
                    String personDesc = String.format("欢迎您, 尊敬的%s", "用户");
                    this.textViewGuidDescription.setText(personDesc);

                    tickTimer.start(3000, Common.TickInterval, (t2) -> {
                        TextView tv = this.view.findViewById(R.id.authTiktokTimeDesc);
                        tv.setText("倒计时" + t2 + "S");
                    }, (t2) -> {
                        EventBus.getDefault().post(new MessageEvent(MessageCode.AUTH_PASS, "用户"));
                    });
                });

                playAudio(AudioScene.AUTH_PASS);
                this.authPassed = true;
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void OnAuthChooseEvent(AuthChooseEvent authChooseEvent) {
        authMessageLoopTimer.start(Common.TickMillis, Common.MessageRequestLoopInterval, (t) -> {
            MessageExchange.sendAuth(authChooseEvent.getAuthFlag());
        }, (f) -> {
            LogExt.d(TAG, "在限定时间内未等到身份验证的响应数据");
        });

    }


    private void handleAuthFail() {
        tickTimer.start(3000, Common.TickInterval, (t2) -> {
            TextView tv = this.view.findViewById(R.id.authTiktokTimeDesc);
            tv.setText("倒计时" + t2 + "S");

            this.layoutAiFace.setVisibility(View.GONE);
            this.layoutRfid.setVisibility(View.GONE);
            this.layoutQrCode.setVisibility(View.GONE);
            this.layoutChooseAuthType.setVisibility(View.GONE);
            this.backChooseButton.setVisibility(View.GONE);
            this.textViewGuidDescription.setVisibility(View.GONE);

            this.layoutAuthError.setVisibility(View.VISIBLE);
        }, (t2) -> {
            backToHome();
        });
    }


}

