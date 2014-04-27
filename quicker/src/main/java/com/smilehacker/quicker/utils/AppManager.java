package com.smilehacker.quicker.utils;

import android.app.LoaderManager;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Parcel;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.smilehacker.quicker.data.SPManager;
import com.smilehacker.quicker.data.model.AppInfo;
import com.smilehacker.quicker.data.model.event.AppEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import de.greenrobot.event.EventBus;
import hugo.weaving.DebugLog;
import se.emilsjolander.sprinkles.ModelList;
import se.emilsjolander.sprinkles.Query;
import se.emilsjolander.sprinkles.Transaction;

/**
 * Created by kleist on 14-4-2.
 */
public class AppManager {

    private Context mContext;
    private List<AppInfo> mAppInfos;
    private PackageManager mPackageManager;
    private SPManager mSPManager;
    private AppT9Parser mParser;
    private Gson mGson;
    private EventBus mEventBus;

    private static AppManager mInstance;

    public static AppManager getInstance(Context context) {
        if (mInstance == null) {
            synchronized (AppManager.class) {
                if (mInstance == null) {
                    mInstance = new AppManager(context.getApplicationContext());
                }
            }
        }

        return mInstance;
    }


    private AppManager(Context context) {
        mContext = context;
        mAppInfos = new ArrayList<AppInfo>();
        mPackageManager = context.getPackageManager();
        mSPManager = SPManager.getInstance(context);
        mParser = new AppT9Parser();
        mGson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
        mEventBus = EventBus.getDefault();
    }

    /**
     * init app list
     * read app from db at first
     * then compare with sys in async
     */
    @DebugLog
    public List<AppInfo> load() {
        final List<AppInfo> appList = loadAppFromDB();
        refreshAppList(appList);

        new AsyncTask<Void, Void, List<AppInfo>>() {

            @Override
            protected List<AppInfo> doInBackground(Void... voids) {
                return loadAppFromSys();
            }

            @Override
            protected void onPostExecute(List<AppInfo> appInfos) {
                super.onPostExecute(appInfos);
                updateSysAppsWithStored(appInfos, appList);
                refreshAppList(appInfos, true);
                updateStoredAppWithSys();
            }
        }.execute();

        return appList;
    }

    private void refreshAppList(List<AppInfo> appInfos) {
        refreshAppList(appInfos, false);
    }

    private void refreshAppList(List<AppInfo> appInfos, Boolean shouldBroadcast) {
        mAppInfos.clear();
        mAppInfos.addAll(appInfos);
        if (shouldBroadcast) {
            DLog.i("refresh apps");
            mEventBus.post(new AppEvent(mAppInfos));
        } else {

        }
    }

    private List<AppInfo> loadAppFromDB() {
       return AppInfo.getInstalledApps();
    }

    private List<AppInfo> loadAppFromSys() {
        List<AppInfo> list = new ArrayList<AppInfo>();
        List<PackageInfo> packages = mPackageManager.getInstalledPackages(PackageManager.PERMISSION_GRANTED);

        for (PackageInfo pkg : packages) {
            if (!isLaunchable(pkg)) {
                continue;
            }
            AppInfo appInfo = new AppInfo();
            appInfo.appName = mPackageManager.getApplicationLabel(pkg.applicationInfo).toString();
            appInfo.packageName = pkg.packageName;
            list.add(appInfo);
        }

        return list;
    }

    private void updateSysAppsWithStored(List<AppInfo> sysApps, List<AppInfo> storedApps) {
        Transaction transaction = new Transaction();
        for (int i = 0, length = sysApps.size(); i < length; i++) {
            AppInfo sysApp = sysApps.get(i);
            AppInfo storedApp = findAppInListByPackage(storedApps, sysApp.packageName);
            if (storedApp != null) {
                sysApps.set(i, storedApp);
            } else {
                mParser.parseAppNameToT9(sysApp);
                storeApp(sysApp, transaction);
            }
        }
        transaction.setSuccessful(true);
        transaction.finish();
    }

    private void updateStoredAppWithSys() {
        List<AppInfo> storedApps = loadAppFromDB();

        Transaction transaction = new Transaction();

        for (AppInfo appInfo : storedApps) {
            AppInfo sysApp = findAppInListByPackage(mAppInfos, appInfo.packageName);
            if (sysApp == null) {
                try {
                    appInfo.delete(transaction);
                } catch (Exception e) {
                    DLog.d(e.toString());
                }
            }
        }

        transaction.setSuccessful(true);
        transaction.finish();
    }

    private AppInfo findAppInListByPackage(List<AppInfo> list, String packageName) {
        for (AppInfo appInfo: list) {
            if (packageName.equals(appInfo.packageName)) {
                return appInfo;
            }
        }
        return null;
    }


    public List<AppInfo> search(String inputNum) {
        List<AppInfo> appInfos = AppSearcher.search(mAppInfos, inputNum);
        return appInfos;
    }

    private Boolean isLaunchable(PackageInfo packageInfo) {
        return  mPackageManager.getLaunchIntentForPackage(packageInfo.packageName) != null;
    }


    private void storeApp(AppInfo appInfo) {
        storeApp(appInfo, null);
    }

    private void storeApp(AppInfo appInfo, Transaction transaction) {
        if (transaction != null) {
            appInfo.save(transaction);
        } else {
            appInfo.save();
        }
    }

    public void increaseLaunchCount(String packageName) {
        AppInfo appInfo = Query.one(AppInfo.class, "SELECT * FROM app WHERE package_name = ?", packageName).get();
        appInfo.launchCount += 1;
        appInfo.save();
    }

    public void increaseLaunchCount(AppInfo appInfo) {
        appInfo.launchCount += 1;
        appInfo.save();
    }

    public List<AppInfo> getRecentUpdateApps() {
        List<AppInfo> appInfos = new ArrayList<AppInfo>(mAppInfos);
        Collections.sort(appInfos, new Comparator<AppInfo>() {
            @Override
            public int compare(AppInfo appInfo, AppInfo appInfo2) {
                return -appInfo.updateDate.compareTo(appInfo2.updateDate);
            }
        });
        return appInfos;
    }


}
