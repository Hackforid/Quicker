package com.smilehacker.meemo.utils;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

import java.util.List;

/**
 * Created by kleist on 14-4-2.
 */
public class PackageHelper {

    private Context mContext;
    private PackageManager mPackageManager;

    public PackageHelper(Context context) {
        mContext = context;
        mPackageManager = context.getPackageManager();
    }

    public PackageInfo getPkgInfoByPkgName(String pkgName) {
        PackageInfo info;
        try {
            info = mPackageManager.getPackageInfo(pkgName, 0);
            return info;
        } catch (PackageManager.NameNotFoundException e) {
            DLog.e(String.format("package %1$s not found", pkgName));
            return null;
        }
    }

    public void openApp(String packageName) {
        PackageInfo pi;
        try {
            pi = mPackageManager.getPackageInfo(packageName, 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return;
        }

        Intent resolveIntent = new Intent(Intent.ACTION_MAIN, null);
        resolveIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        resolveIntent.setPackage(pi.packageName);

        List<ResolveInfo> apps = mPackageManager.queryIntentActivities(resolveIntent, 0);

        ResolveInfo ri = apps.iterator().next();
        if (ri != null ) {
            String aPackageName = ri.activityInfo.packageName;
            String className = ri.activityInfo.name;

            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_LAUNCHER);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            ComponentName cn = new ComponentName(aPackageName, className);

            intent.setComponent(cn);
            mContext.startActivity(intent);
        }
    }

    public String getVersionName() {
        try {
            PackageInfo packageInfo = mPackageManager.getPackageInfo(mContext.getPackageName(), 0);
            return packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            DLog.d(e.toString());
            return "";
        }
    }
}
