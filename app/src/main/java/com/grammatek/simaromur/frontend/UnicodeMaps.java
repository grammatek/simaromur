package com.grammatek.simaromur.frontend;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * This class holds different maps, containing unicode character encodings and their respective
 * replacement for the unicode cleaning of raw text (see @TTSUnicodeNormalizer).
 * The maps are by no means finalized, need to be extended and adjusted, as we run into problematic
 * characters.
 */
public class UnicodeMaps {

    public static Map<Character, String> deleteCharsMap = new HashMap<>();
    static {
        deleteCharsMap.put('\u0000', ""); // <control> NULL
        deleteCharsMap.put('\u0001', ""); // <control> start of heading
        deleteCharsMap.put('\u0002', ""); // <control> start of text
        deleteCharsMap.put('\u0004', ""); // <control> end of transmission
        deleteCharsMap.put('\u0005', ""); // <control> enquiry
        deleteCharsMap.put('\u0006', ""); // <control> acknowledge
        deleteCharsMap.put('\u0008', ""); // <control> backspace
        deleteCharsMap.put('\u0010', ""); // <control> data link escape
        deleteCharsMap.put('\u0011', ""); // <control> device control one
        deleteCharsMap.put('\u0012', ""); // <control> device control two
        deleteCharsMap.put('\u0013', ""); // <control> device control three
        deleteCharsMap.put('\u0014', ""); // <control> device control four
        deleteCharsMap.put('\u0015', ""); // negative acknowledge
        deleteCharsMap.put('\u0016', ""); // synchronous idle
        deleteCharsMap.put('\u0017', ""); // end of transmission block
        deleteCharsMap.put('\u0018', ""); // cancel
        deleteCharsMap.put('\u0019', ""); // end of medium
        deleteCharsMap.put('\u001a', ""); // substitute
        deleteCharsMap.put('\u001b', ""); // escape
        deleteCharsMap.put('\u001f', ""); // unit separator
        deleteCharsMap.put('\u000e', ""); // shift out
        deleteCharsMap.put('\u000f', ""); // shift in
        deleteCharsMap.put('\u0098', ""); // start of string
        deleteCharsMap.put('\u00ad', ""); // soft hyphen
        deleteCharsMap.put('\u00d7', ""); // multiplication sign -> why delete?
        deleteCharsMap.put('\u02c8', ""); // modifier letter vertical line (e.g. as stress mark in IPA)
        deleteCharsMap.put('\u02cc', ""); // modifier letter low vertical line (e.g. as secondary stress mark in IPA)
        deleteCharsMap.put('\u200e', ""); // left-to-right mark
        // deleteCharsMap.put('\u2019', ""); // right single quotation mark -> why delete? moved to otherSubstMap
        deleteCharsMap.put('\ufeff', ""); // zero width no-break space
    }

    public static Map<Character, String> insertSpaceMap = new HashMap<>();
    static {
        insertSpaceMap.put('\u0003', " "); // <control> end of text
        insertSpaceMap.put('\u0007', " "); // <control> bell
        insertSpaceMap.put('\u0009', " "); // <control> horizontal tabulation
        insertSpaceMap.put('\u000b', " "); // <control> vertical tabulation
        insertSpaceMap.put('\u000c', " "); // <control> form feed
        insertSpaceMap.put('\u0080', " "); // <control>
        insertSpaceMap.put('\u0081', " "); // <control>
        insertSpaceMap.put('\u0082', " "); // break permitted here
        insertSpaceMap.put('\u0095', " "); // message waiting
        insertSpaceMap.put('\u00a0', " "); // no-break space
        insertSpaceMap.put('\u200b', " "); // zero width space
        insertSpaceMap.put('\u2028', " "); // line spearator
        insertSpaceMap.put('\u2192', " "); // rightwards arrow
        insertSpaceMap.put('\u220f', " "); // n-ary product -> why delete?
        insertSpaceMap.put('\ufa07', " "); // ideograph spokes of wheel CJK -> check this range, CJK
    }

    // if those characters are found in words not in the pronunciation dictionary,
    // we need to replace them with characters from the Icelandic alphabet
    public static Map<Character, String> postDictLookupMap = new HashMap<>();
    static {
        postDictLookupMap.put('c', "k"); // TODO: can we contextualize this?
        postDictLookupMap.put('w', "v");
        postDictLookupMap.put('z', "s");
        postDictLookupMap.put('q', "k");
        postDictLookupMap.put('å', "o");
        postDictLookupMap.put('ä', "e");
        postDictLookupMap.put('ü', "u");
        postDictLookupMap.put('ø', "ö");
        postDictLookupMap.put('ć', "ts"); // polish
        postDictLookupMap.put('ę', "e");
        postDictLookupMap.put('ł', "ú"); // polish, like English 'w' in 'will'
        postDictLookupMap.put('ń', "n");
        postDictLookupMap.put('ś', "s");
        postDictLookupMap.put('ź', "s");
        postDictLookupMap.put('ż', "s");
        postDictLookupMap.put('C', "K"); // TODO: can we contextualize this?
        postDictLookupMap.put('W', "V");
        postDictLookupMap.put('Z', "S");
        postDictLookupMap.put('Q', "K");
        postDictLookupMap.put('Å', "O");
        postDictLookupMap.put('Ä', "E");
        postDictLookupMap.put('Ü', "U");
        postDictLookupMap.put('Ø', "Ö");
    }

