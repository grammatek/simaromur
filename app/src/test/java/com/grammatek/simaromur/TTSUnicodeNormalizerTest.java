package com.grammatek.simaromur;

import android.content.Context;
import android.os.Build;

import com.grammatek.simaromur.frontend.TTSUnicodeNormalizer;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import androidx.test.core.app.ApplicationProvider;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
// we need this to run the tests explicitly against sdk 28, sdk 29 and 30 need Java 9, we are
// using Java 8
@Config(sdk = {Build.VERSION_CODES.P})
public class TTSUnicodeNormalizerTest {

    private final Context context = ApplicationProvider.getApplicationContext();

    @Test
    public void unicodeNormalizingTest() {
        TTSUnicodeNormalizer normalizer = new TTSUnicodeNormalizer(context);
        String input = "„ Við vorum samheldnir og þéttir og það er gott að innbyrða sigur á útivelli gegn öflugu liði eins og Breiðabliki , “ sagði Willum Þór Þórsson";
        String normalized = normalizer.normalizeEncoding(input);
        assertEquals("\" Við vorum samheldnir og þéttir og það er gott að innbyrða sigur á útivelli gegn öflugu liði eins og Breiðabliki , \" sagði Willum Þór Þórsson", normalized);

        input = "sem hefur gert henni kleyft að nýta sýningarrými – og rými almennt – með nýjum hætti";
        normalized = normalizer.normalizeEncoding(input);
        assertEquals("sem hefur gert henni kleyft að nýta sýningarrými - og rými almennt - með nýjum hætti", normalized);
    }

    @Test
    public void alphabetNormalizingTest() {
        TTSUnicodeNormalizer normalizer = new TTSUnicodeNormalizer(context);
        List<String> testSent = getTestSent();
        List<String> normalized = normalizer.normalizeAlphabet(testSent);
        for (String s : normalized)
            System.out.println(s);
    }

    private List<String> getTestSent() {
        List<String> sent = new ArrayList<>();
        sent.add("þetta er alíslensk setning");
        sent.add("þessi zetning inniheldur orð með c og w");
        sent.add("þessi setning er í tämu tjåni");
        return sent;
    }
}

