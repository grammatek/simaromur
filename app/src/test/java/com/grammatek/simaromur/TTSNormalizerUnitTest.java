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

        input = "Breytileg átt SA-lands seinnipartinn .";
        normalized = normalizer.normalize(input);
        System.out.println(normalized);
        assertEquals("Breytileg átt suðaustanlands seinnipartinn .", normalized);

        input = "Núverandi leiguverð á þorskkvóta er lægst 175 kr/kg og hæst 230 kr. .";
        normalized = normalizer.normalize(input);
        System.out.println(normalized);
        assertEquals("Núverandi leiguverð á þorskkvóta er lægst 175 krónur á kílóið og hæst 230 krónur .", normalized);

        input = "kúlulaga með 70 cm þvermál";
        normalized = normalizer.normalize(input);
        System.out.println(normalized);
        assertEquals("kúlulaga með 70 sentimetra þvermál", normalized);

        input = "Hollenska fjárfestingafyrirtækið EsBro hyggst reisa 15 ha ( 150.000 m² ) gróðurhús til framleiðslu á tómötum , \" segir á vefsíðu Sambands garðyrkjubænda .";
        normalized = normalizer.normalize(input);
        System.out.println(normalized);
        // TODO: why is "reisa 15 ha" normalized to "reisa 15 hektarar" - is this corrected later in the process?
        assertEquals("Hollenska fjárfestingafyrirtækið EsBro hyggst reisa 15 hektarar ( 150.000 fermetrar ) gróðurhús til framleiðslu á tómötum , \" segir á vefsíðu Sambands garðyrkjubænda .", normalized);

        input = "120 g smjör ( eða 1 dl kókosolía ) ";
        normalized = normalizer.normalize(input);
        System.out.println(normalized);
        assertEquals("120 grömm smjör ( eða 1 desilítri kókosolía ) ", normalized);

        input = "að meðaltali 46 klst á viku";
        normalized = normalizer.normalize(input);
        System.out.println(normalized);
        assertEquals("að meðaltali 46 klukkustundir á viku", normalized);

        input = "báðir undir 20 sek.";
        normalized = normalizer.normalize(input);
        System.out.println(normalized);
        assertEquals("báðir undir 20 sekúndum", normalized);

        input = "Þar verður líka selt kaffi og vöfflur með rjóma á kr. 300 .";
        normalized = normalizer.normalize(input);
        System.out.println(normalized);
        assertEquals("Þar verður líka selt kaffi og vöfflur með rjóma á krónur 300 .", normalized);

        input = "Þar af fari 85 MW til Kísilverksmiðju í Ölfus";
        normalized = normalizer.normalize(input);
        System.out.println(normalized);
        assertEquals("Þar af fari 85 Megavött til Kísilverksmiðju í Ölfus", normalized);


        //Flokkurinn mælist með 11% fylgi nú en var með um 5% fylgi
    }
}
