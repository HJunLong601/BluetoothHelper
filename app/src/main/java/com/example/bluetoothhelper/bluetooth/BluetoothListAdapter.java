package com.example.bluetoothhelper.bluetooth;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;


import com.example.bluetoothhelper.R;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class BluetoothListAdapter extends ArrayAdapter<String> {

    private int resourceId;

    public BluetoothListAdapter(Context context, int textViewResourceId, @NonNull List<String> objects){
        super(context,textViewResourceId,objects);
        resourceId = textViewResourceId;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        String name = getItem(position);
        View view ;
        ViewHolder viewHolder;
        if (convertView == null){
            view = LayoutInflater.from(getContext()).inflate(resourceId,parent,false);
            viewHolder = new ViewHolder();
            viewHolder.deviceName = view.findViewById(R.id.device_name_tv);
            view.setTag(viewHolder);

        }else{
            view = convertView;
            viewHolder = (ViewHolder) view.getTag();
        }
        viewHolder.deviceName.setText(name);
        Log.i("Adapter","getView");
        return view;
    }

    @Override
    public int getCount() {
        return super.getCount();
    }

    class ViewHolder {
        //ImageView Image;
        TextView deviceName;
    }

}
