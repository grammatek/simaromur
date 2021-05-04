package com.grammatek.simaromur.frontend;

import java.util.HashMap;
import java.util.Map;

/**
 * Normalization dictionaries for abbreviations, digits and other non-standard-words.
 * The final dictionaries will be stored in external text files, but for preliminary unit-testing
 * having a selection of the patterns here helps.
 */

public class NormalizationDictionaries {

    public static String ACC = "ACC"; // always acc
    public static String DAT = "DAT"; // always dat
    public static String GEN = "GEN"; // always gen
    public static String ACC_DAT = "ACC_DAT"; // acc or dat
    public static String ACC_GEN = "ACC_GEN"; // ACC + GEN
    public static String ACC_DAT_COMB = "ACC_DAT_COMB"; // ACC + DAT + ACC_DAT
    public static String ACC_DAT_GEN_COMB = "ACC_DAT_GEN_COMB"; // ACC + DAT + ACC_DAT + GEN
    public static String AMOUNT = "AMOUNT";

    public static Map<String, String> prepositions = new HashMap<String, String>() {{
        put(ACC, "um(fram|hverfis)|um|gegnum|kringum|við|í|á");
        put(DAT, "frá|a[ðf]|ásamt|gagnvart|gegnt?|handa|hjá|með(fram)?|móti?|undan|nálægt");
        put(GEN, "til|auk|án|handan|innan|meðal|megin|milli|ofan|sakir|sökum|utan|vegna");
        put(ACC_DAT, "eftir|fyrir|með|undir|við|yfir");
        put(ACC_GEN, "um(fram|hverfis)|um|gegnum|kringum|við|í|á|til|auk|án|handan|innan|meðal|megin|milli|ofan|sakir|sökum|utan|vegna");
        put(ACC_DAT_COMB, "um(fram|hverfis)|um|gegnum|kringum|við|í|á|frá|a[ðf]|ásamt|gagnvart|gegnt?|handa|hjá|með(fram)?|móti?|undan|nálægt|eftir|fyrir|með|undir|við|yfir");
        put(ACC_DAT_GEN_COMB, "um(fram|hverfis)|um|gegnum|kringum|við|í|á|frá|a[ðf]|ásamt|gagnvart|gegnt?" +
                "|handa|hjá|með(fram)?|móti?|undan|nálægt|eftir|fyrir|með|undir|við|yfir|til|auk|án|handan|innan|meðal|megin|milli|ofan|sakir|sökum|utan|vegna");
    }};

    public static Map<String, String> patternSelection = new HashMap<String, String>() {{
       put(AMOUNT, "hundr[au]ð|þúsund|milljón(ir)?");
    }};

    public static Map<String, String> preHelpDict = new HashMap<String, String>() {{
        put("(\\W|^)(?i)2ja(\\W|$)", "1xtveggjax2");
        put("(\\W|^)(?i)3ja(\\W|$)", "1xþriggjax2");
        // original has no 'u', why?
        put("(\\W|^)(?i)4ð(a|i|u)(\\W|$)", "1xfjórðx2x3");
        put("(\\W|^)(?i)5t(a|i|u)(\\W|$)", "1xfimmtx2x3");
        put("(\\W|^)(?i)6t(a|i|u)(\\W|$)", "1xsjöttx2x3");
        put("(\\W|^)(?i)7d(a|i|u)(\\W|$)", "1xsjöundx2x3");
        put("(\\W|^)(?i)8d(a|i|u)(\\W|$)", "1xáttundx2x3");
        put("(\\W|^)(?i)9d(a|i|u)(\\W|$)", "1xníundx2x3");

        put("(?i)([a-záðéíóúýþæö]+)(\\d+)", "1x x2");
        put("(?i)(\\d+)([a-záðéíóúýþæö]+)", "1x x2");
        // the following two patterns are originally in the prehelpdict - extract because they
        // don't contain any digits. let's check the digits first.
        //put("(\\W|^)([A-ZÁÐÉÍÓÚÝÞÆÖ]+)(\\-[A-ZÁÐÉÍÓÚÝÞÆÖa-záðéíóúýþæö]+)(\\W|$)", "1x2x x3x4");
        //put("(\\W|^)([A-ZÁÐÉÍÓÚÝÞÆÖa-záðéíóúýþæö]+\\-)([A-ZÁÐÉÍÓÚÝÞÆÖ]+)(\\W|$)", "1x2x x3x4");
        // what are these? degrees and percent with letters?
        //put("(?i)([\\da-záðéíóúýþæö]+)(°)", "1x x2");
        //put("(?i)([\\da-záðéíóúýþæö]+)(%)", "1x x2");
        // dates: why would we handle that here already?
        //put("(\\W|^)(0?[1-9]|[12]\\d|3[01])\\.(0?[1-9]|1[012])\\.(\\d{3,4})(\\W|$)", " x1x2x. x3x. x4x5");
        //put("(\\W|^)(0?[1-9]|[12]\\d|3[01])\\.(0?[1-9]|1[012])\\.(\\W|$)", " x1x2x. x3x.x4");
        // what does that stand for?
        //put("(\\d{3})( )(\\d{4})", " x1x-x3x");
    }};

    public static Map<String, String> hyphenDict = new HashMap<String, String>() {{
        put("(\\W|^)([A-ZÁÐÉÍÓÚÝÞÆÖ]+)(\\-[A-ZÁÐÉÍÓÚÝÞÆÖa-záðéíóúýþæö]+)(\\W|$)", "1x2x x3x4");
        put("(\\W|^)([A-ZÁÐÉÍÓÚÝÞÆÖa-záðéíóúýþæö]+\\-)([A-ZÁÐÉÍÓÚÝÞÆÖ]+)(\\W|$)", "1x2x x3x4");
    }};

