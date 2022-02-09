package com.grammatek.simaromur.frontend;

/**
 * For the minimal product we are only using a map dictionary (word-transcript), but we will
 * enrich dictionary entries during future development. Therefore we use this class from the beginning.
 */

public class PronDictEntry {

    private final String word;
    private final String transcript;

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
}
