package com.grammatek.simaromur.frontend;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.grammatek.simaromur.utils.FileUtils;
import com.grammatek.simaromur.device.NativeG2P;
import com.grammatek.simaromur.R;
import com.grammatek.simaromur.device.SymbolsLvLIs;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class manages the dictionary look-ups and the calls to g2p for unknown words
 */

public class Pronunciation {
    private final static String LOG_TAG = "Simaromur_" + Pronunciation.class.getSimpleName();
    private final Context mContext;
    private NativeG2P mG2P;
    private Map<String, PronDictEntry> mPronDict;
    private Map<String, PronDictEntry> mIpaPronDict;
    private final Map<String, Map<String, Map<String, String>>> mAlphabets;

    private final static String FLITE = "flite";
    private final String beginEndPausePattern = "^§sp|§sp$";

    // Letters that need custom transcription when spoken in isolation (like when using the
    // keyboard). This means partly different transcription of the letter than normal/correct
    // and partly leaving out pause symbols at beginning and/or end.
    // TODO: these mappings are valid for Álfur Flite v0.2, needs revision when voice is
    // updated!
    private static final Map<String, String> CUSTOM_CHAR_TRANSCRIPTS = new HashMap<>();
    static {
        CUSTOM_CHAR_TRANSCRIPTS.put("c", "s j E: ");
        CUSTOM_CHAR_TRANSCRIPTS.put("e", SymbolsLvLIs.SymbolShortPause + " E: E: " + SymbolsLvLIs.SymbolShortPause);
        CUSTOM_CHAR_TRANSCRIPTS.put("i", "I: " + SymbolsLvLIs.SymbolShortPause);
        CUSTOM_CHAR_TRANSCRIPTS.put("j", SymbolsLvLIs.SymbolShortPause + " i: O: T " + SymbolsLvLIs.SymbolShortPause);
        CUSTOM_CHAR_TRANSCRIPTS.put("o", "O: O ");
        CUSTOM_CHAR_TRANSCRIPTS.put("ó", "ou: " + SymbolsLvLIs.SymbolShortPause);
        CUSTOM_CHAR_TRANSCRIPTS.put("s", SymbolsLvLIs.SymbolShortPause + " E s s " + SymbolsLvLIs.SymbolShortPause);
        CUSTOM_CHAR_TRANSCRIPTS.put("u", "Y: " + SymbolsLvLIs.SymbolShortPause);
        CUSTOM_CHAR_TRANSCRIPTS.put("z", "s E: a t a " + SymbolsLvLIs.SymbolShortPause);
    }
    private static final Map<String, String> CUSTOM_TRANSCRIPTS = new HashMap<>();
    static {
        CUSTOM_TRANSCRIPTS.put("merki", "m E r_0 r_0 c I ");
        CUSTOM_TRANSCRIPTS.put("hægri", "h a ai G r I ");
        CUSTOM_TRANSCRIPTS.put("hæ", "h aii ai ");
        CUSTOM_TRANSCRIPTS.put("ég", "i j E: x ");
    }

    public Pronunciation(Context context) {
        mContext = context;
        mPronDict = readPronDict();
        mIpaPronDict = readIpaPronDict();
        mAlphabets = initializeAlphabets();
    }

    public String transcribe(String text) {
        return transcribe(text, "", "");
    }

    public String transcribe(String text, final String voiceType, final String voiceVersion) {
        initializeG2P();    // lazy initialize to break dependencies
        String transcript = "";
        text = text.trim();
        Log.v(LOG_TAG, "voice version => " + voiceVersion);
        // If we run into more special handling with different voice types and versions,
        // we might want to think of another approach to this.
        // For FLITE and v02 check if 'text' is contained in the custom_char_transcripts map
        // and return the respective custom transcript if true.
        if (voiceType.equals(FLITE) && voiceVersion.equals("0.2") &&
                CUSTOM_CHAR_TRANSCRIPTS.containsKey(text))
            return CUSTOM_CHAR_TRANSCRIPTS.get(text);
        else
            transcript = transcribeString(text, voiceType.equals(FLITE) &&
                    voiceVersion.equals("0.2"));

        return processPauses(transcript, voiceType);
    }

