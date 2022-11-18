package com.grammatek.simaromur.frontend;

import android.content.Context;
import android.util.Log;

import com.grammatek.simaromur.device.SymbolsLvLIs;

/**
 * The FrontendManager processes raw input text and prepares it for the TTSEngine.
 * The FrontendManager keeps track of all information between text processing steps if needed,
 * e.g. word indices, and controls what processing steps are performed via parameters.
 * The MVP does only have one pipeline to process text, from raw text to X-SAMPA, but any deviations
 * and special cases should be controlled from here.
 */

public class FrontendManager {
    private final static String LOG_TAG = "Flite_Java_" + FrontendManager.class.getSimpleName();

    private NormalizationManager mNormalizationManager;
    private Pronunciation mPronunciation;

    public FrontendManager(Context context) {
        initializeNormalizationManager(context);
        initializePronunciation(context);
    }

    /**
     * Returns FrontendManager version. This Version is entered into the UtteranceCache and should
     * always change in case there is the possibility for a change either in normalization and/or
     * G2P for any given text.
     *
     * @return  String for the frontend version.
     */
    public static String getVersion() {
        return "1.0";
    }

    /**
     * Processes text for input into a TTS engine. This includes unicode cleaning, tokenizing, and
     * normalizing the the text, and then to convert it into an X-SAMPA transcription.
     *
     * @param text raw input text
     * @return an X-SAMPA transcription of @text
     */
    public String process(String text) {
        final String normalized = mNormalizationManager.process(text);
        return transcribe(normalized, "", "");
    }

    public String transcribe(String text, String voiceType, String voiceVersion) {
        Log.v(LOG_TAG, "transcribe() called");
        final String sp = " " + SymbolsLvLIs.SymbolShortPause + " ";
        final String multiPausePattern = "(§sp ?){2,}";
        final String beginEndPausePattern = "^§sp|§sp$";

        String transcribedText = mPronunciation.transcribe(text, voiceType, voiceVersion);

        // replace special characters
        transcribedText = transcribedText.replaceAll("\\.", sp);
        transcribedText = transcribedText.replaceAll(",", sp);
        transcribedText = transcribedText.replaceAll("\\s{2,}", " ");
        transcribedText = transcribedText.replaceAll(multiPausePattern, "§sp ").trim();

        Log.i(LOG_TAG, text + " => (" + transcribedText + ")");
        return transcribedText;
    }

    public NormalizationManager getNormalizationManager() {
        return mNormalizationManager;
    }

    private void initializePronunciation(Context context) {
        if (mPronunciation == null) {
            mPronunciation = new Pronunciation(context);
        }
    }

    private void initializeNormalizationManager(Context context) {
        if (mNormalizationManager == null) {
            mNormalizationManager = new NormalizationManager(context);
        }
    }
}
