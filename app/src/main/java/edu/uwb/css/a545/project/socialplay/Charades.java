package edu.uwb.css.a545.project.socialplay;

import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

/**
 * Created by Jeremy Woods on 2/5/2017.
 */

public class Charades extends Fragment {

    TextView guessing;
    Button guessed;
    Button end;
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
        end = (Button) view.findViewById(R.id.EndGameButton);
        end.setVisibility(View.INVISIBLE);

        new CountDownTimer(30000, 1000) {

            public void onTick(long millisUntilFinished) {
            }

            public void onFinish() {
                endGame();
            }
        }.start();

        guessing.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(charSequence.toString().equals("Time's up!"))
                {
                    end.setVisibility(View.VISIBLE);
                    if(guessed.getVisibility()==View.VISIBLE)
                    {
                        guessing.setText("You Lose!");
                    }
                    else
                    {
                        guessing.setText("You didn't lose!");
                    }
                    guessed.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        changeText();

        if(!it) {
            guessed.setVisibility(View.INVISIBLE);
        }

        end.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View v) {
                //gracefully exit the game
            }
        });

        guessed.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                DisplayMetrics displaymetrics = new DisplayMetrics();
                getActivity().getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
                int width = displaymetrics.widthPixels - guessed.getMeasuredWidth() - 300;
                int height = displaymetrics.heightPixels - guessed.getMeasuredHeight() - 300;


                Random r = new Random();

                guessed.setX((float) r.nextInt(width ));
                guessed.setY((float) r.nextInt(height ));
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

    public void endGame(){

        if(server) {
            for (int i = 0; i < mServices.size(); i++) {
                sendMessage("Time's up!", i);
            }
            guessing.setText("Time's up!");
        }
    }

    public void changeText() {
        if (server) {
            int itPlayer = (int)(Math.random()*(mServices.size() + 1));
                if(itPlayer == mServices.size()) {
//                    guessed = (Button) getView().findViewById(R.id.guessedButton);
//                    guessing = (TextView) getView().findViewById(R.id.guessbox);
                    guessing.setText("You are it");
                    it = true;
                    if(it) {
                        guessed.setVisibility(View.VISIBLE);
                    }
                    for(int i = 0; i < mServices.size(); i++) {
                        sendMessage("You are not it", i);
                    }
                }
                else {
                    guessing.setText("You are not it");
                    it = false;
                    for(int i = 0; i < mServices.size(); i++) {
                        if(itPlayer == i) {
                            sendMessage("You are it", i);
                        }
                        else {
                            sendMessage("You are not it", i);
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
