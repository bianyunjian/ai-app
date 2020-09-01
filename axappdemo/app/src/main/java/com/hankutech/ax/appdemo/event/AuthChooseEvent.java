package com.hankutech.ax.appdemo.event;

import com.hankutech.ax.message.code.AIAuthFlag;

import lombok.Data;

@Data
public class AuthChooseEvent {
    private AIAuthFlag authFlag;

    public AuthChooseEvent(AIAuthFlag flag) {
        this.authFlag = flag;
    }
}
