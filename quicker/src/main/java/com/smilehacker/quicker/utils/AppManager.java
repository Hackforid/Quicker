package com.smilehacker.quicker.utils;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import com.smilehacker.quicker.data.SPManager;
import com.smilehacker.quicker.data.model.AppInfo;
import com.smilehacker.quicker.data.model.T9PinYin;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by kleist on 14-4-2.
 */
public class AppManager {

    private Context mContext;
    private List<AppInfo> mAppInfos;
    private PackageManager mPackageManager;
    private SPManager mSPManager;
    private AppT9Parser mParser;

    public AppManager(Context context) {
        mContext = context;
        mAppInfos = new ArrayList<AppInfo>();
        mPackageManager = context.getPackageManager();
        mSPManager = SPManager.getInstance(context);
        mParser = new AppT9Parser();
    }

    public void getAllAppName() {
        long time = System.currentTimeMillis();
        List<PackageInfo> packages = mPackageManager.getInstalledPackages(PackageManager.PERMISSION_GRANTED);


        for (PackageInfo pkg : packages) {
            if (!isLaunchable(pkg)) {
                continue;
            }
            AppInfo appInfo = new AppInfo();
            appInfo.appName = mPackageManager.getApplicationLabel(pkg.applicationInfo).toString();
            appInfo.packageName = pkg.packageName;
            parseNameToT9(appInfo);
            mAppInfos.add(appInfo);
        }
        DLog.i("cost " + (System.currentTimeMillis() - time));
    }

    private void parseNameToT9(AppInfo appInfo) {

        T9PinYin pinYin = mSPManager.getT9ByName(appInfo.appName);
        if (pinYin == null) {
            mParser.parseAppNameToT9(appInfo);
            mSPManager.saveT9(appInfo);
        } else {
            appInfo.shortT9 = pinYin.shortT9;
            appInfo.fullT9 = pinYin.fullT9;
        }
    }

    public List<AppInfo> search(String inputNum) {
        List<AppInfo> appInfos = AppSearcher.search(mAppInfos, inputNum);
        return appInfos;
    }

    private void storeApp() {

    }

    private Boolean isSysApp(PackageInfo packageInfo) {
        int mask = ApplicationInfo.FLAG_SYSTEM | ApplicationInfo.FLAG_UPDATED_SYSTEM_APP;
        return (packageInfo.applicationInfo.flags & mask) != 0;
    }

    private Boolean isLaunchable(PackageInfo packageInfo) {
        return  mPackageManager.getLaunchIntentForPackage(packageInfo.packageName) != null;
    }
}
