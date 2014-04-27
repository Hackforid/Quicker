package com.smilehacker.quicker.data.model.event;

import com.smilehacker.quicker.data.model.AppInfo;

import java.util.List;

/**
 * Created by kleist on 14-4-15.
 */
public class AppEvent {

    public enum AppEventType  {REFRESH};
    public List<AppInfo> appInfos;
    public AppEventType type;

    public AppEvent(List<AppInfo> appInfos) {
        this.appInfos = appInfos;
        this.type = AppEventType.REFRESH;
    }

}
