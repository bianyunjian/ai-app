package com.daniulive.smartplayer;

import android.net.wifi.aware.DiscoverySession;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.eventhandle.NTSmartEventCallbackV2;
import com.eventhandle.NTSmartEventID;

public class EventHandeV2 implements NTSmartEventCallbackV2 {

    private static final String TAG = "EventHandeV2";
    public static final int PLAYER_EVENT_MSG = 1;
    private Handler handler;

    @Override
    public void onNTSmartEventCallbackV2(long handle, int id, long param1,
                                         long param2, String param3, String param4, Object param5) {

        //Log.i(TAG, "EventHandeV2: handle=" + handle + " id:" + id);

        String player_event = "";

        switch (id) {
            case NTSmartEventID.EVENT_DANIULIVE_ERC_PLAYER_STARTED:
                player_event = "开始..";
                break;
            case NTSmartEventID.EVENT_DANIULIVE_ERC_PLAYER_CONNECTING:
                player_event = "连接中..";
                break;
            case NTSmartEventID.EVENT_DANIULIVE_ERC_PLAYER_CONNECTION_FAILED:
                player_event = "连接失败..";
                break;
            case NTSmartEventID.EVENT_DANIULIVE_ERC_PLAYER_CONNECTED:
                player_event = "连接成功..";
                break;
            case NTSmartEventID.EVENT_DANIULIVE_ERC_PLAYER_DISCONNECTED:
                player_event = "连接断开..";
                break;
            case NTSmartEventID.EVENT_DANIULIVE_ERC_PLAYER_STOP:
                player_event = "停止播放..";
                break;
            case NTSmartEventID.EVENT_DANIULIVE_ERC_PLAYER_RESOLUTION_INFO:
                player_event = "分辨率信息: width: " + param1 + ", height: " + param2;
                break;
            case NTSmartEventID.EVENT_DANIULIVE_ERC_PLAYER_NO_MEDIADATA_RECEIVED:
                player_event = "收不到媒体数据，可能是url错误..";
                break;
            case NTSmartEventID.EVENT_DANIULIVE_ERC_PLAYER_SWITCH_URL:
                player_event = "切换播放URL..";
                break;
            case NTSmartEventID.EVENT_DANIULIVE_ERC_PLAYER_CAPTURE_IMAGE:
                player_event = "快照: " + param1 + " 路径：" + param3;

                if (param1 == 0) {
                    player_event = player_event + ", 截取快照成功";
                } else {
                    player_event = player_event + ", 截取快照失败";
                }
                break;

            case NTSmartEventID.EVENT_DANIULIVE_ERC_PLAYER_RECORDER_START_NEW_FILE:
                player_event = "[record]开始一个新的录像文件 : " + param3;
                break;
            case NTSmartEventID.EVENT_DANIULIVE_ERC_PLAYER_ONE_RECORDER_FILE_FINISHED:
                player_event = "[record]已生成一个录像文件 : " + param3;
                break;

            case NTSmartEventID.EVENT_DANIULIVE_ERC_PLAYER_START_BUFFERING:
                Log.i(TAG, "Start Buffering");
                break;

            case NTSmartEventID.EVENT_DANIULIVE_ERC_PLAYER_BUFFERING:
                Log.i(TAG, "Buffering:" + param1 + "%");
                break;

            case NTSmartEventID.EVENT_DANIULIVE_ERC_PLAYER_STOP_BUFFERING:
                Log.i(TAG, "Stop Buffering");
                break;

            case NTSmartEventID.EVENT_DANIULIVE_ERC_PLAYER_DOWNLOAD_SPEED:
                player_event = "download_speed:" + param1 + "Byte/s" + ", "
                        + (param1 * 8 / 1000) + "kbps" + ", " + (param1 / 1024)
                        + "KB/s";
                break;

            case NTSmartEventID.EVENT_DANIULIVE_ERC_PLAYER_RTSP_STATUS_CODE:
                Log.e(TAG, "RTSP error code received, please make sure username/password is correct, error code:" + param1);
                player_event = "RTSP error code:" + param1;
                break;

            case NTSmartEventID.EVENT_DANIULIVE_ERC_PLAYER_NEED_KEY:
                Log.e(TAG, "RTMP加密流，请设置播放需要的Key..");
                player_event = "RTMP加密流，请设置播放需要的Key..";
                break;

            case NTSmartEventID.EVENT_DANIULIVE_ERC_PLAYER_KEY_ERROR:
                Log.e(TAG, "RTMP加密流，Key错误，请重新设置..");
                player_event = "RTMP加密流，Key错误，请重新设置..";
                break;
        }

        if (player_event.length() > 0 && handler != null) {
            Log.i(TAG, player_event);
            Message message = new Message();
            message.what = PLAYER_EVENT_MSG;
            message.obj = player_event;
            handler.sendMessage(message);
        }
    }

}
