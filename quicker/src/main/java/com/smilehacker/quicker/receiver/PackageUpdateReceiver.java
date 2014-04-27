package com.smilehacker.quicker.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.smilehacker.quicker.utils.AppManager;
import com.smilehacker.quicker.utils.DLog;

/**
 * Created by kleist on 14-4-5.
 */
public class PackageUpdateReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

        String action = intent.getAction();
        String packageName = intent.getDataString();


        if (packageName.equals("com.smilehacker.quicker")) {
            DLog.i("self install or update");
            return;
        }

        AppManager appManager = AppManager.getInstance(context);

        if (action.equals(Intent.ACTION_PACKAGE_ADDED)) {
            DLog.i(String.format("install package %1$s", packageName));

        } else if (action.equals(Intent.ACTION_PACKAGE_REMOVED)) {
            DLog.i(String.format("uninstall package %1$s", packageName));

        }
    }


}
