package com.grammatek.simaromur;

import android.content.Context;
import android.os.Build;

import androidx.test.core.app.ApplicationProvider;

import com.grammatek.simaromur.frontend.Tokenizer;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(RobolectricTestRunner.class)
// we need this to run the tests explicitly against sdk 28, sdk 29 and 30 need Java 9, we are
// using Java 8
@Config(sdk = {Build.VERSION_CODES.P})

public class TokenizerTest {
    private final static Context context = ApplicationProvider.getApplicationContext();

    @Test
    public void tokenizerTest() {
        Tokenizer tok = new Tokenizer(context);
        String input = "Leikurinn var annar leikur Njarðvíkur í 2. deild karla í knattspyrnu í sumar. Verkin hefur " +
                "hún steypt úr lituðum pappírsmassa, sem hefur gert henni kleyft að nýta sýningarrými - og rými almennt - með nýjum hætti.";
        List<String> tokenized = tok.detectSentences(input);
        assertEquals(2, tokenized.size());
        assertEquals("Leikurinn var annar leikur Njarðvíkur í 2. deild karla í knattspyrnu í sumar .", tokenized.get(0));
        assertEquals("Verkin hefur hún steypt úr lituðum pappírsmassa , sem hefur gert henni kleyft að nýta sýningarrými - og rými almennt - með nýjum hætti .", tokenized.get(1));
    }
}
