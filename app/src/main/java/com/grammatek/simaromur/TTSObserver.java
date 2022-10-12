package com.grammatek.simaromur;

import android.content.Context;
import android.util.Log;

import com.grammatek.simaromur.audio.AudioManager;
import com.grammatek.simaromur.audio.AudioObserver;

public class TTSObserver implements AudioObserver {
    private final static String LOG_TAG = "Simaromur_" + TTSObserver.class.getSimpleName();

    private final float mPitch;
    private final float mSpeed;
    private final int mSampleRate;
    private boolean mIsStopped = false;

    /**
     * Constructor called for device voice synthesis
     * @param pitch         Audio pitch factor
     * @param speed         Audio speed factor
     * @param sampleRate    Sample rate used for the audio playback
     */
    public TTSObserver(float pitch, float speed, int sampleRate) {
        mPitch = pitch;
        mSpeed = speed;
        mSampleRate = sampleRate;
    }

    public float getPitch() {
        return mPitch;
    }

    public float getSpeed() {
        return mSpeed;
    }

    // interface implementation

    /**
     * We receive the audio response from the API, convert it if necessary according to the
     * values given in mPitch and mSpeed, and then feed the resulting buffer piece by piece to
     * the callback object provided by the Android TTS API.
     *
     * @param audioData Audio response data from Network API
     */
    public synchronized void update(final byte[] audioData, TTSRequest ttsRequest) {
        Log.v(LOG_TAG, "update() for (" + ttsRequest.serialize() + ")");
        if (mIsStopped) {
            Log.i(LOG_TAG, "observer was stopped");
            return;
        }
        byte[] processedAudio = AudioManager.applyPitchAndSpeed(audioData, mSampleRate, mPitch, mSpeed);
        // TTS service awaits the result of the synthesis
        TTSProcessingResult goodProcessingResult = new TTSProcessingResult(ttsRequest);
        goodProcessingResult.addAudio(processedAudio);
        App.getAppRepository().enqueueTTSProcessingResult(goodProcessingResult);
    }

    @Override
    public void update(Context context, String assetFilename) {
        // ignore, as we don't play from files
    }

    @Override
    public synchronized void stop(TTSRequest ttsRequest) {
        Log.v(LOG_TAG, "stop()");
        mIsStopped = true;
        TTSProcessingResult stoppedProcessingResult = new TTSProcessingResult(ttsRequest);
        stoppedProcessingResult.setToStopped();
        App.getAppRepository().enqueueTTSProcessingResult(stoppedProcessingResult);
    }

    @Override
    public synchronized void error(String errorMsg, TTSRequest ttsRequestd) {
        Log.e(LOG_TAG, "TTSObserver()::error: " + errorMsg);
        TTSProcessingResult badProcessingResult = new TTSProcessingResult(ttsRequestd);
        badProcessingResult.setError();
        App.getAppRepository().enqueueTTSProcessingResult(badProcessingResult);
    }
}
