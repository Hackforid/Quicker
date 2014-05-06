package com.smilehacker.meemo.activity;

import android.os.Bundle;

import com.smilehacker.meemo.R;
import com.smilehacker.meemo.frgments.FloatFragment;
import com.smilehacker.meemo.plugin.GAActivity;

/**
 * Created by kleist on 14-5-7.
 */
public class FloatActivity extends GAActivity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_float);

        getFragmentManager().beginTransaction()
                .replace(R.id.container, new FloatFragment())
                .commit();
    }

}
