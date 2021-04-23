package com.grammatek.simaromur.frontend;

import android.content.Context;
import android.util.Log;

import com.grammatek.simaromur.NativeG2P;

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

    private TTSUnicodeNormalizer mNormalizer;
    private Tokenizer mTokenizer;
    private Pronunciation mPronunciation;

    public FrontendManager(Context context) {
        mNormalizer = new TTSUnicodeNormalizer();
        initializeTokenizer(context);
        initializePronunciation(context);
    }

    /**
     * Processes text for input into a TTS engine. This includes unicode cleaning, tokenizing, and
     * normalizing the the text, and then to convert it into an X-SAMPA transcription.
     *
     * @param text raw input text
     * @return an X-SAMPA transcription of @text
     */
    public String process(String text) {
        String processed = text;
        String cleaned = mNormalizer.normalize_encoding(processed);
        Log.i(LOG_TAG, text + " => " + cleaned);
        //tokenize
        List<String> sentences = mTokenizer.detectSentences(cleaned);
        // do we need some size restrictions here? don't want to read the bible in one go ...
        String tokenized = getSentencesAsString(sentences);
        Log.i(LOG_TAG, text + " => " + tokenized);
        //normalize
        //TODO

        String transcribedText = mPronunciation.transcribe(tokenized);
        Log.i(LOG_TAG, text + " => " + transcribedText);

        processed = transcribedText;
        return processed;
    }

    // Joins the sentences into one string, separated by a white space. No further processing.
    private String getSentencesAsString(List<String> sentences) {
        StringBuilder sb = new StringBuilder();
        for (String s : sentences) {
            sb.append(s);
            sb.append(" ");
        }
        return sb.toString();
    }

    private void initializeTokenizer(Context context) {
        if (mTokenizer == null) {
            mTokenizer = new Tokenizer(context);
        }
    }

    private void initializePronunciation(Context context) {
        if (mPronunciation == null) {
            mPronunciation = new Pronunciation(context);
        }
    }

}
