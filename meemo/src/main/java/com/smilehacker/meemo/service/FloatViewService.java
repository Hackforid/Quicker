package com.smilehacker.meemo.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.IBinder;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.smilehacker.meemo.R;
import com.smilehacker.meemo.activity.MainActivity;
import com.smilehacker.meemo.app.DeviceInfo;
import com.smilehacker.meemo.data.SPManager;
import com.smilehacker.meemo.utils.DLog;

public class FloatViewService extends Service {

    private DeviceInfo mDeviceInfo;
    private SPManager mSPManager;
    private WindowManager mWindowManager;
    private WindowManager.LayoutParams mWmParams;
    private LinearLayout mLayout;
    private ImageView mImageView;

    private int mStatusBarHeight;

    private int mTouchDownX;
    private int mTouchDownY;
    private Boolean mIsMove;

    private ScreenOrientationChangeBroadcastReceiver mReceiver;

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mStatusBarHeight = getStatusBarHeight();
        mWindowManager = (WindowManager) getApplication().getSystemService(getApplication().WINDOW_SERVICE);
        mDeviceInfo = new DeviceInfo(this);
        mSPManager = SPManager.getInstance(this);
        createFloatView();

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_CONFIGURATION_CHANGED);
        mReceiver = new ScreenOrientationChangeBroadcastReceiver();
        registerReceiver(mReceiver, intentFilter);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        removeView();
        unregisterReceiver(mReceiver);
    }

    private void createFloatView() {
        mWmParams = new WindowManager.LayoutParams();
        mWmParams.type = WindowManager.LayoutParams.TYPE_PHONE;
        mWmParams.format = PixelFormat.RGBA_8888;
        mWmParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        mWmParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
        mWmParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        mWmParams.gravity = Gravity.LEFT | Gravity.TOP;


        mWmParams.x = mSPManager.getFloatViewPosX();
        mWmParams.y = mSPManager.getFloatViewPosY();
        mWmParams.x = mWmParams.x > mDeviceInfo.screenWidth ? mDeviceInfo.screenWidth : mWmParams.x;
        mWmParams.y = mWmParams.y > mDeviceInfo.screenHeight ? mDeviceInfo.screenHeight : mWmParams.y;


        LayoutInflater inflater = LayoutInflater.from(getApplicationContext());
        mLayout = (LinearLayout) inflater.inflate(R.layout.view_flow, null);
        mWindowManager.addView(mLayout, mWmParams);
        mImageView = (ImageView) mLayout.findViewById(R.id.iv_flow);

        configureFloatView();
    }

    private void removeView() {
        if (mLayout != null) {
            mWindowManager.removeView(mLayout);
        }
    }

    @SuppressWarnings("ConstantConditions")
    private void configureFloatView() {
        mImageView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {


                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN: {
                        mIsMove = false;
                        mTouchDownX = (int) motionEvent.getRawX();
                        mTouchDownY = (int) motionEvent.getRawY();
                        mImageView.animate().translationX(0).start();
                    }
                    case MotionEvent.ACTION_MOVE: {
                        int touchX = (int) motionEvent.getRawX();
                        int touchY = (int) motionEvent.getRawY();

                        if (Math.abs(touchX - mTouchDownX) > 20 || Math.abs(touchY - mTouchDownY) > 20) {
                            mIsMove = true;
                            mWmParams.x = (int) motionEvent.getRawX() - mImageView.getMeasuredWidth() / 2;
                            mWmParams.y = (int) motionEvent.getRawY() - mImageView.getMeasuredHeight() / 2 - mStatusBarHeight;
                            updateView();
                        }

                        break;
                    }
                    case MotionEvent.ACTION_UP: {
                       animateToEdge();
                       return mIsMove;
                    }
                }

                return false;
            }
        });

        mImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(FloatViewService.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });

        mImageView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    mImageView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                } else {
                    mImageView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                }
                animateToEdge();
            }
        });
    }

    private void animateToEdge() {
        int posX = mWmParams.x;
        int moveDistance = mLayout.getMeasuredWidth() * 2 / 3;
        int edgeX = (mDeviceInfo.screenWidth - posX) > mDeviceInfo.screenWidth / 2 ? 0 : mDeviceInfo.screenWidth;
        mWmParams.x = edgeX;
        updateView();

        int distance = edgeX == 0 ? -moveDistance : moveDistance;
        mImageView.animate().translationX(distance).start();
        mSPManager.setFloatViewPos(mWmParams.x, mWmParams.y);
    }

    private void updateView() {
        mWindowManager.updateViewLayout(mLayout, mWmParams);
    }

    private int getStatusBarHeight() {
        int statusBarHeight = 0;

        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            statusBarHeight = getResources().getDimensionPixelSize(resourceId);
        }

        return statusBarHeight;
    }

    private class ScreenOrientationChangeBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            mDeviceInfo.getScreenInfo();
            animateToEdge();
        }
    }
}
