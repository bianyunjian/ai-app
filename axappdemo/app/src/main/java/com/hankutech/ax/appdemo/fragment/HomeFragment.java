package com.hankutech.ax.appdemo.fragment;

import android.app.Fragment;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.hankutech.ax.appdemo.R;
import com.hankutech.ax.appdemo.code.AppStatus;
import com.hankutech.ax.appdemo.code.MessageCode;
import com.hankutech.ax.appdemo.data.ConfigData;
import com.hankutech.ax.appdemo.event.MessageEvent;
import com.hankutech.ax.appdemo.util.LogExt;
import com.hankutech.ax.appdemo.view.CustomImageTextView;
import com.hankutech.ax.appdemo.view.IconViewItem;
import com.hankutech.ax.message.code.AIGarbageResultType;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.Date;

public class HomeFragment extends Fragment implements IFragmentOperation {

    private static final String TAG = "HomeFragment";
    private Uri videoUri;
    private View view;
    private VideoFragment currentFragment;
    private Button startProcessButton;
    private String title;
    private AIGarbageResultType garbageResultType;
    private AppStatus appStatus;


    public void setArguments(Uri video, String title, AIGarbageResultType garbageResultType, AppStatus appStatus) {
        setVideoUri(video);
        setTitle(title);
        this.garbageResultType = garbageResultType;
        this.appStatus = appStatus;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        this.view = inflater.inflate(R.layout.fragment_home, container, false);
        return this.view;
    }

    @Override
    public void onResume() {
        super.onResume();

        LogExt.d(TAG, "显示视频播放组件");
        VideoFragment videoFragment = new VideoFragment();
        videoFragment.setVideoUri(this.videoUri);
        getFragmentManager().beginTransaction()
                .replace(R.id.videoview_container, videoFragment).commit();
        getFragmentManager().beginTransaction().show(videoFragment);
        this.currentFragment = videoFragment;
        if (videoFragment != null) {
            if (videoFragment instanceof IFragmentOperation) {
                ((IFragmentOperation) (videoFragment)).init();
            }
        }

        initControls();

    }

    private void initControls() {

        startProcessButton = this.view.findViewById(R.id.btn_start_process);
        startProcessButton.setOnClickListener((t) -> {
            EventBus.getDefault().post(new MessageEvent(MessageCode.PROCESS_START, null));
        });
        updateDayWelcome(new Date());
        updateGarbageType(garbageResultType);
        updateAppStatus(appStatus);

    }

    public void updateAppStatus(AppStatus appStatus) {

        switch (appStatus) {
            case NORMAL:
                startProcessButton.setEnabled(true);
                startProcessButton.setText(appStatus.getDescription());

                break;
            case BUSY:
            case MAINTAIN:
            case ERROR:
                startProcessButton.setEnabled(false);
                startProcessButton.setText(appStatus.getDescription());

                break;
        }
    }

    public void updateDayWelcome(Date date) {
        TextView tv = this.view.findViewById(R.id.textView_day_welcome);

        int hour = date.getHours();
        if (hour < 11) {
            tv.setText("早上好,");
        }
        if (hour >= 11 && hour < 15) {
            tv.setText("中午好,");
        }
        if (hour >= 15 && hour < 18) {
            tv.setText("下午好,");
        }
        if (hour >= 18) {
            tv.setText("晚上好,");
        }
    }

