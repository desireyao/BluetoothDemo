package com.beacool.bluetoothdemo.gatt;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanRecord;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.beacool.bluetoothdemo.advertise.Constants;
import com.beacool.bluetoothdemo.tools.LogTool;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yaoh on 2018/1/16.
 */

public class GattClientManager {
    private static final String TAG = "TAG";

    private BluetoothManager bluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothLeScanner mBluetoothLeScanner;

    private BluetoothGatt mBluetoothGatt;

    public GattClientManager(Context context) {
        bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
        mBluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();
    }

    public void startScanning(ScanCallback mScanCallback) {
        Log.d(TAG, "Starting Scanning");
        mBluetoothLeScanner.startScan(buildScanFilters(), buildScanSettings(), mScanCallback);
    }

    private List<ScanFilter> buildScanFilters() {
        List<ScanFilter> scanFilters = new ArrayList<>();
        ScanFilter.Builder builder = new ScanFilter.Builder();
        builder.setServiceUuid(Constants.Service_UUID);
        scanFilters.add(builder.build());
        return scanFilters;
    }

    private ScanSettings buildScanSettings() {
        ScanSettings.Builder builder = new ScanSettings.Builder();
        builder.setScanMode(ScanSettings.SCAN_MODE_LOW_POWER);
        return builder.build();
    }


    public void connectDevice(BluetoothDevice device, Context context) {
        mBluetoothGatt = device.connectGatt(context, false, mGattCallback);
        Log.d(TAG, "Trying to create a new connection.");
    }


    BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic, int status) {
            LogTool.LogE_DEBUG(TAG, "onCharacteristicRead");
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {
            LogTool.LogE_DEBUG(TAG, "onCharacteristicChanged");
            final byte[] data = characteristic.getValue();
            LogTool.LogD(TAG, LogTool.LogBytes2Hex(data, "recv Ori"));
        }

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status,
                                            int newState) {
            LogTool.LogE_DEBUG(TAG, "onConnectionStateChange--->status=" + status
                    + " newState=" + newState + " GATT_SUCCESS="
                    + BluetoothGatt.GATT_SUCCESS + " STATE_CONNECTED="
                    + BluetoothProfile.STATE_CONNECTED
                    + " STATE_DISCONNECTED="
                    + BluetoothProfile.STATE_DISCONNECTED);

        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            LogTool.LogE_DEBUG(TAG, "onServicesDiscovered--->status = " + status + " GATT_SUCCESS = " + BluetoothGatt.GATT_SUCCESS);

            if (status == BluetoothGatt.GATT_SUCCESS) {
                List<BluetoothGattService> listService = gatt.getServices();
                for (BluetoothGattService service : listService) {
                    LogTool.LogD(TAG, "----- service uuid : " + service.getUuid().toString());
                    List<BluetoothGattCharacteristic> listChara = service.getCharacteristics();
                    for (BluetoothGattCharacteristic characteristic : listChara) {
                        LogTool.LogD(TAG, "        chara uuid : "
                                + characteristic.getUuid().toString());
                    }
                }
            } else {
                gatt.disconnect();
                return;
            }
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt,
                                          BluetoothGattCharacteristic characteristic, int status) {
            LogTool.LogE_DEBUG(TAG, "onCharacteristicWrite status=" + status);
            super.onCharacteristicWrite(gatt, characteristic, status);
        }
    };


}
