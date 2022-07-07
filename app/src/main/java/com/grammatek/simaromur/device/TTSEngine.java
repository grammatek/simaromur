package com.grammatek.simaromur.device;

/**
 * Base class of an on-device voice TTS Engine. This base class is currently only responsible
 * for on-device voices.
 *
 * TODO: generalize also for network voices
 */
public interface TTSEngine {
    /**
     * Returns PCM encoded audio for given text encoded with sampa phonemes. The sample rate
     * is specific to the voice model and can be retrieved via {@link #GetNativeSampleRate()}.
     *
     * @param sampas        Input text encoded as sampa phonemes
     * @return  PCM encoded audio representation
     */
    byte[] SpeakToPCM(String sampas);

    /**
     * Returns native sample rate of the voice.
     *
     * @return  Native sample rate of voice.
     */
    int GetNativeSampleRate();

    /**
     * Stops any ongoing synthesis
     */
    void Stop();
}
