package com.smilehacker.meemo.data.model;

import android.graphics.drawable.Drawable;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Delete;
import com.activeandroid.query.Select;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.Date;
import java.util.List;


/**
 * Created by kleist on 14-4-2.
 */

@Table(name = "appinfo")
public class AppInfo extends Model {

    public final static String tableName = "app";

    @Expose
    @SerializedName("app_name")
    @Column(name = "app_name")
    public String appName;

    @Expose
    @SerializedName("package_name")
    @Column(name = "package_name", index = true)
    public String packageName;

    @Expose
    @SerializedName("full_t9")
    @Column(name = "full_t9")
    public String[] fullT9;

    @Expose
    @SerializedName("short_t9")
    @Column(name = "short_t9")
    public String[] shortT9;

    @Column(name = "launch_count")
    public long launchCount = 0;

    @Column(name = "launch_date")
    public Date launchDate = new Date(0);

    public double priority;

    public Drawable icon;


    public static List<AppInfo> getInstalledApps() {
        return new Select().from(AppInfo.class)
                .execute();
    }

    public static AppInfo getAppByPackage(String packageName) {
        return new Select().from(AppInfo.class)
                .where("package_name = ?", packageName)
                .executeSingle();
    }

    public static void deleteAll() {
        new Delete().from(AppInfo.class).execute();
    }

    public void increaseLaunchCount() {
        launchCount++;
        launchDate = new Date();
        save();
    }
}
