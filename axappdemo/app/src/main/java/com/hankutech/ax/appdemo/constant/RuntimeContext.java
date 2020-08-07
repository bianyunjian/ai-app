package com.hankutech.ax.appdemo.constant;

import com.hankutech.ax.appdemo.ax.code.AIGarbageResultType;
import com.hankutech.ax.appdemo.ax.code.AuthFlag;

public class RuntimeContext {

    /**
     * 当前授权方式
     */
    public static AuthFlag CurrentAuthFlag = AuthFlag.FAILURE;
    /**
     * 当前垃圾投递类型
     */
    public static AIGarbageResultType CurrentGarbageType = AIGarbageResultType.DRY;
}
