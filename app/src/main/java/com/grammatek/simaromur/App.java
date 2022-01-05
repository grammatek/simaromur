package com.grammatek.simaromur;

import android.app.Application;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.NetworkRequest;
import android.os.Build;
import android.util.Log;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.grammatek.simaromur.frontend.NormalizationManager;
import com.grammatek.simaromur.network.ConnectionCheck;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class App extends Application {
    private final static String LOG_TAG = "Simaromur_" + App.class.getSimpleName();
    ExecutorService executorService = Executors.newFixedThreadPool(4);
    AppRepository mAppRepository;
    private static FirebaseAnalytics sFirebaseAnalytics;
    private static App sApplication;

    private NormalizationManager mNormalizationManager;
    ConnectionCheck mConnectionChecker;
    public static App getApplication() {
        return sApplication;
    }
    public static AppRepository getAppRepository() {
        return sApplication.mAppRepository;
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

    public NormalizationManager getNormalizationManager() {
        return mNormalizationManager;
    }

    @Override
    public void onCreate() {
        Log.v(LOG_TAG, "onCreate()");
        super.onCreate();
        sApplication = this;
        mConnectionChecker = new ConnectionCheck(this);
        mConnectionChecker.registerNetworkCallback();
        // Obtain the FirebaseAnalytics instance.
        sFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        try {
            mAppRepository = new AppRepository(this);
        } catch (IOException e) {
            e.printStackTrace();
            // bad, see https://stackoverflow.com/questions/8943288/how-to-implement-uncaughtexception-android#answer-8943671
        }
        mNormalizationManager = new NormalizationManager(this.getBaseContext());
    }
}
