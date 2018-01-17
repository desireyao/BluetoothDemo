package com.beacool.bluetoothdemo.advertise;

import android.bluetooth.le.ScanRecord;
import android.bluetooth.le.ScanResult;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.beacool.bluetoothdemo.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class AdvertiseMainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = AdvertiseMainActivity.class.getSimpleName();

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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.adverise_activity_main);

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
        mDATA = 0;
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

            advertiserServiceUtil.startAdvertising(datas);
        }
    }
}
