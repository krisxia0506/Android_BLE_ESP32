package com.xiajiayi.ble_esp32;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;

public class BluetoothManager {

    private static final String TAG = "BluetoothManager";

    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    // 要连接的蓝牙的mac地址
    private static final String DEVICE_ADDRESS = "08:D1:F9:E7:3A:E6";

    private Context context;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothDevice connectedDevice;
    private BluetoothSocket bluetoothSocket;
    private OutputStream outputStream;

    private Handler handler;
    private static final long RECONNECT_INTERVAL = 5000; // 重连间隔，单位为毫秒
    private ActivityResultLauncher<Intent> enableBluetoothLauncher;


    public BluetoothManager(Context context) {
        Log.i(TAG, "BluetoothManager实例化");
        // 获取全局的 Application Context
        this.context = context;
        handler = new Handler(Looper.getMainLooper());
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        // 初始化 enableBluetoothLauncher
        if (context instanceof AppCompatActivity) {
            AppCompatActivity appCompatActivity = (AppCompatActivity) context;
            enableBluetoothLauncher = appCompatActivity.registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    // 蓝牙已启动，可以执行相应的操作
                    Log.i(TAG, "蓝牙已启动");
                    connectToDevice(); // 或者在这里执行其他相关操作
                } else {
                    Log.i(TAG, "蓝牙未启动");
                    // 处理用户拒绝启动蓝牙或发生错误
                    // 适当处理
                }
            });
        }
    }

    // 检查蓝牙权限
    private boolean checkBluetoothPermission() {
        Log.i(TAG, "检查蓝牙权限");

        // 检查 BLUETOOTH 权限
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED) {
            Log.i(TAG, "缺少 BLUETOOTH 权限");
            return false;
        } else {
            Log.i(TAG, "已获得 BLUETOOTH 权限");
        }

        // 检查 BLUETOOTH_ADMIN 权限
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED) {
            Log.i(TAG, "缺少 BLUETOOTH_ADMIN 权限");
            return false;
        } else {
            Log.i(TAG, "已获得 BLUETOOTH_ADMIN 权限");
        }

        // 检查 BLUETOOTH_CONNECT 权限
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            Log.i(TAG, "缺少 BLUETOOTH_CONNECT 权限");
            return false;
        } else {
            Log.i(TAG, "已获得 BLUETOOTH_CONNECT 权限");
        }

        return true;
    }


    // 请求蓝牙权限
    private void requestBluetoothPermission() {
        Log.i(TAG, "申请蓝牙权限");
        String[] permissions = {Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_ADMIN, Manifest.permission.BLUETOOTH_CONNECT};
        ActivityResultLauncher<String[]> requestPermissionLauncher = ((MainActivity) context).registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), isGranted -> {
            if (isGranted.get(Manifest.permission.BLUETOOTH) && isGranted.get(Manifest.permission.BLUETOOTH_ADMIN) && isGranted.get(Manifest.permission.BLUETOOTH_CONNECT)) {
                // 权限被授予，可以执行相应的操作
                Log.i(TAG, "蓝牙权限已授予");
            } else {
                // 权限被拒绝，可以做一些提示或处理
                Log.i(TAG, "蓝牙权限被拒绝");
            }
        });

        requestPermissionLauncher.launch(permissions);
    }


    // 检查并请求蓝牙权限
    public void checkAndRequestBluetoothPermission() {
        Log.i(TAG, "检查并请求蓝牙权限");
        if (!checkBluetoothPermission()) {
            Log.i(TAG, "没有蓝牙权限");
            requestBluetoothPermission();
        }
        Log.i(TAG, "有蓝牙权限");
    }

    // 启动蓝牙
    // 启动蓝牙
    public void enableBluetooth() {
        Log.i(TAG, "尝试启动蓝牙");
        if (!bluetoothAdapter.isEnabled()) {
            Log.i(TAG, "蓝牙未启动");
            if (checkBluetoothPermission()) {
                // 如果有权限，启动蓝牙
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                enableBluetoothLauncher.launch(enableBtIntent);
            } else {
                // 如果没有蓝牙权限，可以在这里请求权限
                requestBluetoothPermission();
            }
        } else {
            Log.i(TAG, "蓝牙已启动");
            // 蓝牙已启动，可以执行相应的操作
        }
    }


    // 连接蓝牙设备
    public void connectToDevice() {
        Log.i(TAG, "连接蓝牙设备");
        if (!checkBluetoothPermission()) {
            Log.i(TAG, "连接设备时，没有蓝牙权限");
            // 如果没有蓝牙权限，可以在这里请求权限
            requestBluetoothPermission();
            return;
        }
        Log.i(TAG, "连接设备时，有蓝牙权限");

        if (!bluetoothAdapter.isEnabled()) {
            // 如果蓝牙未启动，可以在这里启动蓝牙
            Log.i(TAG, "连接设备时，没有启动蓝牙");
            enableBluetooth();
            return;
        }
        Log.i(TAG, "连接设备时，蓝牙已经启动");

        BluetoothDevice device = bluetoothAdapter.getRemoteDevice(DEVICE_ADDRESS);

        try {
            bluetoothSocket = device.createRfcommSocketToServiceRecord(MY_UUID);
            bluetoothSocket.connect();
            connectedDevice = device;
            outputStream = bluetoothSocket.getOutputStream();
            Log.d(TAG, "已连接上设备" + device.getName());
            Log.i(TAG, "启动重连任务");
            startReconnectTask();

        } catch (IOException e) {
            Log.e(TAG, "蓝牙连接出错: " + e.getMessage());
            closeConnection();
        }
    }


    // 发送HEX编码的蓝牙数据
    public void sendHexData(String hexData) {
        Log.i(TAG, "发送HEX编码的蓝牙数据" + hexData);
        if (outputStream != null) {
            try {
                byte[] data = hexStringToByteArray(hexData);
                Log.i(TAG, "发送真实的蓝牙数据" + data);
                outputStream.write(data);
                Log.d(TAG, "Data sent: " + hexData);
            } catch (IOException e) {
                Log.e(TAG, "Error sending data: " + e.getMessage());
            }
        }
    }

    // 关闭蓝牙连接
    public void closeConnection() {
        try {
            if (outputStream != null) {
                outputStream.close();
            }
            if (bluetoothSocket != null) {
                bluetoothSocket.close();
            }
            connectedDevice = null;
            Log.d(TAG, "Bluetooth connection closed");
        } catch (IOException e) {
            Log.e(TAG, "Error closing connection: " + e.getMessage());
        }
    }

    // 将十六进制字符串转换为字节数组
    private byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }

    // 启动重连任务
    private void startReconnectTask() {
        handler.postDelayed(reconnectRunnable, RECONNECT_INTERVAL);
    }

    // 重连任务
    private Runnable reconnectRunnable = new Runnable() {
        @Override
        public void run() {
            // 在这里执行重连蓝牙的逻辑
            reconnectBluetooth();

            // 完成后再次调度任务，实现循环
            handler.postDelayed(this, RECONNECT_INTERVAL);
        }
    };

    // 重连蓝牙
    private void reconnectBluetooth() {
        // 在这里实现重连蓝牙的逻辑
        // 例如，检查连接状态，如果连接断开，则尝试重新连接
        if (!isBluetoothConnected()) {
            Log.i(TAG, "蓝牙连接断开，尝试重新连接...");
            // 关闭旧的连接
            closeConnection();
            connectToDevice();
        }

    }

    // 检查蓝牙是否连接
    private boolean isBluetoothConnected() {
        if (connectedDevice != null && bluetoothSocket != null) {
            try {
                // 通过发送一个空的数据来检测连接状态
                outputStream.write(new byte[0]);
                Log.i(TAG, "reconnectBluetooth蓝牙连接正常");
                return true;
            } catch (IOException e) {
                // 捕获到异常表示连接已经断开
                Log.i(TAG, "reconnectBluetooth蓝牙连接异常");
                return false;
            }
        }
        return false;
    }


    // ...（其他方法）

    // 关闭定时任务，通常在不需要蓝牙时调用，如退出应用
    public void stopReconnectTask() {
        handler.removeCallbacks(reconnectRunnable);
    }
}
