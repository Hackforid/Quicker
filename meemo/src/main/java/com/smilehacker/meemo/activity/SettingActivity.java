package com.smilehacker.meemo.activity;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
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
import com.smilehacker.meemo.data.PrefsManager;
import com.smilehacker.meemo.data.model.event.FloatViewRefreshEvent;
import com.smilehacker.meemo.plugin.GAPreferenceActivity;
import com.smilehacker.meemo.service.MainService;
import com.smilehacker.meemo.utils.AppManager;
import com.smilehacker.meemo.utils.PackageHelper;

import de.greenrobot.event.EventBus;

public class SettingActivity extends GAPreferenceActivity {

    private Preference mPrefClearCache;
    private Preference mPrefVersion;
    private Preference mPrefAuthor;
    private Preference mPrefUpdate;
    private Preference mPrefFeedback;
    private CheckBoxPreference mPrefFloatView;
    private CheckBoxPreference mPrefAutoBoot;
    private ListPreference mPrefFloatViewSize;

    private Preference mPrefFloatViewSetting;
    private CheckBoxPreference mPrefFloatViewAlignToEdge;


    private AppManager mAppManager;
    private PackageHelper mPackageHelper;
    private PrefsManager mSPManager;
    private EventBus mEventBus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        // Only need a simple preference, give up PreferenceFragment
        addPreferencesFromResource(R.xml.setting_preference);

        mAppManager = AppManager.getInstance(this);
        mPackageHelper = new PackageHelper(this);
        mSPManager = PrefsManager.getInstance(this);
        mEventBus = EventBus.getDefault();

        initPreference();
    }

    private void initPreference() {
        mPrefClearCache = findPreference(getResources().getString(R.string.setting_key_clearcache));
        mPrefVersion = findPreference(getString(R.string.setting_key_version));
        mPrefAuthor = findPreference(getString(R.string.setting_key_author));
        mPrefUpdate = findPreference(getString(R.string.setting_key_update));
        mPrefFeedback = findPreference(getString(R.string.setting_key_feedback));
        mPrefFloatView = (CheckBoxPreference) findPreference(getString(R.string.setting_key_floatview));
        mPrefAutoBoot = (CheckBoxPreference) findPreference(getString(R.string.setting_key_autoboot));
        mPrefFloatViewSetting = findPreference(getString(R.string.setting_key_floatview_setting));
        mPrefFloatViewAlignToEdge = (CheckBoxPreference) findPreference(getString(R.string.setting_key_floatview_edge));
        mPrefFloatViewSize = (ListPreference) findPreference(getString(R.string.setting_key_floatview_size));

        mPrefAutoBoot.setEnabled(mSPManager.getShouldShowFlowView());


        mPrefFloatViewSize.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                mSPManager.setFloatViewSize((String) newValue);
                FloatViewRefreshEvent event = new FloatViewRefreshEvent();
                event.refreshType = FloatViewRefreshEvent.RefreshType.ChangeSize;
                mEventBus.post(event);
                return true;
            }
        });

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

        mPrefFeedback.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                sendFeedbackMail();
                return true;
            }
        });

        mPrefFloatView.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object o) {
                Boolean shouldShowFloatview = (Boolean) o;
                if (shouldShowFloatview) {
                    mPrefAutoBoot.setEnabled(true);
                    mPrefFloatViewSetting.setEnabled(true);
                    Intent intent = new Intent(getApplicationContext(), MainService.class);
                    intent.putExtra(MainService.KEY_COMMAND, MainService.COMMAND_SHOW_FLOAT_VIEW);
                    startService(intent);
                } else {
                    mPrefAutoBoot.setEnabled(false);
                    mPrefFloatViewSetting.setEnabled(false);
                    Intent intent = new Intent(getApplicationContext(), MainService.class);
                    stopService(intent);
                }

                return true;
            }
        });

        mPrefVersion.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {

                Uri uri = Uri.parse(new StringBuilder().append("market://details?id=").append(SettingActivity.this.getPackageName()).toString());
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                return true;
            }
        });

        mPrefUpdate.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {

                Uri uri = Uri.parse(new StringBuilder().append("market://details?id=").append(SettingActivity.this.getPackageName()).toString());
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                return true;
            }
        });

        mPrefFloatViewSetting.setEnabled(mPrefFloatView.isEnabled());

    }

    private void visitAuthorWeibo() {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(Constants.WEIBO));
        startActivity(intent);
    }

    private void sendFeedbackMail() {
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.fromParts("mailto", Constants.EMAIL, null));
        intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.feedback_mail_title));
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
