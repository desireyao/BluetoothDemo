package com.beacool.bluetoothdemo.chat.eventmsg;

/**
 * Created by yaoh on 2018/1/17.
 */

public class MessageEvent {

    public enum NOTIFY_TYPE{
        SERVICE_ONCREATE,
        SERVICE_ONSTART,
        BLUETOOTH_REC_DATA,
    }

    public MessageEvent(NOTIFY_TYPE notifyType){
        mNotifyType  = notifyType;
    }

    public MessageEvent(NOTIFY_TYPE notifyType,byte[] recvDatas){
          this.mNotifyType = notifyType;
          this.recvDatas = recvDatas;
    }

    public NOTIFY_TYPE mNotifyType;

    public byte[] recvDatas;
}
