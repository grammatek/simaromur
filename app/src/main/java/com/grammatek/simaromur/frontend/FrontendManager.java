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
    private NativeG2P mG2P;
    private TTSUnicodeNormalizer mNormalizer;
    private Tokenizer mTokenizer;

    public FrontendManager(Context context) {
        mNormalizer = new TTSUnicodeNormalizer();
        initializeTokenizer(context);
        initializeG2P(context);
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
        String tokenized = getSentencesAsString(sentences);
        Log.i(LOG_TAG, text + " => " + tokenized);
        //normalize
        //TODO
        //transcribe: a) dictionary look-up b) g2p
        // TODO: dictionary lookup, only send unknown words to g2p
        String g2pText = mG2P.process(tokenized);
        Log.i(LOG_TAG, text + " => " + g2pText);

        processed = g2pText;
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

    private void initializeG2P(Context context) {
        if (mG2P != null) {
            // @todo: mG2P.stop();
            mG2P = null;
        }
        mG2P = new NativeG2P(context);
    }

}
