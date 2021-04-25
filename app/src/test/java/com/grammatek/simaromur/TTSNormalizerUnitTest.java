package com.grammatek.simaromur;

import com.grammatek.simaromur.frontend.TTSNormalizer;

import org.junit.Test;

import static org.junit.Assert.*;

public class TTSNormalizerUnitTest {

    @Test
    public void normalizingTest() {
        TTSNormalizer normalizer = new TTSNormalizer();
        String normalized = normalizer.normalize("þetta stóð í 4. gr. laga");
        System.out.println(normalized);
        assertEquals("þetta stóð í 4. grein laga", normalized);

        String input = "ríkissjóður vel í stakk búinn fyrir [ mikinn hallarekstur, innsk. blm. ]";
        normalized = normalizer.normalize(input);
        System.out.println(normalized);
        assertEquals("ríkissjóður vel í stakk búinn fyrir [ mikinn hallarekstur, innskot blaðamanns ]", normalized);

        input = "það var 2ja sæta sófi , keyptur hjá YZ-ÞÆÖ";
        normalized = normalizer.normalize(input);
        System.out.println(normalized);
        assertEquals("það var tveggja sæta sófi , keyptur hjá YZ- ÞÆÖ", normalized);
    }
}
