package com.grammatek.simaromur;

import android.content.Context;
import android.os.Build;

import androidx.test.core.app.ApplicationProvider;

import com.grammatek.simaromur.frontend.NormalizationManager;
import com.grammatek.simaromur.frontend.PronDictEntry;
import com.grammatek.simaromur.frontend.Pronunciation;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
// we need this to run the tests explicitly against sdk 28, sdk 29 and 30 need Java 9, we are
// using Java 8
@Config(sdk = {Build.VERSION_CODES.P})

public class NormalizationManagerTest {

    private final static Context context = ApplicationProvider.getApplicationContext();
    private final static Pronunciation pron = new Pronunciation(context);
    private final static Map<String, PronDictEntry> pronDict = pron.GetIpaPronDict();

    @Test
    public void processTest() {
        String input = "Space-X";
        NormalizationManager manager = new NormalizationManager(context, pronDict);
        String processed = manager.process(input);
        System.out.println(processed);

        assertEquals("space - x .",
                processed);
    }

    @Test
    public void processDigitsTest() {
        NormalizationManager manager = new NormalizationManager(context, pronDict);
        for (String sent : getDigits().keySet()) {
            String processed = manager.process(sent);
            assertEquals(getDigits().get(sent), processed);
        }
    }

    @Test
    public void processSymbolsTest() {
        NormalizationManager manager = new NormalizationManager(context, pronDict);
        for (String sent : getSymbols().keySet()) {
            String processed = manager.process(sent);
            assertEquals(getSymbols().get(sent), processed);
        }
    }

    @Test
    public void processNewIssuesTest() {
        NormalizationManager manager = new NormalizationManager(context, pronDict);
        for (String sent : getNewTestSentences().keySet()) {
            String processed = manager.process(sent);
            assertEquals(getNewTestSentences().get(sent), processed);
        }
    }

    @Test
    public void processV14IssuesTest() {
        NormalizationManager manager = new NormalizationManager(context, pronDict);
        for (String sent : getV14TestSentences().keySet()) {
            String processed = manager.process(sent);
            assertEquals(getV14TestSentences().get(sent), processed);
        }
    }

    @Test
    public void processListTest() {
        NormalizationManager manager = new NormalizationManager(context, pronDict);
        for (String sent : getTestSentences().keySet()) {
            String processed = manager.process(sent);
            assertEquals(getTestSentences().get(sent), processed);
        }
    }

    private Map<String, String> getDigits() {
        Map<String, String> digits = new HashMap<>();
        // POS-tagger tags 'mínúta' as accusative, hence the wrong case for 32.
        // should be 'þrítugasta og önnur' (accusative is 'mínútu')
        digits.put("7", "sjö .");
        digits.put("77", "sjötíu og sjö .");
        digits.put("777", "sjö hundruð sjötíu og sjö .");
        digits.put("7777", "sjö þúsund sjö hundruð sjötíu og sjö .");
        digits.put("77777", "sjötíu og sjö þúsund sjö hundruð sjötíu og sjö .");
        digits.put("119273", "hundrað og nítján þúsund tvö hundruð sjötíu og þrjú .");
        digits.put("77.777", "sjötíu og sjö þúsund sjö hundruð sjötíu og sjö .");
        digits.put("119.273", "hundrað og nítján þúsund tvö hundruð sjötíu og þrjú .");
        digits.put("(32. mín)", "<sil> þrítugustu og aðra mínúta <sil> .");
        digits.put("(37. mín)", "<sil> þrítugustu og sjöundu mínúta <sil> .");
        digits.put("(24. mín)", "<sil> tuttugustu og fjórðu mínúta <sil> .");
        digits.put("1.800.000", "ein milljón og átta hundruð þúsund .");
        digits.put(" 10.000.000", "tíu milljónir .");
        digits.put("876.000", "átta hundruð sjötíu og sex þúsund .");
        digits.put("2.350.000", "tvær milljónir þrjú hundruð og fimmtíu þúsund .");
        digits.put("2.350.123", "tvær milljónir þrjú hundruð og fimmtíu þúsund eitt hundrað tuttugu og þrjú .");
        digits.put("2.357.895", "tvær milljónir þrjú hundruð fimmtíu og sjö þúsund átta hundruð níutíu og fimm .");
        digits.put(" 10.000.005", "tíu milljónir og fimm .");
        digits.put(" 10.000.050", "tíu milljónir og fimmtíu .");
        digits.put(" 10.007.050", "tíu milljónir sjö þúsund og fimmtíu .");
        digits.put(" 13.080.050", "þrettán milljónir áttatíu þúsund og fimmtíu .");
        digits.put(" 13.005.025", "þrettán milljónir fimm þúsund tuttugu og fimm .");
        return digits;
    }

