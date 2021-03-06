package com.grammatek.simaromur.frontend;

import android.content.Context;
import android.util.Log;

import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;


import java.io.IOException;
import java.io.InputStream;
import java.util.List;

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

    private Context mContext;

    public FrontendManager(Context context) {
        initializeNormalizationManager(context);
        initializePronunciation(context);
        this.mContext = context;
    }

    /**
     * Processes text for input into a TTS engine. This includes unicode cleaning, tokenizing, and
     * normalizing the the text, and then to convert it into an X-SAMPA transcription.
     *
     * @param text raw input text
     * @return an X-SAMPA transcription of @text
     */
    public String process(String text) {
        String normalized = mNormalizationManager.process(text);
        String transcribedText = mPronunciation.transcribe(normalized);
        Log.i(LOG_TAG, text + " => " + transcribedText);
        return transcribedText;
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
