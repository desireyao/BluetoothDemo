package com.beacool.bluetoothdemo;

import android.Manifest;
import android.bluetooth.le.ScanRecord;
import android.bluetooth.le.ScanResult;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.yxp.permission.util.lib.PermissionUtil;
import com.yxp.permission.util.lib.callback.PermissionResultAdapter;

import java.text.SimpleDateFormat;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = MainActivity.class.getSimpleName();

    private Button btn_start_ad;
    private Button btn_search;
    private Button btn_reset;

    private TextView tv_content;
    private EditText edit_frequency;

    private AdvertiserUtil advertiserServiceUtil;
    private ScannUtil scannUtil;

    private byte mDATA = 0x00;

    private StringBuffer contentStringBuffer;
    private int mCount;

    @Override
    protected void onResume() {
        super.onResume();

//        PermissionUtil.getInstance().request(new String[]{
//                        Manifest.permission.ACCESS_FINE_LOCATION,
//                        Manifest.permission.ACCESS_COARSE_LOCATION},
//
//                new PermissionResultAdapter() {
//                    @Override
//                    public void onPermissionGranted(String... permissions) {
//                        Toast.makeText(MainActivity.this, permissions[0] + " is granted", Toast.LENGTH_SHORT).show();
//                    }
//
//                    @Override
//                    public void onPermissionDenied(String... permissions) {
//                        Toast.makeText(MainActivity.this, permissions[0] + " is denied", Toast.LENGTH_SHORT).show();
//                    }
//
//                    @Override
//                    public void onRationalShow(String... permissions) {
//                        Toast.makeText(MainActivity.this, permissions[0] + " is rational", Toast.LENGTH_SHORT).show();
//
//                    }
//                });
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
//        advertiserServiceUtil = new AdvertiserUtil(getApplicationContext());
        scannUtil = new ScannUtil(this.getApplicationContext());
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btn_start_ad) {
            contentStringBuffer = new StringBuffer();
            mDATA = 0;
            mCount = 0;
            startAd();
        } else if (id == R.id.btn_search) {
            contentStringBuffer = new StringBuffer();
            mCount = 0;
            contentStringBuffer.append("开始搜索广播---> \n ");
            tv_content.setText(contentStringBuffer.toString());

            scannUtil.setScanCallback(new ScannUtil.ScannListener() {
                @Override
                public void onScann(ScanResult result) {
                    mCount++;
                    ScanRecord record = result.getScanRecord();
                    String time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS").format(System.currentTimeMillis());
                    byte[] datas = record.getBytes();
                    contentStringBuffer.append("search data = " + datas[30]
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
            mCount = 0;
            tv_content.setText(contentStringBuffer.toString());
        }
    }

    public void startAd() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (mDATA < 1000) {
                    mDATA++;
                    byte[] datas = new byte[1];
                    datas[0] = (byte) (mDATA % 100);

                    int frequency = Integer.parseInt(edit_frequency.getText().toString());
                    advertiserServiceUtil = new AdvertiserUtil(getApplicationContext());
                    advertiserServiceUtil.setAdVertiseListener(new AdvertiserUtil.AdVertiseListener() {
                        @Override
                        public void onAdvertiseListener(byte[] data) {
                            mCount++;
                            contentStringBuffer.append("advertisedata:" + data[0]
                                    + " | mCount = " + mCount
                                    + " | time = " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS").format(System.currentTimeMillis()) + "\n");
                            tv_content.setText(contentStringBuffer.toString());
                        }
                    });
                    advertiserServiceUtil.startAd(datas);

                    try {
                        Thread.sleep(frequency);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }
}
