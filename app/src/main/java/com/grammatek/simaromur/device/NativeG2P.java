package com.grammatek.simaromur.device;

import android.content.Context;
import android.util.Log;

import com.grammatek.simaromur.App;
import com.grammatek.simaromur.utils.FileUtils;

import java.io.File;

public class NativeG2P {
    private final static String LOG_TAG = "G2P_Java_" + NativeG2P.class.getSimpleName();
    private final static String ASSET_SUB_PATH = "g2p";
    private final static String G2P_MODEL_FILENAME = "g2p.far";
    static {
        System.loadLibrary("g2p");
        nativeClassInit();
    }

    private final Context mContext;
    private final String mDestinationPath;
    private boolean mInitialized = false;

    public NativeG2P(Context context) {
        mDestinationPath = new File(App.getDataPath()).getParent() +"/" + ASSET_SUB_PATH;
        mContext = context;
        attemptInit();
    }

    @Override
    protected void finalize() {
        nativeDestroy();
    }

    public String process(String text) {
        return nativeProcess(text);
    }

    private void attemptInit() {
        if (mInitialized) {
            return;
        }

        try {
            FileUtils.copyAssetFilesRecursive(mContext.getAssets(), ASSET_SUB_PATH, mDestinationPath);
            // create the native interface
            if (!nativeCreate(mDestinationPath + "/" + G2P_MODEL_FILENAME)) {
                Log.e(LOG_TAG, "Failed to initialize G2P library");
                return;
            }
            Log.i(LOG_TAG, "Initialized G2P");
        } catch (Exception e) {
            Log.e(LOG_TAG, "Failed to initialize G2P library");
        }
        mInitialized = true;
    }

    private long mNativeData;       // keep this here!
    private static native final boolean nativeClassInit();
    private native final boolean nativeCreate(String path);
    private native final boolean nativeDestroy();
    private native String nativeProcess(String text);
}
