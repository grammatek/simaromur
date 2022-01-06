package com.grammatek.simaromur.device;

/**
 *
 */
public interface TTSEngine {
    byte[] SpeakToPCM(String sampas, int sampleRate);
    int GetSampleRate();
}
