package com.hankutech.ax.appdemo.view;

import android.os.Handler;

public interface IFragmentOperation {

    void init();

    void release();

    void setHandler(Handler mHandler);
}
