package com.smilehacker.quicker.frgments;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.app.Fragment;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Point;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AbsListView;
import android.widget.GridLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.smilehacker.quicker.R;
import com.smilehacker.quicker.adapter.AppAdapter;
import com.smilehacker.quicker.data.model.AppInfo;
import com.smilehacker.quicker.data.model.event.AppEvent;
import com.smilehacker.quicker.utils.AppManager;
import com.smilehacker.quicker.utils.DLog;
import com.smilehacker.quicker.utils.PackageHelper;
import com.smilehacker.quicker.views.KeyView;

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

    private String mNumStr;
    private Boolean mIsKeyboardHide = false;
    private int mKeyBoradHeight;
    private int mKeyboardTop;
    private Boolean mIsLoadApps = false;
    private Boolean mShouldRest = false;

    private AppAdapter mAppAdapter;
    private AppManager mAppManager;
    private EventBus mEventBus;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAppAdapter = new AppAdapter(getActivity(), this, new ArrayList<AppInfo>());
        mAppManager = AppManager.getInstance(getActivity());
        mKeyBoradHeight = getResources().getDimensionPixelOffset(R.dimen.keyboard_height);
        mNumStr = "";
        mEventBus = EventBus.getDefault();
        mEventBus.register(this);
        loadApps();
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
                mGlKeyboard.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                mKeyboardTop = mGlKeyboard.getTop();
            }
        });
    }


    private void loadApps() {
        mAppManager.load();
        mAppAdapter.refreshApps(mAppManager.getRecentUpdateApps());
        mIsLoadApps = true;
    }

    private void setViewHeight() {
        TypedArray actionbarSizeTypedArray = getActivity().obtainStyledAttributes(new int[] {
                android.R.attr.actionBarSize
        });
        float actionbarHeight = actionbarSizeTypedArray.getDimension(0, getActivity().getResources().getDimensionPixelSize(R.dimen.dial_header_height));
        mTvNum.setHeight((int) actionbarHeight);

        ViewGroup.LayoutParams footerLayoutParams = mRlFooter.getLayoutParams();
        footerLayoutParams.height = (int) actionbarHeight;
        mRlFooter.setLayoutParams(footerLayoutParams);
    }

    private void initView() {
        mLvApps.setAdapter(mAppAdapter);

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
        });

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
            keyView.setOnClickListener(new KeyboradOnClickListener(i + 1));
        }
    }

    @SuppressWarnings("UnusedDeclaration")
    public void onEvent(AppEvent appEvent) {
        DLog.i("get refresh apps");
        if (appEvent.type == AppEvent.AppEventType.REFRESH) {
            mAppAdapter.updateApps(appEvent.appInfos);
        }
    }

    private void showKeyboard(Boolean isShow) {
        if (isShow && mIsKeyboardHide) {
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
        } else if (!isShow && !mIsKeyboardHide) {
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
            }
        } else {
            if (mTvNum.getVisibility() != View.GONE) {
                mTvNum.setVisibility(View.GONE);
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

    @Override
    public void onResume() {
        super.onResume();
        if (mShouldRest) {
            mShouldRest = false;
            restDialer();
        }
    }

    private void restDialer() {
        mAppAdapter.refreshApps(mAppManager.getRecentUpdateApps());
        mNumStr = "";
        mTvNum.setText("");
        showInputBox(false);
        showKeyboard(true);
    }

    public void openAppAndHideDialer(AppInfo appInfo) {

        PackageHelper packageHelper = new PackageHelper(getActivity());
        mShouldRest = true;

        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        getActivity().startActivity(intent);

        try {
            packageHelper.openApp(appInfo.packageName);
        } catch (Exception e) {
            DLog.e(e.toString());
        }

        mAppManager.increaseLaunchCount(appInfo);
    }
}
