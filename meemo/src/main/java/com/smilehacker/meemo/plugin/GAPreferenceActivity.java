package com.smilehacker.meemo.plugin;

import android.preference.PreferenceActivity;

import com.google.analytics.tracking.android.EasyTracker;

/**
 * Created by kleist on 14-4-28.
 */
public class GAPreferenceActivity extends PreferenceActivity{
    @Override
    protected void onStart() {
        super.onStart();
        EasyTracker.getInstance(this).activityStart(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        EasyTracker.getInstance(this).activityStop(this);
    }
}
