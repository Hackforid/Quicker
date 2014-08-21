package com.smilehacker.meemo.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.smilehacker.meemo.data.PrefsManager;
import com.smilehacker.meemo.service.MainService;

/**
 * Created by kleist on 14-6-7.
 */
public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        PrefsManager spManager = PrefsManager.getInstance(context);
        if (spManager.getShouldShowFlowView() && spManager.getShoudAutoBoot()) {
            Intent serviceIntent = new Intent(context, MainService.class);
            serviceIntent.putExtra(MainService.KEY_COMMAND, MainService.COMMAND_SHOW_FLOAT_VIEW);
            context.startService(serviceIntent);
        }

    }
}
