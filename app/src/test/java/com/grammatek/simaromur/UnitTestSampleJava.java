package com.grammatek.simaromur;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;


import com.grammatek.simaromur.frontend.Tokenizer;
import com.grammatek.simaromur.frontend.TTSUnicodeNormalizer;

import java.util.List;

@RunWith(RobolectricTestRunner.class)
public class UnitTestSampleJava {
    private final Context context = ApplicationProvider.getApplicationContext();
    private final String str1 = "Viltu gera grín að mér?";
    private final String str2 = "Rúmir fimm milljarðar króna hafa bæst við"
        + " heil darrekstrarkostnað júkrunarheimila á Íslandi ef borin eru"
        + " saman árin 2021 og 2019. Fjöldi hjúkrunarheimila er í afar bágri"
        + " fjárhagsstöðu, meðal annars vegna launahækkana sem enn ekki"
        + " hefur fengist aukið framlag frá ríkinu til að greiða fyrir.";

    // @todo: FrontendManager needs G2P, which loads a native lib => bang!
    //        Only possible to use this in AndroidTest. But other components
    //        like Tokenizer/TTSUnicodeNormalizer, etc. can be tested here.

    /**
     * Test Frontend Manager.
     */
    @Test
    public void testFrontendManager() {
        //FrontendManager myFrontendManager = new FrontendManager(context);
        //String sampa = myFrontendManager.process("Viltu gera grín að mér?");
    }

    /**
     * Test Unicode Normalization.
     */
    @Test
    public void testUnicodeNormalization1() {
        TTSUnicodeNormalizer myTtsUnicodeNormalizer = new TTSUnicodeNormalizer();
        String nStr1 = myTtsUnicodeNormalizer.normalize_encoding(str1);
        // tbc ...
    }

    /**
     * Test tokenizer
     */
    @Test
    public void testTokenizer1() {
        Tokenizer myTokenizer = new Tokenizer(context);
        List<String> strings = myTokenizer.detectSentences(str2);
        // tbc ...
    }
}
