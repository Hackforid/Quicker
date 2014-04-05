package com.smilehacker.quicker.adapter;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.smilehacker.quicker.R;
import com.smilehacker.quicker.data.model.AppInfo;
import com.smilehacker.quicker.frgments.DialFragment;
import com.smilehacker.quicker.utils.PackageHelper;

import java.util.List;

/**
 * Created by kleist on 14-4-2.
 */
public class AppAdapter extends BaseAdapter {

    private List<AppInfo> mAppInfos;
    private Context mContext;
    private LayoutInflater mLayoutInflater;
    private PackageHelper mPackageHelper;
    private PackageManager mPackageManager;

    private DialFragment mFragment;

    public AppAdapter(Context context, DialFragment dialFragment, List<AppInfo> appInfos) {
        mAppInfos = appInfos;
        mContext = context;
        mLayoutInflater = LayoutInflater.from(mContext);
        mPackageManager = context.getPackageManager();
        mPackageHelper = new PackageHelper(context);
        mFragment = dialFragment;
    }

    public void refreshApps(List<AppInfo> appInfos) {
        mAppInfos.clear();
        mAppInfos.addAll(appInfos);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mAppInfos.size();
    }

    @Override
    public Object getItem(int i) {
        return mAppInfos.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder holder;
        if (view == null) {
            view = mLayoutInflater.inflate(R.layout.item_app, viewGroup, false);
            holder = new ViewHolder();
            holder.tvAppName = (TextView) view.findViewById(R.id.tv_name);
            holder.ivAppIcon = (ImageView) view.findViewById(R.id.iv_icon);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }

        final AppInfo appInfo = mAppInfos.get(i);
        PackageInfo packageInfo = mPackageHelper.getPkgInfoByPkgName(appInfo.packageName);
        holder.tvAppName.setText(mAppInfos.get(i).appName);
        holder.ivAppIcon.setImageDrawable(packageInfo.applicationInfo.loadIcon(mPackageManager));

        view.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                mFragment.openAppAndHideDialer(appInfo);
            }
        });


        return view;
    }

    private static class ViewHolder {
        public TextView tvAppName;
        public ImageView ivAppIcon;
    }
}