    public static Map<String, String> abbreviationDict = new HashMap<String, String>() {{
            put("(\\d+\\.) gr\\.(\\W|$)", "1x greinx2");
            put("(\\d+\\.) mgr\\.(\\W|$)", "1x málsgrein2");
            put("(\\W|^)[Ii]nnsk\\. (blm\\.|blaðamanns)(\\W|$)", "1xinnskot x2");
            put("(\\W|^)([Ii]nnsk(\\.|ot) )(blm\\.)(\\W|$)", "1x2xblaðamanns x5");
            put("([Ff]\\.[Kk]r\\.?)(\\W|$) ", "fyrir Kristx2");
            put("([Ee]\\.[Kk]r\\.?)(\\W|$) ", "eftir Kristx2");
            put("(\\W|^)([Cc]a|CA)\\.?(\\W|$)", "1xsirkax3");
            put("(\\d+\\.) [Ss]ek\\.?(\\W|$)", "1x sekúndax2");
            put("(\\d+\\.) [Mm]ín\\.?(\\W|$)", "1x mínútax2");
    }};

    public static Map<String, String> directionDict = new HashMap<String, String>() {{
        // we don't accept dashes (u2013 or u2014), only standard hyphenation.
        put("(\\W|^)(SV-(:?(til|lands|átt|verðu|vert)))(\\W|$)", "1xsuðvestanx3x4");
        put("(\\W|^)(NV-(:?(til|lands|átt|verðu|vert)))(\\W|$)", "1xnorðvestanx3x4");
        put("(\\W|^)(NA-(:?(til|lands|átt|verðu|vert)))(\\W|$)", "1xnorðaustanx3x4");
        put("(\\W|^)(SA-(:?(til|lands|átt|verðu|vert)))(\\W|$)", "1xsuðaustanx4x5");
        put("(\\W|^)(A-(:?(til|lands|átt|verðu|vert)))(\\W|$)", "1xaustanx4");
        put("(\\W|^)(S-(:?(til|lands|átt|verðu|vert)))(\\W|$)", "1xsunnanx4");
        put("(\\W|^)(V-(:?(til|lands|átt|verðu|vert)))(\\W|$)", "1xvestanx4");
        put("(\\W|^)(N-(:?(til|lands|átt|verðu|vert)))(\\W|$)", "1xnorðanx4");
    }};

