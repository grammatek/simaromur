package com.grammatek.simaromur.audio;

/**
 * Observer interface for async. speech audio playback
 */
public interface AudioObserver {
    /**
     * Called when new data arrives from API.
     *
     * @param audioData     Audio data buffer - data format is dependent on requested
     *                      OutputFormat
     */
    void update(byte[] audioData);

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