    /**
     *   Converts a transcription in one alphabet to another alphabet.
     *   Logs an error if either from or to alphabet is not available for conversion and returns
     *   the original transcribed string.
     *   If a symbol in the input string is not found in the respective from alphabet, the symbol is
     *   kept as is and not converted. A message is logged as a warning if this happens.
     *
     * @param transcribed a transcribed string
     * @param fromAlphabet the alphabet of the transcribed string
     * @param toAlphabet the alphabet to convert the transcribed string into
     * @param filterUnknown if true, unknown symbols in toAlphabet are filtered out, otherwise
     *                      they are kept as is
     * @return a converted transcription
     */
    public String convert(String transcribed, String fromAlphabet, String toAlphabet, boolean filterUnknown) {
        final List<String> validAlpha = getValidAlphabets();
        if (!validAlpha.contains(fromAlphabet) || !validAlpha.contains(toAlphabet)) {
            Log.e(LOG_TAG, fromAlphabet + " and/or " + toAlphabet + " is not a valid" +
                    " phonetic alphabet. Valid alphabets: " + validAlpha);
            return transcribed;
        }
        if (fromAlphabet.equals(toAlphabet))
            return transcribed;

        StringBuilder converted = new StringBuilder();
        final Map<String, Map<String, String>> currentDict = mAlphabets.get(fromAlphabet);
        for (String symbol : transcribed.split(" ")) {
            assert currentDict != null;
            if (!currentDict.containsKey(symbol)) {
                Log.w(LOG_TAG, "Symbol (" + symbol + ") seems not to be a valid symbol in " + fromAlphabet +
                        ". Skipping conversion.");
                if (!filterUnknown) {
                    converted.append(symbol).append(" ");
                }
            }
            else {
                String convertedSymbol = currentDict.get(symbol).get(toAlphabet);
                converted.append(convertedSymbol);
                converted.append(" ");
            }
        }
        return converted.toString().trim();
    }

    @NonNull
    private String transcribeString(String text, boolean isFlitev02) {
        String[] tokens = text.split(" ");
        StringBuilder sb = new StringBuilder();
        for (String tok : tokens) {
            String transcr = "";
            if (isFlitev02 && CUSTOM_TRANSCRIPTS.containsKey(tok))
                transcr = CUSTOM_TRANSCRIPTS.get(tok);
            else if (mPronDict.containsKey(tok)) {
                transcr = mPronDict.get(tok).getTranscript().trim();
            }
            else if (tok.equals(SymbolsLvLIs.TagPause)){
                transcr = SymbolsLvLIs.SymbolShortPause;
            }
            else {
                transcr = mG2P.process(tok).trim();
            }
            // for tokens like 'myllumerki', 'dollarmerki', 'spurningarmerki', etc.
            // correct transcription does not sound right
            if (isFlitev02 && transcr.matches(".*m E r_0 c I"))
                transcr = transcr.replaceAll("m E r_0 c I", "m E r_0 r_0 c I");
            // Very strange flaw for words ending with "sins", like "leiksins", "tímaritsins", etc.
            // a very bright "s I n EE s" instead of "s I n s". Fix that with this hack
            if (isFlitev02 && transcr.matches(".+s I n s"))
                transcr = transcr.replaceAll("s I n s", "s I n n s");

            // bug in Thrax grammar, catch the error here: insert space before C if missing
            // like in 'Vilhjálmsdóttur' -> 'v I lC au l m s t ou h t Y r'
            // TODO: remove when Thrax grammar is fixed!
            transcr = transcr.replaceAll("([a-zA-Z])C", "$1 C");
            sb.append(transcr).append(" ");
        }
        return sb.toString().trim();
    }

    // only Flite voices need pause symbols at the beginning and end of a transcript
    private String processPauses(String transcript, String voiceType) {
        if (voiceType.equals(FLITE))
            transcript = ensurePauses(transcript);
        else
            transcript = transcript.replaceAll(beginEndPausePattern, "");

        return finalReplacements(transcript);
    }

    // ensure that each transcript starts and ends with a pause symbol
    private String ensurePauses(String transcript) {
        if (!transcript.startsWith(SymbolsLvLIs.SymbolShortPause))
            transcript = SymbolsLvLIs.SymbolShortPause + " " + transcript;
        if (!transcript.endsWith(SymbolsLvLIs.SymbolShortPause))
            transcript += " " + SymbolsLvLIs.SymbolShortPause;
        return transcript;
    }

