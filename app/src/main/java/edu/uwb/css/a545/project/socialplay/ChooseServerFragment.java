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
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AlertDialog;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import static android.R.attr.filter;


/**
 * Created by Jeremy Woods on 1/22/2017.
 */

public class ChooseServerFragment extends Fragment {

    interface GameListener {
        void createGame(BluetoothChatService service);
    }

    // Intent request codes
    private static final int REQUEST_CONNECT_DEVICE_SECURE = 1;
    private static final int REQUEST_CONNECT_DEVICE_INSECURE = 2;
    private static final int REQUEST_ENABLE_BT = 3;

    public static final String EXTRA_DEVICE_ADDRESS = "device_address";
    ArrayAdapter<String> listAdapter;
    IntentFilter filter;
    ListView list;

    /**
     * Local Bluetooth adapter
     */
    BluetoothAdapter mBtAdapter = null;

    BroadcastReceiver receiver;

    /**
     * Member object for the chat services
     */
    private BluetoothChatService mChatService = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        // Get local Bluetooth adapter
        mBtAdapter = BluetoothAdapter.getDefaultAdapter();

        // If the adapter is null, then Bluetooth is not supported
        if (mBtAdapter == null) {
            FragmentActivity activity = getActivity();
            Toast.makeText(activity, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            activity.finish();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        // If BT is not on, request that it be enabled.
        // setupChat() will then be called during onActivityResult
        if (!mBtAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
            // Otherwise, setup the chat session
        } else if (mChatService == null) {
            setupChat();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mChatService != null) {
            mChatService.stop();
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        // Performing this check in onResume() covers the case in which BT was
        // not enabled during onStart(), so we were paused to enable it...
        // onResume() will be called when ACTION_REQUEST_ENABLE activity returns.
        if (mChatService != null) {
            // Only if the state is STATE_NONE, do we know that we haven't started already
            if (mChatService.getState() == BluetoothChatService.STATE_NONE) {
                // Start the Bluetooth chat services
                mChatService.start();
            }
        }
    }

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
                    if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
                        listAdapter.add(device.getName()+"\n"+device.getAddress());
                    }
                }
            }
        };

        mBtAdapter = BluetoothAdapter.getDefaultAdapter();
        for (BluetoothDevice device : mBtAdapter.getBondedDevices()) {
            listAdapter.add(device.getName() + "\n" + device.getAddress());
        }

        filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        getActivity().registerReceiver(receiver,filter);

        mBtAdapter.startDiscovery();
        return view;
    }

    /**
     * Set up the UI and background operations for chat.
     */
    private void setupChat() {
        // Initialize the BluetoothChatService to perform bluetooth connections
        mChatService = new BluetoothChatService(getActivity(), new NewHandler());
    }

    private AdapterView.OnItemClickListener mDeviceClickListener
            = new AdapterView.OnItemClickListener() {
        public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {
            // Cancel discovery because it's costly and we're about to connect
            mBtAdapter.cancelDiscovery();

            // Get the device MAC address, which is the last 17 chars in the View
            String info = ((TextView) v).getText().toString();
            String address = info.substring(info.length() - 17);

            // Create the result Intent and include the MAC address
            Intent intent = new Intent();
            intent.putExtra(EXTRA_DEVICE_ADDRESS, address);

            connectDevice(intent, true);

            GameListener parent = (GameListener) getActivity();
            parent.createGame(mChatService);
       }
    };

    /**
     * Establish connection with other device
     *
     * @param data   An {@link Intent} with {@link ChooseServerFragment#EXTRA_DEVICE_ADDRESS} extra.
     * @param secure Socket Security type - Secure (true) , Insecure (false)
     */
    private void connectDevice(Intent data, boolean secure) {
        // Get the device MAC address
        String address = data.getExtras()
                .getString(ChooseServerFragment.EXTRA_DEVICE_ADDRESS);
        // Get the BluetoothDevice object
        BluetoothDevice device = mBtAdapter.getRemoteDevice(address);
        // Attempt to connect to the device
        mChatService.connect(device, secure);
    }

    private class NewHandler extends Handler {
        public String mConnectedDeviceName = null;
        @Override
        public void handleMessage(Message msg) {
            FragmentActivity activity = getActivity();
            switch (msg.what) {
                case Constants.MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case BluetoothChatService.STATE_CONNECTED:
//                            setStatus(getString(R.string.title_connected_to, mConnectedDeviceName));
//                            mConversationArrayAdapter.clear();
                            break;
                        case BluetoothChatService.STATE_CONNECTING:
//                            setStatus(R.string.title_connecting);
                            break;
                        case BluetoothChatService.STATE_LISTEN:
                        case BluetoothChatService.STATE_NONE:
//                            setStatus(R.string.title_not_connected);
                            break;
                    }
                    break;
                case Constants.MESSAGE_WRITE:
                    byte[] writeBuf = (byte[]) msg.obj;
                    // construct a string from the buffer
                    String writeMessage = new String(writeBuf);
                    break;
                case Constants.MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;
                    // construct a string from the valid bytes in the buffer
                    String readMessage = new String(readBuf, 0, msg.arg1);
                    TextView changeBox = (TextView) getActivity().getSupportFragmentManager().findFragmentById(R.id.fragment_holder).getView().findViewById(R.id.guessbox);
                    changeBox.setText(readMessage);
                    if(readMessage.equals("You are it")) {
                        Button guessed = (Button) getActivity().getSupportFragmentManager().findFragmentById(R.id.fragment_holder).getView().findViewById(R.id.guessedButton);
                        RelativeLayout.LayoutParams absParams =
                        (RelativeLayout.LayoutParams)guessed.getLayoutParams();

                        DisplayMetrics displaymetrics = new DisplayMetrics();
                        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
                        int width = displaymetrics.widthPixels - guessed.getMeasuredWidth() - 300;
                        int height = displaymetrics.heightPixels - guessed.getMeasuredHeight() - 300;


                        Random r = new Random();

                        guessed.setX((float) r.nextInt(width ));
                        guessed.setY((float) r.nextInt(height ));

                        guessed.setVisibility(View.VISIBLE);
                    }

                    break;
                case Constants.MESSAGE_DEVICE_NAME:
                    // save the connected device's name
                    mConnectedDeviceName = msg.getData().getString(Constants.DEVICE_NAME);
                    if (null != activity) {
                        Toast.makeText(activity, "Connected to "
                                + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                    }
                    break;
                case Constants.MESSAGE_TOAST:
                    if (null != activity) {
                        Toast.makeText(activity, msg.getData().getString(Constants.TOAST),
                                Toast.LENGTH_SHORT).show();
                    }
                    break;
            }
        }

    }
}
