package com.beacool.bluetoothdemo.gatt;

import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanRecord;
import android.bluetooth.le.ScanResult;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.beacool.bluetoothdemo.R;
import com.beacool.bluetoothdemo.tools.LogTool;

import java.util.List;

public class GattMainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "GattMainActivity";

    private Button btn_start_advertise;
    private Button btn_search_advertise;

    private GattClientManager gattClientManager;
    private GattServerManager gattServerManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gatt_main);

        btn_start_advertise = findViewById(R.id.btn_start_advertise);
        btn_start_advertise.setOnClickListener(this);

        btn_search_advertise = findViewById(R.id.btn_search_advertise);
        btn_search_advertise.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btn_start_advertise) {
            gattServerManager = new GattServerManager(this.getApplicationContext());
            gattServerManager.startAdvertising();

            gattServerManager.openGattServer(this);
        } else if (id == R.id.btn_search_advertise) {
            gattClientManager = new GattClientManager(this.getApplicationContext());
            gattClientManager.startScanning(mScanCallback);
        }
    }

    ScanCallback mScanCallback = new ScanCallback() {

        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            ScanRecord scanRecord = result.getScanRecord();
            byte[] scanData = scanRecord.getBytes();

            Log.e(TAG, LogTool.LogBytes2Hex(scanData, "data = "));
            Log.e(TAG, "uuid = " + result.getScanRecord().getServiceUuids().toString()
                    + "\n record.getDeviceName() = " + scanRecord.getDeviceName());

            if (TextUtils.equals(scanRecord.getDeviceName(), "小米6")
                    || TextUtils.equals(scanRecord.getDeviceName(), "魅蓝-智向")) {

                gattClientManager.connectDevice(result.getDevice(),GattMainActivity.this);
            }
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            super.onBatchScanResults(results);
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
        }
    };
}
