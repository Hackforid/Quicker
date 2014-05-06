package com.smilehacker.meemo.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.smilehacker.meemo.data.SPManager;
import com.smilehacker.meemo.data.model.AppInfo;
import com.smilehacker.meemo.data.model.event.AppEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import de.greenrobot.event.EventBus;
import hugo.weaving.DebugLog;
import se.emilsjolander.sprinkles.Query;
import se.emilsjolander.sprinkles.Transaction;

/**
 * Created by kleist on 14-4-2.
 */
public class AppManager {

    private Context mContext;
    private List<AppInfo> mAppInfos;
    private PackageManager mPackageManager;
    private AppT9Parser mParser;
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
        mParser = new AppT9Parser();
        mEventBus = EventBus.getDefault();

        loadFromDBAndAsyncSysApps();
    }

    /**
     * init app list
     * read app from db at first
     * then compare with sys in async
     */
    @DebugLog
    public void loadFromDBAndAsyncSysApps() {
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
                refreshAppList(appInfos);
                updateStoredAppWithSys();

                broadcastAppUpdated();
            }
        }.execute();
    }

    private void refreshAppList(List<AppInfo> appInfos) {
        mAppInfos.clear();
        mAppInfos.addAll(appInfos);
    }

    private void broadcastAppUpdated() {
        mEventBus.post(new AppEvent(mAppInfos));
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
        return AppSearcher.search(mAppInfos, inputNum);
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
        appInfo.increaseLaunchCount();
    }

    public List<AppInfo> getRecentUpdateApps() {
        List<AppInfo> result = new ArrayList<AppInfo>();
        Date initData = new Date(0);
        for (AppInfo appInfo: mAppInfos) {
            if (appInfo.launchDate.compareTo(initData) > 0) {
                result.add(appInfo);
            }
        }
        Collections.sort(result, new Comparator<AppInfo>() {
            @Override
            public int compare(AppInfo appInfo, AppInfo appInfo2) {
                return -appInfo.launchDate.compareTo(appInfo2.launchDate);
            }
        });
        return result;
    }

    public void addPackage(String packageName) {
        AppInfo storedAppInfo = AppInfo.getAppByPackage(packageName);
        if (storedAppInfo != null) {
            return;
        }

        PackageHelper packageHelper = new PackageHelper(mContext);
        PackageInfo pkg = packageHelper.getPkgInfoByPkgName(packageName);
        if (pkg == null) {
            return;
        }

        AppInfo newApp = new AppInfo();
        newApp.appName = mPackageManager.getApplicationLabel(pkg.applicationInfo).toString();
        newApp.packageName = pkg.packageName;
        mParser.parseAppNameToT9(newApp);

        newApp.save();
        mAppInfos.add(newApp);
        broadcastAppUpdated();
    }

    public void deletePackage(String packageName) {
        AppInfo appInfo = AppInfo.getAppByPackage(packageName);
        if (appInfo == null) {
            return;
        }

        appInfo.delete();
        if (!mAppInfos.isEmpty()) {
            AppInfo app = findAppInListByPackage(mAppInfos, packageName);
            if (app != null) {
                mAppInfos.remove(app);
                broadcastAppUpdated();
            }
        }
    }

    public void rebuild() {
        AppInfo.deleteAll();
        List<AppInfo> sysAppList = loadAppFromSys();
        updateSysAppsWithStored(sysAppList, Collections.EMPTY_LIST);
        refreshAppList(sysAppList);
        broadcastAppUpdated();
    }

}
