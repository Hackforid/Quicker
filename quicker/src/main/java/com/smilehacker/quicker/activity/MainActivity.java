package com.smilehacker.quicker.activity;

import android.os.Bundle;
import android.os.StrictMode;
import android.view.KeyEvent;

import com.smilehacker.quicker.R;
import com.smilehacker.quicker.data.SPManager;
import com.smilehacker.quicker.frgments.DialFragment;
import com.smilehacker.quicker.plugin.GAActivity;


public class MainActivity extends GAActivity {

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

    private void openStrictMode() {
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                .detectDiskReads()
                .detectDiskWrites()
                .detectNetwork()   // or .detectAll() for all detectable problems
                .penaltyLog()
                .build());
        StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                .detectLeakedSqlLiteObjects()
                .detectLeakedClosableObjects()
                .penaltyLog()
                .penaltyDeath()
                .build());
    }
}
