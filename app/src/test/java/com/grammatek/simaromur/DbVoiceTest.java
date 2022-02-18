package com.grammatek.simaromur;

import android.content.Context;
import android.content.res.AssetManager;
import android.os.Build;
import android.util.Log;

import androidx.test.core.app.ApplicationProvider;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.grammatek.simaromur.device.AssetVoiceManager;
import com.grammatek.simaromur.device.pojo.DeviceVoice;
import com.grammatek.simaromur.device.pojo.DeviceVoices;
import com.grammatek.simaromur.db.Voice;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = {Build.VERSION_CODES.O_MR1})
public class DbVoiceTest {
    private final static String LOG_TAG = "Simarómur_Test_" + FileUtilsTest.class.getSimpleName();
    private final static Context context = ApplicationProvider.getApplicationContext();
    private AssetVoiceManager avm;

    public DbVoiceTest() {
        try {
            avm = new AssetVoiceManager(context);
        }
        catch (IOException e) {
            Log.e(LOG_TAG, "Exception in test: " + e);
            assert(false);
        }
    }

    @Test
    public void voiceFromDeviceVoiceTest() {
        List<Voice> voiceList = new ArrayList<>();

        DeviceVoices deviceVoiceList = avm.getVoiceList();
        assert(deviceVoiceList.Voices.size() > 0);
        try {
            final AssetManager assetManager = context.getAssets();
            for (DeviceVoice deviceVoice:deviceVoiceList.Voices) {
                voiceList.add(new Voice(assetManager, deviceVoice));
            }
            assert(! voiceList.isEmpty());
        }
        catch(IOException e) {
            assert(false);
        }
        avm = null;
    }
}
