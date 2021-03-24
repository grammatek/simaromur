package com.grammatek.simaromur;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


public class NativeG2P {
    private final static String LOG_TAG = "G2P_Java_" + NativeG2P.class.getSimpleName();
    static {
        System.loadLibrary("g2p");
        nativeClassInit();
    }

    private final Context mContext;
    private final String mDatapath;
    private final String mAssetspath;
    private boolean mInitialized = false;

    public NativeG2P(Context context) {
        mDatapath = new File(App.getDataPath()).getParent();
        mAssetspath = mDatapath+"/g2p";
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

        // @todo: extract all files from assets and write them to the
        //        common data path
        copyAssets("g2p");

        // create the native interface
        if (!nativeCreate(mAssetspath+"/g2p.far")) {
            Log.e(LOG_TAG, "Failed to initialize G2P library");
            return;
        }
        Log.i(LOG_TAG, "Initialized G2P");
        mInitialized = true;
    }

    /**
     * Copy G2P assets (far files) to the data directory of the application.
     *
     * @todo: opimization : Add additional file to assets, that marks the version and MD5 sum of each
     *        of the files. We don't want to copy assets each time this application starts
     * @param assetSubPath  Path inside assets where to look for G2P specific files
     */
    private void copyAssets(String assetSubPath) {
        AssetManager assetManager = mContext.getAssets();
        String[] files = null;
        try {
            files = assetManager.list(assetSubPath);
            // create destination assetSubPath if not exists
            String destDir = mDatapath + "/" + assetSubPath + "/";
            try {
            File dir = new File(destDir);
                if (!dir.exists())
                    dir.mkdir();
            } catch(SecurityException e) {
                Log.e(LOG_TAG, "Failed to create directory: " + destDir, e);
            }
            for(String filename : files) {
                try {
                    File outFile = new File(destDir, filename);
                    OutputStream outStream = new FileOutputStream(outFile);
                    InputStream inStream = assetManager.open(assetSubPath + "/" + filename);

                    copyFile(inStream, outStream);
                    inStream.close();
                    outStream.flush();
                    outStream.close();
                    Log.i(LOG_TAG, "Copied " + filename + " to " + outFile.getAbsolutePath());
                } catch(IOException e) {
                    Log.e(LOG_TAG, "Failed to copy asset file: " + filename, e);
                }
            }
        } catch (IOException e) {
            Log.e(LOG_TAG, "Failed to get asset file list.", e);
        }
    }

    private void copyFile(InputStream inStream, OutputStream outStream) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while((read = inStream.read(buffer)) != -1) {
            outStream.write(buffer, 0, read);
        }
    }

    private long mNativeData;
    private static native final boolean nativeClassInit();
    private native final boolean nativeCreate(String path);
    private native final boolean nativeDestroy();
    private native String nativeProcess(String text);
}
