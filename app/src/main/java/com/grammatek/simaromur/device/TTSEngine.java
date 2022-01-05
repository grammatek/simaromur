package com.grammatek.simaromur.device;

/**
 *
 */
public interface TTSEngine {
    // TODO DS: pitch, speed, audio specifications (Sample Rate, Sample size, etc.)
    byte[] SpeakToWav(String sampas, float speed, float pitch, int sampleRate);
}