    public static Map<String, String> denominatorDict = new HashMap<String, String>() {{
       put("\\/kg?(\\W|$)", " á kílóiðx1");
    }};
    /*
    {"\\/t\\.?(\\W|$)": " \u00e1 tonni\u00f0\\g<1>", "\\/ha\\.?(\\W|$)": " \u00e1 hektarann\\g<1>", "\\/ng\\.?(\\W|$)": " \u00e1 nan\u00f3grammi\u00f0\\g<1>", "\\/mg\\.?(\\W|$)": " \u00e1 milligrammi\u00f0\\g<1>", "\\/\u00b5g\\.?(\\W|$)": " \u00e1 m\u00edkr\u00f3grammi\u00f0\\g<1>", "\\/gr?\\.?(\\W|$)": " \u00e1 grammi\u00f0\\g<1>", "\\/kg?(\\W|$)": " \u00e1 k\u00edl\u00f3i\u00f0\\g<1>", "\\/lbs(\\W|$)": " \u00e1 pundi\u00f0\\g<1>", "\\/ml(\\W|$)": " \u00e1 millil\u00edtrann\\g<1>", "\\/dl(\\W|$)": " \u00e1 desil\u00edtrann\\g<1>", "\\/[sc]l(\\W|$)": " \u00e1 sentil\u00edtrann\\g<1>", "\\/l(\\W|$)": " \u00e1 l\u00edtrann\\g<1>", "\\/tsk(\\W|$)": " \u00e1 teskei\u00f0\\g<1>", "\\/msk(\\W|$)": " \u00e1 matskei\u00f0\\g<1>", "\\/ft(\\W|$)": " \u00e1 feti\u00f0\\g<1>", "\\/m\\.?(\\W|$)": " \u00e1 metrann\\g<1>", "\\/pm\\.?(\\W|$)": " \u00e1 p\u00edk\u00f3metra\\g<1>", "\\/nm\\.?(\\W|$)": " \u00e1 nan\u00f3metra\\g<1>", "\\/[cs]m\\.?(\\W|$)": " \u00e1 sentimetra\\g<1>", "\\/dm\\.?(\\W|$)": " \u00e1 desimetra\\g<1>", "\\/km\\.?(\\W|$)": " \u00e1 k\u00edl\u00f3metra\\g<1>", "\\/Nm\\.?(\\W|$)": " \u00e1 Nj\u00fatonmetra\\g<1>", "\\/klst(\\W|$)": " \u00e1 klukkustund\\g<1>", "\\/kw\\.?(st|h)\\.?(\\W|$)": " \u00e1 k\u00edl\u00f3vattstund\\g<2>", "\\/Mw\\.?(st|h)\\.?(\\W|$)": " \u00e1 megavattstund\\g<2>", "\\/Gw\\.?(st|h)\\.?(\\W|$)": " \u00e1 g\u00edgavattstund\\g<2>", "\\/Tw\\.?(st|h)\\.?(\\W|$)": " \u00e1 teravattstund\\g<2>", "\\/s(ek)?\\.?(\\W|$)": " \u00e1 sek\u00fandu\\g<2>", "\\/m\u00edn\\.?(\\W|$)": " \u00e1 m\u00edn\u00fatu\\g<1>", "\\/ms(ek)?\\.?(\\W|$)": " \u00e1 millisek\u00fandu\\g<2>", "\\/kr\\.?(\\W|$)": " \u00e1 kr\u00f3nu\\g<1>", "\\/fm\\.?(\\W|$)": " \u00e1 fermetra\\g<1>", "\\/ferm\\.?(\\W|$)": " \u00e1 fermetra\\g<1>", "\\/m\u00b2(\\W|$)": " \u00e1 fermetra\\g<1>", "\\/m2(\\W|$)": " \u00e1 fermetra\\g<1>", "\\/r\u00famm\\.?(\\W|$)": " \u00e1 r\u00fammetra\\g<1>", "\\/m\u00b3(\\W|$)": " \u00e1 r\u00fammetra\\g<1>", "\\/m3(\\W|$)": " \u00e1 r\u00fammetra\\g<1>", "\\/mm\u00b2(\\W|$)": " \u00e1 fermillimetra\\g<1>", "\\/mm2(\\W|$)": " \u00e1 fermillimetra\\g<1>", "\\/mm\u00b3(\\W|$)": " \u00e1 r\u00fammillimetra\\g<1>", "\\/mm3(\\W|$)": " \u00e1 r\u00fammillimetra\\g<1>", "\\/[cs]m\u00b2(\\W|$)": " \u00e1 fermillimetra\\g<1>", "\\/[cs]m2(\\W|$)": " \u00e1 fermillimetra\\g<1>", "\\/[cs]m\u00b3(\\W|$)": " \u00e1 r\u00fammillimetra\\g<1>", "\\/[cs]m3(\\W|$)": " \u00e1 r\u00fammillimetra\\g<1>", "\\/km\u00b2(\\W|$)": " \u00e1 ferk\u00edl\u00f3millimetra\\g<1>", "\\/km2(\\W|$)": " \u00e1 ferk\u00edl\u00f3millimetra\\g<1>", "\\/km\u00b3(\\W|$)": " \u00e1 r\u00famk\u00edl\u00f3millimetra\\g<1>", "\\/km3(\\W|$)": " \u00e1 r\u00famk\u00edl\u00f3millimetra\\g<1>", "\\/\\%(\\W|$)": " \u00e1 pr\u00f3senti\u00f0\\g<1>", "\\/stk\\.?(\\W|$)": " \u00e1 stykki\u00f0\\g<1>", "\\/V(\\W|$)": " \u00e1 volt\\g<1>", "\\/kV(\\W|$)": " \u00e1 k\u00edl\u00f3volt\\g<1>", "\\/Hz(\\W|$)": " \u00e1 herz\\g<1>", "\\/kHz(\\W|$)": " \u00e1 k\u00edl\u00f3herz\\g<1>", "\\/MHz(\\W|$)": " \u00e1 megaherz\\g<1>", "\\/GHz(\\W|$)": " \u00e1 g\u00edgaherz\\g<1>", "\\/W(\\W|$)": " \u00e1 vatt\\g<1>", "\\/kW(\\W|$)": " \u00e1 k\u00edl\u00f3vatt\\g<1>"}
     */

    public static Map<String, String> weightDict = new HashMap<String, String>() {{
        put("((\\W|^)(" + prepositions.get(DAT) + ") ((\\d{1,2}\\.)?(\\d{3}\\.?)*(\\d*1|\\d,\\d*1))) t\\.?(\\W|$)", "1x tonnix10");
        put("((\\W|^)(" + prepositions.get(GEN) + ") ((\\d{1,2}\\.)?(\\d{3}\\.?)*(\\d*1|\\d,\\d*1))) t\\.?(\\W|$)", "1x tonnsx10");
        put("((\\W|^)(" + prepositions.get(DAT) + ") ((\\d{1,2}\\.)?(\\d{3}\\.?)*(\\d*[02-9]|\\d,\\d*[02-9]))) t\\.?(\\W|$)", "1x tonnumx10");
        put("((\\W|^)(" + prepositions.get(DAT) + ") (((\\d{1,2}\\.)?(\\d{3}\\.?)*|\\d+)(,\\d+)?)?) " + patternSelection.get(AMOUNT) + " t\\.?(\\W|$)", "1 x11x tonnumx13");
        // usw. three more, and the same for grams
        put("((\\W|^)(" + prepositions.get(DAT) + ") ((\\d{1,2}\\.)?(\\d{3}\\.?)*(\\d*1|\\d,\\d*1))) g\\.?(\\W|$)", "1x grammix10");
        put("((\\W|^)(" + prepositions.get(GEN) + ") ((\\d{1,2}\\.)?(\\d{3}\\.?)*(\\d*1|\\d,\\d*1))) g\\.?(\\W|$)", "1x grammsx10");
        put("((\\W|^)(" + prepositions.get(DAT) + ") ((\\d{1,2}\\.)?(\\d{3}\\.?)*(\\d*[02-9]|\\d,\\d*[02-9]))) g\\.?(\\W|$)", "1x grömmumx10");
        put("((\\W|^)(" + prepositions.get(DAT) + ") (((\\d{1,2}\\.)?(\\d{3}\\.?)*|\\d+)(,\\d+)?)?) " + patternSelection.get(AMOUNT) + " g\\.?(\\W|$)", "1 x11x grömmumx13");
        put("(1 )gr?\\.?(\\W|$)", "1xgrammx2");
        put("([02-9]|" + patternSelection.get(AMOUNT) + ") gr?\\.?(\\W|$)", "1x grömmx3");


        // another section for nanó/milli/míkró/píkó/attó/zeptó/yoktó-kíló/pund + grammi/gramms/grömmum, ...
        // see class weight_dict.py in regina
    }};

