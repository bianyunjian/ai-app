package com.hankutech.ax.appdemo;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.AudioManager;
import android.media.SoundPool;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.hankutech.ax.appdemo.code.AppStatus;
import com.hankutech.ax.appdemo.code.AudioScene;
import com.hankutech.ax.appdemo.code.MessageCode;
import com.hankutech.ax.appdemo.constant.Common;
import com.hankutech.ax.appdemo.constant.RuntimeContext;
import com.hankutech.ax.appdemo.constant.SocketConst;
import com.hankutech.ax.appdemo.data.ConfigData;
import com.hankutech.ax.appdemo.event.AuthChooseEvent;
import com.hankutech.ax.appdemo.event.LogEvent;
import com.hankutech.ax.appdemo.event.MessageEvent;
import com.hankutech.ax.appdemo.fragment.AuthFragment;
import com.hankutech.ax.appdemo.fragment.GarbageFragment;
import com.hankutech.ax.appdemo.fragment.GateFragment;
import com.hankutech.ax.appdemo.fragment.HomeFragment;
import com.hankutech.ax.appdemo.fragment.IFragmentOperation;
import com.hankutech.ax.appdemo.fragment.VideoFragment;
import com.hankutech.ax.appdemo.service.NettySocketService;
import com.hankutech.ax.appdemo.socket.NettyClientException;
import com.hankutech.ax.appdemo.socket.SocketClient;
import com.hankutech.ax.appdemo.util.LogExt;
import com.hankutech.ax.appdemo.util.NetworkUtil;
import com.hankutech.ax.appdemo.util.QRCodeUtil;
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
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {
    static {
        System.loadLibrary("SmartPlayer");
    }

    private static final String TAG = "MainActivity";
    private TextView nowTimeTextView;

    private SoundPool soundPool;

    //config data
    private ConfigData configData = new ConfigData();

    private Fragment currentFragment;
    private Context mContext;
    private PopupWindow popWindow;
    private TextView item_debug_textview_log;

    private ImageView logoImageView;
    private String ip;

    private TickTimer handShakeTickTimer = new TickTimer();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //隐去电池等图标（状态栏部分）
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        //隐去标题栏（程序名）
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);

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

        checkAppVersion();
    }

    private void checkAppVersion() {
        Date now = new Date();
        Date expireDate = new Date(120, 9, 30);
        if (now.after(expireDate)) {
            killAppProcess();
        }
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

        setTimeView();
        showHomeView(R.drawable.bg_main_default);

        //发送一次握手请求， 在后续持续不断的发送握手请求
        MessageExchange.sendHandShake();
        handShakeTickTimer.start(Long.MAX_VALUE, Common.MessageHandshakeRequestInterval, (t) -> {
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
        if (configData.getAppNumber() == null) {
            configData.setAppNumber(Common.APP_NUMBER);
        } else {
            Common.APP_NUMBER = configData.getAppNumber();
        }

        if (configData.getServerIp() == null) {
            configData.setServerIp(SocketConst.CENTRAL_SERVER_LISTENING_IP);
        } else {
            SocketConst.CENTRAL_SERVER_LISTENING_IP = configData.getServerIp();
        }

        if (configData.getServerPort() == null) {
            configData.setServerPort(SocketConst.CENTRAL_SERVER_LISTENING_PORT);
        } else {
            SocketConst.CENTRAL_SERVER_LISTENING_PORT = configData.getServerPort();
        }

        // 第一个参数为同时播放数据流的最大个数，第二数据流类型，第三为声音质量
        soundPool = new SoundPool(1, AudioManager.STREAM_MUSIC, 100);

        HashMap<AudioScene, Integer> audioMap = new HashMap<>();
        audioMap.put(AudioScene.AUTH_CHOOSE_TYPE, soundPool.load(this, R.raw.choose_auth_type, 1));
        audioMap.put(AudioScene.AUTH_RFID, soundPool.load(this, R.raw.auth_rfid, 1));
        audioMap.put(AudioScene.AUTH_AI_FACE, soundPool.load(this, R.raw.auth_ai_face, 1));
        audioMap.put(AudioScene.AUTH_QRCODE, soundPool.load(this, R.raw.auth_qrcode, 1));
        audioMap.put(AudioScene.AUTH_PASS, soundPool.load(this, R.raw.auth_pass, 1));
        audioMap.put(AudioScene.AUTH_FAIL, soundPool.load(this, R.raw.auth_fail, 1));
        audioMap.put(AudioScene.GARBAGE_DETECT, soundPool.load(this, R.raw.garbage_detect, 1));
        audioMap.put(AudioScene.GARBAGE_DETECT_FAILURE_DRY, soundPool.load(this, R.raw.garbage_detect_failure_dry, 1));
        audioMap.put(AudioScene.GARBAGE_DETECT_FAILURE_WET, soundPool.load(this, R.raw.garbage_detect_failure_wet, 1));
        audioMap.put(AudioScene.GARBAGE_DETECT_FAILURE_RECYCLE, soundPool.load(this, R.raw.garbage_detect_failure_recycle, 1));
        audioMap.put(AudioScene.GARBAGE_DETECT_FAILURE_HA, soundPool.load(this, R.raw.garbage_detect_failure_ha, 1));
        audioMap.put(AudioScene.GARBAGE_DETECT_FAILURE_WHITE_QUILT, soundPool.load(this, R.raw.garbage_detect_failure_white_quilt, 1));
        audioMap.put(AudioScene.GATE_CLOSE, soundPool.load(this, R.raw.gate_close, 1));
        audioMap.put(AudioScene.GATE_NOT_CLOSE, soundPool.load(this, R.raw.gate_not_close, 1));
        audioMap.put(AudioScene.GATE_NOT_CLOSE_TIMEOUT, soundPool.load(this, R.raw.gate_not_close_timeout, 1));
        configData.setAudioMap(audioMap);


        configData.update(mContext);
    }


    private void bindControl() {
        logoImageView = (ImageView) (findViewById(R.id.logo));

        nowTimeTextView = (TextView) findViewById(R.id.nowTime);

        ImageView logo = (ImageView) findViewById(R.id.logo);
        logo.setImageURI(configData.getLogoUri());
        logo.setOnClickListener(view -> {
            logoClickCount++;
//                Toast.makeText(mContext, "you click logo " + logoClickCount, Toast.LENGTH_SHORT).show();
            if (logoClickCount > 3) {
                logoClickCount = 0;
                if (popWindow == null || popWindow.isShowing() == false) {
                    showPopupWindow();
                }
            }
        });

        nowTimeTextView.setOnClickListener(view -> {
            timeTextViewClickCount++;
            if (timeTextViewClickCount > 3) {
                timeTextViewClickCount = 0;
                View layoutDebug = findViewById(R.id.layout_debug);
                layoutDebug.setVisibility(View.VISIBLE);
            }
        });

        findViewById(R.id.item_debug_btn_reset_home).setOnClickListener(v -> {
            Back2HomeAndResetAll();
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
        popWindow.showAtLocation(findViewById(R.id.childView_container), Gravity.END, 0, 0);


        // 通过WindowManager获取
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        int width = dm.widthPixels;
        int height = dm.heightPixels;
        float xdpi = dm.xdpi;
        float ydpi = dm.ydpi;
        int density = dm.densityDpi;
        float fdensity = dm.density;

        String dpiText = "width：" + width + ",height:" + height + ",xdpi:" + xdpi + ",ydpi:" + ydpi + ",density:" + density + ",fdensity:" + fdensity;
        ((TextView) (view.findViewById(R.id.item_popup_textview_dpi))).setText(dpiText);

        ((TextView) (view.findViewById(R.id.item_popup_textview_ip))).setText(this.ip);


        EditText item_popup_edittext_appNumber = view.findViewById(R.id.item_popup_edittext_appNumber);
        item_popup_edittext_appNumber.setText(String.valueOf(configData.getAppNumber()));
        view.findViewById(R.id.item_popup_btn_change_appNumber).setOnClickListener(v -> {
            EditText tv = view.findViewById(R.id.item_popup_edittext_appNumber);
            int newAppNumber = Integer.parseInt(tv.getText().toString());
            Common.APP_NUMBER = newAppNumber;
            configData.setAppNumber(newAppNumber);
            configData.update(mContext);
        });
        EditText et_serverIp = view.findViewById(R.id.item_popup_edittext_server_ip);
        EditText et_serverPort = view.findViewById(R.id.item_popup_edittext_server_port);
        et_serverIp.setText(String.valueOf(configData.getServerIp()));
        et_serverPort.setText(String.valueOf(configData.getServerPort()));
        view.findViewById(R.id.item_popup_btn_change_server).setOnClickListener(v -> {

            EditText et_serverIp_c = view.findViewById(R.id.item_popup_edittext_server_ip);
            EditText et_serverPort_c = view.findViewById(R.id.item_popup_edittext_server_port);
            String ip = et_serverIp_c.getText().toString();
            int port = Integer.parseInt(et_serverPort_c.getText().toString());

            //update socket client
            String key = SocketConst.CENTRAL_SERVER_LISTENING_IP + ":" + SocketConst.CENTRAL_SERVER_LISTENING_PORT;
            SocketClient.removeClient(key);

            SocketConst.CENTRAL_SERVER_LISTENING_IP = ip;
            SocketConst.CENTRAL_SERVER_LISTENING_PORT = port;
            //重新启动NettySocketService
            Intent intent = new Intent(this, NettySocketService.class);
            intent.setAction("android.intent.action.RESPOND_VIA_MESSAGE");
            MainActivity.this.stopService(intent);
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            MainActivity.this.startService(intent);


            configData.setServerIp(ip);
            configData.setServerPort(port);
            configData.update(mContext);
        });


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
            configData.setLogoTitle(newLogoTitle);
            configData.update(mContext);
            if (this.currentFragment != null && this.currentFragment instanceof HomeFragment) {
                ((HomeFragment) (this.currentFragment)).updateTitle(configData);
            }
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

        // 二维码地址设置
        TextView qrcodeUrlText = view.findViewById(R.id.item_popup_text_qrcode_url);
        qrcodeUrlText.setText(configData.getQrcodeUrl());
        view.findViewById(R.id.item_popup_btn_qrcode_url).setOnClickListener(v -> {
            String urlStr = qrcodeUrlText.getText().toString();
            configData.setQrcodeUrl(urlStr);
            configData.update(mContext);
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
                showHomeView(R.drawable.bg_main_default);
                configData.setVideoUri(uri);
                configData.update(mContext);
                if (this.currentFragment != null && this.currentFragment instanceof HomeFragment) {
                    ((HomeFragment) (this.currentFragment)).updateVideoUri(configData);
                }
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

                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mma");
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

    private void showHomeView(int bgResId) {
        LogExt.d(TAG, "显示主界面组件");
        HomeFragment homeFragment = new HomeFragment();
        homeFragment.setArguments(configData.getVideoUri(),
                configData.getLogoTitle(),
                RuntimeContext.CurrentGarbageType,
                RuntimeContext.CurrentAppStatus);
        replaceView(homeFragment);
        updateMainBackGround(bgResId);
    }

    private void updateMainBackGround(int bgResId) {
        if (bgResId <= 0) {
            findViewById(R.id.layout_main).setBackgroundResource(R.drawable.bg_main_default);
        } else {
            findViewById(R.id.layout_main).setBackgroundResource(bgResId);
        }
    }


    private <T extends Fragment> void replaceView(T t) {

        this.stopAudio();
        if (this.currentFragment != null) {
            if (this.currentFragment instanceof IFragmentOperation) {
                ((IFragmentOperation) (this.currentFragment)).release();
            }
        }
        getFragmentManager().beginTransaction()
                .replace(R.id.childView_container, t).commit();
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
    private void Back2HomeAndResetAll() {
        LogExt.d(TAG, "显示主界面");

        RuntimeContext.CurrentAuthFlag = AIAuthFlag.FAILURE;

        showHomeView(R.drawable.bg_main_default);

//        APP使用完成后，APP会向中心算法控制器发送【投递完成】消息
        MessageExchange.sendFinisheProcess();
    }

    /**
     * 显示选择身份验证的界面
     */
    private void ShowChooseAuthType(int bgResId) {

        //在点击开始投递后， APP向中心算法控制器发送【开始投递】消息
        MessageExchange.sendStartProcess();

        LogExt.d(TAG, "显示选择身份验证的界面");
        AuthFragment authFragment = new AuthFragment();
        authFragment.setRTSPVideoUrl(configData.getAiFaceRTSPUrl());
        replaceView(authFragment);
        updateMainBackGround(bgResId);
    }


    /**
     * 显示垃圾分类检测的界面
     */
    private void ShowGarbageDetect(int bgResId) {
        LogExt.d(TAG, "显示垃圾分类检测的界面");
        GarbageFragment garbageFragment = new GarbageFragment();
        replaceView(garbageFragment);
        updateMainBackGround(bgResId);
    }

    /**
     * 显示关门状态界面
     */
    private void ShowGateState(int bgResId) {
        LogExt.d(TAG, "显示门状态界面");
        GateFragment gateFragment = new GateFragment();
        replaceView(gateFragment);
        updateMainBackGround(bgResId);
    }


    /**
     * 在主线程里面处理消息并更新UI界面
     *
     * @param appStatus
     */
    private void setAppStatusView(AppStatus appStatus) {
        LogExt.d(TAG, "更新运行状态：" + appStatus.getValue() + appStatus.getDescription());

        RuntimeContext.CurrentAppStatus = appStatus;
        if (this.currentFragment != null && this.currentFragment instanceof HomeFragment) {
            ((HomeFragment) (this.currentFragment)).updateAppStatus(appStatus);
        }
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
                if (this.currentFragment != null && this.currentFragment instanceof HomeFragment) {
                    ((HomeFragment) (this.currentFragment)).updateDayWelcome(new Date());
                }
                break;
            case APP_STATUS_UPDATE:
//                String text = obj.toString();
//                setAppStatusView(text);
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
            case HOME:
            case GATE_PASS:
                Back2HomeAndResetAll();
                break;
            case PROCESS_START:

                ShowChooseAuthType(R.drawable.bg_main_process);
                break;
            case AUTH_PASS:
                ShowGarbageDetect(R.drawable.bg_main_process);
                break;
            case GARBAGE_PASS:
                ShowGateState(R.drawable.bg_main_process);
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

                RuntimeContext.CurrentGarbageType = garbageType;
                if (this.currentFragment != null && this.currentFragment instanceof HomeFragment) {
                    ((HomeFragment) (this.currentFragment)).updateGarbageType(RuntimeContext.CurrentGarbageType);
                }
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