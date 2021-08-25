package com.grammatek.simaromur;

import android.content.Context;
import android.media.AudioFormat;
import android.speech.tts.SynthesisCallback;
import android.util.Log;

import com.grammatek.simaromur.audio.AudioObserver;

import static android.speech.tts.TextToSpeech.ERROR_NETWORK;
import static com.grammatek.simaromur.audio.AudioManager.N_CHANNELS;
import static com.grammatek.simaromur.audio.AudioManager.SAMPLE_RATE_WAV;
import static com.grammatek.simaromur.audio.AudioManager.applyPitchAndSpeed;

public class TTSObserver implements AudioObserver {
    private final static String LOG_TAG = "Simaromur_" + TTSObserver.class.getSimpleName();

    private final SynthesisCallback mSynthCb;
    private final float mPitch;
    private final float mSpeed;

    public TTSObserver(SynthesisCallback synthCb, float pitch, float speed) {
        mSynthCb = synthCb;
        mPitch = pitch;
        mSpeed = speed;
    }

    // interface implementation

    /**
     * We receive the audio response from the API, convert it if necessary according to the
     * values given in mPitch and mSpeed, and then feed the resulting buffer piece by piece to
     * the callback object provided by the Android TTS API.
     *
     * @param ttsData Audio response data from Tiro API
     */
    public void update(final byte[] ttsData) {
        if (ttsData.length == 0) {
            Log.v(LOG_TAG, "TTSObserver: Nothing to speak");
            TTSService.playSilence(mSynthCb);
            return;
        }
        final byte[] audioData = applyPitchAndSpeed(ttsData, mPitch, mSpeed);
        int offset = 0;
        startSynthesis(mSynthCb);
        final int maxBytes = mSynthCb.getMaxBufferSize();

        while (offset < audioData.length) {
            Log.v(LOG_TAG, "TTSObserver: offset = " + offset);
            final int bytesConsumed = Math.min(maxBytes, (audioData.length - offset));
            if (mSynthCb.hasStarted()) {
                mSynthCb.audioAvailable(audioData, offset, bytesConsumed);
            }
            offset += bytesConsumed;
        }
        Log.v(LOG_TAG, "TTSObserver: consumed " + offset + " bytes");
        mSynthCb.done();
    }

    private static void startSynthesis(SynthesisCallback mSynthCb) {
        if (! mSynthCb.hasStarted()) {
            mSynthCb.start(SAMPLE_RATE_WAV, AudioFormat.ENCODING_PCM_16BIT, N_CHANNELS);
        }
    }

    @Override
    public void update(Context context, String assetFilename) {
        // ignore, as we don't play from files
    }

    public void stop() {
        if (mSynthCb.hasStarted() && ! mSynthCb.hasFinished()) {
            mSynthCb.done();
        }
    }

    public void error(String errorMsg) {
        Log.e(LOG_TAG, "TTSObserver()::error: " + errorMsg);
        mSynthCb.error(ERROR_NETWORK);
    }
}
