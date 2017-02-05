package edu.uwb.css.a545.project.socialplay;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created by Jeremy Woods on 2/5/2017.
 */

public class Charades extends Fragment {

    TextView guessing;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.charadeslayout, container, false);
        guessing = (TextView) view.findViewById(R.id.guessbox);

        return view;
    }

    public void changeText(String word) {
        guessing.setText(word);
    }

}
