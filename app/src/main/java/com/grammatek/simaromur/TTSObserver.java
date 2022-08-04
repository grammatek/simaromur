package com.grammatek.simaromur;

import android.content.Context;
import android.media.AudioFormat;
import android.speech.tts.SynthesisCallback;
import android.util.Log;

import com.grammatek.simaromur.audio.AudioObserver;
import com.grammatek.simaromur.network.ConnectionCheck;

import static android.speech.tts.TextToSpeech.ERROR_NETWORK;
import static com.grammatek.simaromur.audio.AudioManager.N_CHANNELS;
import static com.grammatek.simaromur.audio.AudioManager.SAMPLE_RATE_WAV;
import static com.grammatek.simaromur.audio.AudioManager.applyPitchAndSpeed;

public class TTSObserver implements AudioObserver {
    private final static String LOG_TAG = "Simaromur_" + TTSObserver.class.getSimpleName();

    private final SynthesisCallback mSynthCb;
    private final float mPitch;
    private final float mSpeed;
    private int mSampleRate = SAMPLE_RATE_WAV;
    private boolean mUsesNetwork = false;
    private boolean mIsStopped = false;

    /**
     * Constructor called for device voice synthesis
     * @param synthCb       Callback to feed audio chunks with for playback
     * @param pitch         Audio pitch factor
     * @param speed         Audio speed factor
     * @param sampleRate    Sample rate used for the audio playback
     */
    public TTSObserver(SynthesisCallback synthCb, float pitch, float speed, int sampleRate) {
        mSynthCb = synthCb;
        mPitch = pitch;
        mSpeed = speed;
        mSampleRate = sampleRate;
    }

    /**
     * Constructor called for network voice synthesis.
     * @param synthCb       Callback to feed audio chunks with for playback
     * @param pitch         Audio pitch factor
     * @param speed         Audio speed factor
     */
    public TTSObserver(SynthesisCallback synthCb, float pitch, float speed) {
        mSynthCb = synthCb;
        mPitch = pitch;
        mSpeed = speed;
        mUsesNetwork = true;
    }

    // interface implementation

    /**
     * We receive the audio response from the API, convert it if necessary according to the
     * values given in mPitch and mSpeed, and then feed the resulting buffer piece by piece to
     * the callback object provided by the Android TTS API.
     *
     * @param ttsData Audio response data from Tiro API
     */
    public synchronized void update(final byte[] ttsData) {
        if (ttsData.length == 0) {
            Log.v(LOG_TAG, "TTSObserver: Nothing to speak");
            TTSService.playSilence(mSynthCb);
            return;
        }
        mIsStopped = false;
        // TODO: should only be done, if no MP3 returned by API
        final byte[] audioData = applyPitchAndSpeed(ttsData, mPitch, mSpeed);
        int offset = 0;
        startSynthesis(mSynthCb, mSampleRate, mUsesNetwork);
        final int maxBytes = mSynthCb.getMaxBufferSize();

        while (offset < audioData.length) {
            Log.v(LOG_TAG, "TTSObserver: offset = " + offset);
            final int bytesConsumed = Math.min(maxBytes, (audioData.length - offset));
            if (mSynthCb.hasStarted()) {
                mSynthCb.audioAvailable(audioData, offset, bytesConsumed);
            }
            if (mIsStopped) {
                break;
            }
            offset += bytesConsumed;
        }
        Log.v(LOG_TAG, "TTSObserver: consumed " + offset + " bytes");
        if (! mIsStopped) {
            // already done if mIsStopped == true
            mSynthCb.done();
        }
    }

    private static void startSynthesis(SynthesisCallback mSynthCb, int sampleRate, boolean usesNetwork) {
        if (usesNetwork && !ConnectionCheck.isTTSServiceReachable()) {
            Log.e(LOG_TAG, "TTSObserver error: Service is not reachable ?!");
            mSynthCb.error(ERROR_NETWORK);
            // TODO: play connection problem wav ?
            return;
        }
        if (! mSynthCb.hasStarted()) {
            mSynthCb.start(sampleRate, AudioFormat.ENCODING_PCM_16BIT, N_CHANNELS);
        }
    }

    @Override
    public void update(Context context, String assetFilename) {
        // ignore, as we don't play from files
    }

    public synchronized void stop() {
        if (mSynthCb.hasStarted() && ! mSynthCb.hasFinished()) {
            mSynthCb.done();
            mIsStopped = true;
        }
    }

    public synchronized void error(String errorMsg) {
        Log.e(LOG_TAG, "TTSObserver()::error: " + errorMsg);
        mSynthCb.error(ERROR_NETWORK);
    }
}
