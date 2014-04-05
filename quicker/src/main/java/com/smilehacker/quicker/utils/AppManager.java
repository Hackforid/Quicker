package com.smilehacker.quicker.utils;

import android.app.LoaderManager;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.smilehacker.quicker.data.SPManager;
import com.smilehacker.quicker.data.model.AppInfo;
import com.smilehacker.quicker.data.model.PackageModel;

import java.util.ArrayList;
import java.util.List;

import se.emilsjolander.sprinkles.CursorList;
import se.emilsjolander.sprinkles.ManyQuery;
import se.emilsjolander.sprinkles.ModelList;
import se.emilsjolander.sprinkles.Query;
import se.emilsjolander.sprinkles.Transaction;

/**
 * Created by kleist on 14-4-2.
 */
public class AppManager {

    private Context mContext;
    private List<AppInfo> mAppInfos;
    private PackageManager mPackageManager;
    private SPManager mSPManager;
    private AppT9Parser mParser;
    private Gson mGson;
    private LoaderManager mLoaderManager;
    private ManyQuery.ResultHandler<PackageModel> mOnPackageLoaded;

    public AppManager(Context context) {
        this(context, null);
    }

    public AppManager(Context context, LoaderManager loaderManager) {
        mContext = context;
        mAppInfos = new ArrayList<AppInfo>();
        mPackageManager = context.getPackageManager();
        mSPManager = SPManager.getInstance(context);
        mParser = new AppT9Parser();
        mGson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
        mLoaderManager = loaderManager;

        mOnPackageLoaded = new ManyQuery.ResultHandler<PackageModel>() {
            @Override
            public boolean handleResult(CursorList<PackageModel> packageModels) {
                List<AppInfo> appInfos = new ArrayList<AppInfo>();
                for (PackageModel packageModel: packageModels) {
                    try {
                        AppInfo appInfo = mGson.fromJson(packageModel.json, AppInfo.class);
                        appInfos.add(appInfo);
                    } catch (Exception e) {
                        DLog.e(e.toString());
                    }
                }

                mAppInfos.clear();
                mAppInfos.addAll(appInfos);

                return true;
            }
        };
    }

    public void loadInstalledApps() {
        if (mSPManager.isInit()) {
            long time = System.currentTimeMillis();
            readPkgs();
            DLog.i("read app from db cost " + (System.currentTimeMillis() - time));
        } else {
            long time = System.currentTimeMillis();
            loadAndStoreAppsFromSys();
            DLog.i("load app cost " + (System.currentTimeMillis() - time));
        }
    }

    public void loadAndStoreAppsFromSys() {
        loadAppsFromSys();
        clearDB();
        storePkgs();
        mSPManager.setIsInit(true);
    }


    private void loadAppsFromSys() {
        List<PackageInfo> packages = mPackageManager.getInstalledPackages(PackageManager.PERMISSION_GRANTED);


        for (PackageInfo pkg : packages) {
            if (!isLaunchable(pkg)) {
                continue;
            }
            AppInfo appInfo = new AppInfo();
            appInfo.appName = mPackageManager.getApplicationLabel(pkg.applicationInfo).toString();
            appInfo.packageName = pkg.packageName;
            mParser.parseAppNameToT9(appInfo);
            mAppInfos.add(appInfo);
        }

    }

    public List<AppInfo> search(String inputNum) {
        List<AppInfo> appInfos = AppSearcher.search(mAppInfos, inputNum);
        return appInfos;
    }

    private Boolean isLaunchable(PackageInfo packageInfo) {
        return  mPackageManager.getLaunchIntentForPackage(packageInfo.packageName) != null;
    }

    private void storePkgs() {
        Transaction transaction = new Transaction();
        try {
            for (AppInfo appInfo : mAppInfos) {
                    String json = mGson.toJson(appInfo);
                    PackageModel packageModel = new PackageModel();
                    packageModel.name = appInfo.packageName;
                    packageModel.json = json;
                    packageModel.save();
            }
            transaction.setSuccessful(true);
        } catch (Exception e) {
            DLog.e(e.toString());
        } finally {
            transaction.finish();
        }
    }

    private void readPkgs() {
        if (mLoaderManager != null) {
            //Query.all(PackageModel.class).getAsync(mLoaderManager, mOnPackageLoaded);
            Query.all(PackageModel.class).get();
            List<AppInfo> appInfos = new ArrayList<AppInfo>();
            for (PackageModel packageModel: Query.all(PackageModel.class).get()) {
                try {
                    AppInfo appInfo = mGson.fromJson(packageModel.json, AppInfo.class);
                    appInfos.add(appInfo);
                } catch (Exception e) {
                    DLog.e(e.toString());
                }
            }

            mAppInfos.clear();
            mAppInfos.addAll(appInfos);
        }
    }

    private void clearDB() {
        ModelList<PackageModel> list = ModelList.from(Query.all(PackageModel.class).get());
        list.deleteAll();
    }

}
