package com.grammatek.simaromur.device;

import android.util.Log;

import java.util.Arrays;
import java.util.HashMap;

/**
 * Symbol mappings used for training Icelandic voices. These are used at Reykjavik University
 * Language & Voice Lab for Icelandic (LvL IS), where our voices are trained.
 */
public class SymbolsLvLIs {
    private final static String LOG_TAG = "Simaromur_" + SymbolsLvLIs.class.getSimpleName();

    // Type of symbol table
    public enum Type {
        TYPE_SAMPA,     // Speech Assessment Methods Phonetic Alphabet
        TYPE_IPA        // International Phonetic Alphabet
    }

    // These symbols map phonemes to training input vectors. The order of symbols is absolutely
    // critical and should never be changed without making sure that the trained model corresponds.
    // Mind off that all but the last Symbol variable parts should end with a SPACE, so that these
    // can be concatenated and split correctly. Some symbols are prefixed with "@", for that
    // they are never matched with normalized output/IPA/Sampa.

    // Punctuation characters. Most are not relevant, but some of these are output by G2P, e.g. "."
    private final static String SymbolsPunctUnused = "@_ @- @! @' @( @) @, @. @: @; @? @SPC ";

    // These unused symbols (of an EN alphabet) are prefixed with "@"
    private final static String SymbolsLettersUnused = "@A @B @C @D @E @F @G @H @I @J @K @L @M @N @O @P @Q" +
            " @R @S @T @U @V @W @X @Y @Z @a @b @c @d @e @f @g @h @i @j @k @l @m @n @o @p @q @r @s @t" +
            " @u @v @w @x @y @z ";

    private final static String SymbolsIPA = "a aː ai aiː au auː c ç cʰ ð ei eiː ɛ" +
            " ɛː f ɣ h i iː ɪ ɪː j k kʰ l l̥ m m̥ n n̥ ɲ ɲ̊ ŋ ŋ̊ œ œː œy œyː" +
            " ou ouː ɔ ɔː ɔi p pʰ r r̥ s t tʰ u uː v x ʏ ʏː ʏi θ ";

    private final static String SymbolsSampa = "a a: ai ai: au au: c C c_h D ei ei: E" +
            " E: f G h i i: I I: j k k_h l l_0 m m_0 n n_0 J J_0 N N_0 9 9: 9i 9i:" +
            " ou ou: O O: Oi p p_h r r_0 s t t_h u u: v x Y Y: Yi T ";

    // special symbols, prefixed with "§"
    public final static String SymbolShortPause = "§sp";
    public final static String SymbolSpokenNoise = "§spn";
    public final static String SymbolSilence = "§sil";
    private final static String SymbolsSpecial = SymbolShortPause + " " + SymbolSpokenNoise + " " +
            SymbolSilence;

    // tags
    public final static String TagPause = "<sil>";

    // IPA symbols as HashMap
    private static final HashMap<String, Integer> IPASymbolMap;
    static {
        final String[] IPASymbols = (SymbolsPunctUnused + SymbolsLettersUnused +
                SymbolsIPA + SymbolsSpecial).split(" ");
        HashMap<String, Integer> aMap = new HashMap<>();
        for (int i=0; i<IPASymbols.length; ++i) {
            aMap.put(IPASymbols[i], i);
        }
        IPASymbolMap = aMap;
    }

    // SAMPA symbols as HashMap
    private static final HashMap<String, Integer> SampaSymbolMap;
    static {
        final String[] SampaSymbols = (SymbolsPunctUnused + SymbolsLettersUnused +
                SymbolsSampa + SymbolsSpecial).split(" ");
        HashMap<String, Integer> aMap = new HashMap<>();
        for (int i=0; i<SampaSymbols.length; ++i) {
            aMap.put(SampaSymbols[i], i);
        }
        SampaSymbolMap = aMap;
    }

    // Maps given symbol to labels/ids used in the model
    public static int[] MapSymbolsToVec(Type type, String symbolSequence) {
        final String[] symbolList = symbolSequence.split(" ");
        int[] sampa2VecInput = new int[symbolList.length];
        for (int n = 0; n < symbolList.length; n++)
        {
            if (type == Type.TYPE_SAMPA) {
                sampa2VecInput[n] = MapSampaToInt(symbolList[n]);
            } else {
                sampa2VecInput[n] = MapIPAToInt(symbolList[n]);
            }
        }
        Log.d(LOG_TAG, "Symbols(" + sampa2VecInput.length + "): " + Arrays.toString(sampa2VecInput));
        return sampa2VecInput;
    }

    // Maps given ipa to labels/id used in the model
    private static int MapIPAToInt(String sampa) {
        Integer order = -1;
        order = IPASymbolMap.get(sampa);
        if (order == null) {
            Log.e(LOG_TAG, "MapIPAToInt(): Unknown symbol: " + sampa);
            order = MapIPAToInt(SymbolShortPause);
        }
        return order;
    }

    // Maps given sampa to labels/id used in the model
    private static int MapSampaToInt(String sampa) {
        Integer order = -1;
        order = SampaSymbolMap.get(sampa);
        if (order == null) {
            Log.e(LOG_TAG, "MapSampaToInt(): Unknown symbol: " + sampa);
            order = MapSampaToInt(SymbolShortPause);
        }
        return order;
    }
}
