package com.smilehacker.quicker.data;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.smilehacker.quicker.data.model.AppInfo;
import com.smilehacker.quicker.data.model.T9PinYin;
import com.smilehacker.quicker.utils.DLog;

/**
 * Created by kleist on 14-4-5.
 */
public class SPManager {

    private final static String T9_PINYIN = "t9_pinyin";

    private static SPManager mInstance;

    private Gson mGson;
    private SharedPreferences mPinYinData;

    private SPManager(Context context) {
        mGson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
        mPinYinData = context.getSharedPreferences(T9_PINYIN, 0);
    }

    public static SPManager getInstance(Context context) {
        if (mInstance == null) {
            return new SPManager(context.getApplicationContext());
        }
        return mInstance;
    }

    public T9PinYin getT9ByName(String name) {
        String pinyinJson = mPinYinData.getString(name, null);

        if (pinyinJson == null) {
            return null;
        }

        T9PinYin pinYin = null;
        try {
            pinYin = mGson.fromJson(pinyinJson, T9PinYin.class);
        } catch (Exception e) {
            DLog.e(e.toString());
        }

        return pinYin;
    }

    public void saveT9(T9PinYin pinYin) {
        String pinYinJson;
        try {
            pinYinJson = mGson.toJson(pinYin);
        } catch (Exception e) {
            DLog.e(e.toString());
            return;
        }

        mPinYinData.edit().putString(pinYin.name, pinYinJson).commit();
    }

    public void saveT9(AppInfo appInfo) {
        T9PinYin pinYin = new T9PinYin();
        pinYin.name = appInfo.appName;
        pinYin.fullT9 = appInfo.fullT9;
        pinYin.shortT9 = appInfo.shortT9;
        saveT9(pinYin);
    }



}
