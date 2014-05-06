package com.smilehacker.meemo.frgments;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.app.Fragment;
import android.content.Intent;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.GridLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.smilehacker.meemo.R;
import com.smilehacker.meemo.activity.SettingActivity;
import com.smilehacker.meemo.adapter.AppAdapter;
import com.smilehacker.meemo.data.SPManager;
import com.smilehacker.meemo.data.model.AppInfo;
import com.smilehacker.meemo.data.model.event.AppEvent;
import com.smilehacker.meemo.service.MainService;
import com.smilehacker.meemo.utils.AppManager;
import com.smilehacker.meemo.utils.DLog;
import com.smilehacker.meemo.utils.PackageHelper;
import com.smilehacker.meemo.views.KeyView;

import java.util.ArrayList;

import de.greenrobot.event.EventBus;
import me.grantland.widget.AutofitTextView;

/**
 * Created by kleist on 14-4-1.
 */
public class DialFragment extends Fragment{

    private GridLayout mGlKeyboard;
    private ListView mLvApps;
    private RelativeLayout mRlFooter;
    private TextView mTvNum;
    private RelativeLayout mRlBackspace;
    private RelativeLayout mRlDialer;
    private RelativeLayout mRlSetting;
    private RelativeLayout mRlRoot;
    private View mStatusBar;
    private View mNavgationBar;

    private String mNumStr;
    private Boolean mIsKeyboardHide = false;
    private int mKeyBoradHeight;
    private int mKeyboardTop;
    private Boolean mIsLoadApps = false;

    private AppAdapter mAppAdapter;
    private AppManager mAppManager;
    private EventBus mEventBus;
    private SPManager mSPManager;

    private Boolean mIsKitKat = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAppAdapter = new AppAdapter(getActivity(), this, new ArrayList<AppInfo>());
        mAppManager = AppManager.getInstance(getActivity());
        mSPManager = SPManager.getInstance(getActivity());
        mKeyBoradHeight = getResources().getDimensionPixelOffset(R.dimen.keyboard_height);
        mNumStr = "";

        mEventBus = EventBus.getDefault();
        mEventBus.register(this);

        startMainService();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mEventBus.unregister(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dialer, container, false);

        mGlKeyboard = (GridLayout) view.findViewById(R.id.gl_keyboard);
        mLvApps = (ListView) view.findViewById(R.id.lv_apps);
        mRlFooter = (RelativeLayout) view.findViewById(R.id.rl_footer);
        mTvNum = (AutofitTextView) view.findViewById(R.id.tv_num);
        mRlBackspace = (RelativeLayout) view.findViewById(R.id.rl_backspace);
        mRlDialer = (RelativeLayout) view.findViewById(R.id.rl_dialer);
        mRlSetting = (RelativeLayout) view.findViewById(R.id.rl_setting);
        mRlRoot = (RelativeLayout) view.findViewById(R.id.rl_root);
        mStatusBar = view.findViewById(R.id.v_status_bar);
        mNavgationBar = view.findViewById(R.id.v_navigation);

