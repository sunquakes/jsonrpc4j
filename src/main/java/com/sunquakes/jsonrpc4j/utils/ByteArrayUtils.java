package com.sunquakes.jsonrpc4j.utils;

import lombok.experimental.UtilityClass;

/**
 * @author : Robert, sunquakes@outlook.com
 * @version : 1.0.0
 * @since : 2022/5/21 1:32 PM
 **/
@UtilityClass
public class ByteArrayUtils {

    public byte[] merge(byte[] bt1, byte[] bt2) {
        byte[] bt3 = new byte[bt1.length + bt2.length];
        System.arraycopy(bt1, 0, bt3, 0, bt1.length);
        System.arraycopy(bt2, 0, bt3, bt1.length, bt2.length);
        return bt3;
    }

    public int strstr(byte[] str1, byte[] str2, int start, int end) {
        int index1 = start;
        int index2 = 0;
        if (str2 != null) {
            while (index1 < str1.length && index1 < end) {
                int dsite = 0;
                while (str1[index1 + dsite] == str2[index2 + dsite]) {
                    if (index2 + dsite + 1 >= str2.length)
                        return index1;
                    dsite++;
                    if (index1 + dsite >= str1.length || index2 + dsite >= str2.length)
                        break;
                }
                index1++;
            }
            return -1;
        } else
            return index1;
    }

    public int strstr(byte[] str1, byte[] str2) {
        int index1 = 0;
        int index2 = 0;
        if (str2 != null) {
            while (index1 < str1.length) {
                int dsite = 0;
                while (str1[index1 + dsite] == str2[index2 + dsite]) {
                    if (index2 + dsite + 1 >= str2.length)
                        return index1;
                    dsite++;
                    if (index1 + dsite >= str1.length || index2 + dsite >= str2.length)
                        break;
                }
                index1++;
            }
            return -1;
        } else
            return index1;
    }

    public int strstr(byte[] str1, byte[] str2, int start) {
        int index1 = start;
        int index2 = 0;
        if (str2 != null) {
            while (index1 < str1.length) {
                int dsite = 0;
                while (str1[index1 + dsite] == str2[index2 + dsite]) {
                    if (index2 + dsite + 1 >= str2.length)
                        return index1;
                    dsite++;
                    if (index1 + dsite >= str1.length || index2 + dsite >= str2.length)
                        break;
                }
                index1++;
            }
            return -1;
        } else
            return index1;
    }
}
