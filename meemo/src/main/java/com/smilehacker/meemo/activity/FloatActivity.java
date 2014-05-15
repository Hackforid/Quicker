package com.smilehacker.meemo.activity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.smilehacker.meemo.R;
import com.smilehacker.meemo.app.DeviceInfo;
import com.smilehacker.meemo.frgments.FloatFragment;
import com.smilehacker.meemo.plugin.GAActivity;

/**
 * Created by kleist on 14-5-7.
 */
public class FloatActivity extends GAActivity{

    public final static String KEY_POSITION_X = "key_position_x";
    public final static String KEY_POSITION_Y = "key_position_y";

    private FrameLayout mContainer;
    private RelativeLayout mRlRoot;
    private DeviceInfo mDeviceInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_float);
        mDeviceInfo = new DeviceInfo(this);

        mContainer = (FrameLayout) findViewById(R.id.container);
        mRlRoot = (RelativeLayout) findViewById(R.id.rl_root);

        enableTransparent();
        computeFloatWindowPostion();
        initRootView();

        getFragmentManager().beginTransaction()
                .replace(R.id.container, new FloatFragment())
                .commit();

    }

    private void computeFloatWindowPostion() {
        Intent intent = getIntent();
        int x = intent.getIntExtra(KEY_POSITION_X, 0);
        int y = intent.getIntExtra(KEY_POSITION_Y, 0);
        int height = getResources().getDimensionPixelSize(R.dimen.float_window_height);
        int width = getResources().getDimensionPixelSize(R.dimen.float_window_width);

        x =  x < mDeviceInfo.screenWidth / 2 ? 0 : mDeviceInfo.screenWidth - width;
        y =  height / 2 > y ? 0 : y - height / 2;


        ViewGroup.LayoutParams lp = mContainer.getLayoutParams();
        lp.height = height;
        lp.width = width;
        mContainer.setLayoutParams(lp);
        mContainer.setX(x);
        mContainer.setY(y);
    }

    private void initRootView() {
        mRlRoot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FloatActivity.this.finish();
            }
        });
    }

    private void enableTransparent() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            return;
        }


        Window window = getWindow();
        window.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION, WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
    }

}
