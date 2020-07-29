package com.hankutech.ax.appdemo;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.SoundPool;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.BarChart;
import com.hankutech.ax.appdemo.code.AppStatus;
import com.hankutech.ax.appdemo.code.AudioScene;
import com.hankutech.ax.appdemo.code.AuthType;
import com.hankutech.ax.appdemo.code.MessageCode;
import com.hankutech.ax.appdemo.service.NettySocketService;
import com.hankutech.ax.appdemo.util.ChartUtils;
import com.hankutech.ax.appdemo.view.AuthFragment;
import com.hankutech.ax.appdemo.view.IFragmentOperation;
import com.hankutech.ax.appdemo.view.VideoFragment;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {
    static {
        System.loadLibrary("SmartPlayer");
    }

    private static final String TAG = "MainActivity";
    private TextView nowTimeTextView;

    private TextView appStatusTextView;
    private BarChart chart;
    private SoundPool soundPool;

    //config data
    private Uri videoUri;
    private String aiFaceRTSPUrl;

    private HashMap<AudioScene, Integer> audioMap = new HashMap<>();
    private Button startProcessButton;
    private Fragment currentFragment;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        //启动NettySocketService
        Intent intent = new Intent(this, NettySocketService.class);
        intent.setAction("android.intent.action.RESPOND_VIA_MESSAGE");
        MainActivity.this.startService(intent);
    }


    @Override
    protected void onResume() {
        super.onResume();

        initConfigData();
        bindControl();

        setAppStatusView("运行中");
        setTimeView();
        setChartView();

        showVideoView();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Intent intent = new Intent(this, NettySocketService.class);
        intent.setAction("android.intent.action.RESPOND_VIA_MESSAGE");
        MainActivity.this.stopService(intent);
    }


    private void initConfigData() {
        String videoPath = "android.resource://com.hankutech.ax.appdemo/" + R.raw.garbage01;
        videoUri = Uri.parse(videoPath);
        this.aiFaceRTSPUrl = "rtsp://admin:NYCQJS@192.168.123.115:554/";

        // 第一个参数为同时播放数据流的最大个数，第二数据流类型，第三为声音质量
        soundPool = new SoundPool(1, AudioManager.STREAM_MUSIC, 100);

        audioMap.put(AudioScene.AUTH_CHOOSE_TYPE, soundPool.load(this, R.raw.choose_auth_type, 1));
        audioMap.put(AudioScene.AUTH_RFID, soundPool.load(this, R.raw.auth_rfid, 1));
        audioMap.put(AudioScene.AUTH_AI_FACE, soundPool.load(this, R.raw.auth_ai_face, 1));
        audioMap.put(AudioScene.AUTH_QRCODE, soundPool.load(this, R.raw.auth_qrcode, 1));
        audioMap.put(AudioScene.AUTH_PASS, soundPool.load(this, R.raw.auth_pass, 1));
    }

    private void bindControl() {
        appStatusTextView = (TextView) findViewById(R.id.app_status);
        nowTimeTextView = (TextView) findViewById(R.id.nowTime);

        chart = findViewById(R.id.chart);

        startProcessButton = (Button) findViewById(R.id.startProcess);
        startProcessButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ShowChooseAuthType();
            }
        });
    }

    private void setTimeView() {

        Timer timer = new Timer();
        TimerTask task = new TimerTask() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void run() {

                SimpleDateFormat format = new SimpleDateFormat("yyyy年MM月dd日 HH时mm分ss秒");
                String nowTimeText = format.format(new Date());
                Message msg = new Message();
                msg.what = MessageCode.TIME_UPDATE.getValue();  //消息(一个整型值)
                msg.obj = nowTimeText;
                mHandler.sendMessage(msg);
            }
        };
        timer.schedule(task, 0, 1000);
    }


    private void setChartView() {
        ArrayList<String> xValues = new ArrayList<>();
        xValues.add("干垃圾");
        xValues.add("湿垃圾");
        xValues.add("有害垃圾");
        xValues.add("其他垃圾");

        ArrayList<Integer> yValues = new ArrayList<>();
        yValues.add(10);
        yValues.add(20);
        yValues.add(30);
        yValues.add(10);

        ChartUtils.initChart(chart, xValues);
        ChartUtils.notifyDataSetChanged(chart, xValues, yValues);
    }

    private void playAudio(AudioScene audioScene, boolean loop) {
        stopAudio();
        if (audioMap.containsKey(audioScene)) {
            Integer audio = audioMap.get(audioScene);
            AudioManager mgr = (AudioManager) this
                    .getSystemService(Context.AUDIO_SERVICE);
            // 获取系统声音的当前音量
            float currentVolume = mgr.getStreamVolume(AudioManager.STREAM_MUSIC);
            // 获取系统声音的最大音量
            float maxVolume = mgr.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
            // 获取当前音量的百分比
            float volume = currentVolume / maxVolume;
            // 第一个参数是声效ID,第二个是左声道音量，第三个是右声道音量，第四个是流的优先级，最低为0，第五个是是否循环播放，第六个播放速度(1.0 =正常播放,范围0.5 - 2.0)
            soundPool.play(audio, volume, volume, 1, loop ? 1 : 0, 1f);
        }
    }

    private void stopAudio() {
        soundPool.stop(1);
    }

    private void showVideoView() {

        VideoFragment videoFragment = new VideoFragment();
        videoFragment.setVideoUri(videoUri);
        replaceView(videoFragment);
    }

    private void showAuthView() {

        AuthFragment authFragment = new AuthFragment();
        authFragment.setRTSPVideoUrl(this.aiFaceRTSPUrl);
        replaceView(authFragment);
    }

    private <T extends Fragment> void replaceView(T t) {

        this.stopAudio();

        if (this.currentFragment != null) {
            if (this.currentFragment instanceof IFragmentOperation) {
                ((IFragmentOperation) (this.currentFragment)).release();
            }
        }
        getFragmentManager().beginTransaction()
                .replace(R.id.main_container, t).commit();
        getFragmentManager().beginTransaction().show(t);
        this.currentFragment = t;
        if (this.currentFragment instanceof IFragmentOperation) {
            ((IFragmentOperation) (this.currentFragment)).setHandler(this.mHandler);
        }
    }

    /**
     * 显示主界面
     */
    private void ShowHome() {
        showVideoView();
        this.startProcessButton.setVisibility(View.VISIBLE);
    }

    /**
     * 显示选择身份验证的界面
     */
    private void ShowChooseAuthType() {
        showAuthView();
        this.startProcessButton.setVisibility(View.INVISIBLE);
    }

    /**
     * 显示等待验证通过的界面
     */
    private void ShowWaitAuth(AuthType authType) {

    }

    /**
     * 显示用户身份的界面
     */
    private void ShowAuthUserInfo() {

    }

    /**
     * 显示垃圾分类检测的界面
     */
    private void ShowGarbageDetect() {

    }

    /**
     * 显示垃圾分类检测的结果界面
     */
    private void ShowGarbageDetectResult() {

    }


    /**
     * 显示关门界面
     */
    private void ShowGateClose() {

    }

    //在主线程里面处理消息并更新UI界面
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            MessageCode code = MessageCode.valueOf(msg.what);
            switch (code) {
                case TIME_UPDATE:
                    String nowTimeText = msg.obj.toString();
                    nowTimeTextView.setText(nowTimeText);
                    break;
                case APP_STATUS_UPDATE:
                    String text = msg.obj.toString();
                    setAppStatusView(text);
                    break;
                case HOME:
                    ShowHome();
                    break;
                case HOME_SLEEP:
                    break;
                case VIDEO_PLAY:
                    break;
                case VIDEO_STOP:
                    break;
                case AUDIO_PLAY:
                    AudioScene audioScene = (AudioScene) (msg.obj);
                    boolean loop = false;
                    playAudio(audioScene, loop);
                    break;
                case AUDIO_STOP:
                    stopAudio();
                    break;
                case TICKTOCK_START:
                    break;
                case TICKTOCK_STOP:
                    break;
                case TICKTOCK_UPDATE:
                    break;
                case UNKNOWN:
                    Log.d(TAG, "handleMessage: " + code);
                default:
                    break;

            }
        }


    };

    private void setAppStatusView(String text) {
        appStatusTextView.setText(text);
        appStatusTextView.setTextColor(Color.GREEN);
        if (text.equals(AppStatus.WAIT)) {
            appStatusTextView.setTextColor(Color.YELLOW);
        }
        if (text.equals(AppStatus.ERROR)) {
            appStatusTextView.setTextColor(Color.RED);
        }
    }
}