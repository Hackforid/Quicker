package com.smilehacker.meemo.view;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.smilehacker.meemo.R;
import com.smilehacker.meemo.data.model.AppInfo;
import com.smilehacker.meemo.util.DLog;
import com.smilehacker.meemo.util.PackageHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by kleist on 14-5-8.
 */
public class HorizentalAppGridView extends ViewPager{

    public final static int GRID_VIEW_COLUMN_NUM = 4;

    private LayoutInflater mLayoutInflater;
    private PackageManager mPackageManager;
    private PackageHelper mPackageHelper;
    private ViewPagerAdapter mAdapter;

    public HorizentalAppGridView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        mPackageHelper= new PackageHelper(getContext());
        mPackageManager = getContext().getPackageManager();
        mLayoutInflater = LayoutInflater.from(getContext());

        mAdapter = new ViewPagerAdapter(new ArrayList<GridView>());
        setAdapter(mAdapter);
    }

    public void setApps(List<AppInfo> appInfos) {
        List<GridView> gridViews = new ArrayList<GridView>();
        int size = appInfos.size();
        int remain = appInfos.size() % GRID_VIEW_COLUMN_NUM;
        for (int i = GRID_VIEW_COLUMN_NUM; i < size; i += GRID_VIEW_COLUMN_NUM) {
            gridViews.add(createGridView(appInfos.subList(i - GRID_VIEW_COLUMN_NUM, i)));
        }
        if (remain != 0) {
            gridViews.add(createGridView(appInfos.subList(size - remain, size)));
        }
        DLog.i("set apps:" + gridViews.size());
        mAdapter.refresh(gridViews);
    }

    private GridView createGridView(List<AppInfo> appInfos) {
        GridView gridView = new GridView(getContext());
        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        gridView.setLayoutParams(layoutParams);
        gridView.setNumColumns(GRID_VIEW_COLUMN_NUM);
        gridView.setGravity(Gravity.CENTER);
        gridView.setAdapter(new AppGridAdapter(appInfos));
        return gridView;
    }

    private class ViewPagerAdapter extends PagerAdapter {

        private List<GridView> gridViews;

        public ViewPagerAdapter(List<GridView> gridViews) {
            this.gridViews = gridViews;
        }

        public void refresh(List<GridView> list) {
            gridViews.clear();
            gridViews.addAll(list);
            notifyDataSetChanged();
            setCurrentItem(0);
        }

        /**
         * Return the number of views available.
         */
        @Override
        public int getCount() {
            return gridViews.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            container.addView(gridViews.get(position));
            return  gridViews.get(position);
        }
    }

    private class AppGridAdapter extends BaseAdapter {

        private List<AppInfo> appInfos;

        public AppGridAdapter(List<AppInfo> appInfos) {
            this.appInfos = appInfos;
        }

        @Override
        public int getCount() {
            return appInfos.size();
        }

        @Override
        public Object getItem(int i) {
            return appInfos.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            ViewHolder viewHolder;
            if (view == null) {
                view = mLayoutInflater.inflate(R.layout.item_grid_app, viewGroup, false);
                viewHolder = new ViewHolder();
                viewHolder.icon = (ImageView) view.findViewById(R.id.img_icon);
                view.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) view.getTag();
            }

            viewHolder.icon.setImageDrawable(getIcon(appInfos.get(i)));

            return view;
        }

        private class ViewHolder {
            public ImageView icon;
        }

        private Drawable getIcon(AppInfo appInfo) {
            PackageInfo packageInfo = mPackageHelper.getPkgInfoByPkgName(appInfo.packageName);
            return packageInfo.applicationInfo.loadIcon(mPackageManager);
        }
    }


}
