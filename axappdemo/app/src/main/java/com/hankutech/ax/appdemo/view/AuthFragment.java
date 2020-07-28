package com.hankutech.ax.appdemo.view;

import android.app.Fragment;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.hankutech.ax.appdemo.R;
import com.hankutech.ax.appdemo.code.AudioScene;
import com.hankutech.ax.appdemo.code.MessageCode;
import com.hankutech.ax.appdemo.constant.Common;
import com.hankutech.ax.appdemo.util.TikTok;

public class AuthFragment extends Fragment implements IFragmentOperation {

    private View view;
    private TikTok tikTok = new TikTok();
    private Handler parentHandler;
    private View layoutRfid;
    private View layoutAiFace;
    private View layoutQrCode;
    private View layoutChooseAuthType;
    private TextView textViewGuidDescription;
    private Button backChooseButton;


    @Nullable
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

        tikTok.start(Common.TikTokSeconds, Common.TikTokInterval, (t) -> {
            TextView tv = this.view.findViewById(R.id.tiktokTimeDesc);
            tv.setText(Common.getTikTokDesc(t));
        }, (t) -> {
            backToHome();
        });

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
                layoutChooseAuthType.setVisibility(View.VISIBLE);
                layoutRfid.setVisibility(View.GONE);
                layoutAiFace.setVisibility(View.GONE);
                layoutQrCode.setVisibility(View.GONE);
                backChooseButton.setVisibility(View.INVISIBLE);

                playAudio(AudioScene.AUTH_CHOOSE_TYPE);
                textViewGuidDescription.setText(AudioScene.AUTH_CHOOSE_TYPE.getDescription());
                tikTok.reset();
            }
        });
        this.view.findViewById(R.id.button_rfid).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                layoutChooseAuthType.setVisibility(View.GONE);
                layoutRfid.setVisibility(View.VISIBLE);
                layoutAiFace.setVisibility(View.GONE);
                layoutQrCode.setVisibility(View.GONE);
                backChooseButton.setVisibility(View.VISIBLE);

                playAudio(AudioScene.AUTH_RFID);
                textViewGuidDescription.setText(AudioScene.AUTH_RFID.getDescription());
                tikTok.reset();
            }
        });

        this.view.findViewById(R.id.button_ai_face).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                layoutChooseAuthType.setVisibility(View.GONE);
                layoutRfid.setVisibility(View.GONE);
                layoutAiFace.setVisibility(View.VISIBLE);
                layoutQrCode.setVisibility(View.GONE);
                backChooseButton.setVisibility(View.VISIBLE);

                playAudio(AudioScene.AUTH_AI_FACE);
                textViewGuidDescription.setText(AudioScene.AUTH_AI_FACE.getDescription());
                tikTok.reset();
            }
        });

        this.view.findViewById(R.id.button_qrcode).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                layoutChooseAuthType.setVisibility(View.GONE);
                layoutRfid.setVisibility(View.GONE);
                layoutAiFace.setVisibility(View.GONE);
                layoutQrCode.setVisibility(View.VISIBLE);
                backChooseButton.setVisibility(View.VISIBLE);

                playAudio(AudioScene.AUTH_QRCODE);
                textViewGuidDescription.setText(AudioScene.AUTH_QRCODE.getDescription());
                tikTok.reset();
            }
        });
    }

    private void playAudio(AudioScene chooseAuthType) {
        Message msg = new Message();
        msg.what = MessageCode.AUDIO_PLAY.getValue();
        msg.obj = chooseAuthType;
        this.parentHandler.sendMessage(msg);
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

