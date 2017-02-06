package edu.uwb.css.a545.project.socialplay;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.io.IOException;
import java.util.UUID;

/**
 * Created by Jeremy Woods on 1/22/2017.
 */

public class CreateServerFragment extends Fragment {

    private static final UUID MY_UUID = UUID.fromString("a4e3e0b2-eb83-11e6-b006-92361f002671");
    Button findPlayerButton;
    Button startGameButton;
    ListView list;
    ArrayAdapter<String> listAdapter;
    BluetoothAdapter btAdapter;
    EditText btName;
    Charades game;
    BluetoothSocket btsSocket;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.create_server_page, container, false);

        game = new Charades();
        btName = (EditText) view.findViewById(R.id.word_box);
        btAdapter = BluetoothAdapter.getDefaultAdapter();
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
                findPlayerButton.setVisibility(View.INVISIBLE);
                startGameButton.setVisibility(View.VISIBLE);
                list.setVisibility(View.VISIBLE);
                btAdapter.setName(btName.getText().toString());
//                btName.setText("");

                BluetoothServerSocket tmp = null;
//                int count = 0;
                try {
                    tmp = btAdapter.listenUsingRfcommWithServiceRecord("Service_name", MY_UUID);
                } catch (IOException e) {}
                while (true) {
                    try {
                        btsSocket = tmp.accept();
                        Toast.makeText(getActivity(), "Accepting Message", Toast.LENGTH_SHORT)
                                .show();
                        listAdapter.add(btsSocket.getRemoteDevice().getName()+"\n"+btsSocket.getRemoteDevice().getAddress());
//                        count++;
                    } catch (IOException e) {}
                    if (btsSocket != null) {
                        try {
                            tmp.close();
                        } catch (IOException e) {}
                        break;
                    }
                }
            }
        });

        startGameButton.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                Charades game = new Charades();
                game.setSocket(btsSocket);
                game.isServer(true);
                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.fragment_holder, game).addToBackStack(null).commit();
            }
        });


        return view;
    }
}