    private Map<String, String> getSymbols() {
        Map<String, String> symbols = new HashMap<>();
        symbols.put("+", "plús .");
        symbols.put("=", "jafnt og merki .");
        symbols.put("/", "skástrik .");
        symbols.put("_", "undirstrik .");
        symbols.put("<", "minna en merki .");
        symbols.put("[", "vinstri hornklofi .");
        symbols.put("]", "hægri hornklofi .");
        symbols.put("!", "upphrópunarmerki");
        symbols.put("@", "att merki .");
        symbols.put("#", "myllumerki .");
        symbols.put("$", "dollari .");
        symbols.put("^", "innskotsmerki .");
        symbols.put("&", "og merki .");
        symbols.put("*", "stjarna .");
        symbols.put("(", "vinstri svigi .");
        symbols.put(")", "hægri svigi .");
        symbols.put("-", "bandstrik .");
        symbols.put("'", "úrfellingarmerki .");
        symbols.put("\"", "gæsalappir .");
        symbols.put(":", "tvípunktur");
        symbols.put(";", "semíkomma");
        symbols.put(",", "komma .");
        symbols.put("?", "spurningamerki");
        symbols.put("÷", "deilingar merki .");
        symbols.put("×", "margföldunar merki .");

        return symbols;
    }

    private Map<String, String> getNewTestSentences() {
        Map<String, String> sent = new HashMap<>();
        sent.put("https://www.mbl.is/frettir/", "m b l punktur is skástrik frettir .");
        sent.put("www.karfan.is", "karfan punktur is .");
        sent.put("www.visir.is", "visir punktur is .");
        sent.put("https://vedur.is/skjalftar-og-eldgos/jardskjalftar", "vedur punktur is skástrik skjalftar bandstrik og bandstrik eldgos skástrik jardskjalftar .");
        sent.put("anna@grammatek.com", "anna hjá grammatek punktur com .");
        sent.put("renna út úr dölunum.ELDFJALLAFRÆÐI OG NÁTTÚRUVÁRHÓPUR HÍ", "renna út úr " +
                "dölunum punktur eldfjallafræði og náttúruvárhópur h í .");
        sent.put("kynnast því á dögunum.INSTAGRAM/@anniethorisdottir", "kynnast því á dögunum " +
                "punktur instagram skástrik anniethorisdottir .");
        sent.put("Mörk Ómars á EM:", "mörk ómars á e m :");
        sent.put("til áramóta 2021/2022", "til áramóta tvö þúsund tuttugu og eitt skástrik tvö þúsund tuttugu og tvö .");
        sent.put("© grammatek", "höfundarréttur grammatek .");
        sent.put("+", "plús .");
        sent.put(",", "komma .");
        sent.put("/", "skástrik .");
        sent.put("2", "tveir .");
        sent.put("2023", "tvö þúsund tuttugu og þrjú .");
        sent.put("\uE9104. ágúst 2022 06:42", "fjórða ágúst tvö þúsund tuttugu og tvö núll sex fjörutíu og tvö .");
        sent.put("Link in bio \uD83D\uDC85\uD83C\uDFFC", "link in bio .");
        return sent;
    }

