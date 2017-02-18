package edu.uwb.css.a545.project.socialplay;

import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by Jeremy Woods on 2/5/2017.
 */

public class Charades extends Fragment {

    TextView guessing;
    Button guessed;
    boolean server;
    boolean it = false;
    ArrayList<String> guessingWords;

    ArrayList<BluetoothChatService> mServices;

    BluetoothChatService mChatService;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.charadeslayout, container, false);
        guessing = (TextView) view.findViewById(R.id.guessbox);
        guessed = (Button) view.findViewById(R.id.guessedButton);

        changeText();

        if(!it) {
            guessed.setVisibility(View.INVISIBLE);
        }

        guessed.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(server) {
                    changeText();
                    if(!it) {
                        guessed.setVisibility(View.INVISIBLE);
                    }
                }
                else {
                    sendMessage("Done", 0);
                    guessed.setVisibility(View.INVISIBLE);
                }
            }
        });

        return view;
    }

    public void setServerServices(ArrayList<BluetoothChatService> services) {
        mServices = services;
    }

    public void setClientService(BluetoothChatService service) {
        mChatService = service;
    }


    public boolean isServer(boolean change) {
        server = change;
        return server;
    }


    public void changeText() {
        if (server) {
            int itPlayer = (int)(Math.random()*(mServices.size() + 1));
                if(itPlayer == mServices.size()) {
                    guessing.setText("You are it");
                    it = true;
                    for(int i = 0; i < mServices.size(); i++) {
                        sendMessage("You are guessing", i);
                    }
                }
                else {
                    guessing.setText("You are guessing");
                    it = false;
                    for(int i = 0; i < mServices.size(); i++) {
                        if(itPlayer == i) {
                            sendMessage("You are it", i);
                        }
                        else {
                            sendMessage("You are guessing", i);
                        }
                    }
                }
        }
    }

    /**
     * Sends a message.
     *
     * @param message A string of text to send.
     */
    private void sendMessage(String message, int target) {

        // Check that we're actually connected before trying anything
        if(server) {
            if (mServices.get(0).getState() != BluetoothChatService.STATE_CONNECTED) {
//            if(mChatService.getState() != BluetoothChatService.STATE_CONNECTED) {
                Toast.makeText(getActivity(), R.string.not_connected, Toast.LENGTH_SHORT).show();
                return;
            }
        }
        else {
            if (mChatService.getState() != BluetoothChatService.STATE_CONNECTED) {
//            if(mChatService.getState() != BluetoothChatService.STATE_CONNECTED) {
                Toast.makeText(getActivity(), R.string.not_connected, Toast.LENGTH_SHORT).show();
                return;
            }
        }

        // Check that there's actually something to send
        if (message.length() > 0) {
            // Get the message bytes and tell the BluetoothChatService to write
            byte[] send = message.getBytes();
            if(server) {
                mServices.get(target).write(send);
            }
            else {
                mChatService.write(send);
            }
        }
    }

}