    // delete in MVP, transliterate later if necessary
    public static Map<Character, String> ipaMap = new HashMap<>();
    static {
        ipaMap.put('\u0252', ""); // latin small letter turned alpha
        ipaMap.put('\u0259', ""); // latin small letter schwa
        ipaMap.put('\u0283', ""); // latin small letter esh
        ipaMap.put('\u028a', ""); // latin small letter upsilon
        ipaMap.put('\u028b', ""); // latin small letter v with hook
    }

    public static Map<Character, String> greekAlphabet = new HashMap<>();
    static {
        greekAlphabet.put('\u0394', "delta"); // greek capital letter delta
        greekAlphabet.put('\u039b', "lambda"); // greek capital letter lambda
        greekAlphabet.put('\u03a3', "sigma"); // greek capital letter sigma
        greekAlphabet.put('\u03a4', "tá"); // greek capital letter tau
        greekAlphabet.put('\u03ac', "alpha"); // greek small letter alpha with tonos
        greekAlphabet.put('\u03ae', "eta"); // greek small letter eta with tonos
        greekAlphabet.put('\u03af', "jóta"); // greek small letter iota with tonos
        greekAlphabet.put('\u03b1', "alpha"); // greek small letter alpha
        greekAlphabet.put('\u03b3', "gamma"); // greek small letter gamma
        greekAlphabet.put('\u03b4', "delta"); // greek small letter delta
        greekAlphabet.put('\u03b5', "epsilon"); // greek small letter epsilon. Subst with 'e'?
        greekAlphabet.put('\u03b7', "eta"); // greek small letter eta
        greekAlphabet.put('\u03b9', "jóta"); // greek small letter iota
        greekAlphabet.put('\u03ba', "kappa"); // greek small letter kappa
        greekAlphabet.put('\u03bb', "lambda"); // greek small letter lambda
        greekAlphabet.put('\u03bc', "mu"); // greek small letter mu. Better subst.?
        greekAlphabet.put('\u03bd', "nu"); // greek small letter nu. Better subst.?
        greekAlphabet.put('\u03bf', "omicron"); // greek small letter omicron
        greekAlphabet.put('\u03c0', "pí"); // greek small letter pi
        greekAlphabet.put('\u03c1', "ró"); // greek small letter rho
        greekAlphabet.put('\u03c2', "sigma"); // greek small letter final sigma. Better subst.?
        greekAlphabet.put('\u03c3', "sigma"); // greek small letter sigma
        greekAlphabet.put('\u03c4', "tá"); // greek small letter tau
        greekAlphabet.put('\u03c5', "upsilon"); // greek small letter upsilon
        greekAlphabet.put('\u03c6', "fí"); // greek small letter phi
        greekAlphabet.put('\u03c7', "hjí"); // greek small letter chi. Better subst.?
        greekAlphabet.put('\u03c9', "omega"); // greek small letter omega
        greekAlphabet.put('\u03cc', "omicron"); // greek small letter omicron with tonos
        greekAlphabet.put('\u03cd', "upsilon"); // greek small letter upsilon with tonos
        greekAlphabet.put('\u1f00', "alpha"); // greek small letter alpha with psili
        greekAlphabet.put('\u1f08', "alpha"); // greek capital letter alpha with psili
        greekAlphabet.put('\u1fc6', "eta"); // greek capital letter eta with perispomeni
    }

    public static Map<Character, String> arabicAlphabet = new HashMap<>();
    static {
        arabicAlphabet.put('\u0627', ""); // arabic letter alef
        arabicAlphabet.put('\u062f', ""); // arabic letter dal
        arabicAlphabet.put('\u0631', ""); // arabic letter reh
        arabicAlphabet.put('\u0641', ""); // arabic letter feh
        arabicAlphabet.put('\u0648', ""); // arabic letter waw
    }

