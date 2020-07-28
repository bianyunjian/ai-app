package com.hankutech.ax.appdemo;

import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.BarChart;
import com.hankutech.ax.appdemo.code.AppStatus;
import com.hankutech.ax.appdemo.code.AuthType;
import com.hankutech.ax.appdemo.code.MessageCode;
import com.hankutech.ax.appdemo.constant.Common;
import com.hankutech.ax.appdemo.util.ChartUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private TextView nowTimeTextView;
    private String videoPath = "android.resource://com.hankutech.ax.appdemo/" + R.raw.garbage01;
    private VideoView videoView;
    private TextView appStatusTextView;
    private BarChart chart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
    }


    @Override
    protected void onResume() {
        super.onResume();

        bindControl();

        setAppStatusView("运行中");
        setTimeView();
        setVideoView();
        setChartView();
    }

    private void bindControl() {
        appStatusTextView = (TextView) findViewById(R.id.app_status);
        nowTimeTextView = (TextView) findViewById(R.id.nowTime);
        videoView = (VideoView) findViewById(R.id.videoView);

        chart = findViewById(R.id.chart);
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
                msg.what = 1;  //消息(一个整型值)
                msg.obj = nowTimeText;
                mHandler.sendMessage(msg);
            }
        };
        timer.schedule(task, 0, 1000);
    }

    private void setVideoView() {

        videoView.setVideoURI(Uri.parse(videoPath));
        if (Common.DebugMode) {
            MediaController mediaController = new MediaController(this);
            videoView.setMediaController(mediaController);
        }
        videoView.start();

        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                Log.d("videoView", "video播完了");
                videoView.setVideoURI(Uri.parse(videoPath));
                videoView.start();
            }
        });
        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
            @Override
            public void onPrepared(MediaPlayer mp) {
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

    /**
     * 显示主界面
     */
    private void ShowHome() {

    }

    /**
     * 显示选择身份验证的界面
     */
    private void ShowChooseAuthType() {

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
                    break;
                case HOME_SLEEP:
                    break;
                case VIDEO_PLAY:
                    videoView.start();
                    break;
                case VIDEO_STOP:
                    videoView.stopPlayback();
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