    private Map<String, String> getV14TestSentences() {
        // test sentences added for the deployment of v1.4
        Map<String, String> sent = new HashMap<>();
        sent.put("láta skoðanir sínar í ljós í athugasemdum.“, segir hún.",
                "láta skoðanir sínar í ljós í athugasemdum \" . , segir hún .");
        sent.put("Ôlafsson, framkvæmdastjóri Stakka víkur ehf., segir",
                "ólafsson , framkvæmdastjóri stakka víkur e h f . , segir .");
        sent.put("Stimpilgjald af kaupsamningi er 0,8% af heildarfasteignamati hjá einstaklingum.",
                "stimpilgjald af kaupsamningi er núll komma átta prósent af heildarfasteignamati hjá einstaklingum .");
        sent.put("íbúðin er 145 fm", "íbúðin er hundrað fjörutíu og fimm fermetrar .");
        sent.put("margir með ADHD", "margir með a d h d .");
        sent.put("Má áætla að þriðji stóri íþróttaviðburðurinn sem horft hafi verið til sé EM " +
                        "í handbolta sem fór fram nú í janúar.",
                "má áætla að þriðji stóri íþróttaviðburðurinn sem horft hafi verið til sé e m í " +
                        "handbolta sem fór fram nú í janúar .");
        sent.put("54. gr. sveitastjórnarlaga nr. 138/2011 segir að",
                "fimmtugasta og fjórða grein sveitastjórnarlaga númer hundrað þrjátíu og átta " +
                        "skástrik tvö þúsund og ellefu segir að .");
        sent.put("Brestur á með V 15-22 m/s suðvestanlands um og upp úr hádegi og stendur í " +
                "um 3 klst. ", "brestur á með vestan fimmtán til tuttugu og tveir metrar á " +
                "sekúndu suðvestanlands um og upp úr hádegi og stendur í um þrjár klukkustundir .");
        sent.put("leigubíl fyrir 2.500 krónur [tæpar 33.000 ISK].",
                "leigubíl fyrir tvö þúsund og fimm hundruð krónur tæpar " +
                        "þrjátíu og þrjú þúsund íslenskar krónur .");
        sent.put("leigubíl fyrir £377 á dag.", "leigubíl fyrir þrjú hundruð sjötíu og sjö pund á dag .");
        sent.put("3.7", "þrír punktur sjö .");
        sent.put("13.7", "einn þrír punktur sjö .");
        sent.put("mbl.is/frettir/innlent/2024/02/02/litlu_hlutirnir_sem_folkid_saknar_helst/",
                "m b l punktur is skástrik frettir skástrik innlent skástrik tvö þúsund tuttugu " +
                        "og fjögur skástrik núll tvö skástrik núll tvö skástrik litlu hlutirnir sem " +
                        "folkid saknar helst skástrik .");
        sent.put("Stjórnendur Rúv gera ráð fyrir því að augýsingatekjur stofnunarinnar hækki " +
                "á þessu ári um 17,4% frá fyrra ári og að útvarpsgjald hækki um 3,5%.",
                "stjórnendur rúv gera ráð fyrir því að augýsingatekjur stofnunarinnar hækki " +
                        "á þessu ári um sautján komma fjögur prósent frá fyrra ári og að " +
                        "útvarpsgjald hækki um þrjú komma fimm prósent .");
        return sent;
    }