    public static Map<String, String> distanceDict = new HashMap<String, String>() {{
        put("((\\W|^)(" + prepositions.get(ACC_DAT_COMB) + ") ((\\d{1,2}\\.)?(\\d{3}\\.?)*(\\d*[02-9]|\\d,\\d*[02-9]))) [sc]m\\.?(\\W|$)", "1x sentimetrax10");
        put("((\\W|^)(" + prepositions.get(ACC_GEN) + ") (((\\d{1,2}\\.)?(\\d{3}\\.?)*|\\d+)(,\\d+)?)?) " + patternSelection.get(AMOUNT) + " [sc]m\\.?(\\W|$)", "1x 13x sentimetrax16x");
        // and many, many, others ...

    }};

    //TODO: take care not to normalize the superscripts away when doing unicode normalization!
    private static Map<String, String> dimensionBefore = new HashMap<String, String>() {{
        put("²", "fer");
       // put("2", "fer");
       // put("³", "rúm");
       // put("3", "rúm");
    }};
    private static Map<String, String> dimensionAfter = new HashMap<String, String>() {{
        put("f", "fer");
        put("fer", "fer");
       // put("rúm", "rúm");
    }};
    private static Map<String, String> prefixMeterDimension = new HashMap<String, String>() {{
        put("", "");
      //  put("m", "milli");
       // put("[cs]", "senti");
       // put("d", "desi");
       // put("k", "kíló");
    }};


    public static Map<String, String> areaDict = new HashMap<>();

    public static Map<String, String> getAreaDict() {
        if (!areaDict.isEmpty())
            return areaDict;

        areaDict.put("((\\W|^)(" + prepositions.get(ACC_DAT_GEN_COMB) + ") ((\\d{1,2}\\.)?(\\d{3}\\.?)*(\\d*1|\\d,\\d*1))) ha\\.?(\\W|$)", "1x hektarax14");
        areaDict.put("((\\W|^)(" + prepositions.get(ACC_GEN) + ") ((\\d{1,2}\\.)?(\\d{3}\\.?)*(\\d*[02-9]|\\d,\\d*[02-9]))) ha\\.?(\\W|$)", "1x hektarax12");
        areaDict.put("((\\W|^)(" + prepositions.get(ACC_GEN) + ") (((\\d{1,2}\\.)?(\\d{3}\\.?)*|\\d+)(,\\d+)?)?) " + patternSelection.get(AMOUNT) + " ha\\.?(\\W|$)", "1x 13x hektarax16x");
        areaDict.put("((\\W|^)(" + prepositions.get(DAT)+ ") ((\\d{1,2}\\.)?(\\d{3}\\.?)*(\\d*[02-9]|\\d,\\d*[02-9]))) ha\\.?(\\W|$)","1x hekturumx10");
        areaDict.put("((\\W|^)(" + prepositions.get(DAT) + ") (((\\d{1,2}\\.)?(\\d{3}\\.?)*|\\d+)(,\\d+)?)?) " + patternSelection.get(AMOUNT) + " ha\\.?(\\W|$)", "1x 11x hekturumx14");
        areaDict.put("(1) ha\\.?(\\W|$)", "1x hektarix2");
        // does this change later? at the moment we get: "reisa 15 ha ..." -> "reisa 15 hektarar ..."
        areaDict.put("([02-9]|" + patternSelection.get(AMOUNT) + ") ha\\.?(\\W|$)","1x hektararx3");

        for (String letter : prefixMeterDimension.keySet()) {
            for (String superscript : dimensionAfter.keySet()) {
                areaDict.put("((\\W|^)(" + prepositions.get(ACC_DAT_GEN_COMB) + ") ((\\d{1,2}\\.)?(\\d{3}\\.?)*(\\d*1|\\d,\\d*1)) " + letter + "m" + superscript + "(\\W|$))",
                        "1x " + dimensionAfter.get(superscript) + prefixMeterDimension.get(letter) + "metrax14");
                areaDict.put("((\\W|^)(" + prepositions.get(ACC_GEN) + ") ((\\d{1,2}\\.)?(\\d{3}\\.?)*(\\d*[02-9]|\\d,\\d*[02-9]))) " + letter + "m" + superscript + "(\\W|$)",
                        "1x " + dimensionAfter.get(superscript) + prefixMeterDimension.get(letter) + "metrax12");
                areaDict.put("((\\W|^)(" + prepositions.get(ACC_GEN) + ") (((\\d{1,2}\\.)?(\\d{3}\\.?)*|\\d+)(,\\d+)?)?) " + patternSelection.get(AMOUNT) + " " + letter + "m" + superscript + "(\\W|$)",
                        "1x 13x " + dimensionAfter.get(superscript) + prefixMeterDimension.get(letter) + "metrax16");
                areaDict.put("((\\W|^)(" + prepositions.get(DAT) + ") ((\\d{1,2}\\.)?(\\d{3}\\.?)*(\\d*[02-9]|\\d,\\d*[02-9]))) " + letter + "m" + superscript + "(\\W|$)",
                        "1x " + dimensionAfter.get(superscript) + prefixMeterDimension.get(letter) + "metrumx10");
                areaDict.put("((\\W|^)(" + prepositions.get(DAT) + ") (((\\d{1,2}\\.)?(\\d{3}\\.?)*|\\d+)(,\\d+)?)?) " + patternSelection.get(AMOUNT) + " " + letter + "m" + superscript + "(\\W|$)",
                        "1x 11x " + dimensionAfter.get(superscript) + prefixMeterDimension.get(letter) + "metrumx14");
                areaDict.put("(1 )" + letter + "m" + superscript + "(\\W|$)", "1x" + dimensionAfter.get(superscript) + prefixMeterDimension.get(letter) + "metrix2");
                areaDict.put("([02-9]|" + patternSelection.get(AMOUNT) + ") " + letter + "m" + superscript + "(\\W|$)", "1x " + dimensionAfter.get(superscript) + prefixMeterDimension.get(letter) + "metrar x3");

            }
        }

        for (String letter : prefixMeterDimension.keySet()) {
            for (String preprefix : dimensionBefore.keySet()) {
                areaDict.put("((\\W|^)(" + prepositions.get(ACC_DAT_GEN_COMB) + ") ((\\d{1,2}\\.)?(\\d{3}\\.?)*(\\d*1|\\d,\\d*1))) " + preprefix + letter + "m\\.?(\\W|$)",
                        "1x " + dimensionBefore.get(preprefix) + prefixMeterDimension.get(letter) + "metrax14");
                areaDict.put("((\\W|^)(" + prepositions.get(ACC_GEN) + ") ((\\d{1,2}\\.)?(\\d{3}\\.?)*(\\d*[02-9]|\\d,\\d*[02-9]))) " + preprefix + letter + "m\\.?(\\W|$)",
                        "1x " + dimensionBefore.get(preprefix) + prefixMeterDimension.get(letter) + "metrax12");
                areaDict.put("((\\W|^)(" + prepositions.get(ACC_GEN) + ") (((\\d{1,2}\\.)?(\\d{3}\\.?)*|\\d+)(,\\d+)?)?) " + patternSelection.get(AMOUNT) + " " + preprefix + letter + "m\\.?(\\W|$)",
                        "1x 13x " + dimensionBefore.get(preprefix) + prefixMeterDimension.get(letter) + "metrax16");
                areaDict.put("((\\W|^)(" + prepositions.get(DAT) + ") ((\\d{1,2}\\.)?(\\d{3}\\.?)*(\\d*[02-9]|\\d,\\d*[02-9]))) " + preprefix + letter + "m\\.?(\\W|$)",
                        "1x " + dimensionBefore.get(preprefix) + prefixMeterDimension.get(letter) + "metrumx10");
                areaDict.put("((\\W|^)(" + prepositions.get(DAT) + ") (((\\d{1,2}\\.)?(\\d{3}\\.?)*|\\d+)(,\\d+)?)?) " + patternSelection.get(AMOUNT) + " " + preprefix + letter + "m\\.?(\\W|$)",
                        "1x 11x" + dimensionBefore.get(preprefix) + prefixMeterDimension.get(letter) + "metrumx14");
                areaDict.put("(1 )" + preprefix + letter + "m\\.?(\\W|$)", "1x " + dimensionBefore.get(preprefix) + prefixMeterDimension.get(letter) + "metrix2");
                areaDict.put("([02-9]|" + patternSelection.get(AMOUNT) + ") " + preprefix + letter + "m\\.?(\\W|$)", "1x " + dimensionBefore.get(preprefix) + prefixMeterDimension.get(letter) + "metrarx3");
                //added ABN, previous patterns did not capture " ... ( 150.000 m² ) ..." - still need to figure that out
                areaDict.put("((\\W|^)(\\d{3}\\.\\d{3})) " + letter + "m" + preprefix + "(\\W|$)", "1x " + dimensionBefore.get(preprefix) + prefixMeterDimension.get(letter) + "metrarx4");
            }
        }
        return areaDict;
    }

