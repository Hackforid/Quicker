package com.smilehacker.quicker.views;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.smilehacker.quicker.R;

/**
 * Created by kleist on 14-4-1.
 */
public class KeyView extends FrameLayout {

    private String[] mLetter = {"", "0", "ABC", "DEF", "GHI", "JKL", "MNO", "PQRS", "TUV", "WXYZ"};

    private TextView mTvNum;
    private TextView mTvLetter;

    private int mNum;


    /**
     *
     * @param context
     * @param num [1-9]
     */
    public KeyView(Context context, int num) {
        super(context);
        mNum = num;

        if (mNum < 0 || mNum > 9) {
            return;
        }

        initView();
    }

    private void initView() {
        LayoutInflater.from(getContext()).inflate(R.layout.view_key, this);
        mTvNum = (TextView) findViewById(R.id.tv_num);
        mTvLetter = (TextView) findViewById(R.id.tv_letter);

        mTvNum.setText(Integer.toString(mNum));
        mTvLetter.setText(mLetter[mNum]);
    }

}
