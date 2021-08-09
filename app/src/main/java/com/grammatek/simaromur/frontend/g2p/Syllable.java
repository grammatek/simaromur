package com.grammatek.simaromur.frontend.g2p;

import com.grammatek.simaromur.frontend.Util;

import java.util.Arrays;

/**
 * Syllabification processes phonetic transcripts of words, where each phone is separated by a space.
 * This space separated transcription represents the content field of a Syllable object.
 * Note that some phones might be written as two characters.
 */
public class Syllable {

    private String mContent;
    private boolean mHasNucleus;
    // consonant clusters play a certain role in syllabification, see class Syllabification
    private String mConsCluster;
    private int mStress;

    public Syllable() {
        mContent = "";
        mHasNucleus = false;
        mConsCluster = "";
        // default value is 0, "no stress"
        mStress = 0;
    }

    public boolean hasNucleus() {
        return mHasNucleus;
    }
    public void setNucleus(boolean hasNucleus) {
        mHasNucleus = hasNucleus;
    }
    public void setContent(String content) {
        mContent = content.trim();
    }

    public String getContent() {
        return mContent;
    }
    public void setConsCluster(String cluster) {
        mConsCluster = cluster;
    }
    public String getConsCluster() {
        return mConsCluster;
    }
    public void append(String phoneString) {
        mContent += phoneString + " ";
    }

    public void appendBefore(String phoneString) {
        mContent = phoneString + " " + mContent;
    }

    /*
    return 'num' last phones from content as string
     */
    public String lastPhones(int num) {
        String lastPhones = "";
        String[] phoneArr = mContent.split(" ");
        if (num > phoneArr.length) {
            //LOG ERROR
        }
        else {
            int startIndex = phoneArr.length - num;
            String[] lastPhonesArr = Arrays.copyOfRange(phoneArr, startIndex, phoneArr.length-1);
            lastPhones = Util.join(lastPhonesArr);
        }
        return lastPhones;
    }

    /*
    return the index of the last character (not space) in content
     */
    public int lastIndex() {
        int contLen = mContent.trim().length();
        return contLen > 0 ? contLen - 1 : 0;
    }

    public boolean startsWith(Character phone) {
        return mContent.charAt(0) == phone;
    }

    public boolean endsWith(Character phone) {
        return mContent.charAt(mContent.length()-1) == phone;
    }

    public int indexOfCluster() {
        if (!mConsCluster.isEmpty()) {
            return mContent.indexOf(mConsCluster);
        }
        else
            return -1;
    }

    /*
    remove the consonant cluster and everything occurring after the cluster from content
     */
    public void removeCluster() {
        int clusterInd = indexOfCluster();
        if (clusterInd > 0)
            mContent = mContent.substring(0, clusterInd);
    }
}
