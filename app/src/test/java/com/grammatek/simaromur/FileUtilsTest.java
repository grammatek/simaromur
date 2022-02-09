package com.grammatek.simaromur;

import android.content.Context;
import android.content.res.AssetManager;

import androidx.test.core.app.ApplicationProvider;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertNotEquals;

@RunWith(RobolectricTestRunner.class)
public class FileUtilsTest {
    private final static String LOG_TAG = "Simarómur_Test_" + FileUtilsTest.class.getSimpleName();
    private final static Context context = ApplicationProvider.getApplicationContext();
    private final static AssetManager assetManager = context.getAssets();

    @Test
    public void readAssetDateList_Test() {
        try {
            HashMap<String, LocalDateTime> fileDateMap = FileUtils.readAssetDateList(assetManager);
            assert(fileDateMap.size() > 0);
            final LocalDateTime now = LocalDateTime.now();
            for (Map.Entry<String, LocalDateTime> entry : fileDateMap.entrySet()) {
                assertNotEquals(entry.getKey(), "");
                assert(entry.getValue().compareTo(now) < 0);
            }
        }
        catch (IOException e) {
            assert(false);
        }
    }

    @Test
    public void getAssetDate_Test() {
        final String TestFile = "lastModified.txt";
        final LocalDateTime now = LocalDateTime.now();
        try {
            LocalDateTime modificationTime = FileUtils.getAssetDate(assetManager, TestFile);
            assert(modificationTime.compareTo(now) < 0);
        }
        catch (IOException e) {
            assert(false);
        }
    }
}
