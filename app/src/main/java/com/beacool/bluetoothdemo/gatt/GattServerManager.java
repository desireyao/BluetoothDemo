package com.beacool.bluetoothdemo.gatt;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothGattServerCallback;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.content.Context;
import android.util.Log;

import com.beacool.bluetoothdemo.advertise.Constants;
import com.beacool.bluetoothdemo.tools.LogTool;

/**
 * Created by yaoh on 2018/1/16.
 */

public class GattServerManager {
    private static final String TAG = "GattServerManager";

    private BluetoothManager bluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothGattServer gattServer;

    private BluetoothLeAdvertiser mBluetoothLeAdvertiser;

    private AdvertiseCallback mAdvertiseCallback;

    public GattServerManager(Context context) {
        bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
        mBluetoothLeAdvertiser = mBluetoothAdapter.getBluetoothLeAdvertiser();
    }

    public void openGattServer(Context context) {
        gattServer = bluetoothManager.openGattServer(context, gattServerCallback);
    }

    /**
     * 开始广播
     */
    public void startAdvertising() {
        Log.d(TAG, "Service: Starting Advertising");
        AdvertiseSettings settings = buildAdvertiseSettings();
        AdvertiseData data = buildAdvertiseData();
        mAdvertiseCallback = new SampleAdvertiseCallback();

        // 开始蓝牙广播
        if (mBluetoothLeAdvertiser != null) {
            mBluetoothLeAdvertiser.startAdvertising(settings, data, mAdvertiseCallback);
        }
    }

    private AdvertiseData buildAdvertiseData() {
        AdvertiseData.Builder dataBuilder = new AdvertiseData.Builder();
        dataBuilder.addServiceUuid(Constants.Service_UUID);
        dataBuilder.setIncludeDeviceName(true);

        byte[] mSendDatas = new byte[]{0x07, 0x08, 0x09};
        dataBuilder.addServiceData(Constants.Service_UUID, mSendDatas);
        return dataBuilder.build();
    }

    /**
     * 设置发包频率 ，ADVERTISE_MODE_LOW_LATENCY 100 ms
     */
    private AdvertiseSettings buildAdvertiseSettings() {
        AdvertiseSettings.Builder settingsBuilder = new AdvertiseSettings.Builder();
        settingsBuilder.setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_POWER);
        settingsBuilder.setTimeout(0);
        return settingsBuilder.build();
    }


    class SampleAdvertiseCallback extends AdvertiseCallback {

        @Override
        public void onStartFailure(int errorCode) {
            if (mBluetoothLeAdvertiser != null) {
                Log.e(TAG, "stop Advertising--->");
            }
        }

        @Override
        public void onStartSuccess(AdvertiseSettings settingsInEffect) {
            super.onStartSuccess(settingsInEffect);
            if (mBluetoothLeAdvertiser != null) {
                Log.e(TAG, "onStartSuccess Advertising--->");
            }
        }
    }

    BluetoothGattServerCallback gattServerCallback = new BluetoothGattServerCallback() {

        @Override
        public void onConnectionStateChange(final BluetoothDevice device, int status, int newState) {
            super.onConnectionStateChange(device, status, newState);
            LogTool.LogE(TAG, " onConnectionStateChange:" + status + " newState:" + newState + " devicename:" + device.getName() + " mac:" + device.getAddress());

        }

        @Override
        public void onServiceAdded(int status, BluetoothGattService service) {
            super.onServiceAdded(status, service);
            LogTool.LogE(TAG, " onServiceAdded status:" + status + " service:" + service.getUuid().toString());
        }

        @Override
        public void onCharacteristicReadRequest(BluetoothDevice device, int requestId, int offset, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicReadRequest(device, requestId, offset, characteristic);
            LogTool.LogE(TAG, " onCharacteristicReadRequest requestId:" + requestId + " offset:" + offset + " characteristic:" + characteristic.getUuid().toString());

        }

        @Override
        public void onCharacteristicWriteRequest(BluetoothDevice device, int requestId, BluetoothGattCharacteristic characteristic, boolean preparedWrite, boolean responseNeeded, int offset, byte[] value) {
            super.onCharacteristicWriteRequest(device, requestId, characteristic, preparedWrite, responseNeeded, offset, value);
            LogTool.LogE(TAG, " onCharacteristicWriteRequest requestId:" + requestId + " preparedWrite:" + preparedWrite
                    + " responseNeeded:" + responseNeeded
                    + " offset:" + offset
                    + " value:" + LogTool.LogBytes(value, "value")
                    + " characteristic:" + characteristic.getUuid().toString());
        }

        @Override
        public void onDescriptorReadRequest(BluetoothDevice device, int requestId, int offset, BluetoothGattDescriptor descriptor) {
            super.onDescriptorReadRequest(device, requestId, offset, descriptor);
            LogTool.LogE(TAG, " onCharacteristicReadRequest requestId:" + requestId + " offset:" + offset + " descriptor:" + descriptor.getUuid().toString());
        }

        @Override
        public void onDescriptorWriteRequest(final BluetoothDevice device, int requestId, BluetoothGattDescriptor descriptor, boolean preparedWrite, boolean responseNeeded, int offset, byte[] value) {
            super.onDescriptorWriteRequest(device, requestId, descriptor, preparedWrite, responseNeeded, offset, value);
            LogTool.LogE(TAG, " onDescriptorWriteRequest requestId:" + requestId
                    + " preparedWrite:" + preparedWrite
                    + " responseNeeded:" + responseNeeded
                    + " offset:" + offset
                    + " value:" + LogTool.LogBytes(value, "value")
                    + " characteristic:" + descriptor.getUuid().toString());
        }

        @Override
        public void onExecuteWrite(BluetoothDevice device, int requestId, boolean execute) {
            super.onExecuteWrite(device, requestId, execute);
            LogTool.LogE(TAG, " onExecuteWrite requestId:" + requestId + " execute:" + execute);
        }

        @Override
        public void onNotificationSent(BluetoothDevice device, int status) {
            super.onNotificationSent(device, status);
            LogTool.LogE(TAG, " onNotificationSent status:" + status);
        }

        @Override
        public void onMtuChanged(BluetoothDevice device, int mtu) {
            super.onMtuChanged(device, mtu);
            LogTool.LogE(TAG, " onMtuChanged mtu:" + mtu);
        }
    };


}
