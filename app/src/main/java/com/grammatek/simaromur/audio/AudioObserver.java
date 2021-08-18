package com.grammatek.simaromur.audio;

import android.content.Context;

/**
 * Observer interface for async. speech audio playback
 */
public interface AudioObserver {
    /**
     * Play audio wav file. This is called e.g. when new TTS audio data arrives from network API.
     *
     * @param audioData     Audio data buffer - data format is dependent on requested
     *                      OutputFormat
     */
    void update(byte[] audioData);

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
     */
    void error(String errorMsg);

    /**
     * Stop actions for Observer.
     */
    void stop();
}
