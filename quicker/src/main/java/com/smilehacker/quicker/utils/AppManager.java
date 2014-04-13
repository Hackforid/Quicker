package com.smilehacker.quicker.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.smilehacker.quicker.data.SPManager;
import com.smilehacker.quicker.data.model.AppInfo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

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


    public AppManager(Context context) {
        mContext = context;
        mAppInfos = new ArrayList<AppInfo>();
        mPackageManager = context.getPackageManager();
        mSPManager = SPManager.getInstance(context);
        mParser = new AppT9Parser();
        mGson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
    }

    public void loadInstalledApps() {
        List<AppInfo> appList = new ArrayList<AppInfo>();
        HashMap<String, AppInfo> appMap = new HashMap<String, AppInfo>();

        long time = System.currentTimeMillis();
        loadAppsFromSys(appList);
        DLog.i("load app from sys cost " + (System.currentTimeMillis() - time));

        time = System.currentTimeMillis();
        readStoredApps(appMap);
        DLog.i("read db cost " + (System.currentTimeMillis() - time));

        Transaction transaction = new Transaction();

        for (int i = 0, length = appList.size(); i < length; i++) {
            AppInfo appInfo = appList.get(i);
            AppInfo app = appMap.get(appInfo.packageName);
            if (app != null) {
                appList.set(i, app);
            } else {
                mParser.parseAppNameToT9(appInfo);
                storeApp(appInfo, transaction);
            }
        }

        transaction.setSuccessful(true);
        transaction.finish();


        mAppInfos.clear();
        mAppInfos.addAll(appList);
    }

    private void loadAppsFromSys(List<AppInfo> list) {
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
    }

    public List<AppInfo> search(String inputNum) {
        List<AppInfo> appInfos = AppSearcher.search(mAppInfos, inputNum);
        return appInfos;
    }

    private Boolean isLaunchable(PackageInfo packageInfo) {
        return  mPackageManager.getLaunchIntentForPackage(packageInfo.packageName) != null;
    }

    private void storeApps() {
        Transaction transaction = new Transaction();
        try {
            for (AppInfo appInfo : mAppInfos) {
                    appInfo.save(transaction);
            }
            transaction.setSuccessful(true);
        } catch (Exception e) {
            DLog.e(e.toString());
        } finally {
            transaction.finish();
        }
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

    private void readStoredApps(HashMap<String, AppInfo> appInfoMap) {
        for (AppInfo appInfo: Query.all(AppInfo.class).get()) {
            try {
                appInfoMap.put(appInfo.packageName, appInfo);
            } catch (Exception e) {
                DLog.e(e.toString());
            }
        }
    }

    private void clearDB() {
        ModelList<AppInfo> list = ModelList.from(Query.all(AppInfo.class).get());
        list.deleteAll();
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
