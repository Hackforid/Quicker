package com.smilehacker.meemo.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.smilehacker.meemo.R;

/**
 * Created by kleist on 14-4-5.
 */
public class PrefsManager {

    private final static String KEY_FLOATVIEW_POS_X = "key_floatview_pos_x";
    private final static String KEY_FLOATVIEW_POS_Y = "key_floatview_pos_y";

    private static PrefsManager mInstance;
    private SharedPreferences mConfigData;
    private Context mContext;

    private Gson mGson;


    private PrefsManager(Context context) {
        mContext = context;
        mGson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
        mConfigData = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public static PrefsManager getInstance(Context context) {
        if (mInstance == null) {
            synchronized (PrefsManager.class) {
                if (mInstance == null) {
                    mInstance = new PrefsManager(context.getApplicationContext());
                }
            }
        }
        return mInstance;
    }

    public Boolean getShouldBackground() {
        return mConfigData.getBoolean(mContext.getString(R.string.setting_key_background), true);
    }

    public Boolean getShouldShowFlowView() {
        return mConfigData.getBoolean(mContext.getString(R.string.setting_key_floatview), true);
    }

    public void setFloatViewSize(String size) {
        mConfigData.edit()
                .putString(mContext.getString(R.string.setting_key_floatview_size), size)
                .commit();
    }
    public int getFloatViewSize() {
        return Integer.valueOf(
                mConfigData.getString(mContext.getString(R.string.setting_key_floatview_size),
                        mContext.getResources().getStringArray(R.array.array_floatview_size_value)[1]));
    }

    public Boolean getShoudAutoBoot() {
        return mConfigData.getBoolean(mContext.getString(R.string.setting_key_autoboot), true);
    }

    public Boolean getShoudUseBigScreenList() {
        return mConfigData.getBoolean(mContext.getString(R.string.setting_key_bigscreen_list), true);
    }

    public Boolean getShouldAlignToEdge() {
        return mConfigData.getBoolean(mContext.getString(R.string.setting_key_floatview_edge), true);
    }

    public void setFloatViewPos(int x, int y) {
        SharedPreferences.Editor editor = mConfigData.edit();
        editor.putInt(KEY_FLOATVIEW_POS_X, x);
        editor.putInt(KEY_FLOATVIEW_POS_Y, y);
        editor.commit();
    }

    public int getFloatViewPosX() {
        return mConfigData.getInt(KEY_FLOATVIEW_POS_X, 0);
    }

    public int getFloatViewPosY() {
        return mConfigData.getInt(KEY_FLOATVIEW_POS_Y, 0);
    }
}
