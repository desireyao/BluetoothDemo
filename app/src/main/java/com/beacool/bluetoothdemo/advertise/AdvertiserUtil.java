package com.beacool.bluetoothdemo.advertise;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.content.Context;
import android.util.Log;

/**
 * Manages BLE Advertising independent of the main app.
 * If the app goes off screen (or gets killed completely) advertising can continue because this
 * Service is maintaining the necessary Callback in memory.
 */
public class AdvertiserUtil {
    private static final String TAG = AdvertiserUtil.class.getSimpleName();

    private BluetoothLeAdvertiser mBluetoothLeAdvertiser;
    private AdvertiseCallback mAdvertiseCallback;
    private byte[] mByteDatas;

    public AdvertiserUtil(Context context) {
        if (mBluetoothLeAdvertiser == null) {
            BluetoothManager mBluetoothManager = (BluetoothManager) context
                    .getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBluetoothManager != null) {
                BluetoothAdapter mBluetoothAdapter = mBluetoothManager.getAdapter();
                if (mBluetoothAdapter != null) {
                    mBluetoothLeAdvertiser = mBluetoothAdapter.getBluetoothLeAdvertiser();
                }
            }
        }
    }


    public void startAdvertising(byte[] datas) {
        this.mByteDatas = datas;
        startAdvertising();
    }

    /**
     * Starts BLE Advertising.
     */
    private void startAdvertising() {
        Log.d(TAG, "Service: Starting Advertising");
        if (mAdvertiseCallback == null) {
            AdvertiseSettings settings = buildAdvertiseSettings();
            AdvertiseData data = buildAdvertiseData();

            mAdvertiseCallback = new SampleAdvertiseCallback();
            if (mBluetoothLeAdvertiser != null) {
                mBluetoothLeAdvertiser.startAdvertising(settings, data, mAdvertiseCallback);
            }
        }
    }

    /**
     * Stops BLE Advertising.
     */
    public void stopAdvertising() {
        Log.d(TAG, "Service: Stopping Advertising--->");
        if (mBluetoothLeAdvertiser != null && mAdvertiseCallback != null) {
            mBluetoothLeAdvertiser.stopAdvertising(mAdvertiseCallback);
            mAdvertiseCallback = null;
        }
    }

    /**
     * Returns an AdvertiseData object which includes the Service UUID and Device Name.
     */
    private AdvertiseData buildAdvertiseData() {
        AdvertiseData.Builder dataBuilder = new AdvertiseData.Builder();
        dataBuilder.addServiceUuid(Constants.Service_UUID);
        dataBuilder.setIncludeDeviceName(false);
        dataBuilder.addServiceData(Constants.Service_UUID, mByteDatas);
        return dataBuilder.build();
    }

    /**
     * 设置发包频率 ，ADVERTISE_MODE_LOW_LATENCY 100 ms
     */
    private AdvertiseSettings buildAdvertiseSettings() {
        AdvertiseSettings.Builder settingsBuilder = new AdvertiseSettings.Builder();
        settingsBuilder.setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY);
        settingsBuilder.setTimeout(0);
        return settingsBuilder.build();
    }

    /**
     * Custom callback after Advertising succeeds or fails to start. Broadcasts the error code
     * in an Intent to be picked up by AdvertiserFragment and stops this Service.
     */
    private class SampleAdvertiseCallback extends AdvertiseCallback {

        @Override
        public void onStartFailure(int errorCode) {
            super.onStartFailure(errorCode);
            int data1 = mByteDatas[0] << 8;
            int data2 = mByteDatas[1] << 4;
            int data3 = mByteDatas[2];
            int data = data1 + data2 + data3;
            Log.e(TAG, "Advertising failed sendData = " + data);

            if (mBluetoothLeAdvertiser != null) {
                Log.e(TAG, "stop Advertising--->");
                stopAdvertising();
            }
        }

        @Override
        public void onStartSuccess(AdvertiseSettings settingsInEffect) {
            super.onStartSuccess(settingsInEffect);

            int data1 = mByteDatas[0] << 8;
            int data2 = mByteDatas[1] << 4;
            int data3 = mByteDatas[2];
            int data = data1 + data2 + data3;
            Log.e(TAG, "Advertising successfully started sendData = " + data);

//            if (mBluetoothLeAdvertiser != null) {
//                Log.e(TAG, "stop Advertising--->");
//                stopAdvertising();
//            }

            if (adVertiseListener != null) {
                adVertiseListener.onAdvertiseListener(mByteDatas);
            }
        }
    }

    private AdVertiseListener adVertiseListener;

    public interface AdVertiseListener {
        void onAdvertiseListener(byte[] data);
    }

    public void setAdVertiseListener(AdVertiseListener callback) {
        this.adVertiseListener = callback;
    }




}