    private String finalReplacements(String transcript) {
        final String sp = " " + SymbolsLvLIs.SymbolShortPause + " ";
        final String multiPausePattern = "(§sp ?){2,}";
        // replace special characters
        transcript = transcript.replaceAll("\\.", sp);
        transcript = transcript.replaceAll(",", sp);
        transcript = transcript.replaceAll("\\s{2,}", " ");
        transcript = transcript.replaceAll(multiPausePattern, "§sp ").trim();
        return transcript;
    }

    private List<String> getValidAlphabets() {
        return List.copyOf(mAlphabets.keySet());
    }

    public void initializeG2P() {
        if (mG2P == null) {
            mG2P = new NativeG2P(this.mContext);
        }
    }


    /**
     * Initialize a symbol dictionary where we can look up mappings between all alphabets in ALPHABETS_FILE.
     * The result is a dictionary of dictionaries, where the top-level keys are the names of the alphabets
     * and the sub-dictionaries the mappings from a top-level dictionary to all other dictionaries.
     *
     * The headers (first line) of the file represent the names of the alphabets.
     *
     * Example:
     *   {'SAMPA' : {'a' : {'IPA' : 'a', 'SINGLE' : 'a', 'FLITE' : 'a'},
     *              {'a:' : {'IPA' : 'aː', 'SINGLE' : 'A', 'FLITE' : 'aa'},
     *              { ... }}
     *    'IPA'   : { ... },
     *   }
     * @return a map with phonetic symbols of different alphabets
     */
    private Map<String, Map<String, Map<String, String>>> initializeAlphabets() {
        Map<String, Map<String, Map<String, String>>> alphabets = new HashMap<>();
        final List<String> fileContent = FileUtils.readLinesFromResourceFile(this.mContext,
                R.raw.sampa_ipa_single_flite);
        final List<String> headers = Arrays.asList(fileContent.get(0).split("\t"));
        for (String header : headers) {
            int ind = headers.indexOf(header);
            alphabets.put(header, new HashMap<>());
            for (int i = 1; i < fileContent.size(); i++) {
                List<String> symbols = Arrays.asList(fileContent.get(i).split("\t"));
                String keySymbol = symbols.get(ind);
                Map<String, String> symbolDict = new HashMap<>();
                for (String h : headers) {
                    if (headers.indexOf(h) == ind)
                        continue;
                    else
                        symbolDict.put(h, symbols.get(headers.indexOf(h)));
                }
                alphabets.get(header).put(keySymbol, symbolDict);
            }
        }
        return alphabets;
    }

    private Map<String, PronDictEntry> readPronDict() {
        Map<String, PronDictEntry> pronDict = new HashMap<>();
        final List<String> fileContent = FileUtils.readLinesFromResourceFile(this.mContext,
                R.raw.ice_pron_dict_standard_clear_2201_extended);
       for (String line : fileContent) {
           String[] transcr = line.trim().split("\t");
           if (transcr.length == 2) {
               pronDict.put(transcr[0], new PronDictEntry(transcr[0], transcr[1]));
           }
       }
        return pronDict;
    }

    private Map<String, PronDictEntry> readIpaPronDict() {
        Map<String, PronDictEntry> pronDict = new HashMap<>();
        final List<String> fileContent = FileUtils.readLinesFromResourceFile(this.mContext,
                R.raw.igc_wiki_news_dict_20240107);
        for (String line : fileContent) {
            String[] transcr = line.trim().split("\t");
            if (transcr.length == 2) {
                pronDict.put(transcr[0], new PronDictEntry(transcr[0], transcr[1]));
            }
        }
        return pronDict;
    }

    public NativeG2P GetG2p() {
        return mG2P;
    }

    public Map<String, PronDictEntry> GetPronDict() {
        return mPronDict;
    }
    public Map<String, PronDictEntry> GetIpaPronDict() {
        return mIpaPronDict;
    }
    public Map<String, Map<String, Map<String, String>>> GetAlphabets() {
        return mAlphabets;
    }
}
