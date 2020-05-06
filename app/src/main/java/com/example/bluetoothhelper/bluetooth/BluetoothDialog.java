package com.example.bluetoothhelper.bluetooth;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.app.DialogFragment;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import com.example.bluetoothhelper.R;

import java.util.ArrayList;

import androidx.annotation.Nullable;

public class BluetoothDialog extends DialogFragment implements AdapterView.OnItemClickListener {

    private Button cancel;
    private Button search;
    private Button disable;
    private ListView listView;


    private DialogListener listener;
    private ArrayList<String> deviceList;
    private ArrayList<BluetoothDevice> devices;

    private BluetoothListAdapter mAdapter;

    private static final String TAG = "BluetoothDialog";



    public interface DialogListener{
        void onCancelClick();
        void onSearchDevice();
        void onDisable();
        void onDevicesClick(AdapterView<?> parent, View view, int position, long id);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.DialogStyle);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_layout,container,false);
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);

        cancel = view.findViewById(R.id.dialog_cancel_btn);
        search = view.findViewById(R.id.dialog_search_btn);
        disable = view.findViewById(R.id.dialog_disable_btn);

        listView = view.findViewById(R.id.dialog_listview);

        listView.setOnItemClickListener(this);

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null){
                    listener.onCancelClick();
                }

            }
        });
        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null){
                    listener.onSearchDevice();
                }
            }
        });
        disable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null){
                    listener.onDisable();
                }

            }
        });

        getDialog().setCanceledOnTouchOutside(true);
        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        return view;
    }


    @Override
    public void onStart() {
        super.onStart();
        if (mAdapter != null){
            listView.setAdapter(mAdapter);
            Log.i(TAG,"setAdapter");
        }

//        animator.start();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (listener != null){
            listener.onDevicesClick(parent,view,position,id);
        }

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);


    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mAdapter = new BluetoothListAdapter(view.getContext(),R.layout.item_listview,deviceList);

    }

    public void setListener(DialogListener listener) {
        this.listener = listener;
    }
    public void setDeviceList(ArrayList<String> deviceList) {
        this.deviceList = deviceList;
        if (mAdapter != null) {
            mAdapter.notifyDataSetChanged();
        }

    }
    public void setDevices(ArrayList<BluetoothDevice> devices) {
        this.devices = devices;
    }
    public ArrayAdapter<String> getmAdapter() {
        return mAdapter;
    }
}

