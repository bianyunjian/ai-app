package com.hankutech.ax.appdemo.socket;

import java.util.Arrays;

public class ByteConverter {
    /**
     * java 中byte数据类型是 有符号位的， 范围 -128~127
     * 而在 C/C++中支持 无符号类型， 范围-256~255
     * 因此java需要用更大的数据类型来做中间转换
     * 例如：
     * java byte  --> 实际值 (C/C++)
     * 0               0
     * 1               1
     * 127             127
     * -128            128
     * -127            129
     * -1              255
     *
     * @param intData
     * @return
     */
    public static byte[] toByte(int[] intData) {
        byte data[] = new byte[intData.length];
        for (int i = 0; i < intData.length; i++) {
            data[i] = (byte) (intData[i] & 0xff);
        }
        return data;
    }


    /**
     * 接收到的byte数组中存在负数的情况，
     * 将接收到的byte数组转换成[0,255]范围内
     *
     * @param bytes
     * @param ignoreConvertIndexs 跳过转换的数据索引位置
     * @return
     */
    public static int[] fromByte(byte[] bytes, int... ignoreConvertIndexs) {
        int data[] = new int[bytes.length];
        for (int i = 0; i < bytes.length; i++) {
            if (ignoreConvertIndexs != null && ignoreConvertIndexs.length > 0) {
                int findItemIndex = Arrays.binarySearch(ignoreConvertIndexs, i);
                if (findItemIndex >= 0) {
                    data[i] = bytes[i];
                    continue;
                }
            }
            data[i] = bytes[i] & 0xff;
        }
        return data;
    }


}
