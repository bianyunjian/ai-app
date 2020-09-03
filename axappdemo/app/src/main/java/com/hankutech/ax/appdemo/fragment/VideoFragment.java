package com.hankutech.ax.appdemo.fragment;

import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.MediaController;
import android.widget.VideoView;

import com.hankutech.ax.appdemo.R;
import com.hankutech.ax.appdemo.code.MessageCode;
import com.hankutech.ax.appdemo.constant.Common;
import com.hankutech.ax.appdemo.event.MessageEvent;
import com.hankutech.ax.appdemo.util.LogExt;

import android.app.Fragment;

import org.greenrobot.eventbus.EventBus;

public class VideoFragment extends Fragment implements IFragmentOperation {

    private Uri videoUri;
    private View view;
    private VideoView videoView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        this.view = inflater.inflate(R.layout.fragment_video, container, false);
        return this.view;
    }

    @Override
    public void onResume() {
        super.onResume();
        videoView = (VideoView) this.view.findViewById(R.id.videoView);
        videoView.setVideoURI(videoUri);
//        if (Common.DebugMode) {
        MediaController mediaController = new MediaController(this.getContext());
        videoView.setMediaController(mediaController);

//        }

        videoView.start();

        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                LogExt.d("videoView", "video播完了");
                videoView.setVideoURI(videoUri);
                videoView.start();
            }
        });
        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
            @Override
            public void onPrepared(MediaPlayer mp) {
                if (Common.VideoMute) {
                    mp.setVolume(0f, 0f);
                }
                videoView.setOnInfoListener(new MediaPlayer.OnInfoListener() {
                    @Override
                    public boolean onInfo(MediaPlayer mp, int what, int extra) {
                        videoView.setBackgroundColor(Color.TRANSPARENT);
                        return false;
                    }
                });
            }
        });
        videoView.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                videoView.stopPlayback(); //播放异常，则停止播放，防止弹窗使界面阻塞
                return true;
            }
        });
    }

    public void setVideoUri(Uri videoUri) {
        this.videoUri = videoUri;
        if (this.videoView != null) {
            this.videoView.stopPlayback();
        }
    }

    @Override
    public void init() {
    }

    public void release() {
        if (this.videoView != null) {
            this.videoView.stopPlayback();
        }
    }


}

