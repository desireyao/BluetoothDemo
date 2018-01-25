package com.beacool.bluetoothdemo;

import android.Manifest;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.beacool.bluetoothdemo.advertise.AdvertiseMainActivity;
import com.beacool.bluetoothdemo.chat.ChatActivity;
import com.beacool.bluetoothdemo.connect.BluetoothActivity;
import com.beacool.bluetoothdemo.gatt.GattMainActivity;
import com.yxp.permission.util.lib.PermissionUtil;
import com.yxp.permission.util.lib.callback.PermissionResultAdapter;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Button btn_advertise_activity;
    private Button btn_gatt_activity;
    private Button btn_chat_activity;
    private Button btn_connect_activity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btn_advertise_activity = findViewById(R.id.btn_advertise_activity);
        btn_advertise_activity.setOnClickListener(this);

        btn_gatt_activity = findViewById(R.id.btn_gatt_activity);
        btn_gatt_activity.setOnClickListener(this);

        btn_chat_activity = findViewById(R.id.btn_chat_activity);
        btn_chat_activity.setOnClickListener(this);

        btn_connect_activity = findViewById(R.id.btn_connect_activity);
        btn_connect_activity.setOnClickListener(this);

    }

    @Override
    protected void onResume() {
        super.onResume();
        requestPermissons();
    }

    private void requestPermissons() {
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
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btn_advertise_activity) {
            startActivity(new Intent(this, AdvertiseMainActivity.class));
        } else if (id == R.id.btn_gatt_activity) {
            startActivity(new Intent(this, GattMainActivity.class));
        }else if(id == R.id.btn_chat_activity){
            startActivity(new Intent(this, ChatActivity.class));
        }else if(id == R.id.btn_connect_activity){
            startActivity(new Intent(this, BluetoothActivity.class));
        }
    }
}
