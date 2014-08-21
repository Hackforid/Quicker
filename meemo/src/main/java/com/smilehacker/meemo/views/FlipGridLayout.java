package com.smilehacker.meemo.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.GridLayout;

/**
 * Created by kleist on 14-6-6.
 */
public class FlipGridLayout extends GridLayout {

    private GestureDetector mGestureDetector;
    private Onfliplistener mOnFlipListener;


    public FlipGridLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public FlipGridLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public FlipGridLayout(Context context) {
        super(context);
        init();
    }

    public void setOnFlipListener(Onfliplistener listener) {
        mOnFlipListener = listener;
    }

    private void init() {
        mGestureDetector = new GestureDetector(new GestureDetector.SimpleOnGestureListener() {
            private static final int FLING_MIN_DISTANCE = 50;
            private static final int FLING_MIN_VELOCITY = 0;
            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                if (e2.getY() - e1.getY() > FLING_MIN_DISTANCE && Math.abs(velocityY) > FLING_MIN_VELOCITY) {
                    if (mOnFlipListener != null) {
                        mOnFlipListener.onflingdown();
                    }
                    return true;
                }
                return super.onFling(e1, e2, velocityX, velocityY);
            }

        });
    }


    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return mGestureDetector.onTouchEvent(ev) || super.onInterceptTouchEvent(ev);
    }

    public interface Onfliplistener {
        public void onflingdown();
    }
}
