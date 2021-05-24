package com.grammatek.simaromur;

import android.app.Application;
import android.content.Context;
import android.os.Environment;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class App extends Application {
    ExecutorService executorService = Executors.newFixedThreadPool(4);

    private static Application sApplication;

    public static Application getApplication() {
        return sApplication;
    }

    public static Context getContext() {
        return getApplication().getApplicationContext();
    }

    public static String getAbsoluteFilePath(String relativePath) {
        if (Utility.isExternalStorageWritable()) {
            return new File(getContext().getExternalFilesDir(null), relativePath).getPath();
        } else {
            return new File(getContext().getFilesDir(), relativePath).getPath();
        }
    }
    public static String getDataPath() {
        return getAbsoluteDirPath("data")+"/";
    }

    public static String getVoiceDataPath() {
        return getAbsoluteDirPath("voices")+"/";
    }

    public static String getAbsoluteDirPath(String relativePath) {
        if (Utility.isExternalStorageWritable()) {
            return new File(getContext().getExternalFilesDir(null), relativePath).getPath();
        } else {
            return new File(getContext().getFilesDir(), relativePath).getParent();
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        sApplication = this;
    }
}