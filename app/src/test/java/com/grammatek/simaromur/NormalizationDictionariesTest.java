package com.grammatek.simaromur;

import com.grammatek.simaromur.frontend.NormalizationDictionaries;
import com.grammatek.simaromur.frontend.TTSNormalizer;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * A test class to test single regexes and their replacement patterns
 */
public class NormalizationDictionariesTest {

    @Test
    public void preHelpDictTest() {
        TTSNormalizer normalizer = new TTSNormalizer();
        String input = "Þetta var í 4ða sinn";
        String normalized = normalizer.replaceFromDict(input, NormalizationDictionaries.preHelpDict);
        System.out.println(normalized);
        assertEquals("Þetta var í fjórða sinn", normalized);
        input = "abcd40";
        normalized = normalizer.replaceFromDict(input, NormalizationDictionaries.preHelpDict);
        System.out.println(normalized);
        assertEquals("abcd 40", normalized);
        input = "5a°";
        normalized = normalizer.replaceFromDict(input, NormalizationDictionaries.preHelpDict);
        System.out.println(normalized);
        assertEquals("5 a °", normalized);
        input = "02.11.1987";
        normalized = normalizer.replaceFromDict(input, NormalizationDictionaries.preHelpDict);
        System.out.println(normalized);
        assertEquals("02. 11. 1987", normalized);
        input = "þetta var 02.11. hélt hann";
        normalized = normalizer.replaceFromDict(input, NormalizationDictionaries.preHelpDict);
        System.out.println(normalized);
        assertEquals("þetta var 02. 11. hélt hann", normalized);
        input = "en hvað með 123 4567";
        normalized = normalizer.replaceFromDict(input, NormalizationDictionaries.preHelpDict);
        System.out.println(normalized);
        assertEquals("en hvað með 123-4567", normalized);
    }

    @Test
    public void directionAndHyphenDictTest() {
        TTSNormalizer normalizer = new TTSNormalizer();
        String input = "rigning S-til";
        String normalized = normalizer.replaceFromDict(input, NormalizationDictionaries.directionDict);
        System.out.println(normalized);
        assertEquals("rigning sunnantil", normalized);
        input = "abcdEF-GHH";
        normalized = normalizer.replaceFromDict(input, NormalizationDictionaries.hyphenDict);
        System.out.println(normalized);
        assertEquals("abcdEF- GHH", normalized);
        input = "ABCD-EFghj";
        normalized = normalizer.replaceFromDict(input, NormalizationDictionaries.hyphenDict);
        System.out.println(normalized);
        assertEquals("ABCD -EFghj", normalized);
    }
    @Test
    public void abbreviationDictTest() {
        TTSNormalizer normalizer = new TTSNormalizer();
        String input = "innan við 1,5-2°C á þessari öld";
        String normalized = normalizer.replaceFromDict(input, NormalizationDictionaries.abbreviationDict);
        System.out.println(normalized);
        assertEquals("innan við 1,5-2 gráður selsíus á þessari öld", normalized);
        input = "það voru a.m.k. þúsund manns";
        normalized = normalizer.replaceFromDict(input, NormalizationDictionaries.abbreviationDict);
        System.out.println(normalized);
        assertEquals("það voru að minnsta kosti þúsund manns", normalized);

    }


}

/*
static {

        preHelpDict.put(BOS + "(0?[1-9]|[12]\\d|3[01])\\.(0?[1-9]|1[012])\\.(\\d{3,4})" + EOS, " x1x2x. x3x. x4x5");
        preHelpDict.put(BOS + "(0?[1-9]|[12]\\d|3[01])\\.(0?[1-9]|1[012])\\." + EOS, " x1x2x. x3x.x4");
        // what does that stand for?
        preHelpDict.put("(\\d{3})( )(\\d{4})", " x1x-x3x");
    }
 */

