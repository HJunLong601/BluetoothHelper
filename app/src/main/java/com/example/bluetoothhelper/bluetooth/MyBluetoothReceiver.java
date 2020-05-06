package com.example.bluetoothhelper.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class MyBluetoothReceiver extends BroadcastReceiver {

    private static final String TAG = "MyBluetoothReceiver";

    private BroadcastListener listener;

    public interface BroadcastListener {
        void onFoundDevice(BluetoothDevice device);
    }


    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
            Log.d(TAG, "开始扫描...");
        }

        if (BluetoothDevice.ACTION_FOUND.equals(action)) {
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            if (device != null) {
                listener.onFoundDevice(device);
            }
        }

        if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
            Log.d(TAG, "扫描结束.");
        }
    }

    public void setListener(BroadcastListener listener) {
        this.listener = listener;
    }

}
