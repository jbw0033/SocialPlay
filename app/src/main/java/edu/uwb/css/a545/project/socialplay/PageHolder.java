package edu.uwb.css.a545.project.socialplay;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.widget.Button;

import java.util.ArrayList;

public class PageHolder extends FragmentActivity {

    ArrayList<Fragment> mFragmentArrayList = new ArrayList<Fragment>();
    Button mPrevButton;
    Button mNextButton;
    int mCurrFrag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_page_holder);

        mPrevButton = (Button) findViewById(R.id.prev_button);
        mNextButton = (Button) findViewById(R.id.next_button);

        mFragmentArrayList.add(new StartFragment());
        mFragmentArrayList.add(new UsernameFragment());
        mFragmentArrayList.add(new GameFragment());
        mFragmentArrayList.add(new JoinCreateFragment());

        mPrevButton.setVisibility(View.INVISIBLE);

        mCurrFrag = 0;

        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();

        transaction.add(R.id.fragment_holder, mFragmentArrayList.get(mCurrFrag));

        transaction.commit();

        mPrevButton.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mCurrFrag == mFragmentArrayList.size() - 1) {
                    mNextButton.setVisibility(View.VISIBLE);
                }
                mCurrFrag--;
                if(mCurrFrag == 0) {
                    mPrevButton.setVisibility(View.INVISIBLE);
                }
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_holder, mFragmentArrayList.get(mCurrFrag)).commit();
            }
        });

        mNextButton.setOnClickListener(new Button.OnClickListener() {

            @Override
            public void onClick(View v) {
                if(mCurrFrag == 0) {
                    mPrevButton.setVisibility(View.VISIBLE);
                }
                mCurrFrag++;
                if(mCurrFrag == mFragmentArrayList.size() - 1) {
                    mNextButton.setVisibility(View.INVISIBLE);
                }
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_holder, mFragmentArrayList.get(mCurrFrag)).commit();
            }
        });
    }
}
