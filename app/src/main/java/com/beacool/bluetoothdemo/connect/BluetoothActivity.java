package com.beacool.bluetoothdemo.connect;

import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.beacool.bluetoothdemo.R;
import com.beacool.bluetoothdemo.chat.eventmsg.MessageEvent;
import com.beacool.bluetoothdemo.chat.eventmsg.NotifyBluetoothState;
import com.beacool.bluetoothdemo.chat.service.BluetoothMainService;
import com.beacool.bluetoothdemo.connect.service.BluetoothService;
import com.beacool.bluetoothdemo.tools.LogTool;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.text.SimpleDateFormat;

public class BluetoothActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "BluetoothActivity";

    private Button btn_startScan;
    private Button btn_stopScan;
    private Button btn_disconnect;
    private Button btn_reconnect;

    private TextView tv_content;

    private BluetoothService myService;

    private static final String BLE_ADDRESS = "FF:1E:85:79:BC:7A";

    private StringBuffer stringBuffer = new StringBuffer();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth);

        btn_startScan = findViewById(R.id.btn_startScan);
        btn_stopScan = findViewById(R.id.btn_stopScan);
        btn_disconnect = findViewById(R.id.btn_disconnect);
        btn_reconnect = findViewById(R.id.btn_reconnect);

        btn_startScan.setOnClickListener(this);
        btn_stopScan.setOnClickListener(this);
        btn_disconnect.setOnClickListener(this);
        btn_reconnect.setOnClickListener(this);

        tv_content = findViewById(R.id.tv_content);

        EventBus.getDefault().register(this);
        startService(new Intent(this, BluetoothService.class));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        unbindService(mConnection);
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btn_startScan) {
            scanDevice();
        } else if (id == R.id.btn_stopScan) {
            stopScanDevice();
        } else if (id == R.id.btn_disconnect) {
            disConnect();
        } else if (id == R.id.btn_reconnect) {
            reConnectDevice();
        }
    }


    private void scanDevice() {
        if (myService != null) {
            myService.scanLeDevice(true, leScanCallback);
        }
    }

    private void stopScanDevice() {
        if (myService != null) {
            myService.scanLeDevice(false, leScanCallback);
        }
    }

    private void disConnect() {
        if (myService != null) {
            myService.disconnect();
        }
    }

    private void reConnectDevice() {
        myService.stopScanLeDevice(leScanCallback);

        if (myService != null) {
            myService.reConnectDevice();
        }
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MessageEvent event) {
        if (event.mNotifyType == MessageEvent.NOTIFY_TYPE.SERVICE_ONCREATE
                || event.mNotifyType == MessageEvent.NOTIFY_TYPE.SERVICE_ONSTART) {

            LogTool.LogE(TAG, "onMessageEvent--->" + event.mNotifyType.name());
            // 绑定service
            Intent intentService = new Intent(this, BluetoothService.class);
            bindService(intentService, mConnection, Context.BIND_AUTO_CREATE);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onStateChanged(NotifyBluetoothState.NOTIFY_TYPE state){
        stringBuffer.append("---> "
                + new SimpleDateFormat("HH:mm:ss").format(System.currentTimeMillis())
                + " | "
                + state.name() + "\n");
        tv_content.setText(stringBuffer.toString());
    }


    ServiceConnection mConnection = new ServiceConnection() {

        public void onServiceConnected(ComponentName className, IBinder service) {
            BluetoothService.LocalBinder binder = (BluetoothService.LocalBinder) service;
            myService = binder.getService();

            if (!myService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                finish();
            }
            Log.e(TAG, "onServiceConnected--->");
        }

        public void onServiceDisconnected(ComponentName className) {
            Log.e(TAG, "onServiceDisconnected--->");
        }
    };

    BluetoothService.LeScanCallback leScanCallback = new BluetoothService.LeScanCallback() {

        @Override
        public void onScanDevice(BluetoothDevice device) {
            String address = device.getAddress();
            LogTool.LogE(TAG, "onScanDevice ---> address = " + device.getAddress());

            if (TextUtils.equals(address, BLE_ADDRESS)) {
                if (myService != null) {

                    myService.stopScanLeDevice(this);
                    myService.connect(address,false);
                }
            }
        }
    };
}
