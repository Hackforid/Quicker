package com.smilehacker.meemo.views;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.smilehacker.meemo.R;

/**
 * Created by kleist on 14-5-7.
 */
public class KeyFloatWindowKey extends RelativeLayout {

    private String[] mLetter = {"", "0", "ABC", "DEF", "GHI", "JKL", "MNO", "PQRS", "TUV", "WXYZ"};

    private TextView mTvNum;
    private TextView mTvLetter;

    private int mNum;

    public KeyFloatWindowKey(Context context, int num) {
        super(context);
        setBackgroundResource(R.drawable.selector_tansparent);

        mNum = num;

        if (mNum < 0 || mNum > 9) {
            return;
        }

        initView();
    }

    private void initView() {
        LayoutInflater.from(getContext()).inflate(R.layout.view_floatwindow_key, this);
        mTvNum = (TextView) findViewById(R.id.tv_num);
        mTvLetter = (TextView) findViewById(R.id.tv_letter);

        mTvNum.setText(Integer.toString(mNum));
        mTvLetter.setText(mLetter[mNum]);
    }
}
