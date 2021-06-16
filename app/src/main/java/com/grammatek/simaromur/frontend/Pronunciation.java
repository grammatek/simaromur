package com.grammatek.simaromur.frontend;

import android.content.Context;
import android.content.res.Resources;

import com.grammatek.simaromur.NativeG2P;
import com.grammatek.simaromur.R;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * This class manages the dictionary look-ups and the calls to g2p for unknown words
 */

public class Pronunciation {
    private Context mContext;
    private NativeG2P mG2P;
    private Map<String, PronDictEntry> mPronDict;

    public Pronunciation(Context context) {
        this.mContext = context;
        initializePronDict();
        initializeG2P();
    }

    public String transcribe(String text) {
        String[] tokens = text.split(" ");
        StringBuilder sb = new StringBuilder();
        for (String tok : tokens) {
            if (mPronDict.containsKey(tok)) {
                sb.append(mPronDict.get(tok).getTranscript()).append(" ");
            } else {
                sb.append(mG2P.process(tok)).append(" ");
            }
        }
        return sb.toString().trim();
    }

    private void initializeG2P() {
        if (mG2P != null) {
            // @todo: mG2P.stop();
            mG2P = null;
        }
        mG2P = new NativeG2P(this.mContext);
    }

    private void initializePronDict() {
        if (mPronDict == null) {
            mPronDict = readPronDict();
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
}
