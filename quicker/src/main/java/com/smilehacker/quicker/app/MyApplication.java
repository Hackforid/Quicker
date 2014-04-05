package com.smilehacker.quicker.app;

import android.app.Application;

import com.smilehacker.quicker.data.model.*;

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
        Migration migration = new Migration();
        migration.createTable(PackageModel.class);
        sprinkles.addMigration(migration);
    }
}
