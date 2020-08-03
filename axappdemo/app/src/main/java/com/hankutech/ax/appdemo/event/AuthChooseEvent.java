package com.hankutech.ax.appdemo.event;

import com.hankutech.ax.appdemo.ax.code.AuthFlag;

import lombok.Data;

@Data
public class AuthChooseEvent {
    private AuthFlag authFlag;

    public AuthChooseEvent(AuthFlag flag) {
        this.authFlag = flag;
    }
}
