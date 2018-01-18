package com.beacool.bluetoothdemo.chat;

import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.beacool.bluetoothdemo.R;
import com.beacool.bluetoothdemo.chat.listener.IRecvBluetoothDataListener;
import com.beacool.bluetoothdemo.chat.service.BluetoothMainService;
import com.beacool.bluetoothdemo.chat.eventmsg.MessageEvent;
import com.beacool.bluetoothdemo.tools.LogTool;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.UnsupportedEncodingException;
import java.util.Set;

public class ChatActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "ChatActivity";

    private EditText edit_data;
    private Button btn_send_data;
    private Button btn_connect_device;
    private Button btn_disconnect_device;

    private BluetoothMainService mService;

    private Handler mHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        edit_data = findViewById(R.id.edit_data);
        btn_send_data = findViewById(R.id.btn_send_data);
        btn_send_data.setOnClickListener(this);
        btn_connect_device = findViewById(R.id.btn_connect_device);
        btn_connect_device.setOnClickListener(this);

        btn_disconnect_device = findViewById(R.id.btn_disconnect_device);
        btn_disconnect_device.setOnClickListener(this);

        EventBus.getDefault().register(this);
        startService(new Intent(this, BluetoothMainService.class));
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.btn_connect_device) {
            if (mService == null) {
                return;
            }

            if (mService.isConnected) {
                return;
            }

            /**
             * 修改为你自己设备的名称
             */
            Set<BluetoothDevice> devices = mService.getBondedDevices();
            LogTool.LogE(TAG, "devices = " + devices.toString());
            for (BluetoothDevice device : devices) {
                LogTool.LogE(TAG, "devices = " + device.getName());
                if (TextUtils.equals(device.getName(), "魅蓝-智向")
                        || TextUtils.equals(device.getName(), "小米6")) {
//                    mService.connect(device, true);
                    // Attempt to connect to the device
                    mService.connect(device, true);
                }
            }
        } else if (id == R.id.btn_send_data) {
            String msg = edit_data.getText().toString();
            sendMessage(msg);
        } else if (id == R.id.btn_disconnect_device) {
            if (mService == null) {
                return;
            }
            mService.stop();
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
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);

        unbindService(mBandServiceConnection);
    }

    ServiceConnection mBandServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            LogTool.LogE(TAG, "onServiceConnected--->" + name.getClassName());
            BluetoothMainService.ServiceBinder binder = (BluetoothMainService.ServiceBinder) service;
            mService = binder.getService();

            mService.setRecvDataListener(new IRecvBluetoothDataListener() {

                @Override
                public void onRecvBluetoothData(final String string) {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(), "recv:" + string, Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            LogTool.LogE(TAG, "onServiceDisconnected--->" + name.getClassName());
        }
    };

    /**
     * 发送信息
     *
     * @param message
     */
    private void sendMessage(String message) {
        // Check that we're actually connected before trying anything
        if (mService.getState() != BluetoothMainService.STATE_CONNECTED) {
            LogTool.LogE(TAG, "Not Connected!");
            return;
        }

        // Check that there's actually something to send
        if (message.length() > 0) {
            // Get the message bytes and tell the BluetoothChatService to write
            try {
                byte[] send = message.getBytes("utf-8");
                mService.write(send);
            } catch (Exception e) {
                LogTool.LogE(TAG, e.toString());
            }
        }
    }


}
