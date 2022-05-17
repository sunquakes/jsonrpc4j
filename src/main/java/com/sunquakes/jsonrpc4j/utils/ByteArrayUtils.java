package com.sunquakes.jsonrpc4j.utils;

import lombok.experimental.UtilityClass;

@UtilityClass
public class ByteArrayUtils {

    public byte[] merge(byte[] bt1, byte[] bt2) {
        byte[] bt3 = new byte[bt1.length + bt2.length];
        System.arraycopy(bt1, 0, bt3, 0, bt1.length);
        System.arraycopy(bt2, 0, bt3, bt1.length, bt2.length);
        return bt3;
    }
}
