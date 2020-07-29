package com.hankutech.ax.appdemo.view;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;


import com.hankutech.ax.appdemo.R;

import com.hankutech.ax.appdemo.code.AudioScene;
import com.hankutech.ax.appdemo.code.MessageCode;
import com.hankutech.ax.appdemo.constant.Common;
import com.hankutech.ax.appdemo.util.TickTimer;
import com.daniulive.smartplayer.EventHandeV2;
import com.daniulive.smartplayer.SmartPlayerJniV2;

public class AuthFragment extends Fragment implements IFragmentOperation {

    private static final String TAG = "AuthFragment";
    private View view;
    private TickTimer tickTimer = new TickTimer();
    private Handler parentHandler;
    private View layoutRfid;
    private View layoutAiFace;
    private View layoutQrCode;
    private View layoutChooseAuthType;
    private TextView textViewGuidDescription;
    private Button backChooseButton;

    private String rtspUrl;
    private SurfaceView sSurfaceView = null;
    private long playerHandle = 0;
    private SmartPlayerJniV2 libPlayer = null;
    private Context myContext;

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

        tickTimer.start(Common.TikTokSeconds, Common.TikTokInterval, (t) -> {
            TextView tv = this.view.findViewById(R.id.tiktokTimeDesc);
            tv.setText(Common.getTikTokDesc(t));
        }, (t) -> {
            stopRTSPVideo();
            backToHome();
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

        this.view.findViewById(R.id.button_back_choose_auth_type).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopRTSPVideo();

                layoutChooseAuthType.setVisibility(View.VISIBLE);
                layoutRfid.setVisibility(View.GONE);
                layoutAiFace.setVisibility(View.GONE);
                layoutQrCode.setVisibility(View.GONE);
                backChooseButton.setVisibility(View.INVISIBLE);

                playAudio(AudioScene.AUTH_CHOOSE_TYPE);
                textViewGuidDescription.setText(AudioScene.AUTH_CHOOSE_TYPE.getDescription());
                tickTimer.reset();
            }
        });
        this.view.findViewById(R.id.button_rfid).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopRTSPVideo();

                layoutChooseAuthType.setVisibility(View.GONE);
                layoutRfid.setVisibility(View.VISIBLE);
                layoutAiFace.setVisibility(View.GONE);
                layoutQrCode.setVisibility(View.GONE);
                backChooseButton.setVisibility(View.VISIBLE);

                playAudio(AudioScene.AUTH_RFID);
                textViewGuidDescription.setText(AudioScene.AUTH_RFID.getDescription());
                tickTimer.reset();
            }
        });

        this.view.findViewById(R.id.button_ai_face).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playRTSPVideo();

                layoutChooseAuthType.setVisibility(View.GONE);
                layoutRfid.setVisibility(View.GONE);
                layoutAiFace.setVisibility(View.VISIBLE);
                layoutQrCode.setVisibility(View.GONE);
                backChooseButton.setVisibility(View.VISIBLE);

                playAudio(AudioScene.AUTH_AI_FACE);
                textViewGuidDescription.setText(AudioScene.AUTH_AI_FACE.getDescription());
                tickTimer.reset();
            }
        });

        this.view.findViewById(R.id.button_qrcode).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopRTSPVideo();

                layoutChooseAuthType.setVisibility(View.GONE);
                layoutRfid.setVisibility(View.GONE);
                layoutAiFace.setVisibility(View.GONE);
                layoutQrCode.setVisibility(View.VISIBLE);
                backChooseButton.setVisibility(View.VISIBLE);

                playAudio(AudioScene.AUTH_QRCODE);
                textViewGuidDescription.setText(AudioScene.AUTH_QRCODE.getDescription());
                tickTimer.reset();
            }
        });
    }

    private void playAudio(AudioScene chooseAuthType) {
        Message msg = new Message();
        msg.what = MessageCode.AUDIO_PLAY.getValue();
        msg.obj = chooseAuthType;
        this.parentHandler.sendMessage(msg);
    }

    private void InitAndSetConfig4RTSP() {

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
    }

    public void release() {

    }

    @Override
    public void setHandler(Handler mHandler) {
        this.parentHandler = mHandler;
    }

    private void backToHome() {
        if (this.parentHandler != null) {
            Message msg = new Message();
            msg.what = MessageCode.HOME.getValue();
            this.parentHandler.sendMessage(msg);
        }
    }
}

