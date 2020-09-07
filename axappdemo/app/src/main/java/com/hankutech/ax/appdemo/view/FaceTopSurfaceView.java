package com.hankutech.ax.appdemo.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import androidx.annotation.NonNull;

public class FaceTopSurfaceView extends SurfaceView implements SurfaceHolder.Callback {

    protected SurfaceHolder sh;
    private int mWidth;
    private int mHeight;

    public FaceTopSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        sh = getHolder();
        sh.addCallback(this);
        sh.setFormat(PixelFormat.TRANSPARENT);
        setZOrderOnTop(true);
    }


    @Override
    public void surfaceCreated(@NonNull SurfaceHolder surfaceHolder) {
        //todo
    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder surfaceHolder, int i, int w, int h) {
        mWidth = w;
        mHeight = h;
    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder surfaceHolder) {

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawColor(Color.TRANSPARENT);
        Paint p = new Paint();
        p.setAntiAlias(true);
        p.setColor(Color.RED);
        p.setStyle(Paint.Style.STROKE);
        //canvas.drawPoint(100.0f, 100.0f, p);
        canvas.drawLine(0,110, 500, 110, p);
        canvas.drawCircle(110, 110, 10.0f, p);
    }

    void clearDraw() {
        Canvas canvas = sh.lockCanvas();
        canvas.drawColor(Color.BLUE);
        sh.unlockCanvasAndPost(canvas);
    }

    public void drawLine()
    {
        Canvas canvas = sh.lockCanvas();
        canvas.drawColor(Color.TRANSPARENT);
        Paint p = new Paint();
        p.setAntiAlias(true);
        p.setColor(Color.RED);
        p.setStyle(Paint.Style.STROKE);
        //canvas.drawPoint(100.0f, 100.0f, p);
        canvas.drawLine(0,110, 500, 110, p);
        canvas.drawCircle(110, 110, 10.0f, p);
        sh.unlockCanvasAndPost(canvas);
    }


}