    public void updateGarbageType(AIGarbageResultType garbageType) {

        String currentGarbageTypeDesc = "当前可投递垃圾种类 — " + garbageType.getDescription();
        TextView tv = this.view.findViewById(R.id.textView_current_garbagetype);
        tv.setText(currentGarbageTypeDesc);

        LinearLayout layout = this.view.findViewById(R.id.layout_current_garbagetype);
        layout.setBackgroundResource(getBackgroundResource(garbageType));
        ImageView imageView = this.view.findViewById(R.id.imageView_current_garbagetype);
        imageView.setImageResource(getImageResource(garbageType));
        startProcessButton.setTextColor(getColor(garbageType));

//        setIconList
        LinearLayout layout_icon_list = (LinearLayout) (this.view.findViewById(R.id.layout_icon_list));
        layout_icon_list.setBackgroundColor(getIconListBackgroundColor(garbageType));
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.gravity = Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL;
        layoutParams.leftMargin = 30;
        ImageView icon_person = new ImageView(this.view.getContext());
        icon_person.setLayoutParams(layoutParams);
        icon_person.setScaleType(ImageView.ScaleType.CENTER_CROP);
        icon_person.setImageResource(getPersonIcon(garbageType));
        layout_icon_list.addView(icon_person);


        Typeface mTypeface = Typeface.createFromAsset(this.getActivity().getAssets(), "msyhbold.ttf");
        LinearLayout.LayoutParams layoutParams_icon = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        layoutParams_icon.gravity = Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL;
        layoutParams_icon.leftMargin = 60;

        ArrayList<IconViewItem> iconList = getIconList(garbageType);
        if (iconList.size() > 0) {
            for (IconViewItem iconViewItem : iconList
            ) {
                CustomImageTextView newIconView = new CustomImageTextView(this.view.getContext());

                newIconView.setLayoutParams(layoutParams_icon);
                newIconView.setText(iconViewItem.getImgResId());
                newIconView.setImgResource(iconViewItem.getTextResId());
                newIconView.setTypeface(mTypeface);
                layout_icon_list.addView(newIconView);
            }
        }

    }

    private int getPersonIcon(AIGarbageResultType garbageType) {
        switch (garbageType) {
            case DRY:
                return R.drawable.home_person_dry;
            case WET:
                return R.drawable.home_person_wet;
            case RECYCLABLE:
                return R.drawable.home_person_recyclable;
            case HAZARDOUS:
                return R.drawable.home_person_hazardous;
            case OTHERS:
                return R.drawable.home_person_others;
            default:
                break;
        }
        return R.drawable.home_person_others;
    }

    private int getColor(AIGarbageResultType garbageType) {

        switch (garbageType) {
            case DRY:
                return Color.rgb(0x7c, 0x7c, 0x7c);
            case WET:
                return Color.rgb(0x61, 0xc9, 0x74);
            case RECYCLABLE:
                return Color.rgb(0x33, 0xac, 0xee);
            case HAZARDOUS:
                return Color.rgb(0xff, 0x58, 0x46);
            case OTHERS:
                return Color.BLACK;
            default:
                break;
        }
        return Color.BLACK;
    }

    private int getImageResource(AIGarbageResultType garbageType) {
        switch (garbageType) {
            case DRY:
                return R.drawable.icon_dry;
            case WET:
                return R.drawable.icon_wet;
            case RECYCLABLE:
                return R.drawable.icon_recyclable;
            case HAZARDOUS:
                return R.drawable.icon_hazardous;
            case OTHERS:
                return R.drawable.icon_others;
            default:
                break;
        }
        return R.drawable.icon_others;

    }

    private int getBackgroundResource(AIGarbageResultType garbageType) {
        switch (garbageType) {
            case DRY:
                return R.drawable.home_garbagetype_dry_background;
            case WET:

                return R.drawable.home_garbagetype_wet_background;

            case RECYCLABLE:
                return R.drawable.home_garbagetype_recyclable_background;
            case HAZARDOUS:
                return R.drawable.home_garbagetype_hazardous_background;
            case OTHERS:
                break;
            default:
                break;
        }
        return R.drawable.home_garbagetype_others_background;

    }

