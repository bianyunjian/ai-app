package com.hankutech.ax.appdemo.ax.protocol;

import com.hankutech.ax.appdemo.ax.code.AIGarbageResultType;
import com.hankutech.ax.appdemo.ax.code.AIGarbageTypeDetectResult;
import com.hankutech.ax.appdemo.ax.code.AuthFlag;
import com.hankutech.ax.appdemo.ax.code.GateState;
import com.hankutech.ax.appdemo.ax.code.SysRunFlag;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class AXRequest {

    /**
     * 字节X1标示艾信控制器系统状态
     */
    SysRunFlag sysRunFlag;

    /**
     * 字节X2标示是否出现了异常情况， 默认=0， 异常=1， 出现异常情况，直接重置
     */
    boolean sysException;
    /**
     * X3标示投递站点是否有人
     */
    boolean personExist;

    /**
     * X4标示授权信息
     */
    AuthFlag authFlag;

    /**
     * X5标示当前时段垃圾投递种类
     */
    AIGarbageResultType garbageType;

    /**
     * X6标示垃圾分类检测结果成功与否,默认值=0， 成功=1，失败=2
     */
    AIGarbageTypeDetectResult garbageTypeDetectResult;


    /**
     * X7标示门是否关好，及关门超时报警
     */
    GateState gateState;

    /**
     * X8标示干垃圾投递数量；
     */
    int count_DRY;
    /**
     * X9标示湿垃圾投递数量
     */
    int count_WET;
    /**
     * X10标示可回收垃圾投递数量
     */
    int count_RECYCLABLE;
    /**
     * X11标示有害垃圾投递数量
     */
    int count_HAZARDOUS;
    /**
     * X12被服投递次数
     */
    int count_BF;


    //第十三个字节X13标示人名第一字；
    //第十四个字节X14标示人名第一字；
    //第十五个字节X15标示人名第二字；
    //第十六个字节X16标示人名第二字；
    //第十七个字节X17标示人名第三字；
    //第十八个字节X18标示人名第三字；
    String personName;

    public boolean isValid() {
        return sysRunFlag != SysRunFlag.EMPTY;
    }

    /**
     * 获取投递的数量
     *
     * @return
     */
    public int getGarbageDeliveredCount() {
        return count_DRY + count_BF + count_HAZARDOUS + count_RECYCLABLE + count_WET;
    }
}