    private static Map<String, String> volumeDict = new HashMap<>();

    public static Map<String, String> getVolumeDict() {
        if (!volumeDict.isEmpty())
            return volumeDict;

        Map<String, String> prefixLiter = new HashMap<String, String>() {{
            put("", "");
            put("d", "desi");
            put("c", "senti");
            put("m", "milli");
        }};

        for (String letter : prefixLiter.keySet()) {
            volumeDict.put("((\\W|^)(" + prepositions.get(ACC_DAT_GEN_COMB)+ ") ((\\d{1,2}\\.)?(\\d{3}\\.?)*(\\d*1|\\d,\\d*1))) " + letter + "[Ll]\\.?(\\W|$)", "1x " + prefixLiter.get(letter) + "lítrax14x");
            volumeDict.put("((\\W|^)(" + prepositions.get(ACC_GEN) + ") ((\\d{1,2}\\.)?(\\d{3}\\.?)*(\\d*[02-9]|\\d,\\d*[02-9]))) " + letter + "[Ll]\\.?(\\W|$)", "1 " + prefixLiter.get(letter) + "lítrax12");
            volumeDict.put("((\\W|^)(" + prepositions.get(ACC_GEN) + ") (((\\d{1,2}\\.)?(\\d{3}\\.?)*|\\d+)(,\\d+)?)?) " + patternSelection.get(AMOUNT) + " " + letter + "[Ll]\\.?(\\W|$)", "1x 13x " + prefixLiter.get(letter) + "lítrax16");
            volumeDict.put("((\\W|^)(" + prepositions.get(DAT) + ") ((\\d{1,2}\\.)?(\\d{3}\\.?)*(\\d*[02-9]|\\d,\\d*[02-9]))) " + letter + "[Ll]\\.?(\\W|$)", "1x " + prefixLiter.get(letter) + "lítrumx10");
            volumeDict.put("((\\W|^)(" + prepositions.get(DAT) + ") (((\\d{1,2}\\.)?(\\d{3}\\.?)*|\\d+)(,\\d+)?)?) " + patternSelection.get(AMOUNT) + " " + letter + "[Ll]\\.?(\\W|$)", "1x 11x " + prefixLiter.get(letter) + "lítrumx14");
            volumeDict.put("(1 )" + letter + "[Ll]\\.?(\\W|$)", "1x" + prefixLiter.get(letter) + "lítrix2");
            volumeDict.put("([02-9]|" + patternSelection.get(AMOUNT) + ") " + letter + "[Ll]\\.?(\\W|$)", "");
            //if (!letter.isEmpty())
            //    volumeDict.put("(\\W|^)" + letter + "l\\.?(\\W|$)", "1x" + prefixLiter.get(letter) + "lítrar x2");
        }

        return volumeDict;
    }

