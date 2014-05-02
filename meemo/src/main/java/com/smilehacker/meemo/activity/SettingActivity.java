package com.smilehacker.meemo.activity;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.smilehacker.meemo.R;
import com.smilehacker.meemo.app.Constants;
import com.smilehacker.meemo.plugin.GAPreferenceActivity;
import com.smilehacker.meemo.utils.AppManager;
import com.smilehacker.meemo.utils.PackageHelper;

public class SettingActivity extends GAPreferenceActivity {

    private Preference mPrefClearCache;
    private Preference mPrefVersion;
    private Preference mPrefAuthor;
    private Preference mPrefUpdate;


    private AppManager mAppManager;
    private PackageHelper mPackageHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActionBar().setDisplayHomeAsUpEnabled(true);

        // Only need a simple preference, give up PreferenceFragment
        addPreferencesFromResource(R.xml.setting_preference);

        mAppManager = AppManager.getInstance(this);
        mPackageHelper = new PackageHelper(this);

        initPreference();
    }

    private void initPreference() {
        mPrefClearCache = findPreference(getResources().getString(R.string.setting_key_clearcache));
        mPrefVersion = findPreference(getString(R.string.setting_key_version));
        mPrefAuthor = findPreference(getString(R.string.setting_key_author));
        mPrefUpdate = findPreference(getString(R.string.setting_key_update));

        mPrefClearCache.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                rebuildAppCache();
                return true;
            }
        });

        mPrefVersion.setTitle(String.format(getString(R.string.setting_title_verison), mPackageHelper.getVersionName()));

        mPrefAuthor.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                visitAuthorWeibo();
                return true;
            }
        });
    }

    private void visitAuthorWeibo() {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(Constants.WEIBO));
        startActivity(intent);
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

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (preference instanceof PreferenceScreen) {
            initializeActionBar((PreferenceScreen) preference);
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    private static void initializeActionBar(PreferenceScreen preferenceScreen) {

        final Dialog dialog = preferenceScreen.getDialog();

        if (dialog != null) {
            // Inialize the action bar
            dialog.getActionBar().setDisplayHomeAsUpEnabled(true);

            // Apply custom home button area click listener to close the PreferenceScreen because PreferenceScreens are dialogs which swallow
            // events instead of passing to the activity
            // Related Issue: https://code.google.com/p/android/issues/detail?id=4611
            View homeBtn = dialog.findViewById(android.R.id.home);

            if (homeBtn != null) {
                View.OnClickListener dismissDialogClickListener = new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                };

                // Prepare yourselves for some hacky programming
                ViewParent homeBtnContainer = homeBtn.getParent();

                // The home button is an ImageView inside a FrameLayout
                if (homeBtnContainer instanceof FrameLayout) {
                    ViewGroup containerParent = (ViewGroup) homeBtnContainer.getParent();

                    if (containerParent instanceof LinearLayout) {
                        // This view also contains the title text, set the whole view as clickable
                        ((LinearLayout) containerParent).setOnClickListener(dismissDialogClickListener);
                    } else {
                        // Just set it on the home button
                        ((FrameLayout) homeBtnContainer).setOnClickListener(dismissDialogClickListener);
                    }
                } else {
                    // The 'If all else fails' default case
                    homeBtn.setOnClickListener(dismissDialogClickListener);
                }
            }
        }
    }
}
