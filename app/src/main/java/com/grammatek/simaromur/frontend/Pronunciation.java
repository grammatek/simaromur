package com.grammatek.simaromur.frontend;

import android.content.Context;
import android.content.res.Resources;
import android.util.Log;

import com.grammatek.simaromur.FileUtils;
import com.grammatek.simaromur.device.NativeG2P;
import com.grammatek.simaromur.R;
import com.grammatek.simaromur.device.SymbolsLvLIs;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
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
    private final Map<String, Map<String, Map<String, String>>> mAlphabets;
    private final String mSilToken = "<sil>";

    public Pronunciation(Context context) {
        this.mContext = context;
        initializePronDict();
        mAlphabets = initializeAlphabets();
    }

    public String transcribe(String text) {
        initializeG2P();    // lazy initialize to break dependencies
        String[] tokens = text.split(" ");
        StringBuilder sb = new StringBuilder();
        for (String tok : tokens) {
            if (mPronDict.containsKey(tok)) {
                sb.append(mPronDict.get(tok).getTranscript()).append(" ");
            }
            else if (tok.equals(mSilToken)){
                sb.append(SymbolsLvLIs.SymbolShortPause).append(" ");
            }
            else {
                sb.append(mG2P.process(tok)).append(" ");
            }
        }
        return sb.toString().trim();
    }

    public String convert(String transcribed, String fromAlphabet, String toAlphabet) {
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
                Log.w(LOG_TAG, symbol + " seems not to be a valid symbol in " + fromAlphabet +
                        ". Skipping conversion.");
                converted.append(symbol);
                converted.append(" ");
            }
            else {
                String convertedSymbol = currentDict.get(symbol).get(toAlphabet);
                converted.append(convertedSymbol);
                converted.append(" ");
            }
        }
        return converted.toString().trim();
    }

    private List<String> getValidAlphabets() {
        return List.copyOf(mAlphabets.keySet());
    }

    private void initializeG2P() {
        if (mG2P == null) {
            mG2P = new NativeG2P(this.mContext);
        }
    }

    private void initializePronDict() {
        if (mPronDict == null) {
            mPronDict = readPronDict();
        }
    }

    private Map<String, Map<String, Map<String, String>>> initializeAlphabets() {
        Map<String, Map<String, Map<String, String>>> alphabets = new HashMap<>();
        List<String> fileContent = readAlphabets();
        List<String> headers = Arrays.asList(fileContent.get(0).split("\t"));
        for (String header : headers) {
            int ind = headers.indexOf(header);
            alphabets.put(header, new HashMap());
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
        Resources res = this.mContext.getResources();
        int resID = R.raw.ice_pron_dict_standard_clear_2201_extended;
        String line = "";
        try {
            InputStream is = res.openRawResource(resID);
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            if (is != null) {
                while ((line = reader.readLine()) != null) {
                    String[] transcr = line.trim().split("\t");
                    if (transcr.length == 2) {
                        pronDict.put(transcr[0], new PronDictEntry(transcr[0], transcr[1]));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return pronDict;
    }

    private List<String> readAlphabets() {
        Resources res = this.mContext.getResources();
        int resID = R.raw.sampa_ipa_single_flite;
        List<String> fileContent = new ArrayList<>();
        String line = "";
        try {
            InputStream is = res.openRawResource(resID);
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            if (is != null) {
                while ((line = reader.readLine()) != null) {
                    fileContent.add(line);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return fileContent;
    }
}