    private static Map<String, String> timeDict = new HashMap<>();

    public static Map<String, String> getTimeDict() {
        if  (!timeDict.isEmpty())
            return timeDict;

        timeDict.put("((\\W|^)(" + prepositions.get(GEN) + ") ((\\d{1,2}\\.)?(\\d{3}\\.?)*(\\d*1|\\d,\\d*1))) klst\\.?(\\W|$)",  "1x klukkustundarx10");
        timeDict.put("((\\W|^)(" + prepositions.get(DAT) + ") ((\\d{1,2}\\.)?(\\d{3}\\.?)*(\\d*[02-9]|\\d,\\d*[02-9]))) klst\\.?(\\W|$)", "1x klukkustundumx10");
        timeDict.put("((\\W|^)(" + prepositions.get(DAT) + ") (((\\d{1,2}\\.)?(\\d{3}\\.?)*|\\d+)(,\\d+)?)?) " + patternSelection.get(AMOUNT) + " klst\\.?(\\W|$)", "1x 11x klukkustundumx14");
        timeDict.put("((\\W|^)(" + prepositions.get(GEN) + ") ((\\d{1,2}\\.)?(\\d{3}\\.?)*(\\d*[02-9]|\\d,\\d*[02-9]))) klst\\.?(\\W|$)", "1x klukkustundax10");
        timeDict.put("((\\W|^)(" + prepositions.get(GEN) + ") (((\\d{1,2}\\.)?(\\d{3}\\.?)*|\\d+)(,\\d+)?)?) " + patternSelection.get(AMOUNT) + " klst\\.?(\\W|$)", "1x 11x klukkustundax14");
        timeDict.put("(1 )klst\\.?(\\W|$)", "1x klukkustundx2");
        timeDict.put("(\\W|^)klst\\.?(\\W|$)", "1xklukkustundirx2x");

        Map<String, String> prefixTime = new HashMap<String, String>() {{
            put("mín()?", "mínút");
            put("s(ek)?", "sekúnd");
            put("ms(ek)?", "millisekúnd");
        }};

        for (String letters : prefixTime.keySet()) {
            timeDict.put("((\\W|^)(" + prepositions.get(ACC_DAT_GEN_COMB) + ") ((\\d{1,2}\\.)?(\\d{3}\\.?)*(\\d*1|\\d,\\d*1))) " + letters + "\\.?(\\W|$)", "1x " + prefixTime.get(letters) + "ux15");
            timeDict.put("((\\W|^)(" + prepositions.get(DAT) + ") ((\\d{1,2}\\.)?(\\d{3}\\.?)*(\\d*[02-9]|\\d,\\d*[02-9]))) " + letters + "\\.?(\\W|$)", "1x " + prefixTime.get(letters) + "umx11");
            timeDict.put("((\\W|^)(" + prepositions.get(DAT) + ") (((\\d{1,2}\\.)?(\\d{3}\\.?)*|\\d+)(,\\d+)?)?) " + patternSelection.get(AMOUNT) + " " + letters + "\\.?(\\W|$)", "1x 11x " + prefixTime.get(letters) + "umx15");
            timeDict.put("((\\W|^)(" + prepositions.get(GEN) + ") ((\\d{1,2}\\.)?(\\d{3}\\.?)*(\\d*[02-9]|\\d,\\d*[02-9]))) " + letters + "\\.?(\\W|$)", "1x " + prefixTime.get(letters) + "nax11");
            timeDict.put("((\\W|^)(" + prepositions.get(GEN) + ") (((\\d{1,2}\\.)?(\\d{3}\\.?)*|\\d+)(,\\d+)?)?) " + patternSelection.get(AMOUNT) + " " + letters + "\\.?(\\W|$)", "1x 11x " + prefixTime.get(letters) + "nax15");
            // added ABN: we need 'undir' ('undir x sek/klst/...')
            timeDict.put("((\\W|^)(" + prepositions.get(ACC_DAT) + ") ((\\d{1,2}\\.)?(\\d{3}\\.?)*(\\d*[02-9]|\\d,\\d*[02-9]))) " + letters + "\\.?(\\W|$)", "1x " + prefixTime.get(letters) + "umx9");

            timeDict.put("(1 )" + letters + "\\.?(\\W|$)", "1x" + prefixTime.get(letters) + "ax2");
            //TODO: this one messes up, need to give the preposition patterns priority and not allow this one to intervene. But why do they both match after one has been substituted? I.e. " ... sekúndur ..." matches the pattern above with preposition
            //timeDict.put("([02-9]|" + patternSelection.get(AMOUNT) + ") " + letters + "\\.?(\\W|$)", "1x " + prefixTime.get(letters) + "ur x3");
        }

        return timeDict;
    }

