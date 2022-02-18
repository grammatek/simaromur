package com.grammatek.simaromur;

import android.content.Context;
import android.os.Build;
import android.util.Log;

import androidx.test.core.app.ApplicationProvider;

import com.grammatek.simaromur.device.AssetVoiceManager;
import com.grammatek.simaromur.device.pojo.DeviceVoice;
import com.grammatek.simaromur.device.pojo.DeviceVoices;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.io.IOException;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = {Build.VERSION_CODES.O_MR1})
public class AssetVoiceTest {
    private final static String LOG_TAG = "Simarómur_Test_" + AssetVoiceTest.class.getSimpleName();

    public AssetVoiceTest() { }

    @Test
    public void AssetVoiceManager_IterateVoiceList() throws IOException {
        Context context = ApplicationProvider.getApplicationContext();
        final AssetVoiceManager avm = new AssetVoiceManager(context);
        final DeviceVoices voiceList = avm.getVoiceList();
        assert(voiceList.Voices.size() > 0);
        for (DeviceVoice voice:voiceList.Voices) {
            Log.v(LOG_TAG, voice.toString());
        }
    }
}
