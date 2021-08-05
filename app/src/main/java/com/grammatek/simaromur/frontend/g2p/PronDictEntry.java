package com.grammatek.simaromur.frontend.g2p;

import java.util.List;

/**
 * For the minimal product we are only using a map dictionary (word-transcript), but we will
 * enrich dictionary entries during future development. Therefore we use this class from the beginning.
 */

public class PronDictEntry {

    private String word;
    private String transcript;

    public PronDictEntry(String word) {
        this(word, "");
    }

    public PronDictEntry(String word, String transcript) {
        this.word = word;
        this.transcript = transcript;
    }

    public String getWord() {
        return word;
    }

    public String getTranscript() {
        return transcript;
    }

    public void setTranscript(String transcr) {
        this.transcript = transcr;
    }

    public static String toTranscribedString(List<PronDictEntry> entryList) {
        StringBuilder sb = new StringBuilder();
        for (PronDictEntry entry : entryList)
            sb.append(entry.getTranscript());

        return sb.toString().trim();
    }
}
