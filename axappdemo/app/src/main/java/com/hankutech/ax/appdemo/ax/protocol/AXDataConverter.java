package com.hankutech.ax.appdemo.ax.protocol;


import com.hankutech.ax.appdemo.ax.SocketConst;
import com.hankutech.ax.appdemo.ax.code.AIGarbageResultType;
import com.hankutech.ax.appdemo.ax.code.AuthFlag;
import com.hankutech.ax.appdemo.ax.code.GateState;
import com.hankutech.ax.appdemo.ax.code.SysRunFlag;

import java.io.UnsupportedEncodingException;

public class AXDataConverter {


    /**
     * 解析字节数据为AXRequest
     *
     * @param convertedData
     * @return
     */
    public static AXRequest parseRequest(int[] convertedData) {

        if (convertedData == null || convertedData.length != SocketConst.REQUEST_DATA_LENGTH) {
            return null;
        }

        AXRequest axRequest = new AXRequest();
        //  X1标示艾信控制器系统状态，5标示停止，8标示运行
        axRequest.setSysRunFlag(SysRunFlag.valueOf(convertedData[0]));
        //  X3标示投递站点是否有人
        if (convertedData[2] == 1) {
            axRequest.setPersonExist(true);
        }

        //  X4标示授权信息
        axRequest.setAuthFlag(AuthFlag.valueOf(convertedData[3]));

        //X5标示当前时段垃圾投递种类
        axRequest.setGarbageType(AIGarbageResultType.valueOf(convertedData[4]));


        //X6标示垃圾分类检测结果成功与否
        if (convertedData[5] == 1) {
            axRequest.setGarbageTypeDetectSuccess(true);
        }

        //X7标示门是否关好，及关门超时报警
        axRequest.setGateState(GateState.valueOf(convertedData[6]));

//        X8标示干垃圾投递数量
        axRequest.setCount_DRY(convertedData[7]);
        //        X9标示湿垃圾投递数量
        axRequest.setCount_WET(convertedData[8]);
        //       X10标示可回收垃圾投递数量
        axRequest.setCount_RECYCLABLE(convertedData[9]);
        //       X11标示有害垃圾投递数量
        axRequest.setCount_HAZARDOUS(convertedData[10]);
        //        X12被服投递次数
        axRequest.setCount_BF(convertedData[11]);

        //  第十三个字节X13标示人名第一字；
        //第十四个字节X14标示人名第一字；
        //第十五个字节X15标示人名第二字；
        //第十六个字节X16标示人名第二字；
        //第十七个字节X17标示人名第三字；
        //第十八个字节X18标示人名第三字；
        byte[] byteData = new byte[]{(byte) convertedData[12],
                (byte) convertedData[13],
                (byte) convertedData[14],
                (byte) convertedData[15],
                (byte) convertedData[16],
                (byte) convertedData[17]};

        String charsetName = "GB18030";
        String personName = null;
        try {
            personName = new String(byteData, charsetName);
            axRequest.setPersonName(personName);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return axRequest;
    }

    /**
     * 转换AXResponse为字节形式
     *
     * @param resp
     * @return
     */
    public static int[] convertResponse(AXResponse resp) {

        int[] resultArray = new int[SocketConst.RESPONSE_DATA_LENGTH];

        // X1标示视频智能算法控制器状态，5标示停止，8标示运行；
        resultArray[0] = resp.getSysRunFlag().getValue();

        // X3标示选择授权方式
        resultArray[2] = resp.getAuthFlag().getValue();

        return resultArray;
    }


}
