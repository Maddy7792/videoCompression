package com.dbst.assignment.Utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetManager;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Utils {

    public static String getProperty(String url, Context context) throws IOException {
        Properties properties = new Properties();
        AssetManager assetManager = context.getAssets();
        InputStream inputStream = assetManager.open("maddy.properties");
        properties.load(inputStream);
        return properties.getProperty(url);
    }


    public static void storeKey(Context context, boolean key) {
        SharedPreferences preferences = context.getSharedPreferences(Constants.ACCESS, 0);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(Constants.ACCESS, key);
        editor.apply();
    }


    public static boolean getKey(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(Constants.ACCESS, 0);
        return preferences.getBoolean(Constants.ACCESS, false);
    }
}
