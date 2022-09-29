package com.grammatek.simaromur.device.flite;

import android.util.Log;

import java.nio.ByteBuffer;

public class NativeFliteVoice {
    private final static String LOG_TAG = "Simaromur_Java_" + NativeFliteVoice.class.getSimpleName();
    private final static int AUDIO_BUFFER_SIZE = 5*1024*1024; // 5 MB

    // load the native flite loader library
    static {
        System.loadLibrary("fliteservice");
        nativeClassInit();
    }

    private final String mVoicePath;
    private ByteBuffer mVoiceData;

    /**
     * Constructor for the native flite voice. It loads the voice file from
     * given path and initializes the voice.
     *
     * This path needs to be placed inside internal storage, otherwise the
     * voice cannot be loaded.
     *
     * @param voicePath path to the voice file that is to be loaded
     */
    public NativeFliteVoice(String voicePath) {
        mVoicePath = voicePath;
        if (!attemptInit()) {
            throw new RuntimeException("Failed to load voice file " + voicePath);
        }
    }

    @Override
    protected void finalize() {
        nativeDestroy();
    }

    /**
     * Attempt to initialize the native flite engine
     */
    private boolean attemptInit() {
        boolean success = false;
        if (nativeCreate(mVoicePath)) {
            mVoiceData = ByteBuffer.allocateDirect(AUDIO_BUFFER_SIZE);
            Log.i(LOG_TAG, "Initialized Flite");
            success = true;
        } else {
            Log.e(LOG_TAG, "Failed to initialize flite library");
        }
        return success;
    }

    /**
     * Synthesize a phoneme string to PCM data
     *
     * @param phonemes phoneme string to synthesize
     *
     * @return PCM data
     */
    public byte[] synthesize(String phonemes) {
        Log.v(LOG_TAG, "Synthesizing ...");
        // we pass the pcm audio buffer to the native code, which will fill it
        // with the synthesized audio data. This way we are controlling the
        // memory allocation
        mVoiceData.clear();
        mVoiceData.put(new byte[AUDIO_BUFFER_SIZE]);
        mVoiceData.clear();
        long bytesInBuffer = nativeSynthesize(phonemes, mVoiceData);
        if (bytesInBuffer <= 0) {
            Log.e(LOG_TAG, "Failed to synthesize phonemes " + phonemes);
            return null;
        }
        float duration = getDuration();
        Log.v(LOG_TAG, "Synthesized " + bytesInBuffer + " bytes of audio data for " + duration + " seconds");
        // allocate a new array with the correct size and copy the data
        byte [] audioData = new byte[(int)bytesInBuffer];
        mVoiceData.get(audioData, 0, (int) bytesInBuffer);
        return audioData;
    }

    /**
     * Returns native sample rate of the voice.
     * @return  Native sample rate of voice.
     */
    public int getSampleRate() {
        return nativeGetSampleRate();
    }

    /**
     * Return bits per sample of the voice.
     * @return  Bits per sample of voice.
     */
    public int getBitsPerSample() {
        return nativeGetBitsPerSample();
    }

    /**
     * Return version of the voice.
     * @return  Version of voice.
     */
    public String getVersion() {
        return nativeGetVersion();
    }

    /**
     * Returns description of the voice.
     * @return  Description of voice.
     */
    public String getDescription() {
        return nativeGetDescription();
    }

    /**
     * Returns duration of the last synthesized phoneme string.
     * @return  Duration of last synthesized phoneme string.
     */
    public float getDuration() {
        return nativeGetLastDuration();
    }

    // JNI methods

    private long mNativeData;   // keep this here!
    private static native boolean nativeClassInit();
    private native boolean nativeCreate(String path);
    private native boolean nativeDestroy();
    private native long nativeSynthesize(String phonemes, ByteBuffer mVoiceData);
    private native int nativeGetSampleRate();
    private native int nativeGetBitsPerSample();
    private native String nativeGetVersion();
    private native String nativeGetDescription();
    private native float nativeGetLastDuration();
}
