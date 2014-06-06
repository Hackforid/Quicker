package com.smilehacker.meemo.plugin;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by kleist on 14-5-27.
 */
public class BaseActivity extends Activity {

    private List<AsyncTask> mAsyncTasks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAsyncTasks = new ArrayList<AsyncTask>();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cancelAllAsyncTask();
    }

    /**
     * Cancel all asyncTask that registered in activity
     */
    public void cancelAllAsyncTask() {
        List<AsyncTask> taskList = new ArrayList<AsyncTask>(mAsyncTasks);
        for (AsyncTask task : taskList) {
            task.cancel(true);
        }
    }

    class SmartAsyncTask extends AsyncTask {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mAsyncTasks.add(this);
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            return null;
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            mAsyncTasks.remove(this);
        }

    }
}

