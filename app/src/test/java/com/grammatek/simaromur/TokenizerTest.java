package com.grammatek.simaromur;

import com.grammatek.simaromur.frontend.Tokenizer;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class TokenizerTest {
    //TODO: mockup context
   /* @Test
    public void tokenizerTest() {
        Tokenizer tok = new Tokenizer();
        String input = "Leikurinn var annar leikur Njarðvíkur í 2. deild karla í knattspyrnu í sumar. Verkin hefur " +
                "hún steypt úr lituðum pappírsmassa, sem hefur gert henni kleyft að nýta sýningarrými - og rými almennt - með nýjum hætti.";
        List<String> tokenized = tok.detectSentences(input);
        assertEquals(2, tokenized.size());
        assertEquals("Leikurinn var annar leikur Njarðvíkur í 2. deild karla í knattspyrnu í sumar .", tokenized.get(0));
        assertEquals("Verkin hefur hún steypt úr lituðum pappírsmassa , sem hefur gert henni kleyft að nýta sýningarrými - og rými almennt - með nýjum hætti .", tokenized.get(1));
    } */
}