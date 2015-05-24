package com.smilehacker.meemo.app;

import android.app.Application;

import com.activeandroid.ActiveAndroid;

/**
 * Created by kleist on 14-4-5.
 */
public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        initDB();

//        Intent intent = new Intent(getApplicationContext(), MainService.class);
//        intent.putExtra(MainService.KEY_COMMAND, MainService.COMMAND_SHOW_FLOAT_VIEW);
//        startService(intent);
    }


    private void initDB() {
        ActiveAndroid.initialize(this);
    }

}
