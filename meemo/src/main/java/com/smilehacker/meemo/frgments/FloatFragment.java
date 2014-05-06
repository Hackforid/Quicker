package com.smilehacker.meemo.frgments;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.smilehacker.meemo.R;

/**
 * Created by kleist on 14-5-7.
 */
public class FloatFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_float, null);
        return view;
    }
}
