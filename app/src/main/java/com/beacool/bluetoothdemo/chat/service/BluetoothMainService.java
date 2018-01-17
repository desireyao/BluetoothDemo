package com.beacool.bluetoothdemo.chat.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import com.beacool.bluetoothdemo.chat.service.eventmsg.MessageEvent;
import com.beacool.bluetoothdemo.tools.LogTool;

import org.greenrobot.eventbus.EventBus;

/**
 * Created by yaoh on 2018/1/17.
 */

public class BluetoothMainService extends Service {
    private static final String TAG = "BluetoothMainService";

    @Override
    public IBinder onBind(Intent intent) {
        return new ServiceBinder(this);
    }

    public class ServiceBinder extends Binder {
        private BluetoothMainService service;

        protected ServiceBinder(BluetoothMainService service) {
            this.service = service;
        }

        public BluetoothMainService getService() {
            return service;
        }
    }

    @Override
    public void onCreate() {
        LogTool.LogE(TAG,"onCreate--->");
        super.onCreate();

        EventBus.getDefault().post(new MessageEvent(MessageEvent.NOTIFY_TYPE.SERVICE_ONCREATE));
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        LogTool.LogE(TAG,"onStartCommand--->");
        EventBus.getDefault().post(new MessageEvent(MessageEvent.NOTIFY_TYPE.SERVICE_ONSTART));
        return START_REDELIVER_INTENT;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        LogTool.LogE(TAG,"onUnbind--->");
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        LogTool.LogE(TAG,"onDestroy--->");
        super.onDestroy();
    }
}
