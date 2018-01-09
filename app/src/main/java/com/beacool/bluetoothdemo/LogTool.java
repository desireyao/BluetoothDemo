package com.beacool.bluetoothdemo;

/**
 * Created by yaoh on 2018/1/9.
 */

public class LogTool {

    public static String LogBytes2Hex(byte[] bytes) {
        StringBuffer buffer = new StringBuffer();
        if (bytes != null) {
            for (int i = 0; i < bytes.length; i++) {
                buffer.append(String.format("%02X ", bytes[i]));
            }
        }
        return buffer.toString();
    }

}
