package com.grammatek.simaromur.frontend;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Regular expressions-based text normalizer for TTS.
 * Relies on unicode normalized input text, see TTSUnicodeNormalizer, that has been tokenized with
 * the Tokenizer.
 *
 * The normalizer is based on the Regina Normalizer for Icelandic, an open source Python
 * package accessible here: https://github.com/cadia-lvl/regina-normalizer
 *
 */

public class TTSNormalizer {

    /*
    public TTSNormalizer(Context context) {
        this.mContext = context;
        this.mRegexMap = readAbbreviations();
    }*/

    public TTSNormalizer() {

    }

    /**
     * Before looking at context we replace and expand some abbreviations. This facilitates tagging and later expanding.
     *
     * @param text input text, unicode-normalized and if splitted on whitespace we have an array of tokens
     * @return pre-normalized text, i.e. some common abbreviations expanded
     */
    public String preNormalize(String text) {
        String normalized = text;
        String domain = ""; //we will need to determine this from "text" in real life!

        // some pre-processing and formatting of digits
        if (normalized.matches(".*\\d.*")) {
            normalized = replaceFromDict(normalized, NormalizationDictionaries.preHelpDict);
        }
        // process strings containing a hyphen, affects weather description and combination of letters and hyphen
        if (normalized.contains("-")) {
            normalized = replaceFromDict(normalized, NormalizationDictionaries.directionDict);
            normalized = replaceFromDict(normalized, NormalizationDictionaries.hyphenDict);
        }
        // most standard abbreviations
        if (normalized.contains(".")) {
            normalized = replaceFromDict(normalized, NormalizationDictionaries.abbreviationDict);
        }
        // looking for patterns like "500 kr/kg"
        if (normalized.contains("/")) {
            normalized = replaceFromDict(normalized, NormalizationDictionaries.denominatorDict);
        }
        if (normalized.matches(".*\\d.*")) {
            normalized = replaceFromDict(normalized, NormalizationDictionaries.weightDict);
        }
        if (normalized.matches(".*\\b([pnµmcsdkN]?m|ft)\\.?\\b.*")) {
            normalized = replaceFromDict(normalized, NormalizationDictionaries.getDistanceDict());
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
        if (normalized.matches(".*(%|\\b(stk|[Kk][Cc]al)\\.?\\b).*")) {
            normalized = replaceFromDict(normalized, NormalizationDictionaries.restDict);
        }
        // if we have domain "sport" a hyphen between numbers is silent, otherwise it is normalized to "til"
        if (normalized.matches(".*-.*")) {
            normalized = replaceHyphen(normalized, domain);
        }
        return normalized;
    }

    /**
     * Performs normalizing of text partly based on POS-tags. For number normalization the algorithm looks at
     * the POS-tags at the next token position, to determine the correct form of the normalization (case, gender, etc.)
     *
     * @param tokens an array of tokens, some of which might have to be normalized
     * @param tags an array of POS-tags, corresponding to the tokens in 'tokens'
     * @return a normalized string created from the normalized tokenis in 'tokens'
     */
    public String postNormalize(String[] tokens, String[] tags) {
        // tokens and tags have to match - tag at index 'i' should be the tag for the token at index 'i'
        if (tokens.length != tags.length)
            return "";

        String token;
        String nextTag;
        String lastToken = "";
        StringBuilder sb = new StringBuilder();
        String linksPattern = NormalizationDictionaries.links.get(NormalizationDictionaries.LINK_PTRN_ALL);

        // we always look at the next tag, hence only iterate up to length-2
        for (int i = 0; i < tags.length - 1; i++) {
            token = tokens[i];
            nextTag = tags[i + 1];
            if (token.matches(".*\\d.*")) {
                token = normalizeNumber(token, nextTag);
            }
            // add space between upper case letters, if they do not build known Acronyms like "RÚV"
            else if (token.matches(NumberHelper.LETTERS_PTRN) && token.length() > 1) {
                if (TTSUnicodeNormalizer.inDictionary(token))
                    token = token.toLowerCase();
                else
                    token = insertSpaces(token);
            }
            else if (token.length() > 1 && token.charAt(0) == token.charAt(1))
                token = insertSpaces(token);
            else if (token.matches(linksPattern))
                token = normalizeURL(token);
            else if (token.matches(NormalizationDictionaries.NOT_LETTER))
                token = normalizeSymbols(token);

            sb.append(token.trim()).append(" ");
            lastToken = tokens[i + 1];
        }
        sb.append(lastToken); //what if this is a digit or something that needs normalizing?
        String result = sb.toString();
        return result.replaceAll("\\s+", " ");
    }

    public String replaceFromDict(String text, Map<String, String> dict) {
        for (String regex : dict.keySet()) {
            text = replacePattern(text, regex, dict);
        }
        return text;
    }

    // Replace a given regex with the corresponding replacement pattern from 'dict'
    private String replacePattern(String text, String regex, Map<String, String> dict) {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(text);
        String replaced = text;
        while (matcher.find()) {
            replaced = matcher.replaceAll(dict.get(regex));
        }
        return replaced;
    }

    private String insertSpaces(String token) {
        return token.replaceAll(".", "$0 ").trim();
    }

    /*
    Replace hyphens based on domain: in sport results we don't speak the hyphen between two digits, otherwise
    it is spoken as "til" (to)
     */
    private String replaceHyphen(String text, String domain) {
        String replacedText = text;
        boolean didReplace = false;
        String[] textArr = text.split(" ");
        for (int i = 2; i < textArr.length - 1; i++) {
            // pattern: "digit - digit"
            if (textArr[i].equals("-") && textArr[i - 1].matches("\\d+\\.?(\\d+)?") && textArr[i + 1].matches("\\d+\\.?(\\d+)?")) {
                if (domain.equals("sport"))
                    textArr[i] = "";
                else
                    textArr[i] = "til";
                didReplace = true;
            }
        }
        if (didReplace) {
            replacedText = Util.join(textArr);
        }
        return replacedText;
    }

    /*
    Look for matching patterns for 'numberToken' and normalize according to 'nextTag', which is the POS-tag of the
    next token in the sentence to normalize. If we don't find a match, use the default 'normalizeDigits()' to
    normalize. Return the normalized numberToken.
     */
    private String normalizeNumber(String numberToken, String nextTag) {
        String normalized = numberToken;
        //1.234. or 1. or 12. or 123.
        if (numberToken.matches(NumberHelper.ORDINAL_THOUSAND_PTRN)) {
            Map<String, Map<String, String>> ordinalThousandDict = makeDict(numberToken, NumberHelper.INT_COLS_THOUSAND); // should look like: {token: {thousands: "", hundreds: "", dozens: "", ones: ""}}
            List<CategoryTuple> mergedTupleList = Stream.of(OrdinalOnesTuples.getTuples(), OrdinalThousandTuples.getTuples(),
                    CardinalThousandTuples.getTuples())
                    .flatMap(Collection::stream)
                    .collect(Collectors.toList());
            normalized = fillDict(numberToken, nextTag, mergedTupleList, ordinalThousandDict, NumberHelper.INT_COLS_THOUSAND);
        }
        //1.234 or 1 or 12 or 123
        else if (numberToken.matches(NumberHelper.CARDINAL_THOUSAND_PTRN)) {
            Map<String, Map<String, String>> cardinalThousandDict = makeDict(numberToken, NumberHelper.INT_COLS_THOUSAND); // should look like: {token: {thousands: "", hundreds: "", dozens: "", ones: ""}}
            List<CategoryTuple> mergedTupleList = Stream.of(CardinalOnesTuples.getTuples(), CardinalThousandTuples.getTuples())
                    .flatMap(Collection::stream)
                    .collect(Collectors.toList());
            normalized = fillDict(numberToken, nextTag, mergedTupleList, cardinalThousandDict, NumberHelper.INT_COLS_THOUSAND);
        }
        //1.234 or 12.345 or 123.456 -> asking the same thing twice, check
        else if (numberToken.matches(NumberHelper.CARDINAL_MILLION_PTRN)) {
            Map<String, Map<String, String>> cardinalMillionDict = makeDict(numberToken, NumberHelper.INT_COLS_MILLION); // should look like: {token: {thousands: "", hundreds: "", dozens: "", ones: ""}}
            List<CategoryTuple> mergedTupleList = Stream.of(CardinalThousandTuples.getTuples(), CardinalMillionTuples.getTuples())
                    .flatMap(Collection::stream)
                    .collect(Collectors.toList());
            normalized = fillDict(numberToken, nextTag, mergedTupleList, cardinalMillionDict, NumberHelper.INT_COLS_MILLION);
        }
        //1.123,4 or 1232,4 or 123,4 or 12,42345 or 1,489 ; NOT: 12345,5
        else if (numberToken.matches(NumberHelper.DECIMAL_THOUSAND_PTRN)) {
            Map<String, Map<String, String>> decimalDict = makeDict(numberToken, NumberHelper.DECIMAL_COLS_THOUSAND); // should look like: {token: {"first_ten", "first_one","between_teams","second_ten", "second_one"}}

            List<CategoryTuple> mergedCardinalTupleList = Stream.of(CardinalOnesTuples.getTuples(), CardinalThousandTuples.getTuples())
                    .flatMap(Collection::stream)
                    .collect(Collectors.toList());
            List<CategoryTuple> mergedTupleList = Stream.of(mergedCardinalTupleList, DecimalThousandTuples.getTuples())
                    .flatMap(Collection::stream)
                    .collect(Collectors.toList());
            normalized = fillDict(numberToken, nextTag, mergedTupleList, decimalDict, NumberHelper.DECIMAL_COLS_THOUSAND);
        }
        // 01:55 or 01.55
        else if (numberToken.matches(NumberHelper.TIME_PTRN)) {
            Map<String, Map<String, String>> timeDict = makeDict(numberToken, NumberHelper.TIME_SPORT_COLS); // should look like: {token: {"first_ten", "first_one","between_teams","second_ten", "second_one"}}
            normalized = fillDict(numberToken, nextTag, TimeTuples.getTuples(), timeDict, NumberHelper.TIME_SPORT_COLS);
        }
        // 4/8 or ⅓ , etc.
        else if (numberToken.matches(NumberHelper.FRACTION_PTRN)) {
            // if domain == "other" - do other things, below is the handling for sport results:
            Map<String, Map<String, String>> sportsDict = makeDict(numberToken, NumberHelper.TIME_SPORT_COLS); // should look like: {token: {"first_ten", "first_one","between_teams","second_ten", "second_one"}}
            normalized = fillDict(numberToken, nextTag, SportTuples.getTuples(), sportsDict, NumberHelper.TIME_SPORT_COLS);
        }
        // 01. (what kind of ordinal is this?)
        else if (numberToken.matches("^0\\d\\.$")) {
            normalized = normalizeDigitOrdinal(numberToken);
        }
        else {
            normalized = normalizeDigits(numberToken);
        }
        return normalized;
    }

    /*
     * Normalize URLs and e-mail addresses. In the current implementation, everything but the suffix .com / .is / .org
     * is separated character by character. We need to have a method that allows us to speak urls like "rúv punktur is",
     * "visir punktur is" etc. instead of "r u v punktur is", as is done right now.
     */
    private String normalizeURL(String token) {
        String normalized = token;
        int ind = token.indexOf('.');
        // does the token end with a domain name?
        if (ind >= 2) {
            String prefix = token.substring(0, ind);
            String suffix = token.substring(ind + 1);
            // how can we choose which words to keep as words and which to separate?
            prefix = insertSpaces(prefix);

            for (String symbol : NumberHelper.WLINK_NUMBERS.keySet()) {
                prefix = prefix.replaceAll(symbol, NumberHelper.WLINK_NUMBERS.get(symbol));
            }
            if (suffix.indexOf('/') > 0) {
                String postSuffix = processSubPath(suffix);
                suffix = suffix.substring(0, suffix.indexOf('/')) + " " + postSuffix;
            }
            normalized = prefix + " punktur " + suffix;
        }
        // we do not have a domain name, maybe a twitter handle with @ symbol
        else {
            normalized = insertSpaces(token);
            for (String symbol : NumberHelper.WLINK_NUMBERS.keySet()) {
                normalized = normalized.replaceAll(symbol, NumberHelper.WLINK_NUMBERS.get(symbol));
            }
            if (normalized.indexOf('/') > 0) {
                String postSuffix = processSubPath(token);
                normalized = normalized.substring(0, normalized.indexOf('/')) + " " + postSuffix;
            }
        }
        return normalized;
    }

    // that token contains '/' should be checked before calling this method
    private String processSubPath(String token) {
        String postSuffix = token.substring(token.indexOf('/'));
        postSuffix = insertSpaces(postSuffix);
        for (String symbol : NumberHelper.WLINK_NUMBERS.keySet()) {
            postSuffix = postSuffix.replaceAll(symbol, NumberHelper.WLINK_NUMBERS.get(symbol));
        }
        return postSuffix;
    }

    private String normalizeSymbols(String token) {
        for (String symbol : NumberHelper.DIGIT_NUMBERS.keySet()) {
            token = token.replaceAll(symbol, NumberHelper.DIGIT_NUMBERS.get(symbol));
        }
        return token;
    }

    private String normalizeDigitOrdinal(String token) {
        for (String digit : NumberHelper.DIGITS_ORD.keySet())
            token = token.replaceAll("^0" + digit + "\\.$", "núll " + NumberHelper.DIGITS_ORD.get(digit));
        return token;
    }

    /* The default/fallback normalization method. We have checked all patterns that migth need special handling
     * now we simply replace digits by their default word representation.
     */
    private String normalizeDigits(String token) {
        token = token.replaceAll(" ", "<sil> ");
        for (String digit : NumberHelper.DIGIT_NUMBERS.keySet()) {
            token = token.replaceAll(digit, NumberHelper.DIGIT_NUMBERS.get(digit));
        }
        return token;
    }

    /*
     * Initializes a map to hold the digit positions for a token, e.g.:
     * {token: {thousands: "", hundreds: "", dozens: "", ones: ""}}
     */
    private Map<String, Map<String, String>> makeDict(String token, String[] columns) {
        Map<String, Map<String, String>> valueDict = new HashMap<>();
        Map<String, String> innerMap = new HashMap<>();
        for (String s : columns)
            innerMap.put(s, "");
        valueDict.put(token, innerMap);
        return valueDict;
    }

    /*
     * Fills a map that holds the digit positions for a token, e.g.:
     * {"1983": {thousands: "", hundreds: "nítján hundruð", dozens: " áttatíu og", ones: "þrjú"}}
     * Returns a string combined of the values, e.g.: "nítján hundruð áttatíu og þrjú"
     */
    private String fillDict(String token, String tag, List<CategoryTuple> tuples, Map<String, Map<String, String>> typeDict, String[] columns) {
        String result = "";

        for (int i = 0; i < tuples.size(); i++) {
            if (token.matches(".*" + tuples.get(i).getNumberPattern() + ".*") && tag.matches(".*" + tuples.get(i).getRule())) {
                if (typeDict.containsKey(token)) {
                    if (typeDict.get(token).containsKey(tuples.get(i).getCategory())) {
                        Map<String, String> tmp = typeDict.get(token);
                        tmp.put(tuples.get(i).getCategory(), tuples.get(i).getExpansion());
                        typeDict.put(token, tmp); // not really necessary, since the previous assignment updates the map in typeDict, but this is more clear
                    }
                }
            }
        }
        for (String s : columns)
            result += typeDict.get(token).get(s);

        return result;
    }

}
