package com.beacool.bluetoothdemo.chat.eventmsg;

/**
 * Created by yaoh on 2018/1/25.
 */

public class NotifyBluetoothState {

    public NOTIFY_TYPE notifyType;

    public enum NOTIFY_TYPE {
        CONNECTING_EXIST_DEVICE,
        CONNECTING_NEW_DEVICE,
        CONNECT_SUCCEED,
        DISCONNECTED
    }




}
