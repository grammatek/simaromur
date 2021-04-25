package com.grammatek.simaromur.frontend;

import java.util.HashMap;
import java.util.Map;

/**
 * Normalization dictionaries for abbreviations, digits and other non-standard-words.
 * The final dictionaries will be stored in external text files, but for preliminary unit-testing
 * having a selection of the patterns here helps.
 */

public class NormalizationDictionaries {

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
}
