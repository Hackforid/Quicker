package com.smilehacker.quicker.data.model;

import com.google.gson.annotations.SerializedName;
import com.google.gson.annotations.Expose;

/**
 * Created by kleist on 14-4-2.
 */
public class AppInfo {

    @Expose
    @SerializedName("app_name")
    public String appName;

    @Expose
    @SerializedName("package_name")
    public String packageName;

    @Expose
    @SerializedName("full_t9")
    public String[] fullT9;

    @Expose
    @SerializedName("short_t9")
    public String[] shortT9;

    public double priority;
}
