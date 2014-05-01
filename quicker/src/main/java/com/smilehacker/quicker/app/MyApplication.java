package com.smilehacker.quicker.app;

import android.app.Application;

import com.smilehacker.quicker.data.model.AppInfo;
import com.smilehacker.quicker.data.model.StringArraySerializer;

import se.emilsjolander.sprinkles.Migration;
import se.emilsjolander.sprinkles.Sprinkles;

/**
 * Created by kleist on 14-4-5.
 */
public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        initDB();
    }

    private void initDB() {
        Sprinkles sprinkles = Sprinkles.init(getApplicationContext());

        sprinkles.registerType(String[].class, new StringArraySerializer());

        Migration migration = new Migration();
        migration.createTable(AppInfo.class);
        sprinkles.addMigration(migration);
    }
}
