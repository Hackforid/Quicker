package com.smilehacker.meemo.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.smilehacker.meemo.util.AppManager;
import com.smilehacker.meemo.util.DLog;

/**
 * Created by kleist on 14-4-5.
 */
public class PackageUpdateReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        String packageName = intent.getDataString().substring(8);


        if (packageName.equals("com.smilehacker.quicker")) {
            DLog.i("self install or update");
            return;
        }

        AppManager appManager = AppManager.getInstance(context);

        if (action.equals(Intent.ACTION_PACKAGE_ADDED)) {
            DLog.i(String.format("install package %1$s", packageName));
            appManager.addPackage(packageName);
        } else if (action.equals(Intent.ACTION_PACKAGE_REMOVED)) {
            DLog.i(String.format("uninstall package %1$s", packageName));
            appManager.deletePackage(packageName);
        }
    }


}
