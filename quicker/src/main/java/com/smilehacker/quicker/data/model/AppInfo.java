package com.smilehacker.quicker.data.model;

import java.util.Date;

import com.google.gson.annotations.SerializedName;
import com.google.gson.annotations.Expose;

import se.emilsjolander.sprinkles.Model;
import se.emilsjolander.sprinkles.annotations.AutoIncrementPrimaryKey;
import se.emilsjolander.sprinkles.annotations.Column;
import se.emilsjolander.sprinkles.annotations.Table;
import se.emilsjolander.sprinkles.annotations.Unique;

/**
 * Created by kleist on 14-4-2.
 */

@Table("app")
public class AppInfo extends Model {

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
    public Date updateDate;

    public double priority;

    @Override
    protected void beforeSave() {
        super.beforeSave();
        updateDate = new Date();
    }
}
