package edu.uwb.css.a545.project.socialplay;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.widget.Button;

import java.util.ArrayList;

public class PageHolder extends FragmentActivity implements JoinCreateFragment.PlayerListener {

    Button mNextButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_page_holder);

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
    public void chooseGame() {
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_holder, new ChooseGameFragment()).addToBackStack(null).commit();
    }

    public void createServer(View view) {
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_holder, new CreateServerFragment()).addToBackStack(null).commit();
    }
}
