package com.grammatek.simaromur.frontend.g2p;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * CompoundTree builds a tree structure out of a PronDictEntry which can be traversed in a preorder way.
 * Aim is to divide an entry into compound subcomponents until each component cannot be divided
 * further. The algorithm relies on dictionaries, i.e. if it does not find a potential head word
 * in the head word dictionary, the segmentation will not be performed.
 * This analysis is done to increase accuracy in syllabification and stress labeling.
 *
 */
public class CompoundTree {

    public static Set<Character> VOWELS = new HashSet<>();
    static {
        VOWELS.add('a');
        VOWELS.add('á');
        VOWELS.add('e');
        VOWELS.add('é');
        VOWELS.add('i');
        VOWELS.add('í');
        VOWELS.add('o');
        VOWELS.add('ó');
        VOWELS.add('u');
        VOWELS.add('ú');
        VOWELS.add('y');
        VOWELS.add('ý');
        VOWELS.add('ö');
    }
    // We only look at words of MIN_COMP_LEN to divide further, everything shorter will be assumed
    // not to be a compound
    private static final int MIN_COMP_LEN = 5;
    private static final int MIN_INDEX = 2; // the position from which to start searching for a head word
    // the elements of a compoundTree
    private final PronDictEntry mElem;
    private CompoundTree mLeft;
    private CompoundTree mRight;

    public CompoundTree(PronDictEntry entry) {
        mElem = entry;
        mLeft = null;
        mRight = null;
    }

    public PronDictEntry getElem() {
        return mElem;
    }

    public CompoundTree getLeft() {
        return mLeft;
    }

    public CompoundTree getRight() {
        return mRight;
    }

    public void setLeft(CompoundTree tree) {
        mLeft = tree;
    }

    public void setRight(CompoundTree tree) {
        mRight = tree;
    }

    /*
    Traverse the tree
     */
    public void preorder() {
        if (mLeft == null)
            System.out.println(mElem);
        else
            mLeft.preorder();
        if (mRight != null)
            mRight.preorder();
    }

    public static CompoundTree buildTree(PronDictEntry entry) {
        CompoundTree tree = new CompoundTree(entry);
        tree.extractCompoundComponents(tree);
        return tree;
    }

    /*
    As long as compound components can be extracted, extract compound components and their transcripts
    recursively.
     */
    private void extractCompoundComponents(CompoundTree tree) {
        String[] compComponents = lookupCompoundComponents(tree.getElem().getWord());
        String modifier = compComponents[0];
        String head = compComponents[1];

        if (!modifier.isEmpty() && !head.isEmpty()) {
            String[] transcrComponents = extractTranscripts(tree.getElem(), head);
            String modTranscr = transcrComponents[0];
            String headTranscr = transcrComponents[1];
            if (!modTranscr.isEmpty() && !headTranscr.isEmpty()) {
                PronDictEntry leftElem = new PronDictEntry(modifier, modTranscr);
                CompoundTree leftTree = new CompoundTree(leftElem);
                tree.setLeft(leftTree);
                PronDictEntry rightElem = new PronDictEntry(head, headTranscr);
                CompoundTree rightTree = new CompoundTree(rightElem);
                tree.setRight(rightTree);
                extractCompoundComponents(leftTree);
                extractCompoundComponents(rightTree);
            }
        }
    }
    /*
    Divides the word based on if its components are found in the compound map. The rule of thumb is that
    the longest possible head word shows the correct division, but if a modifier is found for a shorter
    head word, this one is chosen. If no modifier is found but a valid head word, the longest valid head word is
    returned, with the assumption that the modifier will also be valid, even if it is not in the dictionary.

    Returns an array of length 2, containing extracted modifier and head, if found; empty
    indices otherwise.
     */
    private String[] lookupCompoundComponents(String word) {
        String[] compComponents = new String[] {"", ""};
        if (word.length() < MIN_COMP_LEN)
            return compComponents;

        int n = MIN_INDEX;
        String longestValidHead = "";
        String modifier = "";
        while (n < word.length() - 2) {
            String head = word.substring(n);
            if (Pronunciation.HEAD_MAP.containsKey(head)) {
                if (Pronunciation.MODIFIER_MAP.containsKey(word.substring(0, n))) {
                    compComponents[0] = word.substring(0, n);
                    compComponents[1] = head;
                    return compComponents;
                }
                else if (longestValidHead.isEmpty())
                    longestValidHead = head;
            }
           n++;
        }
        if (!longestValidHead.isEmpty()) {
            // assume we have a valid modifier, even if we didn't found it in the dictionary
            modifier = word.substring(0, word.indexOf(longestValidHead));
            // but only as long as it contains a vowel!
            if (!containsVowel(modifier))
                modifier = "";
        }
        compComponents[0] = modifier;
        compComponents[1] = longestValidHead;
        return compComponents;
    }

    /*
    Get the transcript of comp_head from the pron. dictionary and try to match it with the transcript of
    the whole entry. We are somewhat flexible here: length symbol, voicelessness or postaspiration do not
    cause the matching to fail (a: == a, r_0 == r, t_h == t), since transcriptions might not always match 100%.

    Other variations could be added, like 'r t n' == 't n' like in barn: 'b a t n' vs. 'b a r t n', but we are
    not there yet.
     */
    private String[] extractTranscripts(PronDictEntry entry, String compHead) {
        String[] transcripts = new String[] {"", ""};
        String headTranscr = "";
        if (Pronunciation.PRON_DICT.containsKey(compHead))
            headTranscr = Pronunciation.PRON_DICT.get(compHead).getTranscript();
        else
            headTranscr = "NO_TRANSCRIPT";

        int headSyllableIndex = entry.getTranscript().indexOf(headTranscr);
        if (headSyllableIndex <= 0) {
            headSyllableIndex = compareTranscripts(entry.getTranscript(), headTranscr);
        }
        if (headSyllableIndex <= 0)
            return transcripts;

        String modifierTranscr = entry.getTranscript().substring(0, headSyllableIndex);
        headTranscr = entry.getTranscript().substring(headSyllableIndex);
        transcripts[0] = modifierTranscr;
        transcripts[1] = headTranscr;

        return transcripts;
    }

    /*
    If a transcript differs only in a length mark or in voiced/voiceless or having post aspriation or not,
    it should be recognized as the same transcript (since we have already matched the corresponding word strings)
     */
    private int compareTranscripts(String entryTranscr, String headTranscr) {
        int compInd = entryTranscr.length() - 1;
        int headInd = headTranscr.length() - 1;

        while (headInd >= 0) {
            char headChar = headTranscr.charAt(headInd);
            char compChar = entryTranscr.charAt(compInd);
            if (headChar == compChar) {
                headInd--;
                compInd--;
            }
            else if (headChar == ':')
                headInd--;
            else if (compChar == ':')
                compInd--;
            else if (headChar == '0' || headChar == 'h')
                headInd -= 2;
            else if (compChar == '0' || compChar == 'h')
                compInd -= 2;
            else
                return -1;
        }
        return compInd + 1; // make up for the last iteration where headInd went below zero
    }

    private boolean containsVowel(String word) {
        for (int i = 0; i < word.length(); i++) {
            if (VOWELS.contains(word.charAt(i)))
                return true;
        }
        return false;
    }

}
