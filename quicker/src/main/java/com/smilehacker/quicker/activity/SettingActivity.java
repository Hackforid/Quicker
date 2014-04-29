package com.smilehacker.quicker.activity;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.Preference;
import android.view.MenuItem;

import com.smilehacker.quicker.R;
import com.smilehacker.quicker.plugin.GAPreferenceActivity;
import com.smilehacker.quicker.utils.AppManager;

public class SettingActivity extends GAPreferenceActivity {

    private Preference mPrefClearCache;

    private AppManager mAppManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActionBar().setDisplayHomeAsUpEnabled(true);

        // Only need a simple preference, give up PreferenceFragment
        addPreferencesFromResource(R.xml.setting_preference);

        mAppManager = AppManager.getInstance(this);

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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            this.finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
