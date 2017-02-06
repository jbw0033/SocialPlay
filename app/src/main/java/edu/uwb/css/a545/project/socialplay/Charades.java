package edu.uwb.css.a545.project.socialplay;

import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by Jeremy Woods on 2/5/2017.
 */

public class Charades extends Fragment {

    TextView guessing;
    BluetoothSocket btSocket;
    boolean server;
    ArrayList<String> guessingWords;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.charadeslayout, container, false);
        guessing = (TextView) view.findViewById(R.id.guessbox);

        guessingWords = new ArrayList<String>();

        guessingWords.add("cat");
        guessingWords.add("dog");
        guessingWords.add("lion");
        guessingWords.add("tiger");
        guessingWords.add("bear");
        guessingWords.add("train");

        changeText();

        return view;
    }

    public void setSocket(BluetoothSocket socket) {
        btSocket = socket;
    }

    public boolean isServer(boolean change) {
        server = change;
        return server;
    }


    public void changeText() {
        if (server) {
            try {
                guessing.setText("Game Started");
                ((PageHolder) getActivity()).sendData(btSocket, (guessingWords.get((int)(Math.random()*guessingWords.size()))).getBytes());
            } catch (IOException e) {}
        }
        else {
            try {
                String word = ((PageHolder) getActivity()).receiveData(btSocket);
                guessing.setText(word);
            }
            catch (IOException e) {}
        }
    }

}
