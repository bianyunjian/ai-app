package com.hankutech.ax.appdemo.view;

import lombok.Data;

@Data
public class IconViewItem {
    int imgResId;
    int textResId;

    public IconViewItem(int img, int text) {
        imgResId = img;
        textResId = text;
    }
}
