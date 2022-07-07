package com.grammatek.simaromur.device;

import android.content.res.AssetManager;
import android.util.Log;

import com.grammatek.simaromur.device.flite.NativeFliteTTS;
import com.grammatek.simaromur.device.pojo.DeviceVoice;
import com.grammatek.simaromur.device.pojo.DeviceVoiceFile;

public class TTSEngineFlite  implements TTSEngine {
    private final static String LOG_TAG = "Simaromur_" + TTSEngineFlite.class.getSimpleName();
    private DeviceVoice mVoice = null;
    private NativeFliteTTS mFliteEngine;

    // Constructor for loading models from within Assets
    public TTSEngineFlite(AssetManager asm, DeviceVoice voice) {
        assert (voice.Type.equals("flite"));
        assert (voice.Files.size() == 1);

        String phonemeModelPath = null;

        for (DeviceVoiceFile voiceFile : voice.Files) {
            switch (voiceFile.Type) {
                case "phoneme":
                    phonemeModelPath = "voices/" + voiceFile.Path;
                    break;
                case "grapheme":
                    Log.e(LOG_TAG, "Unsupported Flite model type " + voiceFile.Type);
                    break;
                default:
                    // Unknown
                    Log.e(LOG_TAG, "Unknown model type " + voiceFile.Type);
                    assert(false);
                    break;
            }
        }

        if (phonemeModelPath == null) {
            throw new RuntimeException("Voice " + voice.Name + " doesn't specify a supported model");
        }

        // In case of a new voice: unload all existing models
        if (mVoice != voice) {
            mFliteEngine = null;
        }

        // load voice models
        if (mFliteEngine == null) {
            Log.v(LOG_TAG, "Loading FLite model file " + phonemeModelPath + " ...");
            mFliteEngine = new NativeFliteTTS(null);
        } else {
            Log.v(LOG_TAG, "FLite model file " + phonemeModelPath + " already loaded");
        }

        Log.v(LOG_TAG, "FLite model loaded");
        mVoice = voice;
    }

    @Override
    public byte[] SpeakToPCM(String sampas) {
        return new byte[0];
    }

    @Override
    public int GetNativeSampleRate() {
        return 22050;
    }

    @Override
    public void Stop() {

    }
}
