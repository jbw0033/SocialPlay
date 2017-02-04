package edu.uwb.css.a545.project.socialplay;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.Set;

import static android.R.attr.filter;


/**
 * Created by Jeremy Woods on 1/22/2017.
 */

public class ChooseGameFragment extends Fragment {

    ArrayAdapter<String> listAdapter;
    IntentFilter filter;
    BroadcastReceiver receiver;
    ListView list;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.choose_server_page, container, false);

        listAdapter=new ArrayAdapter<>(this.getContext(),android.R.layout.simple_list_item_1,0);
        filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        list = (ListView) view.findViewById(R.id.deviceList);
        list.setAdapter(listAdapter);
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if(BluetoothDevice.ACTION_FOUND.equals(action)){
                    BluetoothDevice device = intent.getParcelableExtra( BluetoothDevice.EXTRA_DEVICE);
                    listAdapter.add(device.getName()+"\n"+device.getAddress());
                }
            }
        };
        filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        getActivity().registerReceiver(receiver,filter);

        BluetoothAdapter btAdapter=BluetoothAdapter.getDefaultAdapter();
        btAdapter.startDiscovery();
        return view;
    }
}
