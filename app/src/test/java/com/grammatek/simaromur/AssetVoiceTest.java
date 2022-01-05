package com.grammatek.simaromur;

import android.content.Context;
import android.util.Log;

import androidx.test.core.app.ApplicationProvider;

import com.grammatek.simaromur.device.AssetVoiceManager;
import com.grammatek.simaromur.device.pojo.DeviceVoice;
import com.grammatek.simaromur.device.pojo.DeviceVoices;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(RobolectricTestRunner.class)
public class AssetVoiceTest {
    private final static String LOG_TAG = "Simarómur_Test_" + AssetVoiceTest.class.getSimpleName();
    private final Context context = ApplicationProvider.getApplicationContext();
    private AssetVoiceManager avm;

    public AssetVoiceTest() {
        try {
            avm = new AssetVoiceManager(context);
        }
        catch (IOException e) {
            Log.e(LOG_TAG, "Exception in test: " +  e.toString());
        }
    }

    @Test
    public void AssetVoiceManager_isInstantiated() {
        assertNotNull(avm);
    }

    @Test
    public void AssetVoiceManager_IterateVoiceList() {
        DeviceVoices voiceList = avm.getVoiceList();
        assert(voiceList.Voices.size() > 0);
        for (DeviceVoice voice:voiceList.Voices) {
            Log.v(LOG_TAG, voice.toString());
        }
    }
}