    private ArrayList<IconViewItem> getIconList(AIGarbageResultType garbageType) {
        ArrayList<IconViewItem> list = new ArrayList<>();

        switch (garbageType) {
            case DRY:
                list.add(new IconViewItem(R.string.icon_dry_cwfb, R.drawable.icon_dry_cwfb));
                list.add(new IconViewItem(R.string.icon_dry_yt, R.drawable.icon_dry_yt));
                list.add(new IconViewItem(R.string.icon_dry_wrzz, R.drawable.icon_dry_wrzz));
                list.add(new IconViewItem(R.string.icon_dry_hthc, R.drawable.icon_dry_hthc));
                list.add(new IconViewItem(R.string.icon_dry_pjtcp, R.drawable.icon_dry_pjtcp));
                list.add(new IconViewItem(R.string.icon_dry_ycxcj, R.drawable.icon_dry_ycxcj));
                break;
            case WET:

                list.add(new IconViewItem(R.string.icon_wet_czly, R.drawable.icon_wet_czly));
                list.add(new IconViewItem(R.string.icon_wet_ggnz, R.drawable.icon_wet_ggnz));
                list.add(new IconViewItem(R.string.icon_wet_cgcy, R.drawable.icon_wet_cgcy));
                list.add(new IconViewItem(R.string.icon_wet_ggp, R.drawable.icon_wet_ggp));
                list.add(new IconViewItem(R.string.icon_wet_cyzz, R.drawable.icon_wet_cyzz));
                list.add(new IconViewItem(R.string.icon_wet_sfsc, R.drawable.icon_wet_sfsc));
                break;

            case RECYCLABLE:
                list.add(new IconViewItem(R.string.icon_recyclable_slbz, R.drawable.icon_recyclable_slbz));
                list.add(new IconViewItem(R.string.icon_recyclable_js, R.drawable.icon_recyclable_js));
                list.add(new IconViewItem(R.string.icon_recyclable_sl, R.drawable.icon_recyclable_sl));
                list.add(new IconViewItem(R.string.icon_recyclable_bl, R.drawable.icon_recyclable_bl));
                list.add(new IconViewItem(R.string.icon_recyclable_yzb, R.drawable.icon_recyclable_yzb));
                list.add(new IconViewItem(R.string.icon_recyclable_zl, R.drawable.icon_recyclable_zl));
                break;
            case HAZARDOUS:
                list.add(new IconViewItem(R.string.icon_ha_dc, R.drawable.icon_ha_dc));
                list.add(new IconViewItem(R.string.icon_ha_yq, R.drawable.icon_ha_yq));
                list.add(new IconViewItem(R.string.icon_ha_xdyp, R.drawable.icon_ha_xdyp));
                list.add(new IconViewItem(R.string.icon_ha_yp, R.drawable.icon_ha_yp));
                list.add(new IconViewItem(R.string.icon_ha_scj, R.drawable.icon_ha_scj));
                list.add(new IconViewItem(R.string.icon_ha_dg, R.drawable.icon_ha_dg));
                break;
            case OTHERS:
                break;
            default:
                break;
        }


        return list;
    }

    private int getIconListBackgroundColor(AIGarbageResultType garbageType) {

        switch (garbageType) {
            case DRY:
                return Color.rgb(0xF5, 0xF5, 0xF5);
            case WET:
                return Color.rgb(0xF4, 0xFA, 0xF5);
            case RECYCLABLE:
                return Color.rgb(0xF6, 0xfc, 0xff);
            case HAZARDOUS:
                return Color.rgb(0xff, 0xEF, 0xED);
            case OTHERS:
                return Color.WHITE;
            default:
                break;
        }
        return Color.WHITE;
    }

    public void setVideoUri(Uri videoUri) {
        this.videoUri = videoUri;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public void init() {
    }

    public void release() {
        if (this.currentFragment != null) {
            if (this.currentFragment instanceof IFragmentOperation) {
                ((IFragmentOperation) (this.currentFragment)).release();
            }
        }
    }


    public void updateTitle(ConfigData configData) {
        this.title = configData.getLogoTitle();
        TextView tv = view.findViewById(R.id.home_textview_title);
        tv.setText(this.title);

    }

    public void updateVideoUri(ConfigData configData) {
        this.videoUri = configData.getVideoUri();
        this.currentFragment.setVideoUri(this.videoUri);
    }
}

