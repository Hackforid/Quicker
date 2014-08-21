package com.smilehacker.meemo.activity;

import android.os.Bundle;
import android.os.StrictMode;

import com.smilehacker.meemo.R;
import com.smilehacker.meemo.data.PrefsManager;
import com.smilehacker.meemo.frgments.DialFragment;
import com.smilehacker.meemo.plugin.GAActivity;


public class MainActivity extends GAActivity {

    private PrefsManager mSPManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, new DialFragment())
                    .commit();
        }
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
