package com.hankutech.ax.appdemo;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.media.AudioManager;
import android.media.SoundPool;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.hankutech.ax.appdemo.code.AppStatus;
import com.hankutech.ax.appdemo.code.AudioScene;
import com.hankutech.ax.appdemo.code.MessageCode;
import com.hankutech.ax.appdemo.constant.Common;
import com.hankutech.ax.appdemo.constant.RuntimeContext;
import com.hankutech.ax.appdemo.data.ConfigData;
import com.hankutech.ax.appdemo.event.LogEvent;
import com.hankutech.ax.appdemo.event.MessageEvent;
import com.hankutech.ax.appdemo.fragment.AuthFragment;
import com.hankutech.ax.appdemo.fragment.GarbageFragment;
import com.hankutech.ax.appdemo.fragment.GateFragment;
import com.hankutech.ax.appdemo.fragment.IFragmentOperation;
import com.hankutech.ax.appdemo.fragment.VideoFragment;
import com.hankutech.ax.appdemo.service.NettySocketService;
import com.hankutech.ax.appdemo.util.LogExt;
import com.hankutech.ax.appdemo.util.NetworkUtil;
import com.hankutech.ax.appdemo.util.TickTimer;
import com.hankutech.ax.message.code.AIAuthFlag;
import com.hankutech.ax.message.code.AIGarbageResultType;
import com.hankutech.ax.message.protocol.app.AppMessage;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {
    static {
        System.loadLibrary("SmartPlayer");
    }

    private static final String TAG = "MainActivity";
    private TextView nowTimeTextView;
    private TextView appStatusTextView;
    private TextView textViewSupportGarbageType;

    private SoundPool soundPool;

    //config data
    private ConfigData configData = new ConfigData();

    private Button startProcessButton;
    private Fragment currentFragment;
    private Context mContext;
    private PopupWindow popWindow;
    private TextView item_debug_textview_log;
    private TextView logoTitleTextView;
    private ImageView logoImageView;
    private String ip;

    private TickTimer handShakeTickTimer = new TickTimer();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        mContext = MainActivity.this;

        checkNetwork();

        //启动NettySocketService
        Intent intent = new Intent(this, NettySocketService.class);
        intent.setAction("android.intent.action.RESPOND_VIA_MESSAGE");
        MainActivity.this.startService(intent);

        //注册订阅事件
        EventBus.getDefault().register(this);
    }

    private void checkNetwork() {
        this.ip = NetworkUtil.getIPAddress(getApplicationContext());
        LogExt.i(TAG, "ip:" + this.ip);
    }


    @Override
    protected void onResume() {
        super.onResume();

        isGrantExternalRW(this);

        initConfigData();
        bindControl();

        setAppStatusView(AppStatus.NORMAL);
        setTimeView();

        showVideoView();


        handShakeTickTimer.start(Long.MAX_VALUE, 5000, (t) -> {
                    MessageExchange.sendHandShake();
                },
                (f) -> {
                    handShakeTickTimer.reset();
                });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Intent intent = new Intent(this, NettySocketService.class);
        intent.setAction("android.intent.action.RESPOND_VIA_MESSAGE");
        MainActivity.this.stopService(intent);
        //取消注册订阅事件
        EventBus.getDefault().unregister(this);
    }

    public static boolean isGrantExternalRW(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && activity.checkSelfPermission(
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            activity.requestPermissions(new String[]{
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            }, 1);

            return false;
        }

        return true;
    }

    private void initConfigData() {
        configData.loadData(mContext);
        if (configData.getLogoUri() == null) {
            String logoPath = "android.resource://com.hankutech.ax.appdemo/" + R.drawable.ax_logo;
            configData.setLogoUri(Uri.parse(logoPath));
        }
        if (configData.getLogoTitle() == null || configData.getLogoTitle().length() == 0) {
            configData.setLogoTitle(Common.LogoTitle);
        }

        if (configData.getAiFaceRTSPUrl() == null || configData.getAiFaceRTSPUrl().length() == 0) {
            configData.setAiFaceRTSPUrl(Common.AIFace_RTSP_URI);
        }
        if (configData.getVideoUri() == null) {
            String videoPath = "android.resource://com.hankutech.ax.appdemo/" + R.raw.garbage_demo;
            configData.setVideoUri(Uri.parse(videoPath));
        }

        // 第一个参数为同时播放数据流的最大个数，第二数据流类型，第三为声音质量
        soundPool = new SoundPool(1, AudioManager.STREAM_MUSIC, 100);

        HashMap<AudioScene, Integer> audioMap = new HashMap<>();
        audioMap.put(AudioScene.AUTH_CHOOSE_TYPE, soundPool.load(this, R.raw.choose_auth_type, 1));
        audioMap.put(AudioScene.AUTH_RFID, soundPool.load(this, R.raw.auth_rfid, 1));
        audioMap.put(AudioScene.AUTH_AI_FACE, soundPool.load(this, R.raw.auth_ai_face, 1));
        audioMap.put(AudioScene.AUTH_QRCODE, soundPool.load(this, R.raw.auth_qrcode, 1));
        audioMap.put(AudioScene.AUTH_PASS, soundPool.load(this, R.raw.auth_pass, 1));
        audioMap.put(AudioScene.GARBAGE_DETECT, soundPool.load(this, R.raw.garbage_detect, 1));
        audioMap.put(AudioScene.GARBAGE_DETECT_FAILURE, soundPool.load(this, R.raw.garbage_detect_failure, 1));
        audioMap.put(AudioScene.GARBAGE_DETECT_SUCCESS, soundPool.load(this, R.raw.garbage_detect_success, 1));
        audioMap.put(AudioScene.GATE_CLOSE, soundPool.load(this, R.raw.gate_close, 1));
        audioMap.put(AudioScene.GATE_NOT_CLOSE, soundPool.load(this, R.raw.gate_not_close, 1));
        audioMap.put(AudioScene.GATE_NOT_CLOSE_TIMEOUT, soundPool.load(this, R.raw.gate_not_close_timeout, 1));
        configData.setAudioMap(audioMap);


        configData.update(mContext);
    }


    private void bindControl() {
        logoImageView = (ImageView) (findViewById(R.id.logo));
        logoTitleTextView = (TextView) findViewById(R.id.logoTitle);
        appStatusTextView = (TextView) findViewById(R.id.app_sysRunStatus);
        nowTimeTextView = (TextView) findViewById(R.id.nowTime);
        textViewSupportGarbageType = (TextView) findViewById(R.id.textViewSupportGarbageType);

        startProcessButton = (Button) findViewById(R.id.startProcess);
        startProcessButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ShowChooseAuthType();
            }
        });
        ImageView logo = (ImageView) findViewById(R.id.logo);
        logo.setImageURI(configData.getLogoUri());
        logo.setOnClickListener(view -> {
            logoClickCount++;
//                Toast.makeText(mContext, "you click logo " + logoClickCount, Toast.LENGTH_SHORT).show();
            if (logoClickCount > 5) {
                logoClickCount = 0;
                if (popWindow == null || popWindow.isShowing() == false) {
                    showPopupWindow();
                }
            }
        });

        nowTimeTextView.setOnClickListener(view -> {
            timeTextViewClickCount++;
            if (timeTextViewClickCount > 5) {
                timeTextViewClickCount = 0;
                View layoutDebug = findViewById(R.id.layout_debug);
                layoutDebug.setVisibility(View.VISIBLE);
            }
        });

        findViewById(R.id.item_debug_btn_reset_home).setOnClickListener(v -> {
            ShowHome();
        });

        findViewById(R.id.item_debug_btn_exit_app).setOnClickListener(v -> {
            killAppProcess();
        });

        findViewById(R.id.item_debug_btn_hide_debug_layout).setOnClickListener(v -> {
            View layoutDebug = findViewById(R.id.layout_debug);
            layoutDebug.setVisibility(View.GONE);
        });

        item_debug_textview_log = findViewById(R.id.item_debug_textview_log);
        View layoutDebug = findViewById(R.id.layout_debug);
        layoutDebug.setVisibility(View.GONE);
    }

    private void showPopupWindow() {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_popup, null, false);

        //1.构造一个PopupWindow，参数依次是加载的View，宽高
        popWindow = new PopupWindow(view,
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT, true);

        //这些为了点击非PopupWindow区域，PopupWindow会消失的，如果没有下面的
        //代码的话，你会发现，当你把PopupWindow显示出来了，无论你按多少次后退键
        //PopupWindow并不会关闭，而且退不出程序，加上下述代码可以解决这个问题
        popWindow.setTouchable(true);
        popWindow.setTouchInterceptor((v, event) -> {
            // 这里如果返回true的话，touch事件将被拦截
            // 拦截后 PopupWindow的onTouchEvent不被调用，这样点击外部区域无法dismiss
            return false;
        });
        popWindow.setBackgroundDrawable(new ColorDrawable(0xdddddddd));    //设置一个背景

        //设置popupWindow显示的位置，参数依次是参照View，x轴的偏移量，y轴的偏移量
        popWindow.showAtLocation(findViewById(R.id.layout_right), Gravity.END, 0, 0);


        ((TextView) (view.findViewById(R.id.item_popup_textview_ip))).setText(this.ip);


        view.findViewById(R.id.item_popup_btn_choose_logo).setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, null);
            intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    "image/*");
            startActivityForResult(intent, 0x1);
        });


        TextView item_popup_edittext_title = view.findViewById(R.id.item_popup_edittext_title);
        item_popup_edittext_title.setText(configData.getLogoTitle());
        view.findViewById(R.id.item_popup_btn_change_title).setOnClickListener(v -> {
            TextView tv = view.findViewById(R.id.item_popup_edittext_title);
            String newLogoTitle = tv.getText().toString();
            logoTitleTextView.setText(newLogoTitle);
            configData.setLogoTitle(newLogoTitle);
            configData.update(mContext);
        });

        TextView item_popup_edittext_ai_face_rtsp = view.findViewById(R.id.item_popup_edittext_ai_face_rtsp);
        item_popup_edittext_ai_face_rtsp.setText(configData.getAiFaceRTSPUrl());
        view.findViewById(R.id.item_popup_btn_change_ai_face_rtsp).setOnClickListener(v -> {
            TextView tv = view.findViewById(R.id.item_popup_edittext_ai_face_rtsp);
            String newRtsp = tv.getText().toString();
            configData.setAiFaceRTSPUrl(newRtsp);
            configData.update(mContext);
        });

        view.findViewById(R.id.item_popup_btn_choose_video).setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, null);
            intent.setDataAndType(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                    "video/*");
            startActivityForResult(intent, 0x2);
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == 0x1 && resultCode == RESULT_OK) {
            if (data != null) {
                Uri uri = data.getData();
                logoImageView.setImageURI(uri);
                configData.setLogoUri(uri);
                configData.update(mContext);
            }
        }
        if (requestCode == 0x2 && resultCode == RESULT_OK) {
            if (data != null) {
                Uri uri = data.getData();
                showVideoView();
                configData.setVideoUri(uri);
                configData.update(mContext);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    int timeTextViewClickCount = 0;
    private int logoClickCount = 0;

    public void killAppProcess() {
        //注意：不能先杀掉主进程，否则逻辑代码无法继续执行，需先杀掉相关进程最后杀掉主进程
        ActivityManager mActivityManager = (ActivityManager) MainActivity.this.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> mList = mActivityManager.getRunningAppProcesses();
        for (ActivityManager.RunningAppProcessInfo runningAppProcessInfo : mList) {
            if (runningAppProcessInfo.pid != android.os.Process.myPid()) {
                android.os.Process.killProcess(runningAppProcessInfo.pid);
            }
        }
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(0);
    }

    private void setTimeView() {

        Timer timer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {

                SimpleDateFormat format = new SimpleDateFormat("yyyy年MM月dd日 HH时mm分ss秒");
                String nowTimeText = format.format(new Date());

                EventBus.getDefault().post(new MessageEvent(MessageCode.TIME_UPDATE, nowTimeText));

            }
        };
        timer.schedule(task, 0, 1000);
    }


    private void playAudio(AudioScene audioScene, int loopCount) {
        stopAudio();
        if (configData.getAudioMap().containsKey(audioScene)) {
            LogExt.d(TAG, "播放音频:" + audioScene.getDescription());
            Integer audio = configData.getAudioMap().get(audioScene);
            AudioManager mgr = (AudioManager) this
                    .getSystemService(Context.AUDIO_SERVICE);
            // 获取系统声音的当前音量
            float currentVolume = mgr.getStreamVolume(AudioManager.STREAM_MUSIC);
            // 获取系统声音的最大音量
            float maxVolume = mgr.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
            // 获取当前音量的百分比
            float volume = currentVolume / maxVolume;
            // 第一个参数是声效ID,第二个是左声道音量，第三个是右声道音量，第四个是流的优先级，最低为0，第五个是是否循环播放，第六个播放速度(1.0 =正常播放,范围0.5 - 2.0)
            soundPool.play(audio, volume, volume, 1, loopCount, 1f);
        }
    }

    private void stopAudio() {
        LogExt.d(TAG, "停止播放音频");
        soundPool.stop(1);
    }

    private void showVideoView() {
        LogExt.d(TAG, "显示视频播放组件");
        VideoFragment videoFragment = new VideoFragment();
        videoFragment.setVideoUri(configData.getVideoUri());
        replaceView(videoFragment);
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
        if (t != null) {
            if (t instanceof IFragmentOperation) {
                ((IFragmentOperation) (t)).init();
            }
        }
    }

    /**
     * 显示主界面
     */
    private void ShowHome() {
        LogExt.d(TAG, "显示主界面");

        RuntimeContext.CurrentAuthFlag = AIAuthFlag.FAILURE;

        showVideoView();
        this.startProcessButton.setVisibility(View.VISIBLE);

//        APP使用完成后，APP会向中心算法控制器发送【投递完成】消息
        MessageExchange.sendFinisheProcess();
    }

    /**
     * 显示选择身份验证的界面
     */
    private void ShowChooseAuthType() {

        //在点击开始投递后， APP向中心算法控制器发送【开始投递】消息
        MessageExchange.sendStartProcess();

        LogExt.d(TAG, "显示选择身份验证的界面");
        AuthFragment authFragment = new AuthFragment();
        authFragment.setRTSPVideoUrl(configData.getAiFaceRTSPUrl());
        replaceView(authFragment);
        this.startProcessButton.setVisibility(View.INVISIBLE);
    }


    /**
     * 显示垃圾分类检测的界面
     */
    private void ShowGarbageDetect() {
        LogExt.d(TAG, "显示垃圾分类检测的界面");
        GarbageFragment garbageFragment = new GarbageFragment();
        replaceView(garbageFragment);
        this.startProcessButton.setVisibility(View.INVISIBLE);
    }

    /**
     * 显示关门状态界面
     */
    private void ShowGateState() {
        LogExt.d(TAG, "显示关门状态界面");
        GateFragment gateFragment = new GateFragment();
        replaceView(gateFragment);
        this.startProcessButton.setVisibility(View.INVISIBLE);
    }


    /**
     * 在主线程里面处理消息并更新UI界面
     *
     * @param appStatus
     */
    private void setAppStatusView(AppStatus appStatus) {
        LogExt.d(TAG, "更新运行状态：" + appStatus.getValue() + appStatus.getDescription());
        appStatusTextView.setText(appStatus.getDescription());
    }

    /**
     * ThreadMode设置为MAIN，事件的处理会在UI线程中执行，用TextView来展示收到的事件消息
     *
     * @param messageEvent
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void OnMessageEvent(MessageEvent messageEvent) {

        if (messageEvent.getMsgCode() != MessageCode.TIME_UPDATE) {
            LogExt.d(TAG, "OnMessageEvent: " + messageEvent.toString());
        }

        MessageCode code = messageEvent.getMsgCode();
        Object obj = messageEvent.getObject();
        switch (code) {
            case TIME_UPDATE:
                String nowTimeText = obj.toString();
                nowTimeTextView.setText(nowTimeText);
                break;
            case APP_STATUS_UPDATE:
//                String text = obj.toString();
//                setAppStatusView(text);
                break;
            case HOME:
            case GATE_PASS:
                ShowHome();

                break;
            case HOME_SLEEP:

                break;

            case AUDIO_PLAY:
                AudioScene audioScene = (AudioScene) (obj);

                playAudio(audioScene, 0);
                break;
            case AUDIO_PLAY_LOOP:
                AudioScene audioScene_loop = (AudioScene) (obj);
                playAudio(audioScene_loop, 99);
                break;
            case AUDIO_STOP:
                stopAudio();
                break;

            case AUTH_PASS:
                ShowGarbageDetect();
                break;
            case GARBAGE_PASS:
                ShowGateState();
                break;


            case UNKNOWN:
                LogExt.d(TAG, "handleMessage: " + code);
            default:
                break;

        }
    }


    /**
     * ThreadMode设置为MAIN，事件的处理会在UI线程中执行，用TextView来展示收到的事件消息
     *
     * @param dataEvent
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void OnAXDataEventMessage(AppMessage dataEvent) {

        LogExt.d(TAG, "OnAXDataEventMessage: " + dataEvent.toString());

        switch (dataEvent.getMessageType()) {
            case HAND_SHAKE_RESP:
                AIGarbageResultType garbageType = AIGarbageResultType.valueOf(dataEvent.getExtData());
                this.textViewSupportGarbageType.setText(garbageType.getDescription());
                RuntimeContext.CurrentGarbageType = garbageType;
                break;
            case SYS_STATUS_REQ:
                setAppStatusView(AppStatus.valueOf(dataEvent.getPayload()));
                break;
            default:
                break;
        }
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void OnLogEventMessage(LogEvent logEvent) {

        if (this.item_debug_textview_log != null && this.item_debug_textview_log.getVisibility() == View.VISIBLE) {
            this.item_debug_textview_log.append("\n");
            this.item_debug_textview_log.append(logEvent.getData());
            int offset = this.item_debug_textview_log.getLineCount() * this.item_debug_textview_log.getLineHeight();
            if (offset > this.item_debug_textview_log.getHeight()) {
                this.item_debug_textview_log.scrollTo(0, offset - this.item_debug_textview_log.getHeight());
            }
        }
    }


}