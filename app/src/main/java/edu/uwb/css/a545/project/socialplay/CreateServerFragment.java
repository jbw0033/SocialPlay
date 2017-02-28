package edu.uwb.css.a545.project.socialplay;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import java.util.UUID;

/**
 * Created by Jeremy Woods on 1/22/2017.
 */

public class CreateServerFragment extends Fragment {

//    private static final UUID MY_UUID = UUID.fromString("a4e3e0b2-eb83-11e6-b006-92361f002671");
    Button findPlayerButton;
    Button startGameButton;
    ListView list;
    ArrayAdapter<String> listAdapter;
    EditText btName;

    // Intent request codes
    private static final int REQUEST_CONNECT_DEVICE_SECURE = 1;
    private static final int REQUEST_CONNECT_DEVICE_INSECURE = 2;
    private static final int REQUEST_ENABLE_BT = 3;

    /**
     * Local Bluetooth adapter
     */
    BluetoothAdapter mBtAdapter = null;
    /**
     * Member object for the chat services
     */
    private ArrayList<BluetoothChatService> mServices = null;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mBtAdapter = BluetoothAdapter.getDefaultAdapter();
        mServices = new ArrayList<BluetoothChatService>();

        // If the adapter is null, then Bluetooth is not supported
        if (mBtAdapter == null) {
            Toast.makeText(getActivity(), "Bluetooth is not available", Toast.LENGTH_LONG).show();
            getActivity().finish();
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.create_server_page, container, false);

        btName = (EditText) view.findViewById(R.id.word_box);
        mBtAdapter = BluetoothAdapter.getDefaultAdapter();
        findPlayerButton = (Button) view.findViewById(R.id.find_players);
        startGameButton = (Button) view.findViewById(R.id.start_game);
        list = (ListView) view.findViewById(R.id.connectedDeviceList);
        listAdapter = new ArrayAdapter<>(this.getContext(), android.R.layout.simple_list_item_1, 0);
        startGameButton.setVisibility(View.INVISIBLE);
        list.setAdapter(listAdapter);
        list.setVisibility(View.INVISIBLE);

        findPlayerButton.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                ensureDiscoverable();
                findPlayerButton.setVisibility(View.INVISIBLE);
                startGameButton.setVisibility(View.VISIBLE);
                list.setVisibility(View.VISIBLE);
                mBtAdapter.setName(btName.getText().toString());
                btName.setText("");

                if (mServices.get(0) != null) {
                    // Only if the state is STATE_NONE, do we know that we haven't started already
                    if (mServices.get(0).getState() == BluetoothChatService.STATE_NONE) {
                        // Start the Bluetooth chat services
                        for (int i = 0; i < mServices.size(); i++) {
                            try {
                                mServices.get(i).start();
                            } catch (NullPointerException e) {
                                Toast.makeText(getActivity(), "Maximum devices connected", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                }

            }
        });

        startGameButton.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                Charades game = new Charades();
                game.setServerServices(mServices);
                game.isServer(true);
                if(mServices.size() < 7) {
                    mServices.remove(mServices.size() - 1);
                }
                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.fragment_holder, game).addToBackStack(null).commit();
            }
        });


        return view;
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
        } else if(mServices.size() == 0) {
            setupChat();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mServices.get(0) != null) {
            for(int i = 0; i < mServices.size(); i++) {
                mServices.get(i).stop();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        // Performing this check in onResume() covers the case in which BT was
        // not enabled during onStart(), so we were paused to enable it...
        // onResume() will be called when ACTION_REQUEST_ENABLE activity returns.
        if(startGameButton.getVisibility() == View.VISIBLE) {
            if (mServices.get(0) != null) {
                // Only if the state is STATE_NONE, do we know that we haven't started already
                if (mServices.get(0).getState() == BluetoothChatService.STATE_NONE) {
                    // Start the Bluetooth chat services
                    for (int i = 0; i < mServices.size(); i++) {
                        try {
                            mServices.get(i).start();
                        } catch (NullPointerException e) {
                            Toast.makeText(getActivity(), "Maximum devices connected", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
        }
    }

    /**
     * Set up the UI and background operations for chat.
     */
    private void setupChat() {

        // Initialize the BluetoothChatService to perform bluetooth connections
        mServices.add(new BluetoothChatService(getActivity(), new NewHandler()));
    }

    /**
     * Makes this device discoverable for 300 seconds (5 minutes).
     */
    private void ensureDiscoverable() {
        if (mBtAdapter.getScanMode() !=
                BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            startActivity(discoverableIntent);
        }
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
                            if(mServices.size() != 7) {
                                mServices.add(new BluetoothChatService(getActivity(), new NewHandler()));
                                try {
                                    mServices.get(mServices.size() - 1).start();
                                } catch (NullPointerException e) {
                                    Toast.makeText(getActivity(), "Maximum devices connected", Toast.LENGTH_SHORT).show();
                                }
                            }
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
                    if(readMessage.equals("Done")) {
//                        Charades game = new Charades();
//                        game.setServerServices(mServices);
//                        game.isServer(true);
                        Charades game = (Charades) getActivity().getSupportFragmentManager().findFragmentById(R.id.fragment_holder);

                        game.changeText();

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
                    }
                    break;
                case Constants.MESSAGE_DEVICE_NAME:
                    // save the connected device's name
                    mConnectedDeviceName = msg.getData().getString(Constants.DEVICE_NAME);
                    listAdapter.add(mConnectedDeviceName);
                    if (null != activity) {
                        Toast.makeText(activity, "Connected to "
                                + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                    }
                    break;
                case Constants.MESSAGE_TOAST:
                    listAdapter.remove(mConnectedDeviceName);
                    for(int i = 0; i < mServices.size(); i++) {
                        if(mServices.get(i).getState() != BluetoothChatService.STATE_CONNECTED) {
                            mServices.remove(i);
                            break;
                        }
                    }
                    if (null != activity) {
                        Toast.makeText(activity, msg.getData().getString(Constants.TOAST),
                                Toast.LENGTH_SHORT).show();
                    }
                    break;
            }
        }

    }
}
