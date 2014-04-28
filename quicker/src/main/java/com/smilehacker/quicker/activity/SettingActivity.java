package com.smilehacker.quicker.activity;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

import com.smilehacker.quicker.R;
import com.smilehacker.quicker.utils.AppManager;

public class SettingActivity extends PreferenceActivity {

    private Preference mPrefClearCache;

    private AppManager mAppManager;
    private SharedPreferences mSharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Only need a simple preference, give up PreferenceFragment
        addPreferencesFromResource(R.xml.setting_preference);

        mAppManager = AppManager.getInstance(this);
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        initPreference();
    }

    private void initPreference() {
        mPrefClearCache = findPreference(getResources().getString(R.string.setting_key_clearcache));

        mPrefClearCache.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                rebuildAppCache();
                return true;
            }
        });

    }

    private void rebuildAppCache() {
        final ProgressDialog pd = ProgressDialog.show(this, null, getString(R.string.rebuild_cache_summary));
        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... voids) {
                mAppManager.rebuild();
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                pd.dismiss();
            }
        }.execute();
    }



}
