package com.grammatek.simaromur;

import android.app.Application;
import android.content.Context;
import android.os.Environment;
import android.util.Log;

import com.google.firebase.FirebaseApp;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.firebase.perf.FirebasePerformance;
import com.grammatek.simaromur.network.ConnectionCheck;

import java.io.File;
import java.io.IOException;

public class App extends Application {
    private final static String LOG_TAG = "Simaromur_" + App.class.getSimpleName();
    AppRepository mAppRepository;
    private static FirebaseAnalytics sFirebaseAnalytics;
    private static FirebaseCrashlytics sFirebaseCrashlytics;
    private static App sApplication;
    public static App getApplication() {
        return sApplication;
    }
    public static AppRepository getAppRepository() {
        return sApplication.mAppRepository;
    }

    public static Context getContext() {
        return getApplication().getApplicationContext();
    }

    /* Checks if external storage is available for read and write */
    public static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }

    public static String getAbsoluteFilePath(String relativePath) {
        if (isExternalStorageWritable()) {
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
        if (isExternalStorageWritable()) {
            return new File(getContext().getExternalFilesDir(null), relativePath).getPath();
        } else {
            return new File(getContext().getFilesDir(), relativePath).getParent();
        }
    }

    /**
     * Switch on/off Firebase performance/analytics/crashreports
     *
     * @param isEnabled if true, all metrics & crash reports are enabled, and via false
     *                  all metrics & crash reports are disabled.
     */
    public static void setFirebaseAnalytics(boolean isEnabled) {
        FirebasePerformance.getInstance().setPerformanceCollectionEnabled(isEnabled);
        sFirebaseCrashlytics.setCrashlyticsCollectionEnabled(isEnabled);
        sFirebaseAnalytics.setAnalyticsCollectionEnabled(isEnabled);
    }

    @Override
    public void onCreate() {
        Log.v(LOG_TAG, "onCreate()");
        super.onCreate();
        sApplication = this;
        // Obtain the FirebaseAnalytics instance.
        FirebaseApp.initializeApp(getContext());
        sFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        sFirebaseCrashlytics = FirebaseCrashlytics.getInstance();
        try {
            mAppRepository = new AppRepository(this);
        } catch (IOException e) {
            e.printStackTrace();
            // bad, see https://stackoverflow.com/questions/8943288/how-to-implement-uncaughtexception-android#answer-8943671
        }
    }
}
