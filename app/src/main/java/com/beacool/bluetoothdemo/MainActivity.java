package com.beacool.bluetoothdemo;

import android.Manifest;
import android.bluetooth.le.ScanRecord;
import android.bluetooth.le.ScanResult;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.yxp.permission.util.lib.PermissionUtil;
import com.yxp.permission.util.lib.callback.PermissionResultAdapter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = MainActivity.class.getSimpleName();

    private Button btn_start_ad;
    private Button btn_search;
    private Button btn_reset;

    private TextView tv_content;
    private EditText edit_frequency;

    private AdvertiserUtil advertiserServiceUtil;
    private ScannUtil scannUtil;

    private int mDATA = 0;

    private StringBuffer contentStringBuffer;
    private int mCount;
    private int mFrequency;
    private byte[] datas; // 蓝牙广播包数据

    private ScheduledExecutorService executor;

    @Override
    protected void onResume() {
        super.onResume();

        PermissionUtil.getInstance().request(new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION},

                new PermissionResultAdapter() {
                    @Override
                    public void onPermissionGranted(String... permissions) {
                        Toast.makeText(MainActivity.this, permissions[0] + " is granted", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onPermissionDenied(String... permissions) {
                        Toast.makeText(MainActivity.this, permissions[0] + " is denied", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onRationalShow(String... permissions) {
                        Toast.makeText(MainActivity.this, permissions[0] + " is rational", Toast.LENGTH_SHORT).show();

                    }
                });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btn_start_ad = findViewById(R.id.btn_start_ad);
        btn_search = findViewById(R.id.btn_search);
        btn_reset = findViewById(R.id.btn_reset);

        btn_start_ad.setOnClickListener(this);
        btn_search.setOnClickListener(this);
        btn_reset.setOnClickListener(this);

        tv_content = findViewById(R.id.tv_content);
        edit_frequency = findViewById(R.id.edit_frequency);

        initData();
    }

    private void initData() {
        advertiserServiceUtil = new AdvertiserUtil(getApplicationContext());
        scannUtil = new ScannUtil(this.getApplicationContext());
    }

    private ArrayList<Integer> mDataCaCheList;

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btn_start_ad) {
            contentStringBuffer = new StringBuffer();
            mDATA = 0;
            mCount = 0;
            mFrequency = Integer.parseInt(edit_frequency.getText().toString());
//            advertiserServiceUtil.setFrequecy(frequency);

//            startAd();
            executeRateBluetoothAd();
        } else if (id == R.id.btn_search) {
            contentStringBuffer = new StringBuffer();
            mDataCaCheList = new ArrayList<>();

            mCount = 0;
            contentStringBuffer.append("开始搜索广播---> \n ");
            tv_content.setText(contentStringBuffer.toString());

            scannUtil.setScanCallback(new ScannUtil.ScannListener() {
                @Override
                public void onScann(ScanResult result) {
                    ScanRecord record = result.getScanRecord();
                    String time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS").format(System.currentTimeMillis());
                    byte[] datas = ScannUtil.parseAdverData(record.getBytes());
                    int data1 = datas[0] << 8;
                    int data2 = datas[1] << 4;
                    int data3 = datas[2];
                    int data = data1 + data2 + data3;

                    if (mDataCaCheList.contains(data)) {
                        return;
                    }

                    mCount++;
                    mDataCaCheList.add(data);

                    contentStringBuffer.append("search data = " + data
                            + " | name =" + record.getDeviceName()
                            + " | rssi = " + result.getRssi()
                            + " | mCount = " + mCount
                            + " \n  time = " + time
                            + " \n");
                    tv_content.setText(contentStringBuffer.toString());
                }
            });
            scannUtil.startScanning();
        } else if (id == R.id.btn_reset) {
            contentStringBuffer = new StringBuffer();
            mDataCaCheList = new ArrayList<>();
            mCount = 0;
            tv_content.setText(contentStringBuffer.toString());
            advertiserServiceUtil.stopAdvertising();
            if (executor != null)
                executor.shutdown();
        }
    }

    /**
     * 蓝牙以固定频率开始广播
     */
    public void executeRateBluetoothAd() {
        executor = Executors.newScheduledThreadPool(1);
        executor.scheduleAtFixedRate(new Task(), 100, mFrequency,
                TimeUnit.MILLISECONDS);
    }

    class Task implements Runnable {

        @Override
        public void run() {
            // 停止上次的广播
            advertiserServiceUtil.stopAdvertising();
            mDATA++;

            if (mDATA > 50) {
                executor.shutdown();
                return;
            }

            datas = new byte[3];
            datas[0] = (byte) (mDATA >> 8 & 0x0f);
            datas[1] = (byte) (mDATA >> 4 & 0x0f);
            datas[2] = (byte) (mDATA & 0x0f);

            advertiserServiceUtil.setAdVertiseListener(new AdvertiserUtil.AdVertiseListener() {
                @Override
                public void onAdvertiseListener(byte[] datas) {
                    mCount++;
                    int data1 = datas[0] << 8;
                    int data2 = datas[1] << 4;
                    int data3 = datas[2];
                    int data = data1 + data2 + data3;
                    contentStringBuffer.append("advertisedata:" + data
                            + " | mCount = " + mCount
                            + " | time = " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS").format(System.currentTimeMillis()) + "\n");
                    tv_content.setText(contentStringBuffer.toString());
                }
            });

            advertiserServiceUtil.startAdvertising(datas);
        }
    }
}
