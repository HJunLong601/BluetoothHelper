package com.example.bluetoothhelper;

import android.Manifest;
import android.app.FragmentManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.bluetoothhelper.bluetooth.BluetoothDialog;
import com.example.bluetoothhelper.bluetooth.BluetoothHelper;
import com.example.bluetoothhelper.bluetooth.MyBluetoothReceiver;

import java.util.ArrayList;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class MainActivity extends AppCompatActivity implements MyBluetoothReceiver.BroadcastListener {

    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 61;
    private static final int REQUEST_ENABLE_CODE = 62;

    private Button sendBtn;
    private TextView blueStateTv;
    private EditText sendEt;
    private EditText receiveEt;
    private TextView rxTv;
    private TextView txTv;

    int tx_size = 0; //发送的字符数
    int rx_size = 0;

    private MyBluetoothReceiver myBluetoothReceiver;
    private BluetoothDialog dialog ;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothDevice mConnectedDev;
    private BluetoothHelper helper;

    private ArrayList<String> deviceList = new ArrayList<>();
    private ArrayList<BluetoothDevice> devices = new ArrayList<>();

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //动态申请权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_COARSE_LOCATION);
            }
        }

        myBluetoothReceiver = new MyBluetoothReceiver();
        myBluetoothReceiver.setListener(this);
        blueStateTv = findViewById(R.id.search_state);
        sendBtn = findViewById(R.id.send_msg);
        sendEt = findViewById(R.id.send_text);
        receiveEt = findViewById(R.id.recive_text);
        rxTv = findViewById(R.id.rx_text);
        txTv = findViewById(R.id.tx_text);

        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mBluetoothAdapter != null && helper != null){
                    if (helper.sendMsg(sendEt.getText().toString())){
                        tx_size = sendEt.getText().length()+tx_size;
                        txTv.setText("TX: " + tx_size);

                        sendEt.setText("");
                        Toast.makeText(MainActivity.this, "已发送", Toast.LENGTH_SHORT).show();
                    }else {
                        Toast.makeText(MainActivity.this, "失败", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        initDialog();
    }

    private void initDialog() {
        dialog = new BluetoothDialog();
        dialog.setDeviceList(deviceList);
        dialog.setListener(new BluetoothDialog.DialogListener() {
            @Override
            public void onCancelClick() {
                if (mBluetoothAdapter != null && mBluetoothAdapter.isDiscovering()){
                    mBluetoothAdapter.cancelDiscovery();
                }
                if (dialog != null){
                    dialog.dismiss();
                }
            }

            @Override
            public void onSearchDevice() {
                initDevice();
                deviceList.clear();
                devices.clear();
                dialog.getmAdapter().notifyDataSetChanged();
                if (mBluetoothAdapter != null && mBluetoothAdapter.isDiscovering() ){
                    mBluetoothAdapter.cancelDiscovery();
                }

                if (mBluetoothAdapter != null){
                    mBluetoothAdapter.startDiscovery();
                    Toast.makeText(MainActivity.this,"正在搜索蓝牙设备...",Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onDisable() {
                //关闭线程
                if (helper!= null){
                    helper.disableConnected();
                }
                helper = null;
            }

            @Override
            public void onDevicesClick(AdapterView<?> parent, View view, int position, long id) {
                if (mBluetoothAdapter != null && mBluetoothAdapter.isDiscovering() ){
                    mBluetoothAdapter.cancelDiscovery();
                }

                if (helper != null){//避免线程重复开启闪退
                    Toast.makeText(MainActivity.this,"蓝牙已连接或正在连接中...",Toast.LENGTH_SHORT).show();
                }else{
                    mConnectedDev = devices.get(position);
                    connectDevice(mConnectedDev);
                }
            }
        });
    }

    /**
     * 菜单加载和点击事件
     */

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.search_blue:
                FragmentManager fragmentManager = getFragmentManager();
                dialog.show(fragmentManager,"MyDialogFragment");
                break;
            case R.id.cancel_search:
                if (mBluetoothAdapter != null && mBluetoothAdapter.isDiscovering()){
                    mBluetoothAdapter.cancelDiscovery();
                }
                break;
            case R.id.disabled_connected:
                if (helper!= null){
                    helper.disableConnected();
                }
                //关闭线程
                helper = null;
                break;
            case R.id.copy_recived:
                ClipboardManager cm = (ClipboardManager)getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData mClipData = ClipData.newPlainText("temp",receiveEt.getText());
                cm.setPrimaryClip(mClipData);
                Toast.makeText(MainActivity.this,"内容已复制到粘贴板",Toast.LENGTH_SHORT).show();
                break;

            default:
        }
        return true;
    }


    /**
     *连接蓝牙设备
     */

    private void connectDevice(BluetoothDevice device){

        if (helper == null) {
            helper = new BluetoothHelper(device, new BluetoothHelper.BluetoothListener() {
                @Override
                public void onReceiveMessage(final String data) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            receiveEt.setText(receiveEt.getText().append(data));

                            rx_size = receiveEt.getText().length();
                            rxTv.setText("RX: "+rx_size);
                        }
                    });
                }

                @Override
                public void onConnectDevice() {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            dialog.dismiss();
                            blueStateTv.setText("蓝牙已连接到："+mConnectedDev.getName());
                            Toast.makeText(MainActivity.this,"蓝牙已连接",Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                @Override
                public void onDisConnect() {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            blueStateTv.setText("蓝牙已断开");
                            Toast.makeText(MainActivity.this,"蓝牙已断开",Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                @Override
                public void onConnectFail(String message) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            blueStateTv.setText("蓝牙连接失败");
                            Toast.makeText(MainActivity.this,"蓝牙连接失败",Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });
        }

        helper.start();

    }

    @Override
    protected void onResume() {
        super.onResume();

        // 注册广播接收器。
        // 接收蓝牙发现
        IntentFilter filterFound = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(myBluetoothReceiver, filterFound);

        IntentFilter filterStart = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        registerReceiver(myBluetoothReceiver, filterStart);

        IntentFilter filterFinish = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(myBluetoothReceiver, filterFinish);

    }

    @Override
    protected void onPause() {
        super.onPause();
        // 保证一定可以取消注册
        unregisterReceiver(myBluetoothReceiver);
    }


    /**
     * 初始化 打开蓝牙
     */
    private void initDevice (){

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // 检查设备是否支持蓝牙设备
        if (mBluetoothAdapter == null) {
            Log.d(TAG, "设备不支持蓝牙");
            // 不支持蓝牙，退出。
            return;
        }
        // 如果用户的设备没有开启蓝牙，则弹出开启蓝牙设备的对话框，让用户开启蓝牙
        if (!mBluetoothAdapter.isEnabled()) {
            Log.d(TAG, "请求用户打开蓝牙");

            Intent enabler = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enabler, REQUEST_ENABLE_CODE);
            // 接下去，在onActivityResult回调判断
        }
    }

    /**
     * 权限请求结果
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_COARSE_LOCATION:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this,"权限获取成功",Toast.LENGTH_SHORT).show();
                }

                break;
        }
    }

    /**
     * 蓝牙打开结果
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_ENABLE_CODE) {
            if (resultCode == RESULT_OK) {
                Log.d(TAG, "打开蓝牙成功！");
                Toast.makeText(this,"蓝牙已开启",Toast.LENGTH_SHORT).show();
            }

            if (resultCode == RESULT_CANCELED) {
                Log.d(TAG, "放弃打开蓝牙！");
                Toast.makeText(this,"请开启蓝牙",Toast.LENGTH_SHORT).show();
            }

        } else {
            Log.d(TAG, "蓝牙异常！");
            Toast.makeText(this,"蓝牙异常！开启失败！",Toast.LENGTH_SHORT).show();
        }
    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (helper != null){
            helper.disableConnected();
        }

    }

    /**
     * 广播接收器发现蓝牙设备
     * @param device
     */
    @Override
    public void onFoundDevice(BluetoothDevice device) {
        if (device.getName() != null ){
            //对蓝牙设备进行过滤
            if (device.getName().trim().length() == 0){
                deviceList.add("无名称设备: " + device.getAddress());
            }else {
                deviceList.add(device.getName());
            }

            devices.add(device);
        }

        if (dialog != null){
            dialog.getmAdapter().notifyDataSetChanged();
        }
    }
}
