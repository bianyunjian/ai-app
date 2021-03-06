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
        //??????????????????????????????????????????
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        //??????????????????????????????
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.activity_main);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        mContext = MainActivity.this;

        checkNetwork();

        //??????NettySocketService
        Intent intent = new Intent(this, NettySocketService.class);
        intent.setAction("android.intent.action.RESPOND_VIA_MESSAGE");
        MainActivity.this.startService(intent);

        //??????????????????
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

        //??????????????????????????? ??????????????????????????????????????????
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
        //????????????????????????
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

        // ??????????????????????????????????????????????????????????????????????????????????????????????????????
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

        //1.????????????PopupWindow???????????????????????????View?????????
        popWindow = new PopupWindow(view,
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT, true);

        //?????????????????????PopupWindow?????????PopupWindow????????????????????????????????????
        //???????????????????????????????????????PopupWindow????????????????????????????????????????????????
        //PopupWindow????????????????????????????????????????????????????????????????????????????????????
        popWindow.setTouchable(true);
        popWindow.setTouchInterceptor((v, event) -> {
            // ??????????????????true?????????touch??????????????????
            // ????????? PopupWindow???onTouchEvent?????????????????????????????????????????????dismiss
            return false;
        });
        popWindow.setBackgroundDrawable(new ColorDrawable(0xdddddddd));    //??????????????????

        //??????popupWindow???????????????????????????????????????View???x??????????????????y???????????????
        popWindow.showAtLocation(findViewById(R.id.childView_container), Gravity.END, 0, 0);


        // ??????WindowManager??????
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        int width = dm.widthPixels;
        int height = dm.heightPixels;
        float xdpi = dm.xdpi;
        float ydpi = dm.ydpi;
        int density = dm.densityDpi;
        float fdensity = dm.density;

        String dpiText = "width???" + width + ",height:" + height + ",xdpi:" + xdpi + ",ydpi:" + ydpi + ",density:" + density + ",fdensity:" + fdensity;
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
            //????????????NettySocketService
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

        // ?????????????????????
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
        //????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
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
            LogExt.d(TAG, "????????????:" + audioScene.getDescription());
            Integer audio = configData.getAudioMap().get(audioScene);
            AudioManager mgr = (AudioManager) this
                    .getSystemService(Context.AUDIO_SERVICE);
            // ?????????????????????????????????
            float currentVolume = mgr.getStreamVolume(AudioManager.STREAM_MUSIC);
            // ?????????????????????????????????
            float maxVolume = mgr.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
            // ??????????????????????????????
            float volume = currentVolume / maxVolume;
            // ????????????????????????ID,???????????????????????????????????????????????????????????????????????????????????????????????????0?????????????????????????????????????????????????????????(1.0 =????????????,??????0.5 - 2.0)
            soundPool.play(audio, volume, volume, 1, loopCount, 1f);
        }
    }

    private void stopAudio() {
        LogExt.d(TAG, "??????????????????");
        soundPool.stop(1);
    }

    private void showHomeView(int bgResId) {
        LogExt.d(TAG, "?????????????????????");
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
     * ???????????????
     */
    private void Back2HomeAndResetAll() {
        LogExt.d(TAG, "???????????????");

        RuntimeContext.CurrentAuthFlag = AIAuthFlag.FAILURE;

        showHomeView(R.drawable.bg_main_default);

//        APP??????????????????APP?????????????????????????????????????????????????????????
        MessageExchange.sendFinisheProcess();
    }

    /**
     * ?????????????????????????????????
     */
    private void ShowChooseAuthType(int bgResId) {

        //??????????????????????????? APP??????????????????????????????????????????????????????
        MessageExchange.sendStartProcess();

        LogExt.d(TAG, "?????????????????????????????????");
        AuthFragment authFragment = new AuthFragment();
        authFragment.setRTSPVideoUrl(configData.getAiFaceRTSPUrl());
        replaceView(authFragment);
        updateMainBackGround(bgResId);
    }


    /**
     * ?????????????????????????????????
     */
    private void ShowGarbageDetect(int bgResId) {
        LogExt.d(TAG, "?????????????????????????????????");
        GarbageFragment garbageFragment = new GarbageFragment();
        replaceView(garbageFragment);
        updateMainBackGround(bgResId);
    }

    /**
     * ????????????????????????
     */
    private void ShowGateState(int bgResId) {
        LogExt.d(TAG, "?????????????????????");
        GateFragment gateFragment = new GateFragment();
        replaceView(gateFragment);
        updateMainBackGround(bgResId);
    }


    /**
     * ???????????????????????????????????????UI??????
     *
     * @param appStatus
     */
    private void setAppStatusView(AppStatus appStatus) {
        LogExt.d(TAG, "?????????????????????" + appStatus.getValue() + appStatus.getDescription());

        RuntimeContext.CurrentAppStatus = appStatus;
        if (this.currentFragment != null && this.currentFragment instanceof HomeFragment) {
            ((HomeFragment) (this.currentFragment)).updateAppStatus(appStatus);
        }
    }

    /**
     * ThreadMode?????????MAIN????????????????????????UI?????????????????????TextView??????????????????????????????
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
     * ThreadMode?????????MAIN????????????????????????UI?????????????????????TextView??????????????????????????????
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