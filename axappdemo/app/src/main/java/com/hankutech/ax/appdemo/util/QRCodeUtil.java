package com.hankutech.ax.appdemo.util;

import android.graphics.Bitmap;
import android.net.wifi.aware.Characteristics;
import android.os.Build;

import androidx.annotation.RequiresApi;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.Hashtable;

public class QRCodeUtil {


    /**
     * 生成二维码图片
     * @param content 文本内容，可以为url
     * @param width 宽度
     * @param height 高度
     * @return {@link Bitmap}
     */
    public static Bitmap encodeBitmap(String content, int width, int height) {
        //todo
        if (null == content || "".equals(content)) {
            // todo 返回一个默认的二维码图片？
            return null;
        }
        Hashtable<EncodeHintType, Object> hints = new Hashtable<>();
        // 排错率：L<M<Q<H
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
        hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
        // 外边距
        hints.put(EncodeHintType.MARGIN, 1);
        BitMatrix bitMatrix = null;
        try {
            bitMatrix = new MultiFormatWriter().encode(content, BarcodeFormat.QR_CODE, width, height, hints);
        } catch (WriterException e) {
            e.printStackTrace();
        }
        // 将矩阵转换结果横列扫描出来
        int[] pixels = new int[width * height];
        for (int y=0; y<height; y++) {
            for (int x=0; x<width; x++) {
                if (bitMatrix.get(x, y)) {
                    pixels[y * width + x] = 0xff000000;
                } else {
                    pixels[y * width + x] = 0xffffffff;
                }
            }
        }
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height);

        return bitmap;
    }

}
