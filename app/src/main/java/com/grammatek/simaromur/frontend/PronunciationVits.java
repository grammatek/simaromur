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
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;
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
    private final IPAToSAMPAConverter ipaToEspeakConverter;
    private NativeG2P mG2P;
    private Map<String, PronDictEntry> mPronDict;
    private final String beginEndPausePattern = "^§sp|§sp$";

    private static final Map<String, String> CUSTOM_TRANSCRIPTS = new HashMap<>();
    static {
    }


    public class IPAToSAMPAConverter {
        private final Map<Pattern, String> replacementRules = new LinkedHashMap<>();
        IPAToSAMPAConverter() {
            // Define replacement patterns
            // IPA to eSpeak replacement patterns
            //replacementRules.put("a", "a");
            //replacementRules.put("aiː", "aɪ:");
            replacementRules.put(Pattern.compile("ai"), "aɪ");
            //replacementRules.put("auː", "aʊː");
            replacementRules.put(Pattern.compile("au"), "aʊ");

            //replacementRules.put("aː", "aː");
            replacementRules.put(Pattern.compile("c(?!ʰ)"), "ɟ");
            replacementRules.put(Pattern.compile("cʰ"), "c");
            //replacementRules.put("eiː", "eɪː");
            replacementRules.put(Pattern.compile("ei"), "eɪ");

            //replacementRules.put("f", "f");
            //replacementRules.put("h", "h");
            //replacementRules.put("iː", "iː");
            //replacementRules.put("i", "i");
            //replacementRules.put("j", "j");
            replacementRules.put(Pattern.compile("k(?!ʰ)"), "ɡ");
            replacementRules.put(Pattern.compile("kʰ"), "k");
            //replacementRules.put("l", "l");
            // here we have a strange case: the voice doesn't know the symbol '#', eSpeak
            // transcribes hallo to hˈadlɔ
            //replacementRules.put(Pattern.compile("l̥"), "l#");
            replacementRules.put(Pattern.compile("l̥"), "dl");
            //replacementRules.put("m", "m");
            replacementRules.put(Pattern.compile("m̥"), "m#");
            //replacementRules.put("n", "n");
            replacementRules.put(Pattern.compile("n̥"), "n#");
            //replacementRules.put("ouː", "oʊː");
            replacementRules.put(Pattern.compile("ou"), "oʊ");

            replacementRules.put(Pattern.compile("p(?!ʰ)"), "b");
            replacementRules.put(Pattern.compile("pʰ"), "p");
            //replacementRules.put("r", "r");
            replacementRules.put(Pattern.compile("r̥"), "rr#");
            //replacementRules.put("s", "s");
            replacementRules.put(Pattern.compile("t(?!ʰ)"), "d");
            replacementRules.put(Pattern.compile("tʰ"), "t");
            //replacementRules.put("u", "u");
            //replacementRules.put("uː", "uː");
            replacementRules.put(Pattern.compile("v"), "ʋ");
            //replacementRules.put("x", "x");
            //replacementRules.put(Pattern.compile("ç"), "ç");
            //replacementRules.put("ð", "ð");
            //replacementRules.put("ŋ", "ŋ");
            replacementRules.put(Pattern.compile("ŋ̊"), "ŋ #");
            //replacementRules.put("œ", "œ");
            replacementRules.put(Pattern.compile("œy"), "øy");
            //replacementRules.put(Pattern.compile("œyː"), "øyː");
            //replacementRules.put(Pattern.compile("œː"), "œː");
            //replacementRules.put("ɔ", "ɔ");
            //replacementRules.put("ɔi", "ɔi");
            //replacementRules.put("ɔː", "ɔː");
            //replacementRules.put("ɛ", "ɛ");
            //replacementRules.put("ɛː", "ɛː");
            //replacementRules.put("ɣ", "ɣ");
            //replacementRules.put("ɪ", "ɪ");
            ///replacementRules.put("ɪː", "ɪː");
            //replacementRules.put("ɲ", "ɲ");
            //replacementRules.put("ɲ̊", "ɲ̊");
            replacementRules.put(Pattern.compile("ʏ"), "y");
            //replacementRules.put("ʏi", "yi");
            //replacementRules.put("ʏː", "yː");
            //replacementRules.put(Pattern.compile("θ"), "θ");

            // Handle stresses
            //replacementRules.put("ˈ", "'");
            //replacementRules.put("ˌ", ",");
            // ... other diacritics if necessary ...
        }


        public String convertIPAToESPEAK(String ipaString) {
            String espeakString = ipaString;

            for (Map.Entry<Pattern, String> entry : replacementRules.entrySet()) {
                Matcher matcher = entry.getKey().matcher(espeakString);
                espeakString = matcher.replaceAll(entry.getValue());
            }

            return espeakString;
        }
    }

    public PronunciationVits(Pronunciation pronunciation) {
        // here we just save references to the objects, these need to be initialized before
        this.mPronounciation = pronunciation;
        this.mPronDict = pronunciation.GetIpaPronDict();
        this.ipaToEspeakConverter = new IPAToSAMPAConverter();
    }

    public String transcribe(String text) {
        return transcribe(text, "", "");
    }

    public String transcribe(String text, final String voiceType, final String voiceVersion) {
        if (mPronounciation.GetG2p() == null)
            mPronounciation.initializeG2P();
        mG2P = mPronounciation.GetG2p();
        assert(mG2P != null);

        return doTranscribe(text.trim());
    }

    @NonNull
    private String doTranscribe(String text) {
        // if there are multiple commas with space in between, replace them with a single comma
        text = text.replaceAll(",\\s+,", ",");
        text = text.replaceAll("<sil>", "");
        String[] tokens = text.split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (String tok : tokens) {
            String transcr = "";
            String ipaSymbols = "";
            if (CUSTOM_TRANSCRIPTS.containsKey(tok))
                transcr = CUSTOM_TRANSCRIPTS.get(tok);
            else if (mPronDict.containsKey(tok)) {
                transcr = mPronDict.get(tok).getTranscript().trim();
                ipaSymbols = ipaToEspeakConverter.convertIPAToESPEAK(transcr);
                Log.i(LOG_TAG, tok + " => " + ipaSymbols);
            }
            else if (tok.equals(SymbolsLvLIs.TagPause)){
                transcr = SymbolsLvLIs.SymbolShortPause;
                ipaSymbols = mPronounciation.convert(transcr, "IPA", "ESPEAK", true).trim();
            }
            else {
                transcr = mG2P.process(tok).trim();

                // bug in Thrax grammar, catch the error here: insert space before C if missing
                // like in 'Vilhjálmsdóttur' -> 'v I lC au l m s t ou h t Y r'
                // TODO: remove when Thrax grammar is fixed!
                transcr = transcr.replaceAll("([a-zA-Z])C", "$1 C");
                // transcribe to IPA
                ipaSymbols = mPronounciation.convert(transcr, "SAMPA", "ESPEAK", true).trim();
                // remove all eventual spaces in between symbol
                ipaSymbols = ipaSymbols.replaceAll("\\s+", "");
                Log.i(LOG_TAG, "****** THRAX ******  " + ipaSymbols);
            }

            sb.append(ipaSymbols).append(" ");
        }
        String transcript = sb.toString().replaceAll(beginEndPausePattern, "").replaceAll("§sp", ",").trim();
        // check if last element ends with "." and if not, append it
        if (!transcript.endsWith("."))
            transcript += ".";

        return transcript;
    }

}
