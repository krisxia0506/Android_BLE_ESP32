package com.xiajiayi.ble_esp32;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    private static Context appContext;

    private BluetoothManager bluetoothManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        appContext = getApplicationContext();
        bluetoothManager = new BluetoothManager(this);
        bluetoothManager.checkAndRequestBluetoothPermission();
        bluetoothManager.enableBluetooth();

        // 获取按钮实例
        Button button1On = findViewById(R.id.button1_on);
        Button button1Off = findViewById(R.id.button1_off);
        Button button2On = findViewById(R.id.button2_on);
        Button button2Off = findViewById(R.id.button2_off);

        // 新增连接蓝牙按钮
        Button buttonConnectBluetooth = findViewById(R.id.button_connect_bluetooth);

        // 设置按钮点击事件监听器
        button1On.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bluetoothManager.sendHexData("01");
            }
        });

        button1Off.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bluetoothManager.sendHexData("02");
            }
        });

        button2On.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bluetoothManager.sendHexData("03");
            }
        });

        button2Off.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bluetoothManager.sendHexData("04");
            }
        });

        // 设置连接蓝牙按钮点击事件监听器
        buttonConnectBluetooth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 在这里处理连接蓝牙按钮的点击事件
                Log.i("BluetoothManager", "连接蓝牙按钮被点击");
                bluetoothManager.connectToDevice();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.i("BluetoothManager", "requestCode的值为：" + requestCode);
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                // Bluetooth is enabled
                // Connect to the Bluetooth device here if needed
                Log.i("BluetoothManager", "蓝牙已启动");
                Log.i("BluetoothManager", "连接蓝牙设备");
                bluetoothManager.connectToDevice();
            } else {
                Log.i("BluetoothManager", "蓝牙未启动");
                // User denied enabling Bluetooth or an error occurred
                // Handle accordingly
            }
        }
    }
    public static Context getAppContext() {
        return appContext;
    }
}
