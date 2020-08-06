package com.hankutech.ax.appdemo.ax.protocol;


import com.hankutech.ax.appdemo.ax.code.AuthFlag;
import com.hankutech.ax.appdemo.ax.code.SysRunFlag;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class AXResponse {

    /**
     * 字节X1标示视频智能算法控制器状态
     */
    SysRunFlag sysRunFlag;

    /**
     * X3标示选择授权方式
     */
    AuthFlag authFlag;


    /**
     * X4标示 请求PLC开始检测垃圾分类
     * 1= 开始， 0=未开始
     */
    int startGarbageDetectRequestFlag;

    /**
     * get a empty response as default
     *
     * @return
     */
    public static AXResponse DefaultEmpty() {
        AXResponse resp = new AXResponse();
        resp.setSysRunFlag(SysRunFlag.RUN);
        resp.setAuthFlag(AuthFlag.RFID);
        resp.setStartGarbageDetectRequestFlag(0);

        return resp;
    }


}
