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
        if (intent.getDataString().equals("com.smilehacker.quicker")) {
            return;
        }

        DLog.i("reload apps");
        AppManager appManager = new AppManager(context);
        appManager.loadAndStoreAppsFromSys();
    }
}