    private Map<String, String> getTestSentences() {
        Map<String, String> testSentences = new HashMap<>();
        testSentences.put("Í gær greindust 78 með COVID-19",
                "Í gær greindust sjötíu og átta með covid - nítján .".toLowerCase());
        testSentences.put("Í gær greindust 78 með Covid-19",
                "Í gær greindust sjötíu og átta með Covid - nítján .".toLowerCase());
        testSentences.put("Í gær greindust 78 með covid-19",
                "Í gær greindust sjötíu og átta með covid - nítján .".toLowerCase());
        testSentences.put("Í gær greindust 78 með CREW-19",
                "Í gær greindust sjötíu og átta með crew - nítján .".toLowerCase());
        testSentences.put("Í gær greindust 78 með ABCD-19",
                "Í gær greindust sjötíu og átta með a b c d - nítján .".toLowerCase());
        testSentences.put("þetta stóð í 4. gr. laga.",  "þetta stóð í fjórðu grein laga .".toLowerCase());
        testSentences.put("Að jafnaði koma daglega um 48 rútur í Bláa Lónið .",
                "Að jafnaði koma daglega um fjörutíu og átta rútur í Bláa Lónið .".toLowerCase());
        // should be sport domain, for now we can only set domain=sport manually (where the hyphen
        // is replaced with a space instead of "til")
        testSentences.put("Áfram hélt fjörið í síðari hálfleik og þegar 3. leikhluti var tæplega hálfnaður var staðan 64-52 .",
                "Áfram hélt fjörið í síðari hálfleik og þegar þriðji leikhluti var tæplega hálfnaður var staðan sextíu og fjögur til fimmtíu og tvö .".toLowerCase());
        testSentences.put("Viðeignin fer fram í sal FS og hefst klukkan 20 .",
                "Viðeignin fer fram í sal f s og hefst klukkan tuttugu .".toLowerCase()); // spelling error!
        testSentences.put("Annars var verði 3500 fyrir tímann !",
                "Annars var verði þrjú þúsund og fimm hundruð fyrir tímann !".toLowerCase()); // spelling error
        testSentences.put("Af því tilefni verða kynningar um allt land á hinum ýmsu deildum innann SL þriðjudaginn 18. janúar nk. ",
                "Af því tilefni verða kynningar um allt land á hinum ýmsu deildum innann s l þriðjudaginn átjánda janúar næstkomandi .".toLowerCase());
        //testSentences.put("„ Ég kíki daglega á facebook , karfan.is , vf.is , mbl.is , kkí , og utpabroncs.com . “ ",
        //        "\" Ég kíki daglega á facebook , karfan punktur is , v f punktur is , m b l punktur is , k k í , og utpabronks punktur com . \"".toLowerCase());
        testSentences.put("Arnór Ingvi Traustason 57. mín. Jónas Guðni, sem er 33 ára, hóf fótboltaferil sinn árið 2001.",
                "Arnór Ingvi Traustason fimmtugasta og sjöunda mínúta . Jónas Guðni , sem er þrjátíu og þriggja ára , hóf fótboltaferil sinn árið tvö þúsund og eitt .".toLowerCase());
        // correct version impossible, since the tagger tags "lóð" and "byggingarreit" as dative, causing "í einni lóð og einum byggingarreit" (regina original does that as well)
        //testSentences.put("Einnig er gert ráð fyrir að sameina lóðir og byggingarreiti við Sjávarbraut 1 - 7 í 1 lóð og 1 byggingarreit.   Miðaverð fyrir fullorðna kr. 5500 ",
        //        "Einnig er gert ráð fyrir að sameina lóðir og byggingarreiti við Sjávarbraut eitt til sjö í eina lóð og einn byggingarreit . Miðaverð fyrir fullorðna krónur fimm þúsund og fimm hundruð .");
        testSentences.put("Einnig er gert ráð fyrir að sameina lóðir og byggingarreiti við Sjávarbraut 1 - 7 í 1 lóð og 1 byggingarreit.   Miðaverð fyrir fullorðna kr. 5500 ",
                "Einnig er gert ráð fyrir að sameina lóðir og byggingarreiti við Sjávarbraut eitt til sjö í einni lóð og einum byggingarreit . Miðaverð fyrir fullorðna krónur fimm þúsund og fimm hundruð .".toLowerCase());
        testSentences.put("Stolt Sea Farm (SSF), fiskeldisarmur norsku skipa- og iðnaðarsamstæðunnar Stolt-Nielsen, jók sölu sína á flatfiski um 53% á öðrum ársfjórðungi 2015.",
                "Stolt Sea Farm <sil> s s f <sil> , fiskeldisarmur norsku skipa - og iðnaðarsamstæðunnar Stolt - Nielsen , jók sölu sína á flatfiski um fimmtíu og þrjú prósent á öðrum ársfjórðungi tvö þúsund og fimmtán .".toLowerCase());
        testSentences.put("Í janúarbyrjun 1983 var stofnað nýtt hlutafélag, Víkurféttir ehf. sem tók við.",
                "Í janúarbyrjun nítján hundruð áttatíu og þrjú var stofnað nýtt hlutafélag , Víkurféttir e h f sem tók við .".toLowerCase());
        testSentences.put("Jarðskjálfti að stærð 3,9 varð fyrir sunnan Kleifarvatn kl. 19:50 í gærkvöldi.",
                "Jarðskjálfti að stærð þrír komma níu varð fyrir sunnan Kleifarvatn klukkan nítján fimmtíu í gærkvöldi .".toLowerCase());
        // can't interpret "söfnuðu krónum", same error in regina original
        //testSentences.put("Stelpurnar Carmen Diljá Guðbjarnardóttir og Elenora Rós Georgsdóttir söfnuðu 7.046 kr.",
        //        "Stelpurnar Carmen Diljá Guðbjarnardóttir og Elenora Rós Georgsdóttir söfnuðu sjö þúsund fjörutíu og sex krónum .");
        testSentences.put("Stelpurnar Carmen Diljá Guðbjarnardóttir og Elenora Rós Georgsdóttir söfnuðu 7.046 kr.",
                "Stelpurnar Carmen Diljá Guðbjarnardóttir og Elenora Rós Georgsdóttir söfnuðu sjö þúsund fjörutíu og sex krónur .".toLowerCase());
        testSentences.put("Hann skoraði 21 stig og tók 12 fráköst.", "Hann skoraði tuttugu og eitt stig og tók tólf fráköst .".toLowerCase());
        testSentences.put("Opna Suðurnesjamótið í pílu fer fram þann 4. desember nk. kl. 13:00 í píluaðstöðu Pílufélags Reykjanesbæjar að Hrannargötu 6. ",
                "Opna Suðurnesjamótið í pílu fer fram þann fjórða desember næstkomandi klukkan þrettán núll núll ".toLowerCase() +
                        "í píluaðstöðu Pílufélags Reykjanesbæjar að Hrannargötu sex .".toLowerCase());
        testSentences.put("Karlar eru rétt innan við 2% hjúkrunarfræðinga á Íslandi",
                "Karlar eru rétt innan við tvö prósent hjúkrunarfræðinga á Íslandi .".toLowerCase());
        // should be "sjö fimm sjö vélarnar" (same error in original regina)
        testSentences.put("Lendingarnar eru hlutfallslega svo fáar að elstu 757 -vélarnar eru aðeins hálfnaðar hvað líftíma varðar",
                "Lendingarnar eru hlutfallslega svo fáar að elstu sjö hundruð fimmtíu og sjö - vélarnar eru aðeins hálfnaðar hvað líftíma varðar .".toLowerCase());
        testSentences.put("Á samanlögðu svæði Vesturlands og Vestfjarða voru 4.400 gistinætur sem jafngildir 7,5% fækkun milli ára.",
                "Á samanlögðu svæði Vesturlands og Vestfjarða voru fjögur þúsund og fjögur hundruð gistinætur sem jafngildir sjö komma fimm prósent fækkun milli ára .".toLowerCase());
        testSentences.put("Elvar skilaði 22 stigum, þar af 5 af 8 í þriggjastiga.",
                "Elvar skilaði tuttugu og tveimur stigum , þar af fimm af átta í þriggjastiga .".toLowerCase());
        // not possible to get correct looking at next tag, depends on "keyri", direct object in acc
        //testSentences.put("Ég keyri 2000 km á mánuði til og frá vinnu, þetta eru 20 - 25 stundir.",
        //        "Ég keyri tvö þúsund kílómetra á mánuði til og frá vinnu , þetta eru tuttugu til tuttugu og fimm stundir .");
        testSentences.put("Ég keyri 2000 km á mánuði til og frá vinnu, þetta eru 20 - 25 stundir.",
                "Ég keyri tvö þúsund kílómetrar á mánuði til og frá vinnu , þetta eru tuttugu til tuttugu og fimm stundir .".toLowerCase());
        // not possible tu get correct looking at next tag, depends on "Áfangastaðir". Also: should have a dictionary of
        // uppercase token not to separate, like "WOW"
        //testSentences.put("Áfangastaðir WOW air eru nú 31 talsins, 23 innan Evrópu en 8 talsins í Norður Ameríku.",
        //        "Áfangastaðir WOW air eru nú þrjátíu og einn talsins , tuttugu og þrír innan Evrópu en átta talsins í Norður Ameríku .");
        testSentences.put("Áfangastaðir WOW air eru nú 31 talsins, 23 innan Evrópu en 8 talsins í Norður Ameríku.",
                "Áfangastaðir wow air eru nú þrjátíu og eins talsins , tuttugu og þrjú innan Evrópu en átta talsins í Norður Ameríku .".toLowerCase());
        testSentences.put("Fyrstu félögin í Danmörku, Noregi og Svíþjóð 1919 og Norræna félagið á Íslandi 29. september árið 1922 .",
                "Fyrstu félögin í Danmörku , Noregi og Svíþjóð nítján hundruð og nítján og Norræna félagið á Íslandi ".toLowerCase() +
                        "tuttugasta og níunda september árið nítján hundruð tuttugu og tvö .".toLowerCase());
        testSentences.put("Vindmyllurnar eru hvor um sig 900 kW og samanlögð raforkuframleiðsla þeirra er áæetluð um 5,4 GWst á ári.",
                "Vindmyllurnar eru hvor um sig níu hundruð kílóvött og samanlögð raforkuframleiðsla þeirra er áæetluð um ".toLowerCase() +
                        "fimm komma fjórar Gígavattstundir á ári .".toLowerCase());
        // cannot get "ha" correct (reisa + acc) or "fm" (default: fermetrar, next tag: ')' ) regina does the same
        //testSentences.put("Hollenska fjárfestingafyrirtækið EsBro hyggst reisa 15 ha (150.000 m²) gróðurhús til framleiðslu á tómötum",
        //        "Hollenska fjárfestingafyrirtækið EsBro hyggst reisa fimmtán hektara ( hundrað og fimmtíu þúsund fermetra ) gróðurhús til framleiðslu á tómötum");
        //testSentences.put("Hollenska fjárfestingafyrirtækið EsBro hyggst reisa 15 ha (150.000 m²) gróðurhús.",
        //        "Hollenska fjárfestingafyrirtækið EsBro hyggst reisa fimmtán hektarar <sil> hundrað og fimmtíu þúsund fermetrar <sil> gróðurhús .".toLowerCase());
        testSentences.put("Hollenska fjárfestingafyrirtækið EsBro hyggst reisa 15 ha (150.000 fm) gróðurhús til framleiðslu á tómötum",
                "Hollenska fjárfestingafyrirtækið EsBro hyggst reisa fimmtán hektarar <sil> hundrað og fimmtíu þúsund fermetrar <sil> gróðurhús til framleiðslu á tómötum .".toLowerCase());
        //testSentences.put("Við Lindarhvamm í Hafnarfirði er að finna 134 fm efri sérhæð og ris í snyrtilegu tvíbýlishúsi sem reist var árið 1963.",
        //        "við lindarhvamm í hafnarfirði er að finna hundrað þrjátíu og fjögur fermetrar efri sérhæð og ris í snyrtilegu tvíbýlishúsi sem reist var árið nítján hundruð sextíu og þrjú .".toLowerCase());
        testSentences.put("Mynd / elg@vf.is", "Mynd skástrik elg hjá v f punktur is .".toLowerCase());
        testSentences.put("hefur leikið sjö leiki með U-21 árs liðinu.", "hefur leikið sjö leiki með U - tuttugu og eins árs liðinu .".toLowerCase());
        testSentences.put("er þetta í 23. skiptið sem mótið er haldið .", "er þetta í tuttugasta og þriðja skiptið sem mótið er haldið .".toLowerCase());
        testSentences.put("Skráning er hafin á http://keflavik.is/fimleikar/ og ef eitthvað er óljóst er hægt að hafa samband í síma 421-6368 eða á fimleikar@keflavik.is",
                "Skráning er hafin á keflavik punktur is skástrik ".toLowerCase()  +
                        "fimleikar og ef eitthvað er óljóst er hægt að hafa samband í síma fjórir tveir einn- sex þrír sex átta eða á fimleikar hjá keflavik punktur is .".toLowerCase());
        testSentences.put("Austlæg átt, 5-13 m/s síðdegis.", "Austlæg átt , fimm til þrettán metrar á sekúndu síðdegis .".toLowerCase());
        testSentences.put("hlutfallið á Vestfjörðum þar sem 14,1% íbúa eru innflytjendur",
                "hlutfallið á Vestfjörðum þar sem fjórtán komma eitt prósent íbúa eru innflytjendur .".toLowerCase());

        // TODO: these have problems
        //testSentences.put("Hann bætti Íslandsmet sitt í 5.000 m kappakstri um 1 mín.",
        //        "Hann bætti Íslandsmet sitt í fimm þúsund metra kappakstri um eina mínúta .".toLowerCase());
        // both we and regina make this error with "mínútu" instead of "mínútur"
        //testSentences.put("Hann bætti Íslandsmet sitt í 5.000 m kappakstri um 12 mín.",
        //        "Hann bætti Íslandsmet sitt í fimm þúsund metra kappakstri um tólf mínútur .".toLowerCase());
        testSentences.put("Það er rúmlega 93 þús km",
                "Það er rúmlega níutíu og þrjú þúsund kílómetrar .".toLowerCase());
        testSentences.put("gjörgæslurúm per hundrað þúsund íbúa", "gjörgæslurúm per hundrað þúsund íbúa .");
        // TODO: fix this now that we don't spell out all upper case strings per default
        //testSentences.put("C4CE6358DF86040CAAEEBC58951B8E133105D3369980D62D109E5B6B75CCE67C_713x0",
        //        "c fjögur c e sex þúsund þrjú hundruð fimmtíu og átta d f átta sex núll fjórir núll c a a e e b c fimm átta níu fimm einn b átta e einn þrír þrír einn núll fimm d þrír þrír sex níu níu átta núll d sextíu og tvö d hundrað og níu e fimm b sex b sjötíu og fimm c c e sextíu og sjö c sjö hundruð og þrettán x núll ."
        //);
        testSentences.put("Mörk Ómars á EM:", "mörk ómars á e m :");
        testSentences.put("til áramóta 2021/2022", "til áramóta tvö þúsund tuttugu og eitt skástrik tvö þúsund tuttugu og tvö .");
        testSentences.put("© grammatek", "höfundarréttur grammatek .");
        testSentences.put("Álfur P er bestur", "álfur p er bestur .");
        testSentences.put("Það bíður pk eftir þér", "það bíður pakki eftir þér .");
        testSentences.put("sjö ,p,k,r,s", "sjö , p , k , r , s .");

        return testSentences;
    }
}
