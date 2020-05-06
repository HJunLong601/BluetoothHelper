package com.example.bluetoothhelper.bluetooth;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class BluetoothHelper extends Thread{

    public interface BluetoothListener{
        /**
         * 收到数据
         * @param data
         */
        void onReceiveMessage(String data);

        /**
         * 连接到设备
         */
        void onConnectDevice();

        /**
         * 断开连接
         */
        void onDisConnect();

        /**
         * 连接失败
         */
        void onConnectFail(String message);
    }

    private BluetoothSocket socket;
    private BluetoothDevice device;
    private static final String TAG = "BluetoothHelper";

    private InputStream inputStream;
    private OutputStream outputStream;

    private BluetoothListener bluetoothListener;

    private final int BUFFER_SIZE =1024*4;
    private volatile Boolean quitState = false;

    private final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    public BluetoothHelper(BluetoothDevice device,BluetoothListener bluetoothListener){
        this.device = device;
        this.bluetoothListener = bluetoothListener;
        try {
            socket = device.createRfcommSocketToServiceRecord(MY_UUID);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public BluetoothHelper(BluetoothSocket socket){
        this.socket = socket;

    }


    @Override
    public void run() {
        try {

            socket.connect();
            bluetoothListener.onConnectDevice();
            Log.i(TAG,"连接蓝牙设备");

            inputStream = socket.getInputStream();
            outputStream = socket.getOutputStream();

            byte[] buffer = new byte[BUFFER_SIZE];
            int bytes;

            while (!quitState){

                while (inputStream.read(buffer) != -1){
                    bytes = inputStream.read(buffer);
//                    bluetoothListener.onReceiveMessage(new String(buffer,0,bytes,"GBK"));
                    bluetoothListener.onReceiveMessage(new String(buffer,0,bytes));
                }

            }

        }catch (IOException e){
            e.printStackTrace();
            bluetoothListener.onConnectFail(e.getMessage());
        }
    }

    /**
     * 发送消息数据
     * @param msg
     */
    public boolean sendMsg(final String msg){
        if (outputStream != null){
            try {
                byte[] bytes = msg.getBytes("GBK");
                outputStream.write(bytes);
                return true;
            }catch (IOException e){
                e.printStackTrace();
                return false;
            }
        }

        return false;
    }
    /**
     * 断开蓝牙连接
     */
    public void disableConnected(){
        try {
            socket.close();
            bluetoothListener.onDisConnect();
            quitState = true;

        } catch (IOException e) {
            e.printStackTrace();
        }
    }



}
