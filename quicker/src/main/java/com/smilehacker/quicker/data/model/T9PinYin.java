package com.smilehacker.quicker.data.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by kleist on 14-4-5.
 */
public class T9PinYin {
    @Expose
    public String name;

    @Expose
    @SerializedName("full_t9")
    public String[] fullT9;

    @Expose
    @SerializedName("short_t9")
    public String[] shortT9;
}
