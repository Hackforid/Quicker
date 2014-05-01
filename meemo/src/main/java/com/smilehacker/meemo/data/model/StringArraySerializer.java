package com.smilehacker.meemo.data.model;

import android.content.ContentValues;
import android.database.Cursor;

import com.google.gson.Gson;

import se.emilsjolander.sprinkles.typeserializers.SqlType;
import se.emilsjolander.sprinkles.typeserializers.TypeSerializer;

/**
 * Created by kleist on 14-4-8.
 */
public class StringArraySerializer implements TypeSerializer<String[]> {

    private Gson mGson = new Gson();

    @Override
    public String[] unpack(Cursor cursor, String s) {
        return mGson.fromJson(cursor.getString(cursor.getColumnIndexOrThrow(s)), String[].class);
    }

    @Override
    public void pack(String[] strings, ContentValues contentValues, String s) {
        contentValues.put(s, mGson.toJson(strings));
    }

    @Override
    public String toSql(String[] strings) {
        return mGson.toJson(strings);
    }

    @Override
    public SqlType getSqlType() {
        return SqlType.TEXT;
    }
}