    public static Map<Character, String> hebrewAlphabet = new HashMap<>();
    static {
        hebrewAlphabet.put('\u05d3', ""); // hebrew letter dalet
        hebrewAlphabet.put('\u05d4', ""); // hebrew letter he
        hebrewAlphabet.put('\u05d5', ""); // hebrew letter vav
        hebrewAlphabet.put('\u05d9', ""); // hebrew letter yod
        hebrewAlphabet.put('\u05db', ""); // hebrew letter kaf
        hebrewAlphabet.put('\u05dc', ""); // hebrew letter lamed
        hebrewAlphabet.put('\u05de', ""); // hebrew letter mem
        hebrewAlphabet.put('\u05df', ""); // hebrew letter final nun
        hebrewAlphabet.put('\u05e2', ""); // hebrew letter ayin
        hebrewAlphabet.put('\u05e4', ""); // hebrew letter pe
        hebrewAlphabet.put('\u05e7', ""); // hebrew letter qof
        hebrewAlphabet.put('\u05e9', ""); // hebrew letter shin
        hebrewAlphabet.put('\u05ea', ""); // hebrew letter taw
    }

    public static Map<Character, String> cyrillicAlphabet = new HashMap<>();
    static {
        cyrillicAlphabet.put('\u0421', ""); // cyrillic capital letter es
        cyrillicAlphabet.put('\u0430', ""); // cyrillic small letter a
        cyrillicAlphabet.put('\u0438', ""); // cyrillic small letter i
        cyrillicAlphabet.put('\u043b', ""); // cyrillic small letter el
        cyrillicAlphabet.put('\u043d', ""); // cyrillic small letter en
        cyrillicAlphabet.put('\u043f', ""); // cyrillic small letter letter pe
        cyrillicAlphabet.put('\u0440', ""); // cyrillic capital letter er
        cyrillicAlphabet.put('\u0442', ""); // cyrillic small letter te
    }

    public static Map<Character, String> otherSubstMap = new HashMap<>();
    static {
        otherSubstMap.put('\u0085', "..."); // next line (nel) -> why this substitution?
        otherSubstMap.put('\u0091', "'"); // private use one -> why this substitution?
        otherSubstMap.put('\u0092', "’"); // private use two -> why this substitution?
        otherSubstMap.put('\u0096', "-"); // start of guarded area -> why this substitution?
        otherSubstMap.put('\u00a9', "höfundarréttur"); // © copyright sign
        otherSubstMap.put('\u00ae', "skráð vörumerki"); // ® registered trademark symbol
        otherSubstMap.put('\u00b4', "'"); // acute accent
        otherSubstMap.put('\u2010', "-"); // hyphen
        otherSubstMap.put('\u2011', "-"); // non-breaking hyphen
        otherSubstMap.put('\u2012', "-"); // figure dash
        otherSubstMap.put('\u2013', "-"); // en dash
        otherSubstMap.put('\u2014', "-"); // em dash
        otherSubstMap.put('\u2019', "'"); // right single quotation mark
        otherSubstMap.put('\u201a', ","); // single low-9 quotation mark
        otherSubstMap.put('\u201c', "\""); // left double qoutation mark
        otherSubstMap.put('\u201d', "\""); // right double qoutation mark
        otherSubstMap.put('\u201e', "\""); // double low-9 qoutation mark
        otherSubstMap.put('\u201f', "\""); // double high-reversed-9 qoutation mark
        otherSubstMap.put('\u2212', "-"); // minus sign
        otherSubstMap.put('\u2713', "-"); // check mark -> why this substitution?
        otherSubstMap.put('\u0100', "A"); // latin capital letter A with macron (long a)
        otherSubstMap.put('\u0101', "a"); // latin small letter a with macron (long a)
        otherSubstMap.put('\u0106', "Ts"); // latin capital letter C with acute
        otherSubstMap.put('\u0107', "ts"); // latin small letter c with acute
        otherSubstMap.put('\u010c', "Tj"); // latin capital letter C with caron (similar to 'ch' in 'chocolate')
        otherSubstMap.put('\u010d', "tj"); // latin small letter c with caron
        otherSubstMap.put('\u0110', "Ð"); // latin capital letter D with stroke
        otherSubstMap.put('\u0111', "ð"); // latin small letter d with stroke
        otherSubstMap.put('\u0112', "E"); // latin capital letter E with macron
        otherSubstMap.put('\u0113', "e"); // latin small letter e with macron
        otherSubstMap.put('\u011b', "É"); // latin capital letter E with caron, /jE/
        otherSubstMap.put('\u011c', "é"); // latin small letter e with caron, /jE/
        otherSubstMap.put('\u011e', "G"); // latin capital letter G with breve -> note: should be pronunced /G/ SAMPA
        otherSubstMap.put('\u011f', "g"); // latin small letter g with breve -> note: should be pronunced /G/ SAMPA
        otherSubstMap.put('\u0131', "i"); // dotless i
        otherSubstMap.put('\u0141', "Ú"); // latin capital letter L with stroke -> should resemble /w/, rather use 'L' subst?
        otherSubstMap.put('\u0142', "ú"); // latin small letter l with stroke -> should resemble /w/, rather use 'l' subst?
        otherSubstMap.put('\u0143', "Nj"); // latin capital letter N with acute
        otherSubstMap.put('\u0144', "nj"); // latin small letter n with acute
        otherSubstMap.put('\u0147', "Nj"); // latin capital letter N with caron
        otherSubstMap.put('\u0148', "nj"); // latin small letter n with caron
        otherSubstMap.put('\u014c', "O"); // latin capital letter O with macron
        otherSubstMap.put('\u014d', "o"); // latin small letter o with macron
        otherSubstMap.put('\u0152', "E"); // latin ligature OE
        otherSubstMap.put('\u0153', "e"); // latin ligature oe
        otherSubstMap.put('\u0158', "Hr"); // latin capital letter R with caron -> voiceless r
        otherSubstMap.put('\u0159', "hr"); // latin small letter r with caron -> voiceless r
        otherSubstMap.put('\u015e', "Sj"); // latin capital letter S with cedilla -> like German 'sch'
        otherSubstMap.put('\u015f', "sj"); // latin small letter s with cedilla -> like German 'sch'
        otherSubstMap.put('\u0160', "S"); // latin capital letter S with caron -> like 'sh' in 'she'
        otherSubstMap.put('\u0161', "s"); // latin small letter s with caron -> like 'sh' in 'she'
        otherSubstMap.put('\u016a', "Ú"); // latin capital letter U with macron
        otherSubstMap.put('\u016b', "ú"); // latin small letter u with macron
        otherSubstMap.put('\u0179', "S"); // latin capital letter Z with acute
        otherSubstMap.put('\u017a', "s"); // latin small letter z with acute
        otherSubstMap.put('\u017b', "S"); // latin capital letter Z with dot above
        otherSubstMap.put('\u017c', "s"); // latin small letter z with dot above
        otherSubstMap.put('\u0219', "s"); // latin small letter s with comma below
        otherSubstMap.put('\u2032', "fet"); // prime -> add a sign for normalizer, that an inflection might be needed? fet, feta, ...
        otherSubstMap.put('\u00D7', "margföldunar merki"); // the x multiplication symbol
        otherSubstMap.put('\u00F7', "deilingar merki"); // the ÷ division symbol
    }

