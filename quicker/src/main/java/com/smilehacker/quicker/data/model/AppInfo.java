package com.smilehacker.quicker.data.model;

import android.graphics.drawable.Drawable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.Date;
import java.util.List;

import se.emilsjolander.sprinkles.CursorList;
import se.emilsjolander.sprinkles.Model;
import se.emilsjolander.sprinkles.ModelList;
import se.emilsjolander.sprinkles.Query;
import se.emilsjolander.sprinkles.annotations.AutoIncrementPrimaryKey;
import se.emilsjolander.sprinkles.annotations.Column;
import se.emilsjolander.sprinkles.annotations.Table;
import se.emilsjolander.sprinkles.annotations.Unique;

/**
 * Created by kleist on 14-4-2.
 */

@Table("app")
public class AppInfo extends Model {

    public final static String tableName = "app";

    @AutoIncrementPrimaryKey
    @Column("id")
    public long id;

    @Expose
    @SerializedName("app_name")
    @Column("app_name")
    public String appName;

    @Expose
    @SerializedName("package_name")
    @Unique
    @Column("package_name")
    public String packageName;

    @Expose
    @SerializedName("full_t9")
    @Column("full_t9")
    public String[] fullT9;

    @Expose
    @SerializedName("short_t9")
    @Column("short_t9")
    public String[] shortT9;

    @Column("launch_count")
    public long launchCount = 0;

    @Column("update_date")
    public Date launchDate = new Date(0);

    public double priority;

    public Drawable icon;

    @Override
    protected void beforeSave() {
        super.beforeSave();
    }

    public static List<AppInfo> getInstalledApps() {
        CursorList<AppInfo> appInfos = Query.all(AppInfo.class).get();
        List<AppInfo> list = appInfos.asList();
        appInfos.close();
        return  list;
    }

    public static AppInfo getAppByPackage(String packageName) {
        return Query.one(AppInfo.class, "SELECT * FROM app WHERE package_name = ?", packageName).get();
    }

    public static void deleteAll() {
        ModelList.from(Query.all(AppInfo.class).get()).deleteAll();
    }

    public void increaseLaunchCount() {
        launchCount++;
        launchDate = new Date();
        save();
    }
}
