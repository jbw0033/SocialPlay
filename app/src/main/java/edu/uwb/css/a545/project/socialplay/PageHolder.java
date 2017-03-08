package edu.uwb.css.a545.project.socialplay;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class PageHolder extends FragmentActivity implements JoinCreateFragment.PlayerListener, ChooseServerFragment.GameListener {

    BluetoothAdapter mBluetoothAdapter;
    Button mNextButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_page_holder);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            new AlertDialog.Builder(this)
                    .setTitle("Not compatible")
                    .setMessage("Your phone does not support Bluetooth")
                    .setPositiveButton("Exit", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            System.exit(0);
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        } else if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBT = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBT, 1);
        }

        mNextButton = (Button) findViewById(R.id.next_button);

        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();

        transaction.add(R.id.fragment_holder, new StartFragment());

        transaction.commit();

        mNextButton.setOnClickListener(new Button.OnClickListener() {

            @Override
            public void onClick(View v) {
                mNextButton.setVisibility(View.INVISIBLE);
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_holder, new JoinCreateFragment()).commit();
            }
        });
    }

    @Override
    public void chooseServer() {
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_holder, new ChooseServerFragment()).addToBackStack(null).commit();
    }

    public void createServer() {
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_holder, new CreateServerFragment()).addToBackStack(null).commit();
    }

    public void createGame(BluetoothChatService service) {
        Charades game = new Charades();
        game.isServer(false);
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_holder, game).addToBackStack(null).commit();

        game.setClientService(service);
    }

}
