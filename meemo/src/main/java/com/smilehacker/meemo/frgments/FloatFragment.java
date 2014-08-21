package com.smilehacker.meemo.frgments;

import android.app.Fragment;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.GridLayout;
import android.widget.GridView;

import com.smilehacker.meemo.R;
import com.smilehacker.meemo.utils.AppManager;
import com.smilehacker.meemo.utils.DLog;
import com.smilehacker.meemo.views.HorizentalAppGridView;
import com.smilehacker.meemo.views.KeyFloatWindowKey;

/**
 * Created by kleist on 14-5-7.
 */
public class FloatFragment extends Fragment {

    private GridLayout mGlKeyboard;
    private HorizentalAppGridView mAppGrid;

    private AppManager mAppManager;
    private String mNumStr;

    public FloatFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAppManager = AppManager.getInstance(getActivity());
        mNumStr = "";
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_float, container, false);

        mGlKeyboard = (GridLayout) view.findViewById(R.id.gl_keyboard);
        mAppGrid = (HorizentalAppGridView) view.findViewById(R.id.v_app_pager);
        initKeyboard();

        return view;
    }

    private void initKeyboard() {
        mGlKeyboard.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    mGlKeyboard.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                } else {
                    mGlKeyboard.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                }
                addKeyToKeyboard();
            }
        });
    }

    private void addKeyToKeyboard() {
        int gridWidth = mGlKeyboard.getMeasuredWidth() / 3;
        int gridHeight = mGlKeyboard.getMeasuredHeight() / 3;

        for (int i = 0; i < 9; i++) {
            KeyFloatWindowKey keyView = new KeyFloatWindowKey(getActivity(), i+1);
            GridLayout.LayoutParams lp = new GridLayout.LayoutParams(GridLayout.spec(i / 3), GridLayout.spec(i % 3));
            lp.setGravity(Gravity.CENTER);
            lp.width = gridWidth;
            lp.height = gridHeight;
            keyView.setLayoutParams(lp);

            mGlKeyboard.addView(keyView);
            keyView.setOnClickListener(new KeyboradOnClickListener(i+1));
        }
    }

    private class KeyboradOnClickListener implements View.OnClickListener {

        private int num;

        public KeyboradOnClickListener(int num) {
            this.num = num;
        }

        @Override
        public void onClick(View view) {
            mNumStr += Integer.toString(this.num);
            mAppGrid.setApps(mAppManager.search(mNumStr));
        }
    }
}
