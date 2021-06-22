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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        for (String sent : getTestSentences().keySet()) {
            List<String> processed = normalizer.normalizeAlphabet(Arrays.asList(sent));
            assertEquals(getTestSentences().get(sent), processed.get(0));
        }
    }

    private Map<String, String> getTestSentences() {
        Map<String, String> sent = new HashMap<>();
        sent.put("þetta er alíslensk setning", "þetta er alíslensk setning");
        sent.put("þessi zetning inniheldur cunning words orð með c og w", "þessi setning inniheldur kunning words orð með c og w");
        sent.put("þessi setning er í tämu tjåni", "þessi setning er í temu tjoni");
        return sent;
    }
}

