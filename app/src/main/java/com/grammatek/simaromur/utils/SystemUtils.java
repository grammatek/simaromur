package com.grammatek.simaromur.utils;

import android.os.Build;
import android.util.Log;

import java.util.Arrays;

// Some system utilities
public class SystemUtils {
    private static String LOG_TAG = "Simaromur_" + SystemUtils.class.getSimpleName();
    /**
     * Returns the Android arch name
     *
     * One of the following values is returned:
     *  - aarch64
     *  - armv7a
     *  - x86_64
     *  - i686
     *
     * @return  Android arch name, throws a RuntimeException if the arch cannot be determined by
     *          Build.SUPPORTED_ABIS
     */
    public static String androidArchName() throws RuntimeException {
        // https://developer.android.com/ndk/guides/abis
        // https://developer.android.com/reference/android/os/Build.SUPPORTED_ABIS
        String[] abis = Build.SUPPORTED_ABIS;
        Log.v(LOG_TAG, "Build.SUPPORTED_ABIS: " + Arrays.toString(abis));
        for (String archArch : abis) {
            // order in most to least likely
            switch (archArch) {
                case "arm64-v8a":
                    return "aarch64";
                case "armeabi-v7a":
                    return "arm";
                case "x86_64":
                    return "x86_64";
                case "x86":
                    return "i686";
            }
        }
        throw new RuntimeException("System.androidArchName: Unable to determine Android arch name from " + Arrays.toString(abis));
    }
}
