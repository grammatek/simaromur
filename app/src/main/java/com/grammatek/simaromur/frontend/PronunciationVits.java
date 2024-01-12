package com.grammatek.simaromur.frontend;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.grammatek.simaromur.R;
import com.grammatek.simaromur.device.NativeG2P;
import com.grammatek.simaromur.device.SymbolsLvLIs;
import com.grammatek.simaromur.utils.FileUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class manages the dictionary look-ups and the calls to g2p for unknown words
 */

public class PronunciationVits {
    private final static String LOG_TAG = "Simaromur_" + PronunciationVits.class.getSimpleName();
    private static final String PAD_SYMBOL = "_";
    private static final String BOS_SYMBOL = "^";
    private static final String EOS_SYMBOL = "$";
    private final Pronunciation mPronounciation;
    private NativeG2P mG2P;
    private final Map<String, PronDictEntry> mPronDict;
    private final Set<String> Punctuations = new HashSet<>(Arrays.asList(
            "_", "-", "!", "'", "(", ")", ",", ".", ":", ";", "?", "<", ">", "[", "]", "\"", "#"));

    // This is the place to add custom transcriptions for words that are not in the
    // dictionary and are not handled by g2p. This is e.g. if we see that certain words are
    // not handled correctly by g2p, we can add them here.
    private static final Map<String, String> CUSTOM_TRANSCRIPTS = new HashMap<>();
    static {
    }


    public PronunciationVits(Pronunciation pronunciation) {
        // here we just save references to the objects, these need to be initialized before
        this.mPronounciation = pronunciation;
        this.mPronDict = pronunciation.GetIpaPronDict();
    }

    public String transcribe(String text) {
        return transcribe(text, "", "");
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
    public String transcribe(String text, final String voiceType, final String voiceVersion) {
        // lazy initialization of g2p
        if (mPronounciation.GetG2p() == null)
            mPronounciation.initializeG2P();
        mG2P = mPronounciation.GetG2p();
        assert(mG2P != null);

        return doTranscribe(text.trim());
    }

    /**
     * Transcribe text to IPA. Punctuation is kept as is, which conforms to the IPA dialect
     * encoded into the VITS model.
     * <p>
     * First a small post-normalization cleanup is done, then the text is split into tokens.
     * Each token is then looked up in the dictionary, if not found, g2p is called. The result
     * is then converted to IPA and returned. All symbols within a token are separated by PAD_SYMBOL.
     * Each token is separated by a space.
     *
     * @param text  The text to be transcribed to IPA phonemes
     *
     * @return The transcription as IPA phonemes separated by spaces or empty string if no
     *        relevant phonemes were found.
     */
    @NonNull
    private String doTranscribe(String text) {
        Log.d(LOG_TAG, "****** INITIAL ******  (" + text + ")");
        if (text.isEmpty() || text.equals(".")) {
            Log.d(LOG_TAG, "No relevant phonemes found.");
            return "";
        }

        // Cleanup before phonemization: multiple commas are unified and only one space between
        // each token is kept. Also a missing dot at the end is added.
        text = text.replaceAll(",\\s*,", ",");
        // replace some common <sil> variants
        text = text.replaceAll("<sil>\\s*,", ",");
        text = text.replaceAll("<sil>", "#");
        // replace all spaces in between with a single space
        text = text.replaceAll("\\s+", " ").trim();

        // remove beginning comma with surrounding spaces (XXX DS: seems to be replaced from
        // normalizer for beginning double quotes). This causes an unnatural pause/noise in the
        // output
        text = text.replaceAll("^\\s*,\\s*", "");

        // replace all "-" with "#", this is equivalent with <sil> in the VITS model
        text = text.replaceAll("-", "#");

        // Split text into tokens, cleanup, normalization and post-normalization should have
        // transformed the sentence into a format that is easy to split
        String[] tokens = text.split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (String tok : tokens) {
            String transcr = "";
            String ipaSymbols = "";
            if (CUSTOM_TRANSCRIPTS.containsKey(tok))
                ipaSymbols = CUSTOM_TRANSCRIPTS.get(tok);
            else if (mPronDict.containsKey(tok)) {
                ipaSymbols = mPronDict.get(tok).getTranscript().trim();
                //Log.d(LOG_TAG, tok + " => " + ipaSymbols);
            } else if (Punctuations.contains(tok)) {
                ipaSymbols = tok;
            }
            else if (tok.equals(SymbolsLvLIs.TagPause)) {
                transcr = SymbolsLvLIs.SymbolShortPause;
                ipaSymbols = transcr;
            }
            else if (tok.equals(SymbolsLvLIs.PrimaryStress)) {
                ipaSymbols = SymbolsLvLIs.PrimaryStress;
            }
            else if (tok.equals(SymbolsLvLIs.SecondaryStress)) {
                ipaSymbols = SymbolsLvLIs.SecondaryStress;
            }
            else {
                transcr = mG2P.process(tok).trim();

                // bug in Thrax grammar, catch the error here: insert space before C if missing
                // like in 'Vilhjálmsdóttur' -> 'v I lC au l m s t ou h t Y r'
                // TODO: remove when Thrax grammar is fixed!
                transcr = transcr.replaceAll("([a-zA-Z])C", "$1 C");
                // transcribe to IPA
                ipaSymbols = mPronounciation.convert(transcr, "SAMPA", "IPA", false).trim();
                // remove all eventual spaces in between symbol (why ?)
                //ipaSymbols = ipaSymbols.replaceAll("\\s+", "");
                Log.d(LOG_TAG, "****** THRAX ******  (" + transcr + ": " + ipaSymbols + ")");
            }

            // To distinguish between words and symbols, we add a space after each word, but use
            // PAD_SYMBOL to separate the symbols within a word

            // replace all spaces in between with PAD_SYMBOL
            ipaSymbols = ipaSymbols.replaceAll("\\s+", PAD_SYMBOL);

            // after each symbol, we add a space, because this is the normal word/symbol boundary,
            // but not for punctuations like commas, dots, question marks, colons, semicolons, exclamation marks
            //if (! ipaSymbols.matches(",\\.\\?!:;"))
                sb.append(ipaSymbols).append(" ");
        }
        String rv = sb.toString().trim();
        Log.d(LOG_TAG, "****** FINAL after g2p ******  (" + rv + ")");
        //String transcript = sb.toString().replaceAll(beginEndPausePattern, "").replaceAll("§sp", ",").trim();
        return rv;
    }

}
