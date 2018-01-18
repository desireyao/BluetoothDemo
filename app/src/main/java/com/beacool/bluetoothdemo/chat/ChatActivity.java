package com.beacool.bluetoothdemo.chat;

import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.beacool.bluetoothdemo.R;
import com.beacool.bluetoothdemo.chat.service.BluetoothMainService;
import com.beacool.bluetoothdemo.chat.eventmsg.MessageEvent;
import com.beacool.bluetoothdemo.tools.LogTool;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Set;

public class ChatActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "ChatActivity";

    private EditText edit_data;
    private Button btn_send_data;
    private BluetoothMainService mService;

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        edit_data = findViewById(R.id.edit_data);
        btn_send_data = findViewById(R.id.btn_send_data);
        btn_send_data.setOnClickListener(this);

        startService(new Intent(this, BluetoothMainService.class));
    }

    @Override
    public void onClick(View v) {
        if (mService != null) {
            Set<BluetoothDevice> devices = mService.getBondedDevices();
            LogTool.LogE(TAG, "devices = " + devices.toString());

            for (BluetoothDevice device : devices) {
                LogTool.LogE(TAG, "devices = " + device.getName());
                if (TextUtils.equals(device.getName(), "魅蓝-智向")
                        || TextUtils.equals(device.getName(), "小米6")) {

                }
            }
        }
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MessageEvent event) {
        if (event.mNotifyType == MessageEvent.NOTIFY_TYPE.SERVICE_ONCREATE
                || event.mNotifyType == MessageEvent.NOTIFY_TYPE.SERVICE_ONSTART) {
            LogTool.LogE(TAG, "onMessageEvent--->" + event.mNotifyType.name());

            // 绑定service
            Intent intentService = new Intent(this, BluetoothMainService.class);
            bindService(intentService, mBandServiceConnection, Context.BIND_AUTO_CREATE);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);

        unbindService(mBandServiceConnection);
    }

    ServiceConnection mBandServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            LogTool.LogE(TAG, "onServiceConnected--->" + name.getClassName());
            BluetoothMainService.ServiceBinder binder = (BluetoothMainService.ServiceBinder) service;
            mService = binder.getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            LogTool.LogE(TAG, "onServiceDisconnected--->" + name.getClassName());

        }
    };
}
