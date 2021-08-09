package com.grammatek.simaromur.frontend.g2p;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Perform syllabification on words transcribed with X-SAMPA.
 * Syllable rules for Icelandic as described in:
 *     - Anton Karl Ingason (2006): Íslensk atkvæði - vélræn nálgun. (http://www.linguist.is/skjol/atkvadi.pdf)
 *     - Kristján Árnason (2005): Hljóð. Ritröðin Íslensk tunga.
 *
 * The basic assumption is that Icelandic syllable structure follows the onset-rhyme model. From this follows that,
 * whenever possible, a syllable has a 'need' for an onset consonant. This contradicts in some cases with rules for
 * 'between-the-lines' word division rules saying that a word should be divided between lines
 * such that the next line starts with a vowel: 'hes.tur' vs. 'hest.ur'.
 *
 * The following consonant combinations always build an onset together:
 *
 *     s, p, t, k + v, j, r    (sv, sj, sr, pv, pj, pr, etc.)
 *     fr
 *
 * # input: transcribed and aligned word, example: a v p r I G D I (afbrigði)
 * # output: syllabified word: af.prIG.DI (af.brig.ði)
 *
 * # algorithm:
 * # 1) symbols upto the second vowel build the first syllable: 'afpr'
 * # 2) the remaining string is divided into syllables each starting with a vowel: 'afpr.IGD.I'
 * # 3) identify cons cluster: 'af(pr).IGD.I'
 * # 4) move consonants to onset (cons clusters and single consonants): 'af.prIG.DI'
 *
 * This algorithm normally gives correct results for simple words, but can produce errors when applied to compounds.
 * Thus it is important to perform compound analysis before applying the core syllabification algorithm.
 */
public class Syllabification {

    // Each syllable has a vowel as a nucleus. 'e' and 'o' aren't actually in the inventory, but we
    // need to be able to identify 'ei' and 'ou' from the first character only.
    //TODO: rather use set, is used for lookup
    public static final Set<String> VOWELS = new HashSet<>();
    static {
        VOWELS.add("a");
        VOWELS.add("a:");
        VOWELS.add("O");
        VOWELS.add("O:");
        VOWELS.add("u");
        VOWELS.add("u:");
        VOWELS.add("9");
        VOWELS.add("9:");
        VOWELS.add("Y");
        VOWELS.add("Y:");
        VOWELS.add("E");
        VOWELS.add("E:");
        VOWELS.add("I");
        VOWELS.add("I:");
        VOWELS.add("i");
        VOWELS.add("i:");
        VOWELS.add("ai");
        VOWELS.add("ai:");
        VOWELS.add("au");
        VOWELS.add("au:");
        VOWELS.add("ou");
        VOWELS.add("ou:");
        VOWELS.add("9i");
        VOWELS.add("9i:");
        VOWELS.add("Oi");
        VOWELS.add("Yi");
        VOWELS.add("ei");
        VOWELS.add("ei:");
        VOWELS.add("e");
        VOWELS.add("o");
    };

    // These consonant clusters should not be divided between two syllables
    // the general rule is: p, t, k, s, b, d, g, f + v, j, r. But not all of these combinations are
    // valid ('sr', 'pv', 'fv')
    public static final String CONS_CLUSTERS[] = new String[]{"s v", "s j", "p j", "p r", "t v",
            "t j", "t r", "k v", "k j", "k r", "p_h j", "p_h r", "t_h v", "t_h j", "t_h r", "k_h v",
            "k_h j", "k_h r", "f r", "f j"};


    /**
     * Syllabify each entry in the tree list.
     * @param treeList a list of CompoundTrees to syllabify
     * @return a list of syllabifies PronDictEntrys
     */
    public List<PronDictEntry> syllabifyCompoundTrees(List<CompoundTree> treeList) {
        List<PronDictEntry> syllabified = new ArrayList<>();
        for (CompoundTree tree : treeList) {
            List<Syllable> syllables = new ArrayList<>();
            syllabifyTree(tree, syllables);
            tree.getElem().setSyllables(syllables);
            syllabified.add(tree.getElem());
        }
        return syllabified;
    }

    /* Recursively call syllabification on each element of tree.
    Add up the syllables of the leaf nodes to build the syllable structure of the root element.
     */
    private void syllabifyTree(CompoundTree tree, List<Syllable> syllables) {
        if (tree.getLeft() == null) {
            syllabifyEntry(tree.getElem());
            syllables.addAll(tree.getElem().getSyllables());
        }
        if (tree.getLeft() != null) {
            syllabifyTree(tree.getLeft(), syllables);
        }
        if (tree.getRight() != null) {
            syllabifyTree(tree.getRight(), syllables);
        }
    }

    private void syllabifyEntry(PronDictEntry entry) {
        entry.setSyllables(syllabifyOnNucleus(entry.getTranscriptArr()));
        identifyClusters(entry);
        syllabifyFinal(entry);
    }

    /*
    First round of syllabification. Divide the word such that each syllable starts with a vowel
    (except the first one, if the word starts with a consonant).
     */
    private List<Syllable> syllabifyOnNucleus(String[] transcrArr) {
        List<Syllable> syllables = new ArrayList<>();
        Syllable currentSyllable = new Syllable();
        for (String phone : transcrArr) {
            if (currentSyllable.hasNucleus() && VOWELS.contains(phone)) {
                syllables.add(currentSyllable);
                currentSyllable = new Syllable();
            }
            if (VOWELS.contains(phone))
                currentSyllable.setNucleus(true);
            currentSyllable.append(phone);
        }
        // append last syllable
        syllables.add(currentSyllable);
        return syllables;
    }

    private void identifyClusters(PronDictEntry entry) {
        for (Syllable syll : entry.getSyllables()) {
            for (String cluster : CONS_CLUSTERS) {
                if (syll.getContent().endsWith(cluster))
                    syll.setConsCluster(cluster);
            }
        }
    }

    /*
    Iterate once more over the syllable structure and move consonants from rhyme to onset
    where appropriate, i.e. where one syllable ends with a consonant and the
    next one starts with a vowel. If a syllable ends with a consonant cluster, the whole
    cluster is moved to the next syllable, otherwise only the last consonant.
    However, if a syllable has a fixed boundary (as a result of decompounding), end or start,
    the boundary can not be changed.
     */
    private void syllabifyFinal(PronDictEntry entry) {
        for (int i = 0; i < entry.getSyllables().size(); i++) {
            if (i == 0)
                continue;
            Syllable syll = entry.getSyllables().get(i);
            Syllable prevSyll = entry.getSyllables().get(i - 1);
            // syllable after the first syllable starts with a vowel - look for consonant onset in previous syllable
            // and move the consonant / consonant cluster from the previous to the current syllable
            String syllStart = syll.getContent().substring(0, 1);
            if (VOWELS.contains(syllStart)) {
                if (!prevSyll.getConsCluster().isEmpty()) {
                    // copy consCluster to next syllable
                    syll.appendBefore(prevSyll.getConsCluster());
                    prevSyll.removeCluster();
                    entry.updateSyllables(i, prevSyll, syll);
                }
                else if (!VOWELS.contains(prevSyll.lastPhones(1))) {
                    // handle 'jE' (=é) as one vowel
                    // TODO: test cases: why are those blocks the same?
                    if (prevSyll.endsWith('j') && syll.startsWith('E')) {
                        String phone = prevSyll.lastPhones(1);
                        syll.appendBefore(phone);
                        prevSyll.setContent(prevSyll.getContent()
                                .substring(0, prevSyll.getContent().length() - phone.length()));
                    }
                    else {
                        String phone = prevSyll.lastPhones(1);
                        syll.appendBefore(phone);
                        prevSyll.setContent(prevSyll.getContent()
                                .substring(0, prevSyll.getContent().length() - phone.length()));
                    }
                    entry.updateSyllables(i, prevSyll, syll);
                }
            }
        }
    }
}
