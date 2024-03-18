package com.grammatek.simaromur.frontend;

import android.content.Context;
import android.content.res.Resources;

import com.grammatek.simaromur.R;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;

/**
 * This class initializes and stores sets of abbreviations from res/raw abbreviation files.
 */
public class Abbreviations {
    private final Context context;
    private Set<String> abbreviations = new HashSet<>();
    // nonEndingAbbr are not allowed at the end of a sentence
    private Set<String> nonEndingAbbr = new HashSet<>();

    public Abbreviations(Context c) {
        this.context = c;
    }

    public Set<String> getAbbreviations() {
        if (abbreviations.isEmpty())
            abbreviations = readAbbrFromFile(R.raw.abbreviations_general);
        return abbreviations;
    }

    public Set<String> getNonEndingAbbr() {
        if (nonEndingAbbr.isEmpty())
            nonEndingAbbr = readAbbrFromFile(R.raw.abbreviations_nonending);
        return nonEndingAbbr;
    }

    private Set<String> readAbbrFromFile(int resID) {
        // TODO: move to FileUtils
        Set<String> abbrSet = new HashSet<>();
        Resources res = context.getResources();
        String line;
        try {
            InputStream is = res.openRawResource(resID);
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            if (is != null) {
                while ((line = reader.readLine()) != null) {
                    abbrSet.add(line.trim());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return abbrSet;
    }
}
