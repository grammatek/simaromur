package com.grammatek.simaromur.audio;

import android.content.Context;

import com.grammatek.simaromur.TTSRequest;

import java.util.UUID;

/**
 * Observer interface for async. speech audio playback
 */
public interface AudioObserver {
    // If no uuid can be provided, use this constant. This is not a magic constant, but a unique value
    // for the lifetime of the app, so that we can mark speech requests that are not originating
    // from the TTS service, but from a dummy TTS request, e.g. when an error occurs that cannot be
    // tracked to the appropriate cache item.
    String DUMMY_CACHEITEM_UUID = UUID.randomUUID().toString();

    /**
     * Play audio wav file. This is called e.g. when new TTS audio data arrives from network API.
     *
     * @param audioData     Audio data buffer - data format is dependent on requested
     *                      OutputFormat
     * @param ttsRequest    TTS request object
     */
    void update(byte[] audioData, TTSRequest ttsRequest);

    /**
     * Play Audio file from assets.
     *
     * @param context           Context for the MediaPlayer to be associated with
     * @param assetFilename     Asset filename to play. This can have any format the MediaPlayer
     *                          supports.
     */
    void update(Context context, String assetFilename);

    /**
     * Called when an error occurs. Currently only the error message is passed on.
     *
     * @param errorMsg  Error message
     * @param ttsRequest TTS request object
     */
    void error(String errorMsg, TTSRequest ttsRequest);

    /**
     * Stop actions for Observer.
     *
     * @param ttsRequest    TTSRequest that is currently played
     */
    void stop(TTSRequest ttsRequest);

    /**
     * returns the pitch value for the audio playback
     * @return pitch value
     */
    float getPitch();

    /**
     * returns the speed value for the audio playback
     * @return speed value
     */
    float getSpeed();
}
