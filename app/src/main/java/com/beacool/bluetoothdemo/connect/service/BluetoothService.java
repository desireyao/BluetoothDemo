package com.beacool.bluetoothdemo.connect.service;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Binder;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;

import com.beacool.bluetoothdemo.advertise.Constants;
import com.beacool.bluetoothdemo.advertise.ScannUtil;
import com.beacool.bluetoothdemo.chat.eventmsg.MessageEvent;
import com.beacool.bluetoothdemo.chat.eventmsg.NotifyBluetoothState;
import com.beacool.bluetoothdemo.tools.LogTool;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by yaoh on 2018/1/25.
 */

public class BluetoothService extends Service {
    private static final String TAG = "BluetoothService";

    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothLeScanner mBluetoothLeScanner;

    private String connectedAddress;
    private BluetoothGatt mBluetoothGatt;

    public BluetoothService() {
    }

    public class LocalBinder extends Binder {
        public BluetoothService getService() {
            return BluetoothService.this;
        }
    }

    private final IBinder mBinder = new LocalBinder();

    @Override
    public IBinder onBind(Intent intent) {
        Log.e(TAG, "onBind--->");
        return mBinder;
    }

    @Override
    public void onCreate() {
        Log.e(TAG, "onCreate--->");
        EventBus.getDefault().post(new MessageEvent(MessageEvent.NOTIFY_TYPE.SERVICE_ONCREATE));
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        LogTool.LogE(TAG, "onStartCommand--->");

        EventBus.getDefault().post(new MessageEvent(MessageEvent.NOTIFY_TYPE.SERVICE_ONSTART));
        return START_STICKY;
    }


    @Override
    public boolean onUnbind(Intent intent) {
        LogTool.LogE(TAG, "onUnbind ---> ");
        return super.onUnbind(intent);
    }

    final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                EventBus.getDefault().post(NotifyBluetoothState.NOTIFY_TYPE.CONNECT_SUCCEED);
                Log.e(TAG, "Connected to GATT server.");
                // Attempts to discover services after successful connection.
                Log.e(TAG, "Attempting to start service discovery:" + mBluetoothGatt.discoverServices());
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                EventBus.getDefault().post(NotifyBluetoothState.NOTIFY_TYPE.DISCONNECTED);
                Log.e(TAG, "Disconnected from GATT server.");
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            Log.e(TAG, "onServicesDiscovered status: " + status);

            if (status == BluetoothGatt.GATT_SUCCESS) {
            } else {
                Log.w(TAG, "onServicesDiscovered received: " + status);
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic, int status) {
            Log.d(TAG, "onCharacteristicRead:" + status);

            if (status == BluetoothGatt.GATT_SUCCESS) {

            }
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt,
                                          BluetoothGattCharacteristic characteristic, int status) {
            System.out.println("--------write success----- status:" + status);
        }

        ;

        /*
         * when connected successfully will callback this method
         * this method can dealwith send password or data analyze
         *
         * */
        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {
            LogTool.LogE(TAG, "onCharacteristicChanged --->");

        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt,
                                      BluetoothGattDescriptor descriptor, int status) {
            UUID uuid = descriptor.getCharacteristic().getUuid();
        }

        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
            System.out.println("rssi = " + rssi);
        }
    };

    public boolean initialize() {
        if (mBluetoothManager == null) {
            mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBluetoothManager == null) {
                Log.e(TAG, "Unable to initialize BluetoothManager.");
                return false;
            }
        }

        if (mBluetoothAdapter == null) {
            mBluetoothAdapter = mBluetoothManager.getAdapter();
            if (mBluetoothAdapter == null) {
                Log.e(TAG, "Unable to obtain a BluetoothAdapter.");
                return false;
            }
        }

        if (mBluetoothLeScanner == null) {
            mBluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();
            if (mBluetoothLeScanner == null) {
                Log.e(TAG, "Unable to obtain a mBluetoothLeScanner.");
                return false;
            }
        }

        return true;
    }

    /**
     * 连接设备
     *
     * @param address
     * @return
     */
    public boolean connect(final String address) {
        if (mBluetoothAdapter == null || address == null) {
            Log.w(TAG, "BluetoothAdapter not initialized or unspecified address.");
            return false;
        }

        // Previously connected device. Try to reconnect
        if (connectedAddress != null && address.equals(connectedAddress)
                && mBluetoothGatt != null) {
            EventBus.getDefault().post(NotifyBluetoothState.NOTIFY_TYPE.CONNECTING_EXIST_DEVICE);

            Log.d(TAG, "Trying to use an existing mBluetoothGatt for connection.");
            if (mBluetoothGatt.connect()) {
                return true;
            } else {
                return false;
            }
        }

        final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        if (device == null) {
            Log.d(TAG, "没有设备");
            return false;
        }

        EventBus.getDefault().post(NotifyBluetoothState.NOTIFY_TYPE.CONNECTING_NEW_DEVICE);
        mBluetoothGatt = device.connectGatt(this, false, mGattCallback);
        Log.d(TAG, "Trying to create a new connection.");

        connectedAddress = address;
        return true;
    }

    /**
     * 搜索设备
     *
     * @param enable
     */
    public void scanLeDevice(final boolean enable, LeScanCallback mLeScanCallback) {
        if (enable) {
            mBluetoothLeScanner.startScan(buildScanFilters(), buildScanSettings(), mLeScanCallback);
        } else {
            mBluetoothLeScanner.stopScan(mLeScanCallback);
        }
    }

    /**
     * 停止搜索设备
     *
     * @param mLeScanCallback
     */
    public void stopScanLeDevice(LeScanCallback mLeScanCallback) {
        mBluetoothLeScanner.stopScan(mLeScanCallback);
    }


    public static abstract class LeScanCallback extends ScanCallback {

        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            LogTool.LogE(TAG, "onScanResult = " + result.toString());

            onScanDevice(result.getDevice());
        }

        public abstract void onScanDevice(BluetoothDevice device);
    }

    /**
     * Return a List of {@link ScanFilter} objects to filter by Service UUID.
     */
    private List<ScanFilter> buildScanFilters() {
        List<ScanFilter> scanFilters = new ArrayList<>();
        ScanFilter.Builder builder = new ScanFilter.Builder();
        // Comment out the below line to see all BLE devices around you
//      builder.setServiceUuid(Constants.Service_UUID);
        scanFilters.add(builder.build());
        return scanFilters;
    }

    /**
     * Return a {@link ScanSettings} object set to use low power (to preserve battery life).
     */
    private ScanSettings buildScanSettings() {
        ScanSettings.Builder builder = new ScanSettings.Builder();
        builder.setScanMode(ScanSettings.SCAN_MODE_LOW_POWER);
        return builder.build();
    }


    /**
     * 断开设备
     */
    public void disconnect() {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }

        mBluetoothGatt.disconnect();
    }

    public void reConnectDevice() {
        if (!TextUtils.isEmpty(connectedAddress)) {
            connect(connectedAddress);
        }
    }
}
