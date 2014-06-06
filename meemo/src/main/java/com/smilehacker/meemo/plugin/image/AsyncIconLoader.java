package com.smilehacker.meemo.plugin.image;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.util.LruCache;
import android.widget.ImageView;


import com.smilehacker.meemo.R;

import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by kleist on 14-5-19.
 */
public class AsyncIconLoader {

    private LruCache<String, Bitmap> mMemoryCache;
    private PackageManager mPackageManager;
    private Context mContext;

    private Executor mExecutor;

    public AsyncIconLoader(Context context) {
        mPackageManager = context.getPackageManager();
        mContext = context;
        mExecutor = new ThreadPoolExecutor(4, 128, 10, TimeUnit.SECONDS, new LinkedBlockingDeque<Runnable>());
        initCache();
    }

    private void initCache() {
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        final int cacheSize = maxMemory / 8;
        mMemoryCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap value) {
                return value.getByteCount() / 1024;
            }
        };

    }

    private void addBitmapToMemoryCache(String key, Bitmap bitmap) {
        if (getBitmapFromMemCache(key) == null) {
            mMemoryCache.put(key, bitmap);
        }
    }

    private Bitmap getBitmapFromMemCache(String key) {
        return mMemoryCache.get(key);
    }

    public void loadBitmap(String packageName, ImageView imageView) {
        Bitmap bitmap = getBitmapFromMemCache(packageName);
        if (bitmap != null) {
            imageView.setImageBitmap(bitmap);
            return;
        } else {
            asyncLoadIcon(packageName, imageView);
        }
    }

    private void asyncLoadIcon(String packageName, final ImageView imageView) {
        AsyncIconLoadTask task = new AsyncIconLoadTask(packageName, imageView);
        task.executeOnExecutor(mExecutor);
    }

    private Bitmap getIcon(String packageName) {
        try {
            PackageInfo packageInfo = mPackageManager.getPackageInfo(packageName, 0);
            Drawable drawable = packageInfo.applicationInfo.loadIcon(mPackageManager);
            return ((BitmapDrawable) drawable).getBitmap();
        } catch (PackageManager.NameNotFoundException e) {
            return null;
        }
    }

    private class AsyncIconLoadTask extends AsyncTask<Void, Void, Bitmap> {

        private String packageName;
        private ImageView imageView;

        public AsyncIconLoadTask(String packageName, ImageView imageView) {
            this.packageName = packageName;
            this.imageView = imageView;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            imageView.setTag(packageName);
            imageView.setImageDrawable(mContext.getResources().getDrawable(R.drawable.transparent));
        }

        @Override
        protected Bitmap doInBackground(Void... voids) {
            return getIcon(packageName);
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            if (bitmap != null) {
                addBitmapToMemoryCache(packageName, bitmap);
                if (packageName.equals(imageView.getTag())) {
                    imageView.setImageBitmap(bitmap);
                }
            }
        }
    }
}
