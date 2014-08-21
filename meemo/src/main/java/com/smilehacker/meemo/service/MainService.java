package com.smilehacker.meemo.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.IBinder;
import android.os.SystemClock;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.smilehacker.meemo.R;
import com.smilehacker.meemo.activity.FloatActivity;
import com.smilehacker.meemo.activity.MainActivity;
import com.smilehacker.meemo.app.DeviceInfo;
import com.smilehacker.meemo.data.PrefsManager;
import com.smilehacker.meemo.data.model.event.FloatViewRefreshEvent;
import com.smilehacker.meemo.utils.DLog;

import de.greenrobot.event.EventBus;

public class MainService extends Service {

    public final static String KEY_COMMAND = "key_command";
    public final static String COMMAND_SHOW_FLOAT_VIEW = "command_show_float_view";
    public final static String COMMAND_REMOVE_FLOAT_VIEW = "command_remove_float_view";
    public final static String COMMAND_CHECK = "command_check";

    private DeviceInfo mDeviceInfo;
    private PrefsManager mSPManager;
    private WindowManager mWindowManager;
    private WindowManager.LayoutParams mWmParams;
    private LinearLayout mLayout;
    private ImageView mImageView;

    private int mStatusBarHeight;

    private int mTouchDownX;
    private int mTouchDownY;
    private Boolean mIsMove;
    private Boolean mIsFloatViewShow = false;
    private AlarmManager mAlarmManager;
    private PendingIntent mAlarmPendingIntent;

    private ScreenOrientationChangeBroadcastReceiver mReceiver;
    private EventBus mEventBus;

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mSPManager = PrefsManager.getInstance(this);
        mAlarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        mEventBus = EventBus.getDefault();
        mEventBus.register(this);

        Intent intent = new Intent(this, MainService.class);
        intent.putExtra(KEY_COMMAND, COMMAND_CHECK);
        mAlarmPendingIntent = PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        long triggerTime = SystemClock.elapsedRealtime();
        mAlarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME, triggerTime, 10000, mAlarmPendingIntent);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String command = intent.getStringExtra(KEY_COMMAND);
        if (command == null) {
            DLog.i("command null");
            showFloatView();
        } else if (command.equals(COMMAND_SHOW_FLOAT_VIEW)) {
            DLog.i("command show");
            showFloatView();
        } else if (command.equals(COMMAND_REMOVE_FLOAT_VIEW)) {
            DLog.i("command remove");
            this.stopSelf();
        } else if (command.equals(COMMAND_CHECK)) {
            DLog.i("service check");
            if (mSPManager.getShouldShowFlowView()) {
                showFloatView();
            } else {
                this.stopSelf();
            }
        }

        return START_REDELIVER_INTENT;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        DLog.i("service destroy");
        mAlarmManager.cancel(mAlarmPendingIntent);
        mEventBus.unregister(this);
        removeFloatView();
    }

    @SuppressWarnings("UnusedDeclaration")
    public void onEventMainThread(FloatViewRefreshEvent event) {
        switch (event.refreshType) {
            case ChangeSize:
                showFloatView();
                break;
            default:
                break;
        }
    }

    private void showFloatView() {
        if (mSPManager.getShouldShowFlowView()) {
            if (mIsFloatViewShow) {
                removeFloatView();
            }
            createFloatView();
        }
    }

    private void createFloatView() {
        mStatusBarHeight = getStatusBarHeight();
        mWindowManager = (WindowManager) getApplication().getSystemService(getApplication().WINDOW_SERVICE);
        mDeviceInfo = new DeviceInfo(this);

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
        mImageView = (ImageView) mLayout.findViewById(R.id.iv_flow);
        setFloatImageSize(mImageView);

        mWindowManager.addView(mLayout, mWmParams);
        mIsFloatViewShow = true;

        configureFloatView();
        listenScreenOrientationChange(true);
    }

    private void setFloatImageSize(ImageView imageView) {
        int size = (int) (mDeviceInfo.density * mSPManager.getFloatViewSize());
        setFloatImageSize(imageView, size);
    }

    private void setFloatImageSize(ImageView imageView, int size) {
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(size, size);
        imageView.setLayoutParams(layoutParams);
    }

    private void listenScreenOrientationChange(Boolean willListen) {
        if (willListen) {
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(Intent.ACTION_CONFIGURATION_CHANGED);
            mReceiver = new ScreenOrientationChangeBroadcastReceiver();
            registerReceiver(mReceiver, intentFilter);

        } else {
            if (mReceiver != null) {
                unregisterReceiver(mReceiver);
            }
        }
    }

    private void removeFloatView() {
        if (mLayout != null) {
            mWindowManager.removeView(mLayout);
        }
        listenScreenOrientationChange(false);
        mIsFloatViewShow = false;
    }

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
                Intent intent = new Intent(MainService.this, MainActivity.class);
                intent.putExtra(FloatActivity.KEY_POSITION_X, mWmParams.x);
                intent.putExtra(FloatActivity.KEY_POSITION_Y, mWmParams.y);
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
        if (!mSPManager.getShouldAlignToEdge()) {
            if (!(posX < 30 || mDeviceInfo.screenWidth - posX - mLayout.getMeasuredWidth() < 30)) {
                return;
            }
        }

        int moveDistance = mLayout.getMeasuredWidth() * 3 / 5;
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