    public static Map<String, String> currencyDict = new HashMap<>();

    public static Map<String, String> getCurrencyDict() {
        if (!currencyDict.isEmpty())
            return currencyDict;
        // krónur:
        currencyDict.put("((\\W|^)(" + prepositions.get(DAT) + ")) kr\\.?\\-? ?((((\\d{1,2}\\.)?(\\d{3}\\.?)*|\\d+)(,\\d+)?)? " + patternSelection.get(AMOUNT) + ")(\\W|$)", "1x 6x krónumx15");
        currencyDict.put("((\\W|^)(" + prepositions.get(GEN) + ")) kr\\.?\\-? ?((((\\d{1,2}\\.)?(\\d{3}\\.?)*|\\d+)(,\\d+)?)? " + patternSelection.get(AMOUNT) + ")(\\W|$)", "1x 6xkrónax15");
        currencyDict.put("(\\W|^)[Kk]r\\.? ?((((\\d{1,2}\\.)?(\\d{3}\\.?)*|\\d+)(,\\d+)?)? " + patternSelection.get(AMOUNT) + ")(\\W|$)", "1x 2x krónurx11");
        currencyDict.put("((\\W|^)(" + prepositions.get(ACC_DAT_GEN_COMB) + ") ((\\d{1,2}\\.)?(\\d{3}\\.?)*(\\d*1|\\d,\\d*1))) ?kr\\.?\\-?(\\W|$)", "1x krónux14");
        currencyDict.put("((\\W|^)(" + prepositions.get(ACC_DAT_GEN_COMB) + ")) kr\\.?\\-? ?((\\d{1,2}\\.)?(\\d{3}\\.?)*(\\d*1|\\d,\\d*1))(\\W|$)", "1x 10x krónux14");
        currencyDict.put("((\\W|^)(" + prepositions.get(DAT) + ") ((\\d{1,2}\\.)?(\\d{3}\\.?)*(\\d*[02-9]|\\d,\\d*[02-9]))) kr\\.?\\-?(\\W|$)", "1x krónumx10");
        currencyDict.put("((\\W|^)(" + prepositions.get(DAT) + ") (((\\d{1,2}\\.)?(\\d{3}\\.?)*|\\d+)(,\\d+)?)? " + patternSelection.get(AMOUNT) + ") kr\\.?\\-?(\\W|$)", "1x 8xkrónumx14");
        currencyDict.put("((\\W|^)(" + prepositions.get(DAT) + ")) kr\\.?\\-? ?((\\d{1,2}\\.)?(\\d{3}\\.?)*(\\d*[02-9]|\\d,\\d*[02-9]))(\\W|$)", "1x 9x krónumx10");
        currencyDict.put("((\\W|^)(" + prepositions.get(GEN) + ") ((\\d{1,2}\\.)?(\\d{3}\\.?)*(\\d*[02-9]|\\d,\\d*[02-9]))) kr\\.?\\-?(\\W|$)", "1x krónax10");
        currencyDict.put("((\\W|^)(" + prepositions.get(GEN) + ") (((\\d{1,2}\\.)?(\\d{3}\\.?)*|\\d+)(,\\d+)?)? " + patternSelection.get(AMOUNT) + ") kr\\.?\\-?(\\W|$)", "1x 8xkrónax14");
        currencyDict.put("((\\W|^)(" + prepositions.get(GEN) + ")) kr\\.?\\-? ?((\\d{1,2}\\.)?(\\d{3}\\.?)*(\\d*[02-9]|\\d,\\d*[02-9]))(\\W|$)", "1x 9x krónax10");
        currencyDict.put("(1 ?)kr\\.?\\-?(\\W|$)", "1xkrónax2");
        currencyDict.put("([02-9]|" + patternSelection.get(AMOUNT) + ") ?kr\\.?\\-?(\\W|$)", "1x krónurx3");
        // is this an error? (2 times group 2)
        //currencyDict.put("(\\W|^)[Kk]r\\.? ?(\\d)", "1x2x krónurx2");
        currencyDict.put("(\\W|^)[Kk]r\\.? ?(\\d)", "1xkrónur x2");

        // MUCH more here! other currencies, etc.

        return currencyDict;

    }

    public static Map<String, String> electronicDict = new HashMap<>();

    public static Map<String, String> getElectronicDict() {
        if (!electronicDict.isEmpty())
            return electronicDict;

        Map<String, String> wattPrefix = new HashMap<String, String>() {{
           put("", "");
           put("k", "kíló");
           put("M", "Mega");
           put("G", "Gíga");
           put("T", "Tera");
        }};

        Map<String, String> measurement = new HashMap<String, String>() {{
            put("V", "volt");
            put("Hz", "herz");
        }};

        for (String letter : wattPrefix.keySet()) {
            electronicDict.put("((\\W|^)(" + prepositions.get(GEN) + ") ((\\d{1,2}\\.)?(\\d{3}\\.?)*(\\d*1|\\d,\\d*1))) " + letter + "[Ww]\\.?(st|h)\\.?(\\W|$)", "1x " + wattPrefix.get(letter) + "vattstundarx11");
            electronicDict.put("((\\W|^)(" + prepositions.get(DAT) + ") ((\\d{1,2}\\.)?(\\d{3}\\.?)*(\\d*[02-9]|\\d,\\d*[02-9]))) " + letter + "[Ww]\\.?(st|h)\\.?(\\W|$)", "1x " + wattPrefix.get(letter) + "vattstundumx11");
            electronicDict.put("((\\W|^)(" + prepositions.get(DAT) + ") (((\\d{1,2}\\.)?(\\d{3}\\.?)*|\\d+)(,\\d+)?)?) " + patternSelection.get(AMOUNT) + " " + letter + "[Ww]\\.?(st|h)\\.?(\\W|$)", "1x 11x " + wattPrefix.get(letter) + "vattstundumx15");
            electronicDict.put("([02-9]|" + patternSelection.get(AMOUNT) + ") " + letter + "W(\\W|$)", "1x " + wattPrefix.get(letter) + "vöttx3");
            electronicDict.put("(1 )" + letter + "W(\\W|$)", "1x " + wattPrefix.get(letter) + "vattx2");

            // etc.
        }
        return electronicDict;
    }