    public static Set<Character> CharactersOutOfRange2Keep = new HashSet<>();
    static {
        CharactersOutOfRange2Keep.add('\u20a4'); // Lira sign
        CharactersOutOfRange2Keep.add('\u20ac'); // Euro sign
    }

    public static Character CombiningGraveAccent = '\u0300'; // ̀
    public static Character CombiningAcuteAccent = '\u0301'; // ´ as in á,é, ...
    public static Character CombiningCircumflexAccent = '\u0302'; // ̂
    public static Character CombiningTilde = '\u0303'; // ̃
    public static Character CombiningMacron = '\u0304'; // ̄
    public static Character CombiningOverline = '\u0305'; // ̅
    public static Character CombiningBreve = '\u0306'; // ̆
    public static Character CombiningDotAbove = '\u0307'; // ̇
    public static Character CombiningDiaeresis = '\u0308'; // ¨ as in ä, ü, ...
    // etc. upto u0362 all kinds of "combining" characters

    public static Map<String, String> SymbolsMap = new HashMap<>();
    static {
        SymbolsMap.put("+", "plús");
        SymbolsMap.put("=", "jafnt og merki");
        SymbolsMap.put("/", "skástrik");
        SymbolsMap.put("_", "undirstrik");
        SymbolsMap.put("<", "minna en merki");
        SymbolsMap.put("[", "vinstri hornklofi");
        SymbolsMap.put("]", "hægri hornklofi");
        SymbolsMap.put("!", "upphrópunarmerki");
        SymbolsMap.put("@", "att merki");
        SymbolsMap.put("#", "myllumerki");
        SymbolsMap.put("$", "dollaramerki");
        SymbolsMap.put("^", "innskotsmerki");
        SymbolsMap.put("&", "og merki");
        SymbolsMap.put("*", "stjarna");
        SymbolsMap.put("(", "vinstri svigi");
        SymbolsMap.put(")", "hægri svigi");
        SymbolsMap.put("-", "bandstrik");
        SymbolsMap.put("'", "úrfellingarmerki");
        SymbolsMap.put("\"", "gæsalappir");
        SymbolsMap.put(":", "tvípunktur");
        SymbolsMap.put(";", "semíkomma");
        SymbolsMap.put(",", "komma");
        SymbolsMap.put("?", "spurningamerki");
    }
}
