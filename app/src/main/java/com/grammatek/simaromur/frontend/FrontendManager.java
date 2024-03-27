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
    private final static String LOG_TAG = "Simaromur_" + FrontendManager.class.getSimpleName();

    private final static String IGNORE_TYPE = "ignoreType";
    private final static String IGNORE_VERSION = "ignoreVersion";

    private NormalizationManager mNormalizationManager = null;
    private Pronunciation mPronunciation = null;
    private PronunciationVits mPronunciationVits = null;

    public FrontendManager(Context context) {
        mPronunciation = new Pronunciation(context);
        mPronunciationVits = new PronunciationVits(mPronunciation);
        mNormalizationManager = new NormalizationManager(context, mPronunciation.GetIpaPronDict());
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
     * Transcribe text to IPA symbols. Punctuation is kept as is, which conforms to the kind of
     * IPA dialect encoded into the VITS model.
     *
     * @param text          The text to be transcribed to IPA phonemes
     * @param voiceType     The voice type, e.g. "onnx"
     * @param voiceVersion  The voice version, e.g. "1.0"
     *
     * @return  The transcription as IPA phonemes separated by spaces or empty string if no
     *          relevant phonemes were found.
     */
    public String transcribe(String text, String voiceType, String voiceVersion) {
        Log.v(LOG_TAG, "transcribe() called");

        String transcribedText = "";
        if (voiceType.equals("onnx")) {
            transcribedText =  mPronunciationVits.transcribe(text, voiceType, voiceVersion);
        } else {
            transcribedText = mPronunciation.transcribe(text, voiceType, voiceVersion);
        }

        Log.i(LOG_TAG, text + " => (" + transcribedText + ")");
        return transcribedText;
    }

    public NormalizationManager getNormalizationManager() {
        return mNormalizationManager;
    }
}
