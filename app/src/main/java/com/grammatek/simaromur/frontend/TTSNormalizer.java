package com.grammatek.simaromur.frontend;

import androidx.annotation.NonNull;

import com.grammatek.simaromur.device.SymbolsLvLIs;

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

    private final List<CategoryTuple> BigCardinalFilledTupleList = Stream.of(CardinalOnesTuples.getTuples(), CardinalThousandTuples.getTuples(), CardinalMillionTuples.getTuples(),
                    CardinalBigTuples.getTuples())
            .flatMap(Collection::stream)
            .collect(Collectors.toList());
    private final List<CategoryTuple> BigCardinalTupleList = Stream.of(CardinalMillionTuples.getTuples(), CardinalBigTuples.getTuples())
            .flatMap(Collection::stream)
            .collect(Collectors.toList());
    private final List<CategoryTuple> OnesThousandsOrdinalTupleList = Stream.of(OrdinalOnesTuples.getTuples(), OrdinalThousandTuples.getTuples(),
                    CardinalThousandTuples.getTuples())
            .flatMap(Collection::stream)
            .collect(Collectors.toList());
    private final List<CategoryTuple> OnesThousandsCardinalTupleList = Stream.of(CardinalOnesTuples.getTuples(), CardinalThousandTuples.getTuples())
            .flatMap(Collection::stream)
                    .collect(Collectors.toList());
    private final List<CategoryTuple> ThousandsMillionsTupleList = Stream.of(OnesThousandsCardinalTupleList, CardinalMillionTuples.getTuples())
            .flatMap(Collection::stream)
            .collect(Collectors.toList());
    private final List<CategoryTuple> DecimalThousandsTupleList = Stream.of(OnesThousandsCardinalTupleList, DecimalThousandTuples.getTuples())
            .flatMap(Collection::stream)
            .collect(Collectors.toList());
    public static String VOWELS = "[AEIOUYÁÉÍÓÚÝÖaeiouyáéíóúýö]";
    // Max length of a token that should be spelled out, even if it contains a vowel.
    // Do we have examples of longer tokens?
    public static Integer MAX_SPELLED_OUT = 4;

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
        // most standard abbreviations - they don't necessarily contain a dot, so all sentences
        // are tested for abbreviations
        normalized = replaceFromDict(normalized, NormalizationDictionaries.abbreviationDict);

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
        if (tokens.length != tags.length) {
            //Log.w(LOG_TAG, "tokens and tags have different lengths, returning empty string");
            return "";
        }

        String token;
        String nextTag;
        String lastToken = "";
        StringBuilder sb = new StringBuilder();
        final String linksPattern = NormalizationDictionaries.links.get(NormalizationDictionaries.LINK_PTRN_ALL);

        // we always look at the next tag, hence only iterate up to length-2
        for (int i = 0; i < tags.length - 1; i++) {
            token = tokens[i];
            nextTag = tags[i + 1];
            // in case the tag is "§", this special character returned from the PoS tagging function
            // means, we don't have any numbers at all in tokens and PoS tagging has been skipped
            if ((!nextTag.equals("§")) && token.matches(".*\\d.*")) {
                token = normalizeNumber(token, nextTag);
            }
            // add space between upper case letters, if they do not build known Acronyms like "RÚV"
            else if (token.matches(NumberHelper.LETTERS_PTRN) && token.length() > 1) {
                token = processLettersPattern(token);
            }
            else if (linksPattern != null) {
                if (token.matches(linksPattern) || token.matches(".+@.+"))
                    token = normalizeURL(token);
                else if (token.length() > 1 && token.charAt(0) == token.charAt(1))
                    token = insertSpaces(token);
                else if (token.matches(NormalizationDictionaries.NOT_LETTER))
                    token = normalizeDigits(token);
            }

            sb.append(token.trim()).append(" ");
            lastToken = tokens[i + 1];
        }
        if (tokens.length == 1 && tokens[0].matches(Tokenizer.EOS_SYMBOLS))
            lastToken = tokens[0];
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
            final String regexS = dict.get(regex);
            if (regexS != null) {
                replaced = matcher.replaceAll(regexS);
            }
        }
        return replaced;
    }

    /*
    * Pronounce letter patterns/uppercase tokens letter by letter
    * UNLESS they are either contained in the pronunciation dictionary or
    * pass the test in pronounceAsWord(). However, all shorter tokens than MAX_SPELLED_OUT
    * that are not contained in the dictionary are returned as spelled out.
    *
    * //TODO:
    * We might still run occasionally into problems here: the acronym 'FÁ' (a school in Reykjavík)
    * should always be spelled out, but the verb 'fá' could also be in the dictionary causing
    * this token to be returned as a word.
    * We could keep some kind of a whitelist, but we also need context: if 'FÁ' occurs in lowercase
    * context ("she studies at FÁ") then it should be spelled out, but in an uppercase context
    * ("ÉG ÆTLA AÐ FÁ ÞETTA") it should be spoken as word.
    *
    * Example:
    *  - ELDGOS -> eldgos (in dict)
    *  - UNESCO -> unesco (pronounceAsWord: true)
    *  - ASÍ -> a s í (too short to be sent to pronounceAsWord)
    */
    private String processLettersPattern(String token) {
        String lower = token.toLowerCase();
        if (TTSUnicodeNormalizer.inDictionary(lower))
            return lower;
        // if we have a short uppercase token that isn't in the dictionary, let's spell it out
        // check for vowel consonant ratio for longer tokens
        if (lower.length() > MAX_SPELLED_OUT && pronounceAsWord(lower))
            return lower;
        token = insertSpaces(token);
        return token.toLowerCase();
    }

    /*
     * Insert space between all characters in 'token'
     */
    private String insertSpaces(String token) {
        // insert space after each character, we really mean "." here, not "\\."
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
        String normalized;
        // default for one digit numbers masc and not neutr as in original Regina
        // 1, 2, 3, 4
        if (numberToken.matches("\\d") && (nextTag.isEmpty() || nextTag.equals("p"))) {
            normalized = NumberHelper.DIGIT_NUMBERS.get(numberToken);
        }
        else if (numberToken.matches(NumberHelper.CARDINAL_BIG_PTRN)) {
            final Map<String, Map<String, String>> cardinalMillionsDict = makeDict(numberToken, NumberHelper.INT_COLS_BIG);
            normalized = fillDict(numberToken, nextTag, BigCardinalFilledTupleList, cardinalMillionsDict, NumberHelper.INT_COLS_BIG);
            //normalized = fillDict(numberToken, nextTag, BigCardinalTupleList, cardinalMillionsDict, NumberHelper.INT_COLS_BIG);
        }
        //1.234. or 1. or 12. or 123.
        else if (numberToken.matches(NumberHelper.ORDINAL_THOUSAND_PTRN)) {
            // should look like: {token: {thousands: "", hundreds: "", dozens: "", ones: ""}}
            final Map<String, Map<String, String>> ordinalThousandDict = makeDict(numberToken, NumberHelper.INT_COLS_THOUSAND);
            normalized = fillDict(numberToken, nextTag, OnesThousandsOrdinalTupleList, ordinalThousandDict, NumberHelper.INT_COLS_THOUSAND);
        }
        //1.234 or 1 or 12 or 123
        else if (numberToken.matches(NumberHelper.CARDINAL_THOUSAND_PTRN)) {
            normalized = normalizeThousandDigit(numberToken, nextTag);
        }
        //1.234 or 12.345 or 123.456 -> asking the same thing twice, check
        else if (numberToken.matches(NumberHelper.CARDINAL_MILLION_PTRN)) {
            // should look like: {token: {thousands: "", hundreds: "", dozens: "", ones: ""}}
            final Map<String, Map<String, String>> cardinalMillionDict = makeDict(numberToken, NumberHelper.INT_COLS_MILLION);
            normalized = fillDict(numberToken, nextTag, ThousandsMillionsTupleList, cardinalMillionDict, NumberHelper.INT_COLS_MILLION);
        }
        //1.123,4 or 1232,4 or 123,4 or 12,42345 or 1,489 ; NOT: 12345,5
        else if (numberToken.matches(NumberHelper.DECIMAL_THOUSAND_PTRN)) {
            // should look like: {token: {"first_ten", "first_one","between_teams","second_ten", "second_one"}}
            final Map<String, Map<String, String>> decimalDict = makeDict(numberToken, NumberHelper.DECIMAL_COLS_THOUSAND);
            normalized = fillDict(numberToken, nextTag, DecimalThousandsTupleList, decimalDict, NumberHelper.DECIMAL_COLS_THOUSAND);
        }
        // 01:55 or 01.55
        else if (numberToken.matches(NumberHelper.TIME_PTRN)) {
            // should look like: {token: {"first_ten", "first_one","between_teams","second_ten", "second_one"}}
            final Map<String, Map<String, String>> timeDict = makeDict(numberToken, NumberHelper.TIME_SPORT_COLS);
            normalized = fillDict(numberToken, nextTag, TimeTuples.getTuples(), timeDict, NumberHelper.TIME_SPORT_COLS);
        }
        else if (numberToken.matches("^\\d{1,2}/\\d{1,2}$")) {
            // if domain == "other" - do other things, below is the handling for sport results:
            // should look like: {token: {"first_ten", "first_one","between_teams","second_ten", "second_one"}}
            final Map<String, Map<String, String>> sportsDict = makeDict(numberToken, NumberHelper.TIME_SPORT_COLS);
            normalized = fillDict(numberToken, nextTag, SportTuples.getTuples(), sportsDict, NumberHelper.TIME_SPORT_COLS);
        }
        // 4/8 or ⅓ , etc.
        else if (numberToken.matches(NumberHelper.FRACTION_PTRN)) {
            String[] splitted = numberToken.split("/");
            String part1 = splitted[0];
            String part2 = splitted[1];
            if (part1.matches(NumberHelper.CARDINAL_THOUSAND_PTRN))
                part1 = normalizeThousandDigit(part1, nextTag);
            else
                part1 = normalizeNumber(part1, nextTag);
            if (part2.matches(NumberHelper.CARDINAL_THOUSAND_PTRN))
                part2 = normalizeThousandDigit(part2, nextTag);
            else
                part2 = normalizeNumber(part2, nextTag);
            normalized = part1 + " " + SymbolsLvLIs.TagPause + " " + part2;
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

    private String normalizeThousandDigit(String numberToken, String nextTag) {
        // should look like: {token: {thousands: "", hundreds: "", dozens: "", ones: ""}}
        final Map<String, Map<String, String>> cardinalThousandDict = makeDict(numberToken, NumberHelper.INT_COLS_THOUSAND);
        return fillDict(numberToken, nextTag, OnesThousandsCardinalTupleList, cardinalThousandDict, NumberHelper.INT_COLS_THOUSAND);
    }

    /*
     * Normalize URLs, e-mail addresses, and twitter/instagram handles ("@my_twitter_name").
     *
     * This is an "everyday-friendly" implementation, if requests for 1:1 reading of URLs and similar patterns
     * come up, we need to implement an additional handling of those tokens.
     * By "everyday-friendly" we mean e.g. skipping the initial 'www' and/or 'http', which is cumbersome
     * to pronounce in Icelandic ("tvöfalt vaff tvöfalt vaff ...").
     * Example:
     *  https://www.visir.is/     becomes: vísir punktur is
     *
     * Possible input patterns:
     *  - starting with http or www
     *  - starting with http or file, possibly including localhost
     *  - e-mail patterns
     *  - twitter and instagram handles (containing '@')
     */
    private String normalizeURL(String token) {
        String normalized = "";
        // analyse a URL or e-mail pattern
        if (token.indexOf('.') > 0) {
            normalized = processTokenWithDots(token);
        }
        else
            normalized = token;
        // don't pronounce the last "slash" at the end of a URL
        // "mbl.is/frettir/" becomes "m b l punktur is skástrik fréttir"
        if (normalized.endsWith("/"))
            normalized = normalized.substring(0, normalized.length() - 1);
        // replace symbols that might be left in the normalized string
        for (String symbol : NumberHelper.WLINK_NUMBERS.keySet()) {
            normalized = normalized.replaceAll(symbol,
                    " " + NumberHelper.WLINK_NUMBERS.get(symbol) + " ");
        }
        return normalized;
    }

    @NonNull
    private String processTokenWithDots(String token) {
        StringBuilder normalized = new StringBuilder();
        String[] arr = token.split("\\.");
        String prefix = arr[0];
        // only keep the first part if it is not an 'http' or a 'www' prefix
        if (prefix.matches("https?://.+")) {
            prefix = prefix.substring(prefix.indexOf("//") + 2);
        }
        if (prefix.startsWith("www")) {
            prefix = "";
        }
        // process each element between dots, insert " punktur " (" dot ") between
        // elements
        String tokenRepr;
        for (int i = 0; i < arr.length; i++) {
            String tok;
            if (i == 0) {
                tok = prefix;
            }
            else
                tok = arr[i];
            tokenRepr = processElement(tok);
            if (normalized.length() == 0)
                normalized.append(tokenRepr);
            else
                normalized.append(" punktur ").append(tokenRepr);
        }
        return normalized.toString();
    }

    /*
     * Process an element from a URL or an e-mail address, an element between two dots.
     * The element might contain "@", we split on that and send on to core element processing.
     * '@' is replaced with ' hjá ' (Icelandic for "at")
     */
    private String processElement(String token) {
        StringBuilder processed = new StringBuilder();
        String[] arr = token.split("@");
        if (arr.length > 1) {
            for (String s : arr) {
                String processedElem = processURLElement(s);
                if (processed.length() == 0)
                    processed.append(processedElem);
                else
                    processed.append(" hjá ").append(processedElem);
            }
        }
        else
            processed = new StringBuilder(processURLElement(token));
        return processed.toString();
    }

    /*
     * Process a single element from a URL or an e-mail address, e.g. "mbl" from "www.mbl.is".
     * Convert to Icelandic orthography if necessary or insert spaces for spelled out
     * pronunciation.
     */
    private String processURLElement(String token) {
        if (pronounceAsWord(token)) {
            return convert2Ice(token);
        }
        else {
            return insertSpaces(token);
        }
    }

    /*
     * Should this token be spoken as a word or spelled out?
     * Simple heuristic, a token having a reasonable vowel/consonant ratio should be
     * spoken as a word, otherwise spelled out
     */
    private boolean pronounceAsWord(String token) {
        Pattern vowelPattern = Pattern.compile(VOWELS);
        Matcher matcher = vowelPattern.matcher(token);
        double count = 0.0;
        while (matcher.find())
            count++;

        // Somewhat arbitrary ratio, we have an example like 'Busch' with a vowel-consonant
        // ratio of 0.2 where we definitely want to keep the token as is.
        // TODO: examine this further as we get more borderline examples
        return count/token.length() > 0.15;
    }

    /* URLs mostly contain ASCII characters only, but we want them to be read as if they
     * were written including Icelandic letters from the original word where appropriate.
     *
     * Examples: visir -> vísir, ruv -> rúv, fotbolti -> fótbolti
     * This is necessary for the correct dictionary lookup or g2p
     *
     * The input token might contain '/', if so, we split and process each token separately
     * and concatenate the '/' again afterwards.
     */
    private String convert2Ice(String token) {
        StringBuilder converted = new StringBuilder();
        String[] arr = token.split("/");
        for (String s : arr) {
            converted.append(NormalizationDictionaries.urlElements.getOrDefault(s, s));
            if (arr.length > 1)
                converted.append("/");
        }
        return converted.toString();
    }

    private String normalizeDigitOrdinal(String token) {
        for (String digit : NumberHelper.DIGITS_ORD.keySet())
            token = token.replaceAll("^0" + digit + "\\.$",
                    "núll " + NumberHelper.DIGITS_ORD.get(digit));
        return token;
    }

    /* The default/fallback normalization method. We have checked all patterns that might need special handling
     * now we simply replace digits by their default word representation.
     * The replacement process replaces single digits and replaces them with their
     * nominative representation, also  '+', '/', ':' are replaced.
     */
    private String normalizeDigits(String token) {
        token = token.replaceAll(" ", SymbolsLvLIs.TagPause + " ");
        for (String digit : NumberHelper.DIGIT_NUMBERS.keySet()) {
            final String replacement = NumberHelper.DIGIT_NUMBERS.get(digit);
            if (replacement != null) {
                token = token.replaceAll(digit, replacement);
            }
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
        StringBuilder result = new StringBuilder();

        for (int i = 0; i < tuples.size(); i++) {
            String numberPattern = tuples.get(i).getNumberPattern();
            String rule = tuples.get(i).getRule();
            if (token.matches(".*" + numberPattern + ".*") && tag.matches(".*" + rule)) {
                if (typeDict.containsKey(token)) {
                    final String category = tuples.get(i).getCategory();
                    if (typeDict.get(token).containsKey(category)) {
                        Map<String, String> tmp = typeDict.get(token);
                        tmp.put(category, tuples.get(i).getExpansion());
                        // not really necessary, since the previous assignment updates the map in
                        // typeDict, but this is more clear
                        typeDict.put(token, tmp);
                    }
                }
            }
        }
        for (String s : columns)
            result.append(typeDict.get(token).get(s));

        return result.toString();
    }

}