        setTransparent();
        setViewHeight();
        initKeyboard();
        initView();


        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mGlKeyboard.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    mGlKeyboard.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                } else {
                    mGlKeyboard.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                }
                mKeyboardTop = mGlKeyboard.getTop();
            }
        });
        loadApps();
    }


    private void loadApps() {
        mAppAdapter.refreshApps(mAppManager.getRecentUpdateApps());
        mIsLoadApps = true;
    }

    private void setTransparent() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            mIsKitKat = false;
            return;
        }

        mIsKitKat = true;

        Window window = getActivity().getWindow();
        window.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION, WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);

        mStatusBar.setVisibility(View.VISIBLE);
        ViewGroup.LayoutParams lp = mStatusBar.getLayoutParams();
        lp.height = getStatusBarHeight();
        mStatusBar.setLayoutParams(lp);

        ViewGroup.LayoutParams navgationLp = mNavgationBar.getLayoutParams();
        navgationLp.height = getNavigationBarHeight();
        mNavgationBar.setLayoutParams(navgationLp);
    }

    private void setViewHeight() {
        int actionbarHeight = (int) getActionBarHeight();

        mTvNum.setHeight(actionbarHeight);

        ViewGroup.LayoutParams footerLayoutParams = mRlFooter.getLayoutParams();
        footerLayoutParams.height = actionbarHeight;
        mRlFooter.setLayoutParams(footerLayoutParams);
    }

    private float getActionBarHeight() {
        TypedArray actionbarSizeTypedArray = getActivity().obtainStyledAttributes(new int[] {
                android.R.attr.actionBarSize
        });
        return actionbarSizeTypedArray.getDimension(0, getResources().getDimensionPixelSize(R.dimen.dial_header_height));
    }

    private int getStatusBarHeight() {
        int statusBarHeight = 0;

        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            statusBarHeight = getResources().getDimensionPixelSize(resourceId);
        }

        return statusBarHeight;
    }

    private int getNavigationBarHeight() {
        if (ViewConfiguration.get(getActivity()).hasPermanentMenuKey()) {
            return 0;
        }

        Resources resources = getResources();
        int resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android");
        if (resourceId > 0) {
            return resources.getDimensionPixelSize(resourceId);
        }
        return 0;
    }

    private void initView() {
        mLvApps.setAdapter(mAppAdapter);

        setBackspace();

        mLvApps.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int i) {
                if (i == SCROLL_STATE_TOUCH_SCROLL) {
                    showKeyboard(false);
                }
            }

            @Override
            public void onScroll(AbsListView absListView, int i, int i2, int i3) {
            }
        });


        mRlDialer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showKeyboard(mIsKeyboardHide);
            }
        });

        mRlSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), SettingActivity.class);
                startActivity(intent);
            }
        });
    }

    private Boolean deleteAllInputNum() {
        if (!mIsLoadApps) {
            return false;
        }

        if (TextUtils.isEmpty(mNumStr)) {
            return false;
        } else {
            mNumStr = "";
            mTvNum.setText(mNumStr);
            showInputBox(false);
            return true;
        }
    }

    private void setBackspace() {
        final GestureDetector gestureDetector = new GestureDetector(getActivity(), new GestureDetector.SimpleOnGestureListener() {
            private static final int FLING_MIN_DISTANCE = 50;
            private static final int FLING_MIN_VELOCITY = 0;
            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                if (e1.getX() - e2.getX() > FLING_MIN_DISTANCE && Math.abs(velocityX) > FLING_MIN_VELOCITY) {
                    deleteAllInputNum();
                }
                return super.onFling(e1, e2, velocityX, velocityY);
            }
        });

        mRlBackspace.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                return gestureDetector.onTouchEvent(event);
            }
        });

        mRlBackspace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (!mIsLoadApps) {
                    return;
                }

                if (TextUtils.isEmpty(mNumStr)) {
                    return;
                } else {
                    mNumStr = mNumStr.substring(0, mNumStr.length() - 1);
                    mTvNum.setText(mNumStr);
                    if (TextUtils.isEmpty(mNumStr)) {
                        showInputBox(false);
                    } else {
                        searchAppByNum();
                    }
                }
            }
        });

        mRlBackspace.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                return deleteAllInputNum();
            }

        });
    }

    private void setKeyboardGesture(View view) {
        final GestureDetector gestureDetector = new GestureDetector(getActivity(), new GestureDetector.SimpleOnGestureListener() {
            private static final int FLING_MIN_DISTANCE = 50;
            private static final int FLING_MIN_VELOCITY = 0;
            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                if (e2.getY() - e1.getY() > FLING_MIN_DISTANCE && Math.abs(velocityY) > FLING_MIN_VELOCITY) {
                    showKeyboard(mIsKeyboardHide);
                }
                return super.onFling(e1, e2, velocityX, velocityY);
            }
        });

        view.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                return gestureDetector.onTouchEvent(event);
            }
        });
    }

    private void initKeyboard() {

        Point size = new Point();
        getActivity().getWindowManager().getDefaultDisplay().getSize(size);
        int gridWidth = size.x / 3;
        int gridHeight = getResources().getDimensionPixelSize(R.dimen.keyboard_height) / 3;

        for (int i = 0; i < 9; i++) {
            KeyView keyView = new KeyView(getActivity(), i+1);

            GridLayout.LayoutParams param = new GridLayout.LayoutParams(GridLayout.spec(i / 3), GridLayout.spec(i % 3));
            param.setGravity(Gravity.CENTER);
            param.width = gridWidth;
            param.height = gridHeight;
            keyView.setLayoutParams(param);

            mGlKeyboard.addView(keyView);
            setKeyboardGesture(keyView);
            keyView.setOnClickListener(new KeyboradOnClickListener(i + 1));
        }
    }

    @SuppressWarnings("UnusedDeclaration")
    public void onEventMainThread(AppEvent appEvent) {
        DLog.i("get refresh apps");
        if (appEvent.type == AppEvent.AppEventType.REFRESH) {
            mAppAdapter.updateApps(appEvent.appInfos);
        }
    }

    private void showKeyboard(Boolean toShow) {
        if (toShow && mIsKeyboardHide) {
            ValueAnimator va = ValueAnimator.ofInt(0, mKeyBoradHeight);
            va.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    Integer value = (Integer) valueAnimator.getAnimatedValue();
                    mLvApps.setPadding(mLvApps.getPaddingLeft(), mLvApps.getPaddingTop(), mLvApps.getPaddingRight(), value);

                    mGlKeyboard.setTop(mKeyboardTop + mKeyBoradHeight - value);

                    ViewGroup.LayoutParams lp = mGlKeyboard.getLayoutParams();
                    lp.height = value;
                    mGlKeyboard.setLayoutParams(lp);
                }
            });
            va.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animator) {

                }

                @Override
                public void onAnimationEnd(Animator animator) {
                    mIsKeyboardHide = false;
                }

                @Override
                public void onAnimationCancel(Animator animator) {

                }

                @Override
                public void onAnimationRepeat(Animator animator) {

                }
            });
            va.setDuration(300);
            va.start();
        } else if (!toShow && !mIsKeyboardHide) {
            ValueAnimator va = ValueAnimator.ofInt(0, mKeyBoradHeight);
            va.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    Integer value = (Integer) valueAnimator.getAnimatedValue();
                    mLvApps.setPadding(mLvApps.getPaddingLeft(), mLvApps.getPaddingTop(), mLvApps.getPaddingRight(), mKeyBoradHeight - value);


                    ViewGroup.LayoutParams lp = mGlKeyboard.getLayoutParams();
                    lp.height = mKeyBoradHeight - value;
                    mGlKeyboard.setLayoutParams(lp);

                    mGlKeyboard.setTop(mKeyboardTop + value);
                }
            });
            va.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animator) {
                    //mLvApps.setPadding(mLvApps.getPaddingLeft(), mLvApps.getPaddingTop(), mLvApps.getPaddingRight(), mKeyBoradHeight);
                }

                @Override
                public void onAnimationEnd(Animator animator) {
                    mIsKeyboardHide = true;
                }

                @Override
                public void onAnimationCancel(Animator animator) {

                }

                @Override
                public void onAnimationRepeat(Animator animator) {

                }
            });
            va.setDuration(300);
            va.start();
        }
    }

    private void showInputBox(Boolean isShow) {
        if (isShow) {
            if (mTvNum.getVisibility() != View.VISIBLE) {
                mTvNum.setVisibility(View.VISIBLE);
                mStatusBar.setBackgroundColor(getResources().getColor(R.color.bg_input_box_transparent));
            }
        } else {
            if (mTvNum.getVisibility() != View.GONE) {
                mTvNum.setVisibility(View.GONE);
                mStatusBar.setBackgroundColor(getResources().getColor(android.R.color.transparent));
            }
        }
    }

    private class KeyboradOnClickListener implements View.OnClickListener {

        private int num;

        public KeyboradOnClickListener(int num) {
            this.num = num;
        }

        @Override
        public void onClick(View view) {
            if (!mIsLoadApps) {
                return;
            }

            mNumStr += Integer.toString(this.num);
            mTvNum.setText(mNumStr);
            showInputBox(true);
            searchAppByNum();
        }
    }

    private void searchAppByNum() {
        mAppAdapter.refreshApps(mAppManager.search(mNumStr));

        if (!mLvApps.isStackFromBottom()) {
            mLvApps.setStackFromBottom(true);
        }
        mLvApps.setStackFromBottom(false);
    }

    public void openAppAndHideDialer(AppInfo appInfo) {
        PackageHelper packageHelper = new PackageHelper(getActivity());


        try {
            packageHelper.openApp(appInfo.packageName);
        } catch (Exception e) {
            DLog.e(e.toString());
        }

        mAppManager.increaseLaunchCount(appInfo);

        getActivity().finish();
    }


    private void startMainService() {
        Intent intent = new Intent(getActivity().getApplicationContext(), MainService.class);
        if (mSPManager.getShouldShowFlowView()) {
            intent.putExtra(MainService.KEY_COMMAND, MainService.COMMAND_SHOW_FLOAT_VIEW);
        }
        getActivity().startService(intent);
    }
}
