package com.grammatek.simaromur.frontend.g2p;

import android.content.Context;
import android.content.res.Resources;

import com.grammatek.simaromur.NativeG2P;
import com.grammatek.simaromur.R;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class manages the dictionary look-ups and the calls to g2p for unknown words
 */

public class Pronunciation {
    public static Map<String, PronDictEntry> PRON_DICT;
    public static Map<String, List<String>> MODIFIER_MAP;
    public static Map<String, List<String>> HEAD_MAP;

    private Context mContext;
    private NativeG2P mG2P;


    public Pronunciation(Context context) {
        this.mContext = context;
        initializePronDict();
        initializeModifierMap();
        initializeHeadMap();
       // initializeG2P();
    }

    public String transcribe2String(String text) {
        String[] tokens = text.split(" ");
        StringBuilder sb = new StringBuilder();
        for (String tok : tokens) {
            if (PRON_DICT.containsKey(tok)) {
                sb.append(PRON_DICT.get(tok).getTranscript()).append(" ");
            } else {
                sb.append(mG2P.process(tok)).append(" ");
            }
        }
        return sb.toString().trim();
    }

    public List<PronDictEntry> transcribe2Entries(String text) {
        String[] tokens = text.split(" ");
        List<PronDictEntry> entryList = new ArrayList<>();
        for (String tok : tokens) {
            PronDictEntry entry = new PronDictEntry(tok);
            if (PRON_DICT.containsKey(tok)) {
                entry.setTranscript(PRON_DICT.get(tok).getTranscript());

            } else {
                entry.setTranscript(mG2P.process(tok));
            }
            entryList.add(entry);
        }
        return entryList;
    }

    private void initializeG2P() {
        if (mG2P != null) {
            // @todo: mG2P.stop();
            mG2P = null;
        }
        mG2P = new NativeG2P(this.mContext);
    }

    private void initializePronDict() {
        if (PRON_DICT == null) {
            PRON_DICT = readPronDict();
        }
    }

    private void initializeModifierMap() {
        if (MODIFIER_MAP == null) {
            MODIFIER_MAP = readCompDict(R.raw.modifier_map);
        }
    }
    private void initializeHeadMap() {
        if (HEAD_MAP == null) {
            HEAD_MAP = readCompDict(R.raw.head_map);
        }
    }

    private Map<String, PronDictEntry> readPronDict() {
        Map<String, PronDictEntry> pronDict = new HashMap<>();
        Resources res = this.mContext.getResources();
        int resID = R.raw.ice_pron_dict_standard_clear_2102;
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

    private Map<String, List<String>> readCompDict(int resID) {
        Map<String, List<String>> compDict = new HashMap<>();
        Resources res = this.mContext.getResources();
        String line = "";
        try {
            InputStream is = res.openRawResource(resID);
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            if (is != null) {
                while ((line = reader.readLine()) != null) {
                    String[] transcr = line.trim().split("\t");
                    if (transcr.length >= 2) {
                        List<String> values = new ArrayList<>();
                        for (String s : transcr) {
                            values.add(s);
                        }
                        // first value is the key, so remove that
                        values.remove(0);
                        compDict.put(transcr[0], values);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return compDict;
    }
}
