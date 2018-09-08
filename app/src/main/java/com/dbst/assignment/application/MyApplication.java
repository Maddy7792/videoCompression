package com.dbst.assignment.application;

import android.app.Application;
import android.os.Environment;

import com.dbst.assignment.Utils.Utils;
import com.facebook.soloader.SoLoader;

import java.io.IOException;

public class MyApplication extends Application{

    private static MyApplication mInstance;
    private static final String DROP_FILES_DIR = "/PICTURES/";
    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
        SoLoader.init(this, false);
    }

    public static MyApplication getInstance() {
        return mInstance;
    }

    public static void setInstance(MyApplication mInstance) {
        MyApplication.mInstance = mInstance;
    }


    public String getCompressedClip() {
        String croppedClip = null;

        try {
            croppedClip = this.getFileStoragePath() + "/PICTURES/" + Utils.getProperty("CompressedClipName", this);
        } catch (IOException var3) {
            var3.printStackTrace();
        }

        return croppedClip;
    }

    private String getFileStoragePath() {
        return Environment.getExternalStorageDirectory().toString();
    }
}
