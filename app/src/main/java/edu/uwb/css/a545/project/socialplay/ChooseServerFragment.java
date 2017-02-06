package edu.uwb.css.a545.project.socialplay;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.Set;
import java.util.UUID;

import static android.R.attr.filter;


/**
 * Created by Jeremy Woods on 1/22/2017.
 */

public class ChooseServerFragment extends Fragment {

    public interface GameListener {
        void createGame();
    }

    private static final String EXTRA_DEVICE_ADDRESS = "device_address";
    ArrayAdapter<String> listAdapter;
    IntentFilter filter;
    BroadcastReceiver receiver;
    ListView list;
    BluetoothAdapter btAdapter;
    Charades game;

    private static final UUID MY_UUID = UUID.fromString("a4e3e0b2-eb83-11e6-b006-92361f002671");

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.choose_server_page, container, false);

        listAdapter=new ArrayAdapter<>(this.getContext(),android.R.layout.simple_list_item_1,0);
        filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        list = (ListView) view.findViewById(R.id.deviceList);
        list.setAdapter(listAdapter);
        list.setOnItemClickListener(mDeviceClickListener);


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

        btAdapter = BluetoothAdapter.getDefaultAdapter();
        for (BluetoothDevice device : btAdapter.getBondedDevices()) {
            listAdapter.add(device.getName() + "\n" + device.getAddress());
        }

        filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        getActivity().registerReceiver(receiver,filter);

        btAdapter = BluetoothAdapter.getDefaultAdapter();
        btAdapter.startDiscovery();
        return view;
    }

    private AdapterView.OnItemClickListener mDeviceClickListener
            = new AdapterView.OnItemClickListener() {
        public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {
            // Cancel discovery because it's costly and we're about to connect
            btAdapter.cancelDiscovery();

            // Get the device MAC address, which is the last 17 chars in the View
            String info = ((TextView) v).getText().toString();
            String address = info.substring(info.length() - 17);

            // Create the result Intent and include the MAC address
            Intent intent = new Intent();
            intent.putExtra(EXTRA_DEVICE_ADDRESS, address);

            // Set result and finish this Activity
            getActivity().setResult(Activity.RESULT_OK, intent);

            BluetoothSocket bTSocket;
            BluetoothSocket tmp = null;
            try {
                tmp = btAdapter.getRemoteDevice(address).createRfcommSocketToServiceRecord(MY_UUID);
                Toast.makeText(getActivity(), "Joined " + btAdapter.getRemoteDevice(address).getName() + "'s game", Toast.LENGTH_SHORT)
                        .show();
            } catch (IOException e) {}

            bTSocket = tmp;

            try {
                bTSocket.connect();
            } catch (IOException e) {}

            game = new Charades();
            game.setSocket(bTSocket);
            game.isServer(false);
            getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.fragment_holder, game).addToBackStack(null).commit();

//            GameListener parent = (GameListener) getActivity();
//            parent.createGame();
        }
    };
}
