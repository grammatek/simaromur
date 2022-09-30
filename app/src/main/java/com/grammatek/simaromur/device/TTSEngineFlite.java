package com.grammatek.simaromur.device;

import android.util.Log;

import com.grammatek.simaromur.App;
import com.grammatek.simaromur.db.Voice;
import com.grammatek.simaromur.device.flite.NativeFliteVoice;
import com.grammatek.simaromur.device.pojo.DeviceVoice;
import com.grammatek.simaromur.frontend.Pronunciation;
import com.grammatek.simaromur.utils.FileUtils;

import java.time.Duration;
import java.time.Instant;

public class TTSEngineFlite  implements TTSEngine {
    private final static String LOG_TAG = "Simaromur_" + TTSEngineFlite.class.getSimpleName();
    private static NativeFliteVoice sFliteEngine;
    private final Pronunciation mPronunciation;

    // Constructor for loading models from within Assets
    public TTSEngineFlite(Voice voice, DeviceVoice deviceVoice) {
        assert (deviceVoice.Type.equals("flite"));
        assert (voice.url.equals("disk"));
        String phonemeModelPath = voice.downloadPath;
        Log.v(LOG_TAG, "Trying flite voice " + phonemeModelPath);
        if (FileUtils.exists(phonemeModelPath)) {
            Log.i(LOG_TAG, "Found voice file " + phonemeModelPath);
        } else {
            Log.e(LOG_TAG, "Voice file not found " + phonemeModelPath);
            throw new IllegalArgumentException("No voice file found for " + voice.name);
        }

        // load voice models
        if (sFliteEngine == null) {
            Log.v(LOG_TAG, "Loading FLite model file " + phonemeModelPath + " ...");
            sFliteEngine = new NativeFliteVoice(phonemeModelPath);
        } else {
            Log.v(LOG_TAG, "FLite model file " + phonemeModelPath + " already loaded");
        }
        mPronunciation = new Pronunciation(App.getContext());
        Log.v(LOG_TAG, "FLite model loaded");
    }

    @Override
    public byte[] SpeakToPCM(String sampas) {
        final Instant startTime = Instant.now();

        String flitePhonemes = mPronunciation.convert(sampas, "SAMPA", "FLITE", true);
        Log.v(LOG_TAG, "Flite phonemes: " + flitePhonemes);
        byte[] pcm = sFliteEngine.synthesize(flitePhonemes);

        final long timeElapsedMs = Duration.between(startTime, Instant.now()).toMillis();
        final float audioTimeInSecs = (float) pcm.length
                / ((float) sFliteEngine.getBitsPerSample() / 8.0F) / GetNativeSampleRate();
        Log.v(LOG_TAG, "Voice generation ran for " + timeElapsedMs / 1000.0F
                + " secs, for audio of length " + audioTimeInSecs + " secs: "
                + audioTimeInSecs * 1000.0F / timeElapsedMs + " x real-time");
        return pcm;
    }

    @Override
    public int GetNativeSampleRate() {
        return sFliteEngine.getSampleRate();
    }
}
