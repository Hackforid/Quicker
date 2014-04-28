package com.smilehacker.quicker.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;

import com.smilehacker.quicker.R;
import com.smilehacker.quicker.data.SPManager;
import com.smilehacker.quicker.frgments.DialFragment;


public class MainActivity extends Activity {

    private SPManager mSPManager;
    private DialFragment mDialFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSPManager = SPManager.getInstance(this);
        mDialFragment = new DialFragment();

        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, mDialFragment)
                    .commit();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (mSPManager.getShouldBackground()) {
                mDialFragment.runInBackground();
                return true;
            }
        }



        return super.onKeyDown(keyCode, event);
    }
}
