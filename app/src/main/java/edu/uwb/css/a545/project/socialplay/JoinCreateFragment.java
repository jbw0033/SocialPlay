package edu.uwb.css.a545.project.socialplay;

import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

/**
 * Created by Jeremy Woods on 1/22/2017.
 */

public class JoinCreateFragment extends Fragment {
    Button mClientButton;
    Button mServerButton;

    public interface PlayerListener {
        void createServer();

        void chooseServer();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.join_page, container, false);

        mClientButton = (Button) view.findViewById(R.id.client_button);
        mServerButton = (Button) view.findViewById(R.id.server_button);

        mClientButton.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                PlayerListener parent = (PlayerListener) getActivity();
                parent.chooseServer();
            }
        });

        mServerButton.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                PlayerListener parent = (PlayerListener) getActivity();
                parent.createServer();
            }
        });
        return view;
    }
}