    public static Map<String, String> restMeasurementDict = new HashMap<String, String>() {{
        put("((\\W|^)(" + prepositions.get(DAT) + ") ((\\d{1,2}\\.)?(\\d{3}\\.?)*(\\d*1|\\d,\\d*1))) ?\\%(\\W|$)","1x prósentix10");
        put("((\\W|^)(" + prepositions.get(GEN)  + ") ((\\d{1,2}\\.)?(\\d{3}\\.?)*(\\d*1|\\d,\\d*1))) ?\\%(\\W|$)", "1x prósentsx10");
        put("((\\W|^)(" + prepositions.get(DAT) + ") ((\\d{1,2}\\.)?(\\d{3}\\.?)*(\\d*[02-9]|\\d,\\d*[02-9]))) ?\\%(\\W|$)", "1x prósentumx10");
        put("((\\W|^)(" + prepositions.get(GEN) + ") ((\\d{1,2}\\.)?(\\d{3}\\.?)*(\\d*[02-9]|\\d,\\d*[02-9]))) ?\\%(\\W|$)", "1x prósentax10");
        put("((\\W|^)(" + prepositions.get(GEN) + ") (((\\d{1,2}\\.)?(\\d{3}\\.?)*|\\d+)(,\\d+)?)?) " + patternSelection.get(AMOUNT) + " \\%(\\W|$)", "1x 11x prósentax14");
        put("\\%", " prósent");

        // and many more, also "stk. / stykki" and "kcal / kílókaloríur"
    }};

    public static Map<String, String> periodDict = new HashMap<String, String>() {{
        put("(\\W|^)mán(ud)?\\.?(\\W|$)", "1xmánudagx3");
        put("(\\W|^)þri(ðjud)?\\.?(\\W|$)", "1xþriðjudagx3");
        put("(\\W|^)mið(vikud)?\\.?(\\W|$)", "1xmiðvikudagx3");
        put("(\\W|^)fim(mtud)?\\.?(\\W|$)", "1xfimmtudagx3");
        put("(\\W|^)fös(tud)?\\.?(\\W|$)", "1xföstudagx3");
        put("(\\W|^)lau(gard)?\\.?(\\W|$)", "1xlaugardagx3");
        put("(\\W|^)sun(nud)?\\.?(\\W|$)", "1xsunnudagx3");

        put("(\\W|^)jan\\.?(\\W|$)", "1xjanúarx3");
        put("(\\W|^)feb\\.?(\\W|$)", "1xfebrúarx3");
        put("(\\W|^)mar\\.?(\\W|$)", "1xmarsx3");
        put("(\\W|^)apr\\.?(\\W|$)", "1xaprílx3");
        put("(\\W|^)jún\\.?(\\W|$)", "1xjúníx3");
        put("(\\W|^)júl\\.?(\\W|$)", "1xjúlíx3");
        put("(\\W|^)ágú?\\.?(\\W|$)", "1xágústx3");
        put("(\\W|^)sept?\\.?(\\W|$)", "1xseptemberx3");
        put("(\\W|^)okt\\.?(\\W|$)", "1xoktóberx3");
        put("(\\W|^)nóv\\.?(\\W|$)", "1xnóvemberx3");
        put("(\\W|^)des\\.?(\\W|$)", "1xdesemberx3");

        /*
                "(\W|^)II\.?(\W|$)": "\g<1>annar\g<2>",
                "(\W|^)III\.?(\W|$)": "\g<1>þriðji\g<2>",
                "(\W|^)IV\.?(\W|$)": "\g<1>fjórði\g<2>",
                "(\W|^)VI\.?(\W|$)": "\g<1>sjötti\g<2>",
                "(\W|^)VII\.?(\W|$)": "\g<1>sjöundi\g<2>",
                "(\W|^)VIII\.?(\W|$)": "\g<1>áttundi\g<2>",
                "(\W|^)IX\.?(\W|$)": "\g<1>níundi\g<2>",
                "(\W|^)XI\.?(\W|$)": "\g<1>ellefti\g<2>",
                "(\W|^)XII\.?(\W|$)": "\g<1>tólfti\g<2>",
                "(\W|^)XIII\.?(\W|$)": "\g<1>þrettándi\g<2>",
                "(\W|^)XIV\.?(\W|$)": "\g<1>fjórtándi\g<2>",
                "(\W|^)XV\.?(\W|$)": "\g<1>fimmtándi\g<2>",
                "(\W|^)XVI\.?(\W|$)": "\g<1>sextándi\g<2>",
                "(\W|^)XVII\.?(\W|$)": "\g<1>sautjándi\g<2>",
                "(\W|^)XVIII\.?(\W|$)": "\g<1>átjándi\g<2>",
                "(\W|^)XIX\.?(\W|$)": "\g<1>nítjándi\g<2>"} */

    }};


}
