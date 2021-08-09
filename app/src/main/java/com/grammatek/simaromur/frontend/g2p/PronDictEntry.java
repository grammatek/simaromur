package com.grammatek.simaromur.frontend.g2p;

import java.util.ArrayList;
import java.util.List;

/**
 * For the minimal product we are only using a map dictionary (word-transcript), but we will
 * enrich dictionary entries during future development. Therefore we use this class from the beginning.
 */

public class PronDictEntry {

    private String mWord;
    private String mTranscript;
    private String[] mTranscriptArr;
    private List<Syllable> mSyllables;

    public PronDictEntry(String word) {
        this(word, "");
    }

    public PronDictEntry(String word, String transcript) {
        mWord = word;
        mTranscript = transcript;
        mTranscriptArr = mTranscript.split(" ");
        mSyllables = new ArrayList<>();
    }

    public String getWord() {
        return mWord;
    }

    public String getTranscript() {
        return mTranscript;
    }

    public void setTranscript(String transcr) {
        mTranscript = transcr;
        mTranscriptArr = mTranscript.split(" ");
    }

    public String[] getTranscriptArr() {
        return mTranscriptArr;
    }

    public void setSyllables(List<Syllable> syllables) {
        mSyllables = syllables;
    }
    public List<Syllable> getSyllables() {
        return mSyllables;
    }

    public void updateSyllables(int index, Syllable prevSyll, Syllable syll) {
        if (index <= 0) {
            //TODO: log error or warning
            return;
        }
        mSyllables.set(index - 1, prevSyll);
        mSyllables.set(index, syll);
    }

    public String syllableDotFormat() {
        StringBuilder sb = new StringBuilder();
        for (Syllable syll : mSyllables) {
            sb.append(syll.getContent().trim()).append('.');
        }
        String syllabified = sb.substring(0, sb.length() - 1);
        return syllabified;
    }

    public static String toTranscribedString(List<PronDictEntry> entryList) {
        StringBuilder sb = new StringBuilder();
        for (PronDictEntry entry : entryList)
            sb.append(entry.getTranscript());

        return sb.toString().trim();
    }
}
