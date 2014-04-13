package com.smilehacker.quicker.data;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Created by kleist on 14-4-5.
 */
public class SPManager {

    private final static String CONFIG = "config";
    private final static String CONFIG_IS_INIT = "config_is_init";

    private static SPManager mInstance;
    private SharedPreferences mConfigData;

    private Gson mGson;

    private SPManager(Context context) {
        mGson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
        mConfigData = context.getSharedPreferences(CONFIG, 0);
    }

    public static SPManager getInstance(Context context) {
        if (mInstance == null) {
            return new SPManager(context.getApplicationContext());
        }
        return mInstance;
    }

    public void setIsInit(Boolean isInit) {
        mConfigData.edit().putBoolean(CONFIG_IS_INIT, isInit).commit();
    }

    public Boolean isInit() {
        return mConfigData.getBoolean(CONFIG_IS_INIT, false);
    }
}
