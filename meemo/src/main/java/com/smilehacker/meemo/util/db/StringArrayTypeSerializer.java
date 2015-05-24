package com.smilehacker.meemo.util.db;

import com.activeandroid.serializer.TypeSerializer;


/**
 * Created by kleist on 15/5/24.
 */
public class StringArrayTypeSerializer extends TypeSerializer {
    private final static String SEP = ":我是分隔符:";

    @Override
    public Class<?> getDeserializedType() {
        return String[].class;
    }

    @Override
    public Class<?> getSerializedType() {
        return String.class;
    }

    @Override
    public String serialize(Object data) {
        if (data == null) {
            return null;
        }
        return joinArray(data);
    }

    @Override
    public String[] deserialize(Object data) {
        return splitArray((String) data);
    }

    private String joinArray(Object data) {
        String[] strs = (String[]) data;
        StringBuilder builder = new StringBuilder();
        for (String str : strs) {
            builder.append(str);
            builder.append(SEP);
        }
        return builder.toString();
    }

    private String[] splitArray(String str) {
        return str.split(SEP);
    }
}
