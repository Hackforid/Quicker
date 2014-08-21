package com.smilehacker.meemo.data.model.event;

/**
 * Created by kleist on 14-8-21.
 */
public class FloatViewRefreshEvent {
    public static enum RefreshType {
        ChangeSize, ChangePlace, AlignToEdge
    }

    public RefreshType refreshType;
    public int size;

}
