package com.hankutech.ax.appdemo.util;

import android.os.CountDownTimer;
import android.util.Log;

import java.util.function.Consumer;

import lombok.Data;


public class TickTimer {
    private static final String TAG = "TickTimer";
    private long countDownInterval;
    private long totalMillis;
    private CountDownTimer timer;
    private Consumer<Long> onTickConsumer;
    private Consumer onFinishConsumer;

    public void start(long totalMillis, long countDownInterval, Consumer<Long> onTick, Consumer onFinish) {
        this.totalMillis = totalMillis;
        this.countDownInterval = countDownInterval;
        this.onTickConsumer = onTick;
        this.onFinishConsumer = onFinish;

        this.timer = new CountDownTimer(this.totalMillis, this.countDownInterval) {

            @Override
            public void onTick(long millisUntilFinished) {

                LogExt.d(TAG, "倒计时(秒): " + millisUntilFinished / 1000);
                if (onTickConsumer != null) {
                    onTickConsumer.accept(millisUntilFinished / 1000);
                }
            }

            @Override
            public void onFinish() {
                LogExt.d(TAG, "done!");
                if (onFinishConsumer != null) {
                    onFinishConsumer.accept(null);
                }
            }
        };
        this.timer.start();
    }

    public void cancel() {
        if (this.timer != null) this.timer.cancel();
    }

    public void reset() {
        cancel();
        start(this.totalMillis, this.countDownInterval, this.onTickConsumer, this.onFinishConsumer);
    }

    public void resetWithNewMillis(long totalMillis, long countDownInterval) {
        cancel();
        this.totalMillis = totalMillis;
        this.countDownInterval = countDownInterval;
        start(this.totalMillis, this.countDownInterval, this.onTickConsumer, this.onFinishConsumer);
    }


}
