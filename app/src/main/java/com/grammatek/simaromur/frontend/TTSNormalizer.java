package com.grammatek.simaromur.frontend;

import android.content.Context;
import android.content.res.Resources;

import com.grammatek.simaromur.R;

import is.iclt.icenlp.core.icetagger.IceTagger;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Text normalizer for TTS.
 * Relies on unicode normalized input text, see TTSUnicodeNormalizer.
 */

public class TTSNormalizer {

    private Context mContext;
    private Map<String, String> mRegexMap;
    // Possible replacement patterns - INT=group index, STR=string replacement
    private String INT = "INT";
    private String STR = "STR";
    private String STRINT = "STRINT";
    private String STRINTSTRINT = "STRINTSTRINT";
    private String INTSTRINT = "INTSTRINT";
    private String INTSTRINTINT = "INTSTRINTINT";
    private String INTINTSTRINT = "INTINTSTRINT";
    private String INTINTSTRINTINT = "INTINTSTRINTINT";

    /*
    public TTSNormalizer(Context context) {
        this.mContext = context;
        this.mRegexMap = readAbbreviations();
    }*/

    public TTSNormalizer() {

    }

    public String normalize(String text) {
        String normalized = text;
        //IceTagger tagger = new IceTagger();

        if (normalized.matches(".*\\d.*")) {
            normalized = replaceFromDict(normalized, NormalizationDictionaries.preHelpDict);
        }
        if (normalized.contains("-")) {
            normalized = replaceFromDict(normalized, NormalizationDictionaries.directionDict);
            normalized = replaceFromDict(normalized, NormalizationDictionaries.hyphenDict);
        }
        if (normalized.contains(".")) {
            normalized = replaceFromDict(normalized, NormalizationDictionaries.abbreviationDict);
        }
        if (normalized.contains("/")) {
            normalized = replaceFromDict(normalized, NormalizationDictionaries.denominatorDict);
        }
        normalized = replaceFromDict(normalized, NormalizationDictionaries.weightDict);
        if (normalized.matches(".*\\b([pnµmcsdkN]?m|ft)\\.?\\b.*")) {
            normalized = replaceFromDict(normalized, NormalizationDictionaries.distanceDict);
        }
        if (normalized.matches(".*(\\bha\\.?\\b).*|([pnµmcsdk]?m\\b\\.?)|([pnµmcsdk]?m[²2³3]).*")) {
            normalized = replaceFromDict(normalized, NormalizationDictionaries.getAreaDict());
        }
        if (normalized.matches(".*\\b[dcmµ]?[Ll]\\.?\\b.*")) {
            normalized = replaceFromDict(normalized, NormalizationDictionaries.getVolumeDict());
        }
        if (normalized.matches(".*\\b(klst|mín|m?s(ek)?)\\b.*")) {
            normalized = replaceFromDict(normalized, NormalizationDictionaries.getTimeDict());
        }
        if (normalized.matches(".*(\\W|^)((ma?\\.?)?[Kk]r\\.?-?|C(HF|AD|ZK)|(DK|SE|NO)K|EUR|GBP|I[NS]K|JPY|PTE|(AU|US)D|mlj[óa]\\.?)((\\W|$)|[$£¥])(.*)")) {
            normalized = replaceFromDict(normalized, NormalizationDictionaries.getCurrencyDict());
        }
        if (normalized.matches(".*\\b([kMGT]?(V|Hz|B|W|W\\.?(st|h)))\\.?\\b.*")) {
            normalized = replaceFromDict(normalized, NormalizationDictionaries.getElectronicDict());
        }
        return normalized;
    }

    private String replaceFromDict(String text, Map<String, String> dict) {
        for (String regex : dict.keySet()) {
            Pattern pattern = Pattern.compile(regex);
            text = replacePattern(text, pattern, getExpression(dict.get(regex)));
        }
        return text;
    }

    // Replace a given regex with a
    private String replacePattern(String text, Pattern regex, Function<Matcher, String> converter) {
        int lastIndex = 0;
        StringBuilder normalized = new StringBuilder();
        Matcher matcher = regex.matcher(text);
        while(matcher.find()) {
            System.out.println(text);
            System.out.println(matcher.groupCount());
            normalized.append(text, lastIndex, matcher.start())
                    .append(converter.apply(matcher));
            lastIndex = matcher.end();
        }
        if (lastIndex < text.length()) {
            normalized.append(text, lastIndex, text.length());
        }
        return normalized.toString();
    }

    // Build the lambda expression for the replacement pattern.
    // The replacement pattern consists of group indices and replacement strings, indicating
    // how the normalization for a given pattern should be performed.
    // Example:
    // replacement: "1xsome-replacementx2
    // lambda expression: match -> match.group(1) + "some-replacement" + match.group(2)
    private Function<Matcher, String> getExpression(String replacement) {
        String[] elements = replacement.split("x");
        // map of group indices, map index indicating where in the lambda expression the group index should be
        Map<Integer,Integer> groupMap = new HashMap<>();
        // map of string replacements, map index indicating where in the lambda expression the group index should be
        Map<Integer,String> replacementMap = new HashMap<>();
        StringBuilder replacementPattern = new StringBuilder();
        // extract group indices and replacement strings and
        // build the replacement pattern
        for (int i = 0; i < elements.length; i++) {
            if (elements[i].trim().matches("\\d+")) {
                groupMap.put(i, Integer.parseInt(elements[i].trim()));
                replacementPattern.append(INT);
            }
            else {
                replacementMap.put(i, elements[i]);
                replacementPattern.append(STR);
            }
        }
        if (replacementPattern.toString().equals(INTSTRINT))
            return match -> match.group(groupMap.get(0)) + replacementMap.get(1) + match.group(groupMap.get(2));
        if (replacementPattern.toString().equals(STRINT))
            return match -> replacementMap.get(0) + match.group(groupMap.get(1));
        if (replacementPattern.toString().equals(INTSTRINTINT))
            return match -> match.group(groupMap.get(0)) + replacementMap.get(1)
                    + match.group(groupMap.get(2)) + match.group(groupMap.get(3));
        if (replacementPattern.toString().equals(INTINTSTRINT))
            return match -> match.group(groupMap.get(0)) + match.group(groupMap.get(1)) + replacementMap.get(2)
                    + match.group(groupMap.get(3));
        if (replacementPattern.toString().equals(INTINTSTRINTINT))
            return match -> match.group(groupMap.get(0)) + match.group(groupMap.get(1)) + replacementMap.get(2)
                    + match.group(groupMap.get(3)) + match.group(groupMap.get(4));
        else
            return match -> replacement;
    }

    private Map<String, String> readAbbreviations() {
        Map<String, String> regexMap = new HashMap<>();
        Resources res = this.mContext.getResources();
        int resID = R.raw.abbr_regex_1;
        String line = "";
        try {
            InputStream is = res.openRawResource(resID);
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            if (is != null) {
                while ((line = reader.readLine()) != null) {
                    String[] regexes = line.trim().split("\t");
                    if (regexes.length == 2) {
                        regexMap.put(regexes[0], regexes[1]);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return regexMap;
    }

}
