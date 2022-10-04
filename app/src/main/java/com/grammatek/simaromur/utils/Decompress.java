package com.grammatek.simaromur.utils;

import android.util.Log;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class Decompress {
    private final static String LOG_TAG = "Simaromur_" + Decompress.class.getSimpleName();
    private final String mZipfile;
    private String mLocation;
    private String mLastEntry;
    private final ArrayList<String> mIgnoredFiles = new ArrayList<>();
    private long mLastFileSize = 0;


    /**
     * Constructor. Initializes the decompress object.
     *
     * @param zipFile       Full path to the zip file
     * @param location      Full path to the location where the zip file will be decompressed
     * @param ignoreList    List of files to be ignored during decompression
     */
    public Decompress(String zipFile, String location, List<String> ignoreList) {
        mZipfile = zipFile;
        mLocation = location;
        if (! mLocation.endsWith("/")) {
            mLocation += "/";
        }
        if (! FileUtils.mkdir(mLocation)) {
            throw new RuntimeException("Decompress: Unable to create directory " + mLocation);
        }
        mIgnoredFiles.addAll(ignoreList);
    }

    /**
     * Unzip a ZIP archive to a location specified in constructor. The ZIP archive is also given in
     * the constructor.
     *
     * @return true if successful, false otherwise
     */
    public boolean unzip() {
        try  {
            FileInputStream inputStream = new FileInputStream(mZipfile);
            ZipInputStream zipInputStream = new ZipInputStream(inputStream);
            ZipEntry zipEntry;
            byte[] buffer = new byte[1024*1024];
            Log.v(LOG_TAG, "Decompress: ");
            while ((zipEntry = zipInputStream.getNextEntry()) != null) {
                Log.v(LOG_TAG, "Unzipping " + zipEntry.getName());
                if (zipEntry.isDirectory()) {
                    FileUtils.mkdir(mLocation + zipEntry.getName());
                } else {
                    if (matchIgnoreList(zipEntry)) {
                        Log.v(LOG_TAG, "Ignoring " + zipEntry.getName());
                        continue;
                    }
                    FileOutputStream outputStream = new FileOutputStream(mLocation + zipEntry.getName());
                    int count;
                    while ((count = zipInputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, count);
                    }
                    zipInputStream.closeEntry();
                    outputStream.close();
                    mLastEntry = mLocation + zipEntry.getName();
                    mLastFileSize = zipEntry.getSize();
                }
            }
            zipInputStream.close();
            return true;
        } catch(Exception e) {
            Log.e("Decompress", "unzip", e);
        }
        return false;
    }

    private boolean matchIgnoreList(ZipEntry zipEntry) {
        for (String ignoredFile : mIgnoredFiles) {
            if (zipEntry.getName().endsWith(ignoredFile)) {
                return true;
            }
        }
        return false;
    }

    public String getLastEntry() {
        return mLastEntry;
    }
    public long getLastFileSize() {
        return mLastFileSize;
    }
}