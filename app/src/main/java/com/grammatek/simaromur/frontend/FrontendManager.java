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
    private SyllableStressAnalyzer mSyllabStress;

    private Context mContext;

    public FrontendManager(Context context) {
        //TODO: move these initializations up?
        // we only want to do this once (init POS-model, init g2p, etc.)
        // and not for each sentence!
        initializeNormalizationManager(context);
        initializePronunciation(context);
        this.mContext = context;
    }

    /**
     * Processes text for input into a TTS engine. If no boolean parameters are given, we only
     * normalize the text.
     *
     * @param text raw input text
     * @return a normalized version of @text
     */
    public String process(String text) {
        return process(text, false, false);
    }

    /**
     * Processes text for input into a TTS engine. The text is normalized, and if @g2p is true,
     * it is also transcribed and an X-SAMPA transcription of the normalized text is returned.
     *
     * @param text raw input text
     * @return an X-SAMPA transcription of @text if @g2p is true, normalized text otherwise
     */
    public String process(String text, boolean g2p) {
        return process(text, g2p, false);
    }

    /**
     * Processes text for input into a TTS engine. Default processing is normalize the text,
     * if g2p and syllabStress are true, it is further transcribed to X-SAMPA and labelded with
     * syllables and stress. If g2p is false, syllabStress is redundant and will not be
     * checked (we can only label transcribed text with syllables and stress).
     *
     * @param text raw input text
     * @return a processed version of @text: plain normalized if @g2p is false, X-SAMPA transcribed
     * otherwise, with syllable and stress labeling if @syllabStess is true.
     */
    public String process(String text, boolean g2p, boolean syllabStress) {
        String resultText = text;
        resultText = mNormalizationManager.process(resultText);
        if (g2p) {
            resultText = mPronunciation.transcribe(resultText);
            // we can't perform syllabStress unless we have a transcribed (g2p) version of text
            if (syllabStress) {
                resultText = mSyllabStress.label(resultText);
            }
        }
        Log.i(LOG_TAG, text + " => " + resultText);
        return resultText;
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
