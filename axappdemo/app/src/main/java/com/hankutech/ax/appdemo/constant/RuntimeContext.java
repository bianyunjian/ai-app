package com.hankutech.ax.appdemo.constant;

import com.hankutech.ax.appdemo.code.AppStatus;
import com.hankutech.ax.message.code.AIAuthFlag;
import com.hankutech.ax.message.code.AIGarbageResultType;

public class RuntimeContext {

    /**
     * 当前授权方式
     */
    public static AIAuthFlag CurrentAuthFlag = AIAuthFlag.FAILURE;
    /**
     * 当前垃圾投递类型
     */
    public static AIGarbageResultType CurrentGarbageType = AIGarbageResultType.WET;
    /**
     * 当前工作状态
     */
    public static AppStatus CurrentAppStatus = AppStatus.NORMAL;
}
