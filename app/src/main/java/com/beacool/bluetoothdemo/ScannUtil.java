package com.beacool.bluetoothdemo;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanRecord;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yaoh on 2018/1/9.
 */

public class ScannUtil {
    private static final String TAG = ScannUtil.class.getSimpleName();

    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothLeScanner mBluetoothLeScanner;

    private ScanCallback mScanCallback;

    public ScannUtil(Context context) {
        mBluetoothAdapter = ((BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE))
                .getAdapter();

        mBluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();
    }


    /**
     * Start scanning for BLE Advertisements (& set it up to stop after a set period of time).
     */
    public void startScanning() {
        if (mScanCallback == null) {
            Log.d(TAG, "Starting Scanning");

            // Kick off a new scan.
            mScanCallback = new SampleScanCallback();
            mBluetoothLeScanner.startScan(buildScanFilters(), buildScanSettings(), mScanCallback);
        }
    }

    /**
     * Return a List of {@link ScanFilter} objects to filter by Service UUID.
     */
    private List<ScanFilter> buildScanFilters() {
        List<ScanFilter> scanFilters = new ArrayList<>();
        ScanFilter.Builder builder = new ScanFilter.Builder();
        // Comment out the below line to see all BLE devices around you
        builder.setServiceUuid(Constants.Service_UUID);
        scanFilters.add(builder.build());
        return scanFilters;
    }

    /**
     * Return a {@link ScanSettings} object set to use low power (to preserve battery life).
     */
    private ScanSettings buildScanSettings() {
        ScanSettings.Builder builder = new ScanSettings.Builder();
        builder.setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY);
        return builder.build();
    }

    /**
     * Custom ScanCallback object - adds to adapter on success, displays error on failure.
     */
    private class SampleScanCallback extends ScanCallback {

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            super.onBatchScanResults(results);
        }

        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);

            ScanRecord record = result.getScanRecord();
            Log.e(TAG, "data = " + LogTool.LogBytes2Hex(record.getBytes()));
            Log.e(TAG, "uuid = " + record.getServiceUuids().toString()
                    + "\n record.getDeviceName() = " + record.getDeviceName());

            if (scannListener != null) {
                scannListener.onScann(result);
            }
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
            Log.e(TAG, "Scan failed with error: " + errorCode);
        }
    }

    public interface ScannListener {
        void onScann(ScanResult result);
    }

    private ScannListener scannListener;

    public void setScanCallback(ScannListener listener) {
        scannListener = listener;
    }

    public static byte[] parseAdverData(byte[] scanRecord) {
        int len = scanRecord.length;
        int start = 0;
        for (int i = 0; i < len - 1; i++) {
            if ((scanRecord[i] & 0xff) == 0xb8
                    && (scanRecord[i + 1] & 0xff) == 0x06) {
                start = i + 5;
                break;
            }
        }

        byte[] data = new byte[3];
        data[0] = scanRecord[start];
        data[1] = scanRecord[start + 1];
        data[2] = scanRecord[start + 2];

        return data;
    }

}
