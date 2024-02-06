package com.grammatek.simaromur.frontend;

import org.apache.commons.collections4.OrderedMap;
import org.apache.commons.collections4.map.ListOrderedMap;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;


/**
 * Normalization dictionaries for abbreviations, digits and other non-standard-words.
 * TODO: This class still needs refactoring!
 * For reference files see: https://github.com/cadia-lvl/regina-normalizer
 */

public class NormalizationDictionaries {

    public static final String ACC = "ACC"; // always acc
    public static final String DAT = "DAT"; // always dat
    public static final String GEN = "GEN"; // always gen
    public static final String ACC_DAT = "ACC_DAT"; // acc or dat
    public static final String ACC_GEN = "ACC_GEN"; // ACC + GEN
    public static final String ACC_DAT_COMB = "ACC_DAT_COMB"; // ACC + DAT + ACC_DAT
    public static final String ACC_DAT_GEN_COMB = "ACC_DAT_GEN_COMB"; // ACC + DAT + ACC_DAT + GEN
    public static final String AMOUNT = "AMOUNT";
    public static final String LINK_PTRN_EXTERNAL = "LINK_PTRN_EXTERNAL";
    public static final String LINK_PTRN_INTERNAL = "LINK_PTRN_INTERNAL";
    public static final String LINK_PTRN_MAIL = "LINK_PTRN_MAIL";
    public static final String LINK_PTRN_HASHTAG = "LINK_PTRN_HASHTAG";
    public static final String LINK_PTRN_ALL = "LINK_PTRN_ALL";

    //regex bits:
    public static final String MATCH_ANY = ".*";
    public static final String BOS = "([^\\wÁÉÍÓÚÝÐÞÆÖáéíóúýðþæö]|^)"; // non-word char OR beginning of string
    public static final String EOS = "([^\\wÁÉÍÓÚÝÐÞÆÖáéíóúýðþæö]|$)"; // non-word char OR end of string, note that \\w and \\W are ascii-based! //TODO: check with Helga
    public static final String DOT = "\\."; // escaped dot
    public static final String DOT_ONE_NONE = "\\.?"; // escaped dot
    public static final String LETTER = "[A-ZÁÉÍÓÚÝÐÞÆÖa-záéíóúýðþæö]";
    public static final String LETTER_OR_DIGIT = "[A-ZÁÉÍÓÚÝÐÞÆÖa-záéíóúýðþæö\\d]";
    public static final Pattern NOT_LETTER = Pattern.compile("[^A-ZÁÉÍÓÚÝÐÞÆÖa-záéíóúýðþæö]");
    public static final String MONTH = "jan(úar)?|feb(rúar)?|mars?|apr(íl)?|maí|jú[nl]í?|ág(úst)?|sep(t(ember)?)?|okt(óber)?|nóv(ember)?|des(ember)?";
    public static final String THE_CLOCK = "(núll|eitt|tvö|þrjú|fjögur|fimm|sex|sjö|átta|níu|tíu|ellefu|tólf" +
            "|((þret|fjór|fimm|sex)tán)|((sau|á|ní)tján)|tuttugu( og (eitt|tvö|þrjú|fjögur))?)";
    // e.g. 12.345.678,59 or 34,5 etc. //TODO: this would match 12.345.1234556677,34 -> is this deliberate?
    public static final String MEASURE_PREFIX_DIGITS = "((\\d{1,2}" + DOT + ")?(\\d{3}" + DOT + ")*\\d{3}|\\d+)(,\\d+)?";
    public static final String MEASURE_PREFIX_WORDS = "([Hh]undr[au]ð|HUNDR[AU]Ð|[Þþ]úsund|ÞÚSUND|[Mm]illjón(ir)?|MILLJÓN(IR)?)";

    // any number, (large, small, with or without thousand separators, with or without a decimal point) that does NOT end with a "1"
    public static final String NUMBER_EOS_NOT_1 = "(\\d{1,2}\\.)?(\\d{3}\\.?)*(\\d*[02-9]|\\d,\\d*[02-9]))";
    // any number, (large, small, with or without thousand separators, with or without a decimal point) that DOES end with a "1"
    // TODO: ask Helga: this does not quite match, since a larger number than one digit plus decimal point does not match
    // e.g. 21,1 does not match, whereas 1,1 matches
    public static final String NUMBER_EOS_1 = "(\\d{1,2}\\.)?(\\d{3}\\.?)*(\\d*1|\\d,\\d*1)";
    // any number (large, small, with or without thousand separators, with or without a decimal point)
    public static final String NUMBER_ANY = "(((\\d{1,2}\\.)?(\\d{3}\\.?)*|\\d+)(,\\d+)?)?";

    private NormalizationDictionaries() {}

    // words from common URLs written in ASCII only, mapped to their Icelandic orthography
    public static final Map<String, String> urlElements = new HashMap<>();
    static {
        urlElements.put("adrar-ithrottir", "aðrar íþróttir");
        urlElements.put("dagskra", "dagskrá");
        urlElements.put("fotbolti", "fótbolti");
        urlElements.put("frettabladid", "fréttablaðið");
        urlElements.put("frettir", "fréttir");
        urlElements.put("frjalsar", "frjálsar");
        urlElements.put("ithrottir", "íþróttir");
        urlElements.put("jardskjalftar", "jarðskjálftar");
        urlElements.put("keflavik", "keflavík");
        urlElements.put("korfubolti", "körfubolti");
        urlElements.put("mannlif", "mannlíf");
        urlElements.put("ruv", "rúv");
        urlElements.put("skjalftar-og-eldgos", "skjálftar og eldgos");
        urlElements.put("sjonvarp", "sjónvarp");
        urlElements.put("stadaspar", "staðaspár");
        urlElements.put("utvarp", "útvarp");
        urlElements.put("vedur", "veður");
        urlElements.put("visir", "vísir");
    }

    // prepositions control the case of the following word, can be used to determine number formats
    public static final Map<String, String> prepositions = new HashMap<>();
    static {
        prepositions.put(ACC, "um(fram|hverfis)|um|gegnum|kringum|við|í|á");
        prepositions.put(DAT, "frá|a[ðf]|ásamt|gagnvart|gegnt?|handa|hjá|með(fram)?|móti?|undan|nálægt");
        prepositions.put(GEN, "til|auk|án|handan|innan|meðal|megin|milli|ofan|sakir|sökum|utan|vegna");
        prepositions.put(ACC_DAT, "eftir|fyrir|með|undir|við|yfir");
        prepositions.put(ACC_GEN, "um(fram|hverfis)|um|gegnum|kringum|við|í|á|til|auk|án|handan|innan|meðal|megin|milli|ofan|sakir|sökum|utan|vegna");
        prepositions.put(ACC_DAT_COMB, "um(fram|hverfis)|um|gegnum|kringum|við|í|á|frá|a[ðf]|ásamt|gagnvart|gegnt?|handa|hjá|með(fram)?|móti?|undan|nálægt|eftir|fyrir|með|undir|við|yfir");
        prepositions.put(ACC_DAT_GEN_COMB, "um(fram|hverfis)|um|gegnum|kringum|við|í|á|frá|a[ðf]|ásamt|gagnvart|gegnt?" +
                "|handa|hjá|með(fram)?|móti?|undan|nálægt|eftir|fyrir|með|undir|við|yfir|til|auk|án|handan|innan|meðal|megin|milli|ofan|sakir|sökum|utan|vegna");
    }

    //The link patterns handle external patterns like https://mbl.is/innlent, internal patterns like https://localholst:8888,
    //mail patterns like name@address.com, twitter handles like @handle and hashtags, e.g. #thisrules2021
    public static final Map<String, String> patternSelection = new HashMap<>();
    static {
        patternSelection.put(AMOUNT, "(hundr[au]ð|þúsund|milljón(ir)?)");
        patternSelection.put(LINK_PTRN_EXTERNAL, "((https?:\\/\\/)?(www\\.)?([A-ZÁÐÉÍÓÚÝÞÆÖa-záðéíóúýþæö\\d\\-_\\.\\/]+)?\\.[A-ZÁÐÉÍÓÚÝÞÆÖa-záðéíóúýþæö\\d\\-_\\.\\/]+)");
        patternSelection.put(LINK_PTRN_INTERNAL, "((file|(https?:\\/\\/)?localhost):[A-ZÁÐÉÍÓÚÝÞÆÖa-záðéíóúýþæö\\d_\\?\\/\\.=\\-\\&\\%\\#]+)");
        patternSelection.put(LINK_PTRN_MAIL, "([A-ZÁÐÉÍÓÚÝÞÆÖa-záðéíóúýþæö\\d\\-_\\.]*@[A-ZÁÐÉÍÓÚÝÞÆÖa-záðéíóúýþæö\\d\\-_\\.]+(\\.[A-Za-z])?)");
        patternSelection.put(LINK_PTRN_HASHTAG, "(# ?[A-ZÁÐÉÍÓÚÝÞÆÖa-záðéíóúýþæö\\d\\-_]+)");
    }
    public static final Map<String, Pattern> links = new HashMap<>();
    static {
        links.put(LINK_PTRN_ALL, Pattern.compile("^(" + patternSelection.get(LINK_PTRN_EXTERNAL) + "|" + patternSelection.get(LINK_PTRN_INTERNAL)
                + "|" + patternSelection.get(LINK_PTRN_MAIL) + "|" + patternSelection.get(LINK_PTRN_HASHTAG) + ")$"));
    }

    // TODO: some seemingly random replacements, need to categorize and rename, 'preHelp' does not tell us anything
    public static final OrderedMap<Pattern, String> preHelpDict = new ListOrderedMap<>();
    static {
        int flags = Pattern.CASE_INSENSITIVE;
        //int flags = 0;
        preHelpDict.put(Pattern.compile(BOS + "(?i)2ja" + EOS, flags), "$1tveggja$2");
        //TODO: is this save? couldn't it be 'þriðja/þriðju/þriðji'?
        preHelpDict.put(Pattern.compile(BOS + "(?i)3ja" + EOS, flags), "$1þriggja$2");
        preHelpDict.put(Pattern.compile(BOS + "(?i)4ð(a|i|u)" + EOS, flags), "$1fjórð$2$3");
        preHelpDict.put(Pattern.compile(BOS + "(?i)5t(a|i|u)" + EOS, flags), "$1fimmt$2$3");
        preHelpDict.put(Pattern.compile(BOS + "(?i)6t(a|i|u)" + EOS, flags), "$1sjött$2$3");
        preHelpDict.put(Pattern.compile(BOS + "(?i)7d(a|i|u)" + EOS, flags), "$1sjöund$2$3");
        preHelpDict.put(Pattern.compile(BOS + "(?i)8d(a|i|u)" + EOS, flags), "$1áttund$2$3");
        preHelpDict.put(Pattern.compile(BOS + "(?i)9d(a|i|u)" + EOS, flags), "$1níund$2$3");

        // separate digits and letters
        preHelpDict.put(Pattern.compile("(?i)([a-záðéíóúýþæö]+)(\\d+)", flags), "$1 $2");
        preHelpDict.put(Pattern.compile("(?i)(\\d+)([a-záðéíóúýþæö]+)", flags), "$1 $2");

        //TODO: what are these? degrees and percent with letters?
        preHelpDict.put(Pattern.compile("(?i)([\\da-záðéíóúýþæö]+)(°)", flags), "$1 $2");
        //TODO: this causes normalization errors at later stages, analyze why
        //preHelpDict.put("(?i)([\\da-záðéíóúýþæö]+)(%)", "$1 $2");
        // dates
        preHelpDict.put(Pattern.compile(BOS + "(0?[1-9]|[12]\\d|3[01])\\.(0?[1-9]|1[012])\\.(\\d{3,4})" + EOS, flags), "$1$2. $3. $4$5");
        preHelpDict.put(Pattern.compile(BOS + "(0?[1-9]|[12]\\d|3[01])\\.(0?[1-9]|1[012])\\." + EOS, flags), "$1$2. $3.$4");
        // insert a hyphen between digit groups of 3 and 4, most likely a telephone number
        preHelpDict.put(Pattern.compile("(\\d{3})( )(\\d{4})", flags), "$1-$3");
    }

    public static final Map<Pattern, String> hyphenDict = new HashMap<>();
    static {
        int flags = 0;
        //TODO: explain what this pattern should do: why insert a space on the one side of the hyphen,
        // depending on which side has upper case only? (abcdEF-GHJ -> abcdEF- GHJ) What are real world examples here?
        hyphenDict.put(Pattern.compile(BOS + "([A-ZÁÐÉÍÓÚÝÞÆÖ]+)(\\-[A-ZÁÐÉÍÓÚÝÞÆÖa-záðéíóúýþæö]+)" + EOS, flags), "$1$2 $3$4");
        hyphenDict.put(Pattern.compile(BOS + "([A-ZÁÐÉÍÓÚÝÞÆÖa-záðéíóúýþæö]+\\-)([A-ZÁÐÉÍÓÚÝÞÆÖ]+)" + EOS, flags), "$1$2 $3$4");
    }

    // See a list of examples for the below abbreviations in "abbreviation_examples.txt"
    // It is recommended to update that file when this dictionary is updated to keep an overview of covered
    // abbreviations
    public static final Map<Pattern, String> abbreviationDict = new HashMap<>();
    static {
        int flags = 0;
        abbreviationDict.put(Pattern.compile("(\\d+\\.) gr" + DOT + EOS, flags), "$1 grein$2");
        abbreviationDict.put(Pattern.compile("(\\d+\\.) mgr" + DOT + EOS, flags), "$1 málsgrein2");
        abbreviationDict.put(Pattern.compile(BOS + "[Ii]nnsk" + DOT + "(blm" + DOT + "|blaðamanns)" + EOS, flags), "$1innskot $2");
        abbreviationDict.put(Pattern.compile(BOS + "([Ii]nnsk(" + DOT + "|ot) )(blm" + DOT + ")" + EOS, flags), "$12xblaðamanns $5");
        abbreviationDict.put(Pattern.compile("([Ff]" + DOT + "[Kk]r" + DOT + "?)" + EOS, flags), "fyrir Krist$2");
        abbreviationDict.put(Pattern.compile("([Ee]" + DOT + "[Kk]r" + DOT + "?)" + EOS, flags), "eftir Krist$2");
        abbreviationDict.put(Pattern.compile(BOS + "([Cc]a|CA)" + DOT_ONE_NONE + EOS, flags), "$1sirka$3");
        abbreviationDict.put(Pattern.compile("(\\d+" + DOT + ") [Ss]ek" + DOT_ONE_NONE + EOS, flags), "$1 sekúnda$2");
        abbreviationDict.put(Pattern.compile("(\\d+" + DOT + ") [Mm]ín" + DOT_ONE_NONE + EOS, flags), "$1 mínúta$2");

        abbreviationDict.put(Pattern.compile(BOS + "(\\d{1,2}\\/\\d{1,2} )frák" + DOT_ONE_NONE + EOS, flags), "$12x fráköst$3");
        abbreviationDict.put(Pattern.compile(BOS + "(\\d{1,2}\\/\\d{1,2} )stoðs" + DOT_ONE_NONE + EOS, flags), "$12x stoðsendingar$3");
        abbreviationDict.put(Pattern.compile(BOS + "([Nn]" + DOT_ONE_NONE + "k|N" + DOT_ONE_NONE + "K)" + DOT_ONE_NONE + EOS, flags), "$1næstkomandi$3");
        abbreviationDict.put(Pattern.compile(BOS + "(?i)(atr" + DOT_ONE_NONE + ")" + EOS, flags), "$1 atriði$3");
        abbreviationDict.put(Pattern.compile(BOS + "(?i)(ath" + DOT_ONE_NONE + ")" + EOS, flags), "$1 athugið$3");
        abbreviationDict.put(Pattern.compile(BOS + "(?i)(aths" + DOT_ONE_NONE + ")" + EOS, flags), "$1 athugasemd$3");
        abbreviationDict.put(Pattern.compile(BOS + "([Ff]" + DOT_ONE_NONE + "hl|F" + DOT + "HL)" + DOT_ONE_NONE + EOS, flags), "$1 fyrri hluti$3");
        abbreviationDict.put(Pattern.compile(BOS + "([Ss]" + DOT_ONE_NONE + "hl|S" + DOT + "HL)" + DOT_ONE_NONE + EOS, flags), "$1 síðari hluti$3");

        abbreviationDict.put(Pattern.compile("(?i)" + BOS + "(e" + DOT_ONE_NONE + "h" + DOT_ONE_NONE + "f" + DOT_ONE_NONE + ")" + EOS, flags), "$1E H F$3");
        abbreviationDict.put(Pattern.compile("(?i)" + BOS + "(o" + DOT_ONE_NONE + "h" + DOT_ONE_NONE + "f" + DOT_ONE_NONE + ")" + EOS, flags), "$1O H F$3");
        abbreviationDict.put(Pattern.compile("(?i)" + BOS + "(h" + DOT_ONE_NONE + "f" + DOT_ONE_NONE + ")" + EOS, flags), "$1H F$3");
        abbreviationDict.put(Pattern.compile("(?i)" + BOS + "(s" + DOT_ONE_NONE + "f" + DOT_ONE_NONE + ")" + EOS, flags), "$1S F$3");
        abbreviationDict.put(Pattern.compile("(?i)" + BOS + "(q" + DOT_ONE_NONE + "e" + DOT_ONE_NONE + "d" + DOT_ONE_NONE + ")" + EOS, flags), "$1Q E D$3");
        abbreviationDict.put(Pattern.compile(BOS + "([Pp]" + DOT_ONE_NONE + "s" + DOT_ONE_NONE + ")" + EOS, flags), "$1P S$3");

        abbreviationDict.put(Pattern.compile(BOS + "([Aa]" + DOT_ONE_NONE + "m" + DOT_ONE_NONE + "k|A" + DOT_ONE_NONE + "M" + DOT_ONE_NONE + "K)" + DOT_ONE_NONE + EOS, flags), "$1að minnsta kosti$3");
        abbreviationDict.put(Pattern.compile(BOS + "([Aa]" + DOT + "m" + DOT + "m" + DOT_ONE_NONE + ")" + EOS, flags), "$1að mínu mati$3");
        abbreviationDict.put(Pattern.compile(BOS + "([Aa]" + DOT + "n" + DOT + "l" + DOT_ONE_NONE + ")" + EOS, flags), "$1að nokkru leyti$3");
        abbreviationDict.put(Pattern.compile(BOS + "([Aa]lþm" + DOT_ONE_NONE + ")" + EOS, flags), "$1alþingismaður$3");
        abbreviationDict.put(Pattern.compile(BOS + "([Aa]lm|ALM)" + DOT_ONE_NONE + EOS, flags), "$1almennt$3");
        abbreviationDict.put(Pattern.compile(BOS + "(bls" + DOT_ONE_NONE + ") (" + NUMBER_ANY + "|" + MEASURE_PREFIX_WORDS +
                "( )?)" + EOS, flags), "$1blaðsíða $3$12"); //TODO: realistic? million pages? 45,3 pages?
        abbreviationDict.put(Pattern.compile("(?i)" + BOS + "(B" + DOT + "S[Cc]" + DOT + "?)" + EOS, flags), "$1B S C$3");
        abbreviationDict.put(Pattern.compile("(?i)" + BOS + "(M" + DOT + "S[Cc]" + DOT + "?)" + EOS, flags), "$1M S C$3");

        abbreviationDict.put(Pattern.compile(BOS + "([Dd]r" + DOT_ONE_NONE + ")" + EOS, flags), "$1doktor$3");
        abbreviationDict.put(Pattern.compile("(" + BOS + "\\() ?(e" + DOT + ")" + EOS, flags), "$1enska$4");
        abbreviationDict.put(Pattern.compile(BOS + "([Ee]" + DOT + "k|E" + DOT + "K)" + DOT_ONE_NONE + EOS, flags), "$1 einhvers konar$3");
        abbreviationDict.put(Pattern.compile(BOS + "([Ee]\\-s konar)" + DOT_ONE_NONE+ EOS, flags), "$1 einhvers konar$3");
        abbreviationDict.put(Pattern.compile(BOS + "([Ee]" + DOT + "t" + DOT + "v" + DOT_ONE_NONE + ")" + EOS, flags), "$1 ef til vill$3");

        abbreviationDict.put(Pattern.compile(BOS + "([Ee]\\-ð)" + EOS, flags), "$1eitthvað$3");
        abbreviationDict.put(Pattern.compile(BOS + "([Ee]\\-ju)" + EOS, flags), "$1einhverju$3");
        abbreviationDict.put(Pattern.compile(BOS + "([Ee]\\-s)" + EOS, flags), "$1einhvers$3");
        abbreviationDict.put(Pattern.compile(BOS + "([Ee]\\-r)" + EOS, flags), "$1einhvers$3");
        abbreviationDict.put(Pattern.compile(BOS + "([Ee]\\-n)" + EOS, flags), "$1einhvern$3");
        abbreviationDict.put(Pattern.compile(BOS + "([Ee]\\-um)" + EOS, flags), "$1einhverjum$3");
        //TODO: do we have "fædd" somewhere or how do we know this should be "fæddur"?
        abbreviationDict.put(Pattern.compile(BOS + "(f" + DOT + ") (^((([012]?[1-9]|3[01])" + DOT + " ?)?(" + MONTH + ") )\\d{2,4}$)" + EOS, flags), "$1fæddur $3$4");
        abbreviationDict.put(Pattern.compile(BOS + "([Ff]él" + DOT_ONE_NONE + ")" + EOS, flags), "$1félag$3");
        abbreviationDict.put(Pattern.compile(BOS + "([Ff]rh|FRH)" + DOT_ONE_NONE + EOS, flags), "$1framhald$3");
        abbreviationDict.put(Pattern.compile(BOS + "([Ff]rt|FRT)" + DOT_ONE_NONE + EOS, flags), "$1framtíð$3");
        abbreviationDict.put(Pattern.compile(BOS + "([Ff]" + DOT + "o" + DOT + "t" + DOT +")" + EOS, flags), "$1fyrir okkar tímatal$3");
        abbreviationDict.put(Pattern.compile(BOS + "([Ff]" + DOT + "h" + DOT_ONE_NONE + ")" + EOS, flags), "$1fyrir hönd$3");
        abbreviationDict.put(Pattern.compile(BOS + "([Gg]" + DOT_ONE_NONE + "r" + DOT_ONE_NONE + "f|G" + DOT_ONE_NONE +"R" + DOT_ONE_NONE + "F)"
                + DOT_ONE_NONE + EOS, flags), "$1gerum ráð fyrir$3");
        abbreviationDict.put(Pattern.compile(BOS + "([Gg]" + DOT_ONE_NONE + "m" + DOT_ONE_NONE + "g|G" + DOT_ONE_NONE +"M" + DOT_ONE_NONE + "G)"
                + DOT_ONE_NONE + EOS, flags), "$1guð minn góður$3");

        abbreviationDict.put(Pattern.compile(BOS + "([Hh]dl" + DOT_ONE_NONE + ")" + EOS, flags), "$1héraðsdómslögmaður$3");
        abbreviationDict.put(Pattern.compile(BOS + "([Hh]rl" + DOT_ONE_NONE + ")" + EOS, flags), "$1hæstarréttarlögmaður$3");
        abbreviationDict.put(Pattern.compile(BOS + "([Hh]öf|HÖF)" + DOT_ONE_NONE + EOS, flags), "$1höfundur$3");
        abbreviationDict.put(Pattern.compile(BOS + "([Hh]v?k)" + EOS, flags), "$1hvorugkyn$3");
        abbreviationDict.put(Pattern.compile(BOS + "([Hh]r" + DOT_ONE_NONE + ")" + EOS, flags), "$1herra$3");
        abbreviationDict.put(Pattern.compile(BOS + "([Hh]v" + DOT_ONE_NONE + ")" + EOS, flags), "$1hæstvirtur$3"); //case?
        abbreviationDict.put(Pattern.compile(BOS + "([Hh]" + DOT + "u" + DOT + "b|H" + DOT + "U" + DOT + "B)" + DOT_ONE_NONE + EOS, flags), "$1hér um bil$3");

        abbreviationDict.put(Pattern.compile("(" + LETTER + "+ )([Jj]r)" + DOT_ONE_NONE + EOS, flags), "$1junior$3");

        abbreviationDict.put(Pattern.compile(BOS + "([Kk]k)" + EOS, flags), "$1karlkyn$3");
        abbreviationDict.put(Pattern.compile(BOS + "([Kk]vk)" + EOS, flags), "$1kvenkyn$3");
        abbreviationDict.put(Pattern.compile(BOS + "([Kk]t" + DOT_ONE_NONE + ")(:| \\d{6}\\-?\\d{4})" + EOS, flags), "$1kennitala$3");
        abbreviationDict.put(Pattern.compile(BOS + "([Kk]l ?(" + DOT + "|\\:)?)(\\s?\\d{2}([:" + DOT + "]\\d{2})?)", flags), "$1klukkan$4");
        //TODO: how do we get this, ef number normalizing is done after abbreviation normalizing?
        abbreviationDict.put(Pattern.compile("(?i)" + BOS + "kl ?(" + DOT + "|\\:)? ?(" + THE_CLOCK + " )", flags), "klukkan$2");

        abbreviationDict.put(Pattern.compile(BOS + "([Kk]höfn|[Kk]bh|KBH)" + EOS, flags), "$1Kaupmannahöfn$2"); // inflections?
        abbreviationDict.put(Pattern.compile(BOS + "([Ll]h" + DOT_ONE_NONE + "nt" + DOT_ONE_NONE + ")" + EOS, flags), "$1lýsingarháttur nútíðar$3");
        abbreviationDict.put(Pattern.compile(BOS + "([Ll]h" + DOT_ONE_NONE + "þt" + DOT_ONE_NONE + ")" + EOS, flags), "$1lýsingarháttur þátíðar$3");
        abbreviationDict.put(Pattern.compile(BOS + "([Ll]td|LTD)" + DOT_ONE_NONE + EOS, flags), "$1limited$3");

        abbreviationDict.put(Pattern.compile(BOS + "([Mm]" + DOT + "a" + DOT + ")" + EOS, flags), "$1meðal annars$3");
        abbreviationDict.put(Pattern.compile(BOS + "([Mm]" + DOT + "a" + DOT + "s|M" + DOT + "A" + DOT + "S)" + DOT_ONE_NONE + EOS, flags), "$1meira að segja$3");
        abbreviationDict.put(Pattern.compile(BOS + "([Mm]" + DOT + "a" + DOT + "o|M" + DOT + "A" + DOT + "O)" + DOT_ONE_NONE + EOS, flags), "$1meðal annarra orða$3");
        abbreviationDict.put(Pattern.compile("(" + MEASURE_PREFIX_DIGITS + "|" + MEASURE_PREFIX_WORDS + ")?([Mm]" + DOT + "y" + DOT + "s" + DOT_ONE_NONE + ")" + EOS, flags), "$1metra yfir sjávarmáli$9"); // do we really need the millions here? million metres above n.n.?
        abbreviationDict.put(Pattern.compile(BOS + "([Mm]" + DOT_ONE_NONE + "v" + DOT_ONE_NONE + ")" + EOS, flags), "$1miðað við$3");
        abbreviationDict.put(Pattern.compile(BOS + "([Mm]" + DOT_ONE_NONE + "t" + DOT_ONE_NONE + "t|M" + DOT_ONE_NONE + "T" + DOT_ONE_NONE + "T)"
                + DOT_ONE_NONE + EOS, flags), "$1með tilliti til$3");
        abbreviationDict.put(Pattern.compile(BOS +  "([Mm]" + DOT_ONE_NONE + "ö" + DOT_ONE_NONE + "o|M" + DOT_ONE_NONE + "Ö" + DOT_ONE_NONE + "O)"
                + DOT_ONE_NONE + EOS, flags), "$1með öðrum orðum$3");
        abbreviationDict.put(Pattern.compile(BOS + "([Mm]fl|MFL)" + DOT_ONE_NONE + EOS, flags), "$1meistaraflokkur$3");
        abbreviationDict.put(Pattern.compile("(" + MEASURE_PREFIX_DIGITS + "|" + MEASURE_PREFIX_WORDS + ")?(m ?\\^ ?2)" + EOS, flags), "$1 fermetrar$9");
        abbreviationDict.put(Pattern.compile("(" + MEASURE_PREFIX_DIGITS + "|" + MEASURE_PREFIX_WORDS + ")?(m ?\\^ ?3)" + EOS, flags), "$1 rúmmetrar$9");

        abbreviationDict.put(Pattern.compile(BOS + "([Nn]úv" + DOT_ONE_NONE + ")" + EOS, flags), "$1núverandi$3");
        abbreviationDict.put(Pattern.compile(BOS + "([Nn]" + DOT_ONE_NONE + "t" + DOT_ONE_NONE + "t|N" + DOT_ONE_NONE + "T" + DOT + "T)"
                + DOT_ONE_NONE + "($)" + EOS, flags), "$1nánar tiltekið$3");
        abbreviationDict.put(Pattern.compile(BOS + "([Nn]kl|NKL)" + EOS, flags), "nákvæmlega");
        abbreviationDict.put(Pattern.compile(BOS + "([Oo]" + DOT_ONE_NONE + "fl|O" + DOT_ONE_NONE + "FL)" + DOT_ONE_NONE + EOS, flags), "$1og fleira$3");
        abbreviationDict.put(Pattern.compile(BOS + "([Oo]" + DOT_ONE_NONE + "m" + DOT_ONE_NONE + "fl|O" + DOT_ONE_NONE + "M" + DOT_ONE_NONE + "FL)"
                + DOT_ONE_NONE + EOS, flags), "$1og margt fleira$3");
        abbreviationDict.put(Pattern.compile(BOS + "([Oo]" + DOT_ONE_NONE + "s" + DOT_ONE_NONE + "frv?|O" + DOT_ONE_NONE + "S" + DOT_ONE_NONE + "FRV?)"
                + DOT_ONE_NONE + EOS, flags), "$1og svo framvegis$3");
        abbreviationDict.put(Pattern.compile(BOS + "([Oo]" + DOT_ONE_NONE + "þ" + DOT_ONE_NONE + "h|O" + DOT_ONE_NONE + "Þ" + DOT_ONE_NONE + "H)"
                + DOT_ONE_NONE + EOS, flags), "$1og þess háttar$3");
        abbreviationDict.put(Pattern.compile(BOS + "([Oo]" + DOT_ONE_NONE + "þ" + DOT_ONE_NONE + "u" + DOT_ONE_NONE + "l|O" + DOT_ONE_NONE + "Þ"
                + DOT_ONE_NONE + "U" + DOT_ONE_NONE + "L)" + DOT_ONE_NONE + EOS, flags), "$1og því um líkt$3");
        abbreviationDict.put(Pattern.compile(BOS + "([Pp]k" + DOT_ONE_NONE + ")" + EOS, flags), "$1pakki$3");

        abbreviationDict.put(Pattern.compile(BOS + "([Rr]n" + DOT_ONE_NONE + ")(:| [\\d\\-]+)" + EOS, flags), "$1reikningsnúmer$3$4");
        abbreviationDict.put(Pattern.compile(BOS + "([Rr]itstj" + DOT_ONE_NONE + ")" + EOS, flags), "$1ritstjóri$3");
        abbreviationDict.put(Pattern.compile(BOS + "([Rr]slm" + DOT_ONE_NONE + ")" + EOS, flags), "$1rannsóknarlögreglumaður$3");
        abbreviationDict.put(Pattern.compile(BOS + "([Rr]ví?k|RVÍ?K)" + EOS, flags), "$1Reykjavík$3");

        abbreviationDict.put(Pattern.compile(BOS + "(([Ss]íma)?nr" + DOT_ONE_NONE + ")", flags), "$1númer ");
        abbreviationDict.put(Pattern.compile(BOS + "([Ss]"+ DOT_ONE_NONE + ")(:| \\d{3}\\-?\\d{4}|\\d{7})" + EOS, flags), "$1sími$3$4");
        abbreviationDict.put(Pattern.compile(BOS + "([Ss]br" + DOT_ONE_NONE + ")" + EOS, flags), "$1samanber$3");
        abbreviationDict.put(Pattern.compile(BOS + "([Ss]" + DOT_ONE_NONE + "l" + DOT_ONE_NONE + "|SL" + DOT + ")" + EOS, flags), "síðastliðinn");
        abbreviationDict.put(Pattern.compile(BOS + "([Ss]" + DOT_ONE_NONE + "k" + DOT_ONE_NONE + ")" + EOS, flags), "$1svokallað$3");
        abbreviationDict.put(Pattern.compile(BOS + "([Ss]kv|SKV)" + DOT_ONE_NONE + EOS, flags), "$1samkvæmt$3");
        abbreviationDict.put(Pattern.compile(BOS + "([Ss]" + DOT + " ?s" + DOT_ONE_NONE + ")" + EOS, flags), "$1svo sem$3");
        abbreviationDict.put(Pattern.compile(BOS + "([Ss]amþ|SAMÞ)" + DOT_ONE_NONE + EOS, flags), "$1samþykki$3"); // ekki "samþykkt"?
        abbreviationDict.put(Pattern.compile(BOS + "([Ss]gt" + DOT_ONE_NONE + ")" + EOS, flags), "$1sergeant$3");
        abbreviationDict.put(Pattern.compile(BOS + "([Ss]t" + DOT_ONE_NONE + ")" + EOS, flags), "$1saint$3");
        abbreviationDict.put(Pattern.compile(BOS + "([Ss]ltjn|SLTJN)" + DOT_ONE_NONE + EOS, flags), "$1Seltjarnarnes$3");
        abbreviationDict.put(Pattern.compile(BOS + "([Ss]thlm|STHLM)" + EOS, flags), "$1Stokkhólmur$3");

        abbreviationDict.put(Pattern.compile(BOS + "([Tt]bl|TBL)" + DOT_ONE_NONE + EOS, flags), "$1tölublað$3"); //beyging?
        abbreviationDict.put(Pattern.compile(BOS + "([Tt]l" + DOT_ONE_NONE + ")" + EOS, flags), "$1tengiliður$3");
        abbreviationDict.put(Pattern.compile(BOS + "([Tt]" + DOT + "h" + DOT_ONE_NONE + ")" + EOS, flags), "$1til hægri$3");
        abbreviationDict.put(Pattern.compile(BOS + "([Tt]" + DOT + "v" + DOT_ONE_NONE + ")" + EOS, flags), "$1til vinstri$3");
        abbreviationDict.put(Pattern.compile(BOS + "([Tt]" + DOT + "a" + DOT + "m|T" + DOT + "A" + DOT + "M)" + DOT_ONE_NONE + EOS, flags), "$1til að mynda$3");
        abbreviationDict.put(Pattern.compile(BOS + "([Tt]" + DOT_ONE_NONE + "d|T" + DOT_ONE_NONE + "D)" + DOT_ONE_NONE + EOS, flags), "$1til dæmis$3");

        abbreviationDict.put(Pattern.compile(BOS + "([Uu]" + DOT_ONE_NONE + "þ" + DOT_ONE_NONE + "b|U" + DOT_ONE_NONE + "Þ" + DOT_ONE_NONE + "B)"
                + DOT_ONE_NONE + EOS, flags), "$1um það bil$3");
        abbreviationDict.put(Pattern.compile(BOS + "([Uu]ppl|UPPL)" + DOT_ONE_NONE + EOS, flags), "$1upplýsingar$3");
        abbreviationDict.put(Pattern.compile(BOS + "([Uu]td|UTD)" + DOT_ONE_NONE + EOS, flags), "$1united$3");
        abbreviationDict.put(Pattern.compile(BOS + "([Vv]s|VS)" + DOT_ONE_NONE + EOS, flags), "$1versus$3");
        abbreviationDict.put(Pattern.compile(BOS + "([Vv]sk" + DOT_ONE_NONE + ")" + EOS, flags), "$1virðisaukaskatt$3"); // beyginar? ekki "skattur"?

        abbreviationDict.put(Pattern.compile(BOS + "([Þþ]f" + DOT_ONE_NONE+ ")" + EOS, flags), "$1þolfall$3");
        abbreviationDict.put(Pattern.compile(BOS + "([Þþ]gf" + DOT_ONE_NONE+ ")" + EOS, flags), "$1þágufall$3");
        abbreviationDict.put(Pattern.compile(BOS + "([Þþ]lt" + DOT_ONE_NONE+ ")" + EOS, flags), "$1þáliðin tíð$3");
        abbreviationDict.put(Pattern.compile(BOS + "([Þþ]" + DOT + "á" + DOT + ")" + EOS, flags), "$1þessa árs$3");
        abbreviationDict.put(Pattern.compile(BOS + "([Þþ]" + DOT + "h" + DOT + ")" + EOS, flags), "$1þess háttar$3");
        abbreviationDict.put(Pattern.compile(BOS + "([Þþ]" + DOT + "m" + DOT + ")" + EOS, flags), "$1þessa mánaðar$3");
        abbreviationDict.put(Pattern.compile(BOS + "([Þþ]" + DOT + "a" + DOT_ONE_NONE + ")" + EOS, flags), "$1þannig að$3");
        abbreviationDict.put(Pattern.compile(BOS + "([Þþ]" + DOT_ONE_NONE + "e" + DOT_ONE_NONE + "a" + DOT_ONE_NONE + "s|" +
                "Þ" + DOT_ONE_NONE + "E" + DOT_ONE_NONE + "A" + DOT_ONE_NONE + "S)" + DOT_ONE_NONE + EOS, flags), "$1það er að segja$3");
        abbreviationDict.put(Pattern.compile(BOS + "([Þþ]" + DOT_ONE_NONE + "a" + DOT_ONE_NONE + DOT_ONE_NONE + "a|Þ" + DOT_ONE_NONE + "A"
                + DOT_ONE_NONE + "A)" + DOT_ONE_NONE + EOS, flags), "$1þá og því aðeins að$3");
        abbreviationDict.put(Pattern.compile(BOS + "([Þþ]" + DOT + "u" + DOT + "l" + DOT + "|Þ" + DOT + "U" + DOT + "L)" + DOT_ONE_NONE + EOS, flags),
                "$1því um líkt$3");
        abbreviationDict.put(Pattern.compile(BOS + "([Þþ]" + DOT + "a" + DOT + "l" + DOT + "|Þ" + DOT + "A" + DOT + "L)" + DOT_ONE_NONE + EOS, flags),
                "$1þar af leiðandi$3");
        abbreviationDict.put(Pattern.compile(BOS + "([Þþ]" + DOT + "á(" + DOT + "| )m" + DOT + "|Þ" + DOT + "Á(" + DOT + "| )M)" + DOT_ONE_NONE + EOS, flags),
                "$1þar á meðal$3");
        abbreviationDict.put(Pattern.compile(BOS + "([Þþ]" + DOT + "m" + DOT + "t" + DOT + "|Þ" + DOT + "M" + DOT + "T)" + DOT_ONE_NONE + EOS, flags),
                "$1þar með talið$3");

        abbreviationDict.put(Pattern.compile(" (((" + MEASURE_PREFIX_DIGITS + "|" + MEASURE_PREFIX_WORDS + ")( )?)\\s)(þús" + DOT_ONE_NONE + " )" +
                "(" + LETTER + "*)?", flags), " $1þúsund $13");
        abbreviationDict.put(Pattern.compile(" ([Mm]örg )þús" + DOT_ONE_NONE + "( " + LETTER + "*)?", flags), "$1þúsund$2");

        abbreviationDict.put(Pattern.compile("(\\d+" + DOT + ") [Áá]rg" + DOT + EOS, flags), "$1 árgangur$2");
        abbreviationDict.put(Pattern.compile(BOS + "([Óó]ákv" + DOT + "gr" + DOT + ")" + EOS, flags), "$1óáveðinn greinir$3");
        abbreviationDict.put(Pattern.compile("(\\d+" + DOT + ") útg" + DOT + EOS, flags), "$1 útgáfa$2");
        abbreviationDict.put(Pattern.compile(BOS + "([Íí]sl|ÍSL)" + DOT_ONE_NONE + EOS, flags), "$1íslenska$3");

        abbreviationDict.put(Pattern.compile("([02-9])( )?°C" + EOS, flags), "$1 gráður selsíus $2"); // 3 -> 2, all entries below
        abbreviationDict.put(Pattern.compile("(1)( )?°C", flags), "$1 gráða selsíus$2");
        abbreviationDict.put(Pattern.compile("([02-9])( )?°F" + EOS, flags), "$1 gráður farenheit $2");
        abbreviationDict.put(Pattern.compile("(1)( )?°F", flags), "$1 gráða farenheit$2");
        abbreviationDict.put(Pattern.compile("([02-9])( )?°W" + EOS, flags), "$1 gráður vestur $2");
        abbreviationDict.put(Pattern.compile("(1)( )?°W", flags), "$1 gráða vestur$2");
        abbreviationDict.put(Pattern.compile("([02-9])( )?°N" + EOS, flags), "$1 gráður norður $2");
        abbreviationDict.put(Pattern.compile("(1)( )?°N", flags), "$1 gráða norður$2");
        abbreviationDict.put(Pattern.compile("([02-9])( )?°E" + EOS, flags), "$1 gráður austur $2");
        abbreviationDict.put(Pattern.compile("(1)( )?°E", flags), "$1 gráða austur$2");
        abbreviationDict.put(Pattern.compile("([02-9])( )?°S" + EOS, flags), "$1 gráður suður $2");
        abbreviationDict.put(Pattern.compile("(1)( )?°S", flags), "$1 gráða suður $2");
        abbreviationDict.put(Pattern.compile("[Hh]&[mM]", flags), "h og m");
    }

    public static final Map<Pattern, String> directionDict = new HashMap<>();
    static {
        int flags = 0;
        // we don't accept dashes (u2013 or u2014), only standard hyphenation.
        //TODO: see more patterns in directiondict.txt
        directionDict.put(Pattern.compile(BOS + "(SV-(:?(til|lands|átt|verðu|vert)))" + EOS, flags), "$1suðvestan$3$4");
        directionDict.put(Pattern.compile(BOS + "(NV-(:?(til|lands|átt|verðu|vert)))" + EOS, flags), "$1norðvestan$3$4");
        directionDict.put(Pattern.compile(BOS + "(NA-(:?(til|lands|átt|verðu|vert)))" + EOS, flags), "$1norðaustan$3$4");
        directionDict.put(Pattern.compile(BOS + "(SA-(:?(til|lands|átt|verðu|vert)))" + EOS, flags), "$1suðaustan$4$5");
        directionDict.put(Pattern.compile(BOS + "(A-(:?(til|lands|átt|verðu|vert)))" + EOS, flags), "$1austan$4");
        directionDict.put(Pattern.compile(BOS + "(S-(:?(til|lands|átt|verðu|vert)))" + EOS, flags), "$1sunnan$4");
        directionDict.put(Pattern.compile(BOS + "(V-(:?(til|lands|átt|verðu|vert)))" + EOS, flags), "$1vestan$4");
        directionDict.put(Pattern.compile(BOS + "(N-(:?(til|lands|átt|verðu|vert)))" + EOS, flags), "$1norðan$4");
        directionDict.put(Pattern.compile(BOS + "(SV)( \\d\\d?)" + EOS, flags), "$1suðvestan$3$4");
        directionDict.put(Pattern.compile(BOS + "(NV)( \\d\\d?)" + EOS, flags), "$1norðvestan$3$4");
        directionDict.put(Pattern.compile(BOS + "(NA)( \\d\\d?)" + EOS, flags), "$1norðaustan$3$4");
        directionDict.put(Pattern.compile(BOS + "(SA)( \\d\\d?)" + EOS, flags), "$1suðaustan$3$4");
        directionDict.put(Pattern.compile(BOS + "(A)( \\d\\d?)" + EOS, flags), "$1austan$3$4");
        directionDict.put(Pattern.compile(BOS + "(S)( \\d\\d?)" + EOS, flags), "$1sunnan$3$4");
        directionDict.put(Pattern.compile(BOS + "(V)( \\d\\d?)" + EOS, flags), "$1vestan$3$4");
        directionDict.put(Pattern.compile(BOS + "(N)( \\d\\d?)" + EOS, flags), "$1norðan$3$4");
    }

    public static final Map<Pattern, String> denominatorDict = new HashMap<>();
    static {
        int flags = Pattern.CASE_INSENSITIVE;
        //TODO: not complete, see denominatordict.txt for further patterns
        denominatorDict.put(Pattern.compile("\\/kg" + DOT_ONE_NONE + EOS, flags), " á kílóið$1");
        denominatorDict.put(Pattern.compile("\\/t" + DOT_ONE_NONE + EOS, flags), " á tonnið$1");
        denominatorDict.put(Pattern.compile("\\/ha" + DOT_ONE_NONE + EOS, flags), " á hektarann$1");
        denominatorDict.put(Pattern.compile("\\/mg" + DOT_ONE_NONE + EOS, flags), " á milligrammið$1");
        denominatorDict.put(Pattern.compile("\\/gr" + DOT_ONE_NONE + EOS, flags), " á grammið$1");
        denominatorDict.put(Pattern.compile("\\/ml" + DOT_ONE_NONE + EOS, flags), " á millilítrann$1");
        denominatorDict.put(Pattern.compile("\\/dl" + DOT_ONE_NONE + EOS, flags), " á desilítrann$1");
        denominatorDict.put(Pattern.compile("\\/l" + DOT_ONE_NONE + EOS, flags), " á lítrann$1");
        denominatorDict.put(Pattern.compile("\\/km" + DOT_ONE_NONE + EOS, flags), " á kílómetra$1");
        denominatorDict.put(Pattern.compile("\\/klst" + DOT_ONE_NONE + EOS, flags), " á klukkustund$1");
        denominatorDict.put(Pattern.compile("\\/kw" + DOT_ONE_NONE + "(st|h)" + DOT_ONE_NONE + EOS, flags), " á kílóvattstund$2");
        denominatorDict.put(Pattern.compile("\\/Mw" + DOT_ONE_NONE + "(st|h)" + DOT_ONE_NONE + EOS, flags), " á megavattstund$2");
        denominatorDict.put(Pattern.compile("\\/Gw" + DOT_ONE_NONE + "(st|h)" + DOT_ONE_NONE + EOS, flags), " á gígavattstund$2");
        denominatorDict.put(Pattern.compile("\\/Tw" + DOT_ONE_NONE + "(st|h)" + DOT_ONE_NONE + EOS, flags), " á teravattstund$2");
        denominatorDict.put(Pattern.compile("\\/s(ek)?" + DOT_ONE_NONE + EOS, flags), " á sekúndu$2");
        denominatorDict.put(Pattern.compile("\\/mín" + DOT_ONE_NONE + EOS, flags), " á mínútu$1");
        denominatorDict.put(Pattern.compile("\\/fm" + DOT_ONE_NONE + EOS, flags), " á fermetra$1");
        denominatorDict.put(Pattern.compile("\\/ferm" + DOT_ONE_NONE + EOS, flags), " á fermetra$1");
    }

    // TODO: missing tests !!
    public static final Map<Pattern, String> weightDict = new HashMap<>();
    static {
        int flags = Pattern.CASE_INSENSITIVE;
        weightDict.put(Pattern.compile("(" + BOS + "(" + prepositions.get(DAT) + ") ((\\d{1,2}\\.)?(\\d{3}\\.?)*(\\d*1|\\d,\\d*1))) t" + DOT_ONE_NONE + EOS, flags), "$1 tonni$10");
        weightDict.put(Pattern.compile("(" + BOS + "(" + prepositions.get(GEN) + ") ((\\d{1,2}\\.)?(\\d{3}\\.?)*(\\d*1|\\d,\\d*1))) t" + DOT_ONE_NONE + EOS, flags), "$1 tonns$10");
        weightDict.put(Pattern.compile("(" + BOS + "(" + prepositions.get(DAT) + ") ((\\d{1,2}\\.)?(\\d{3}\\.?)*(\\d*[02-9]|\\d,\\d*[02-9]))) t" + DOT_ONE_NONE + EOS, flags), "$1 tonnum$9"); // changed from group 10 to 9
        weightDict.put(Pattern.compile("(" + BOS + "(" + prepositions.get(DAT) + ") (((\\d{1,2}\\.)?(\\d{3}\\.?)*|\\d+)(,\\d+)?)?) " + patternSelection.get(AMOUNT) + " t" + DOT_ONE_NONE + EOS, flags), "1 $1$1 tonnum$13");
        //TODO usw. three more, and the same for grams
        weightDict.put(Pattern.compile("(" + BOS + "(" + prepositions.get(DAT) + ") ((\\d{1,2}\\.)?(\\d{3}\\.?)*(\\d*1|\\d,\\d*1))) g" + DOT_ONE_NONE + EOS, flags), "$1 grammi$10");
        weightDict.put(Pattern.compile("(" + BOS + "(" + prepositions.get(GEN) + ") ((\\d{1,2}\\.)?(\\d{3}\\.?)*(\\d*1|\\d,\\d*1))) g" + DOT_ONE_NONE + EOS, flags), "$1 gramms$10");
        weightDict.put(Pattern.compile("(" + BOS + "(" + prepositions.get(DAT) + ") ((\\d{1,2}\\.)?(\\d{3}\\.?)*(\\d*[02-9]|\\d,\\d*[02-9]))) g\\.?(\\W|$)", flags), "$1 grömmum$10");
        weightDict.put(Pattern.compile("(" + BOS + "(" + prepositions.get(DAT) + ") (((\\d{1,2}\\.)?(\\d{3}\\.?)*|\\d+)(,\\d+)?)?) " + patternSelection.get(AMOUNT) + " g"+ DOT_ONE_NONE + EOS, flags), "1 $1$1 grömmum$13");
        weightDict.put(Pattern.compile("(1 )gr?" + DOT_ONE_NONE + EOS, flags), "$1gramm$2");
        weightDict.put(Pattern.compile("([02-9]|" + patternSelection.get(AMOUNT) + ") gr?\\.?(\\W|$)", flags), "$1 grömm$3");

        //TODO: another section for nanó/milli/míkró/píkó/attó/zeptó/yoktó-kíló/pund + grammi/gramms/grömmum, ...
        // see class weight_dict.py in regina
    }

    // TODO: missing tests !!
    final private static OrderedMap<Pattern, String> distanceDict = new ListOrderedMap<>();
    public static Map<Pattern, String> getDistanceDict() {
        if (!distanceDict.isEmpty())
            return distanceDict;

        int flags = 0;
        //TODO: first initialized with "fet", "tomma"
        distanceDict.put(Pattern.compile("(" + BOS + "(" + prepositions.get(ACC_DAT_GEN_COMB) + ") " + NUMBER_EOS_1 + " )m" + DOT_ONE_NONE +
                "( (?![kmgyabefstvö]" + DOT + ")" + LETTER_OR_DIGIT + "*" + EOS  + ")", flags), "$1 metra$10"); // from 14 to 10
        distanceDict.put(Pattern.compile("(" + BOS + "(" + prepositions.get(ACC_GEN) + ") (" + NUMBER_EOS_NOT_1 + " )m" + DOT_ONE_NONE +
                "( (?![kmgyabefstvö]" + DOT + ")" + LETTER_OR_DIGIT + "*" + EOS  + ")", flags), "$1 metra$9$10");
        distanceDict.put(Pattern.compile("(" + BOS + "(" + prepositions.get(ACC_GEN) + ") " + NUMBER_ANY + " " + patternSelection.get(AMOUNT) + " )m" + DOT_ONE_NONE +
                "( (?![kmgyabefstvö]" + DOT + ")" + LETTER_OR_DIGIT + "*" + EOS  + ")", flags), "$1 $13 metra$16");
        distanceDict.put(Pattern.compile("(" + BOS + "(" + prepositions.get(DAT) + ") (" + NUMBER_EOS_NOT_1 + " )m" + DOT_ONE_NONE +
                "( (?![kmgyabefstvö]" + DOT + ")" + LETTER_OR_DIGIT + "*" + EOS + ")", flags), "$1 metrum$10");
        distanceDict.put(Pattern.compile("(" + BOS + "(" + prepositions.get(DAT) + ") " + NUMBER_ANY + " " + patternSelection.get(AMOUNT) + " )m" + DOT_ONE_NONE +
                "( (?![kmgyabefstvö]" + DOT + ")(?![kmgyabefstvö]" + DOT + ")" + LETTER_OR_DIGIT + "*" + EOS  + ")", flags), "$1 1$1 metrum$14");
        distanceDict.put(Pattern.compile("(1 )m" + DOT_ONE_NONE + "( (?![kmgyabefstvö]" + DOT + ")" + LETTER_OR_DIGIT + "*" + EOS + ")", flags),
                "$1metri$2");
        distanceDict.put(Pattern.compile("([02-9] )m" + DOT_ONE_NONE + "( (?![kmgyabefstvö]" + DOT + ")" + LETTER_OR_DIGIT + "*" + EOS  + ")", flags),
                "$1metrar $2");

        final Map<String, String> prefixMeter = new HashMap<>() {{
            put("m", "milli");
            put("[cs]", "senti");
            put("d", "desi");
            put("k", "kíló");
            //TODO: also píkó, nanó, míkró, njúton, in original regina
        }};

        for (String letter : prefixMeter.keySet()) {
            distanceDict.put(Pattern.compile("(" + BOS + "(" + prepositions.get(ACC_DAT_GEN_COMB) + ") ((\\d{1,2}" + DOT + ")?(\\d{3}"
                    + DOT + ")*(\\d*1|\\d,\\d*1))) " + letter + "m" + DOT_ONE_NONE + EOS, flags), "$1 " + prefixMeter.get(letter) + "metra$10"); //14->10
            distanceDict.put(Pattern.compile("(" + BOS + "(" + prepositions.get(ACC_GEN) + ") " + NUMBER_EOS_NOT_1 + " " + letter + "m"
                    + DOT_ONE_NONE + EOS, flags), "$1 " + prefixMeter.get(letter) + "metra$8"); // different group count from regina! (12)
            distanceDict.put(Pattern.compile("(" + BOS + "(" + prepositions.get(ACC_GEN) + ") " + NUMBER_ANY + ") " + patternSelection.get(AMOUNT) +
                    " " + letter + "m" + DOT_ONE_NONE + EOS, flags), "$1 $10 " + prefixMeter.get(letter) + "metra$12");
            distanceDict.put(Pattern.compile("(" + BOS + "(" + prepositions.get(DAT) + ") " + NUMBER_EOS_NOT_1 + " " + letter + "m" +
                    DOT_ONE_NONE + EOS, flags), "$1 " + prefixMeter.get(letter) + "metrum$8"); //10 -> 8
            distanceDict.put(Pattern.compile("(" + BOS + "(" + prepositions.get(DAT) + ") " + NUMBER_ANY + ") " + patternSelection.get(AMOUNT) +
                    " " + letter + "m" + DOT_ONE_NONE + EOS, flags), "$1 $11" + prefixMeter.get(letter) + "metrum$14");
            distanceDict.put(Pattern.compile("(1 )" + letter + "m" + DOT_ONE_NONE + EOS, flags), "$1 " + prefixMeter.get(letter) + "metri $2");
            distanceDict.put(Pattern.compile("([0-9]|" + patternSelection.get(AMOUNT) + ") " + letter + "m" + DOT_ONE_NONE + EOS, flags), "$1 " + prefixMeter.get(letter) + "metrar $3");
        }
        return distanceDict;
    }

    //TODO: take care not to normalize the superscripts away when doing unicode normalization!
    private static final Map<String, String> dimensionAfter = new HashMap<>() {{
        put("", "");    // also match without prefix
        put("²", "fer");
        // put("2", "fer");
        put("³", "rúm");
        // put("3", "rúm");
    }};

    private static final Map<String, String> dimensionBefore = new HashMap<>() {{
        put("f", "fer");
        put("fer", "fer");
        put("rúm", "rúm");
    }};

    public static final Map<Pattern, String> areaDict = new HashMap<>();
    // TODO: missing explicit tests !!
    public static Map<Pattern, String> getAreaDict() {
        if (!areaDict.isEmpty())
            return areaDict;
        int flags = 0;
        areaDict.put(Pattern.compile("((\\W|^)(" + prepositions.get(ACC_DAT_GEN_COMB) + ") ((\\d{1,2}\\.)?(\\d{3}\\.?)*(\\d*1|\\d,\\d*1))) ha\\.?(\\W|$)", flags), "$1 hektara$14");
        areaDict.put(Pattern.compile("((\\W|^)(" + prepositions.get(ACC_GEN) + ") ((\\d{1,2}\\.)?(\\d{3}\\.?)*(\\d*[02-9]|\\d,\\d*[02-9]))) ha\\.?(\\W|$)", flags), "$1 hektara$12");
        areaDict.put(Pattern.compile("((\\W|^)(" + prepositions.get(ACC_GEN) + ") (((\\d{1,2}\\.)?(\\d{3}\\.?)*|\\d+)(,\\d+)?)?) " + patternSelection.get(AMOUNT) + " ha\\.?(\\W|$)", flags), "$1 13x hektara$16x");
        areaDict.put(Pattern.compile("((\\W|^)(" + prepositions.get(DAT)+ ") ((\\d{1,2}\\.)?(\\d{3}\\.?)*(\\d*[02-9]|\\d,\\d*[02-9]))) ha\\.?(\\W|$)", flags),"$1 hekturum$10");
        areaDict.put(Pattern.compile("((\\W|^)(" + prepositions.get(DAT) + ") (((\\d{1,2}\\.)?(\\d{3}\\.?)*|\\d+)(,\\d+)?)?) " + patternSelection.get(AMOUNT) + " ha\\.?(\\W|$)", flags), "$1 1$1 hekturum$14");
        areaDict.put(Pattern.compile("(1) ha\\.?(\\W|$)", flags), "$1 hektari$2");
        areaDict.put(Pattern.compile("([02-9]|" + patternSelection.get(AMOUNT) + ") ha\\.?(\\W|$)", flags),"$1 hektarar $3");

        final Map<String, String> prefixMeterDimension = new HashMap<>() {{
            put("", "");        // also match without prefix
            put("m", "milli");
            put("[cs]", "senti");
            put("d", "desi");
            put("k", "kíló");
            put("f", "fer");
            put("rúm", "rúm");
            //TODO: also píkó, nanó, míkró, njúton, in original regina
        }};

        for (String letter : prefixMeterDimension.keySet()) {
            for (String superscript : dimensionAfter.keySet()) {
                areaDict.put(Pattern.compile("((\\W|^)(" + prepositions.get(ACC_DAT_GEN_COMB) + ") ((\\d{1,2}\\.)?(\\d{3}\\.?)*(\\d*1|\\d,\\d*1)) " + letter + "m" + superscript + "(\\W|$))", flags),
                        "$1 " + dimensionAfter.get(superscript) + prefixMeterDimension.get(letter) + "metra$14");
                areaDict.put(Pattern.compile("((\\W|^)(" + prepositions.get(ACC_GEN) + ") ((\\d{1,2}\\.)?(\\d{3}\\.?)*(\\d*[02-9]|\\d,\\d*[02-9]))) " + letter + "m" + superscript + "(\\W|$)", flags),
                        "$1 " + dimensionAfter.get(superscript) + prefixMeterDimension.get(letter) + "metra$12");
                areaDict.put(Pattern.compile("((\\W|^)(" + prepositions.get(ACC_GEN) + ") (((\\d{1,2}\\.)?(\\d{3}\\.?)*|\\d+)(,\\d+)?)?) " + patternSelection.get(AMOUNT) + " " + letter + "m" + superscript + "(\\W|$)", flags),
                        "$1 13x " + dimensionAfter.get(superscript) + prefixMeterDimension.get(letter) + "metra$16");
                areaDict.put(Pattern.compile("((\\W|^)(" + prepositions.get(DAT) + ") ((\\d{1,2}\\.)?(\\d{3}\\.?)*(\\d*[02-9]|\\d,\\d*[02-9]))) " + letter + "m" + superscript + "(\\W|$)", flags),
                        "$1 " + dimensionAfter.get(superscript) + prefixMeterDimension.get(letter) + "metrum$10");
                areaDict.put(Pattern.compile("((\\W|^)(" + prepositions.get(DAT) + ") (((\\d{1,2}\\.)?(\\d{3}\\.?)*|\\d+)(,\\d+)?)?) " + patternSelection.get(AMOUNT) + " " + letter + "m" + superscript + "(\\W|$)", flags),
                        "$1 1$1 " + dimensionAfter.get(superscript) + prefixMeterDimension.get(letter) + "metrum$14");
                areaDict.put(Pattern.compile("(1 )" + letter + "m" + superscript + "(\\W|$)", flags), "$1" + dimensionAfter.get(superscript) + prefixMeterDimension.get(letter) + "metri$2");
                areaDict.put(Pattern.compile("([02-9] )" + letter + "m" + superscript + "(\\W|$)", flags), "$1" + dimensionAfter.get(superscript) + prefixMeterDimension.get(letter) + "metrar$2");
                areaDict.put(Pattern.compile("([02-9]|" + patternSelection.get(AMOUNT) + ") " + letter + "m" + superscript + "(\\W|$)", flags), "$1 " + dimensionAfter.get(superscript) + prefixMeterDimension.get(letter) + "metrar $3");
            }
        }
        for (String letter : prefixMeterDimension.keySet()) {

                areaDict.put(Pattern.compile("((\\W|^)(" + prepositions.get(ACC_DAT_GEN_COMB) + ") ((\\d{1,2}\\.)?(\\d{3}\\.?)*(\\d*1|\\d,\\d*1)) " + letter + "m" + EOS + ")", flags),
                        "$1 " + prefixMeterDimension.get(letter) + "metra$14");
                areaDict.put(Pattern.compile("((\\W|^)(" + prepositions.get(ACC_GEN) + ") ((\\d{1,2}\\.)?(\\d{3}\\.?)*(\\d*[02-9]|\\d,\\d*[02-9]))) " + letter + "m" + EOS, flags),
                        "$1 " + prefixMeterDimension.get(letter) + "metra$12");
                areaDict.put(Pattern.compile("((\\W|^)(" + prepositions.get(ACC_GEN) + ") (((\\d{1,2}\\.)?(\\d{3}\\.?)*|\\d+)(,\\d+)?)?) " + patternSelection.get(AMOUNT) + " " + letter + "m" + EOS, flags),
                        "$1 $13 " + prefixMeterDimension.get(letter) + "metra$16");
                areaDict.put(Pattern.compile("((\\W|^)(" + prepositions.get(DAT) + ") ((\\d{1,2}\\.)?(\\d{3}\\.?)*(\\d*[02-9]|\\d,\\d*[02-9]))) " + letter + "m" + EOS, flags),
                        "$1 " + prefixMeterDimension.get(letter) + "metrum$10");
                areaDict.put(Pattern.compile("((\\W|^)(" + prepositions.get(DAT) + ") (((\\d{1,2}\\.)?(\\d{3}\\.?)*|\\d+)(,\\d+)?)?) " + patternSelection.get(AMOUNT) + " " + letter + "m" + EOS, flags),
                        "$1 1$1 " + prefixMeterDimension.get(letter) + "metrum$14");
                areaDict.put(Pattern.compile("(1 )" + letter + "m" + EOS, flags), "$1" + prefixMeterDimension.get(letter) + "metri$2");
                areaDict.put(Pattern.compile("([02-9] )" + letter + "m" + EOS, flags), "$1" + prefixMeterDimension.get(letter) + "metrar$2");
                areaDict.put(Pattern.compile("([02-9]|" + patternSelection.get(AMOUNT) + ") " + letter + "m" + EOS, flags), "$1 " + prefixMeterDimension.get(letter) + "metrar $3");

        }
        return areaDict;

        /**     Not clear, what the following code is supposed to do

         final Map<String, String> dimensionBefore = new HashMap<>() {{
            put("f", "fer");
            put("fer", "fer");
            put("rúm", "rúm");
         }};
        for (String letter : prefixMeterDimension.keySet()) {
            for (String preprefix : dimensionBefore.keySet()) {
                areaDict.put(Pattern.compile("((\\W|^)(" + prepositions.get(ACC_DAT_GEN_COMB) + ") ((\\d{1,2}\\.)?(\\d{3}\\.?)*(\\d*1|\\d,\\d*1))) " + preprefix + letter + "m\\.?(\\W|$)", flags),
                        "$1 " + dimensionBefore.get(preprefix) + prefixMeterDimension.get(letter) + "metra$14");
                areaDict.put(Pattern.compile("((\\W|^)(" + prepositions.get(ACC_GEN) + ") ((\\d{1,2}\\.)?(\\d{3}\\.?)*(\\d*[02-9]|\\d,\\d*[02-9]))) " + preprefix + letter + "m\\.?(\\W|$)", flags),
                        "$1 " + dimensionBefore.get(preprefix) + prefixMeterDimension.get(letter) + "metra$12");
                areaDict.put(Pattern.compile("((\\W|^)(" + prepositions.get(ACC_GEN) + ") (((\\d{1,2}\\.)?(\\d{3}\\.?)*|\\d+)(,\\d+)?)?) " + patternSelection.get(AMOUNT) + " " + preprefix + letter + "m\\.?(\\W|$)", flags),
                        "$1 13x " + dimensionBefore.get(preprefix) + prefixMeterDimension.get(letter) + "metra$16");
                areaDict.put(Pattern.compile("((\\W|^)(" + prepositions.get(DAT) + ") ((\\d{1,2}\\.)?(\\d{3}\\.?)*(\\d*[02-9]|\\d,\\d*[02-9]))) " + preprefix + letter + "m\\.?(\\W|$)", flags),
                        "$1 " + dimensionBefore.get(preprefix) + prefixMeterDimension.get(letter) + "metrum$8"); //10 -> 8
                areaDict.put(Pattern.compile("((\\W|^)(" + prepositions.get(DAT) + ") (((\\d{1,2}\\.)?(\\d{3}\\.?)*|\\d+)(,\\d+)?)?) " + patternSelection.get(AMOUNT) + " " + preprefix + letter + "m\\.?(\\W|$)", flags),
                        "$1 1$1" + dimensionBefore.get(preprefix) + prefixMeterDimension.get(letter) + "metrum$14");
                areaDict.put(Pattern.compile("(1 )" + preprefix + letter + "m\\.?(\\W|$)", flags), "$1 " + dimensionBefore.get(preprefix) + prefixMeterDimension.get(letter) + "metri$2");
                areaDict.put(Pattern.compile("([02-9]|" + patternSelection.get(AMOUNT) + ") " + preprefix + letter + "m\\.?(\\W|$)", flags), "$1 " + dimensionBefore.get(preprefix) + prefixMeterDimension.get(letter) + "metrar$3");
                //added ABN, previous patterns did not capture " ... ( 150.000 m² ) ..." - still need to figure that out
                areaDict.put(Pattern.compile("((\\W|^)(\\d{3}\\.\\d{3})) " + letter + "m" + preprefix + "(\\W|$)", flags), "$1 " + dimensionBefore.get(preprefix) + prefixMeterDimension.get(letter) + "metrar$4");
            }
        }
        return areaDict;
         */
    }

    private static final Map<Pattern, String> volumeDict = new HashMap<>();

    // TODO: missing explicit tests !!
    public static Map<Pattern, String> getVolumeDict() {
        if (!volumeDict.isEmpty())
            return volumeDict;

        Map<String, String> prefixLiter = new HashMap<>() {{
            put("", "");
            put("d", "desi");
            put("c", "senti");
            put("m", "milli");
        }};
        int flags = 0;
        for (String letter : prefixLiter.keySet()) {
            volumeDict.put(Pattern.compile("((\\W|^)(" + prepositions.get(ACC_DAT_GEN_COMB)+ ") ((\\d{1,2}\\.)?(\\d{3}\\.?)*(\\d*1|\\d,\\d*1))) " + letter + "[Ll]\\.?(\\W|$)", flags), "$1 " + prefixLiter.get(letter) + "lítra$14x");
            volumeDict.put(Pattern.compile("((\\W|^)(" + prepositions.get(ACC_GEN) + ") ((\\d{1,2}\\.)?(\\d{3}\\.?)*(\\d*[02-9]|\\d,\\d*[02-9]))) " + letter + "[Ll]\\.?(\\W|$)", flags), "1 " + prefixLiter.get(letter) + "lítra$9"); // 12->9
            volumeDict.put(Pattern.compile("((\\W|^)(" + prepositions.get(ACC_GEN) + ") (((\\d{1,2}\\.)?(\\d{3}\\.?)*|\\d+)(,\\d+)?)?) " + patternSelection.get(AMOUNT) + " " + letter + "[Ll]\\.?(\\W|$)", flags), "$1 13x " + prefixLiter.get(letter) + "lítra$16");
            volumeDict.put(Pattern.compile("((\\W|^)(" + prepositions.get(DAT) + ") ((\\d{1,2}\\.)?(\\d{3}\\.?)*(\\d*[02-9]|\\d,\\d*[02-9]))) " + letter + "[Ll]\\.?(\\W|$)", flags), "$1 " + prefixLiter.get(letter) + "lítrum$10");
            volumeDict.put(Pattern.compile("((\\W|^)(" + prepositions.get(DAT) + ") (((\\d{1,2}\\.)?(\\d{3}\\.?)*|\\d+)(,\\d+)?)?) " + patternSelection.get(AMOUNT) + " " + letter + "[Ll]\\.?(\\W|$)", flags), "$1 1$1 " + prefixLiter.get(letter) + "lítrum$14");
            volumeDict.put(Pattern.compile("(1 )" + letter + "[Ll]\\.?(\\W|$)", flags), "$1" + prefixLiter.get(letter) + "lítri$2");
            volumeDict.put(Pattern.compile("([02-9]|" + patternSelection.get(AMOUNT) + ") " + letter + "[Ll]\\.?(\\W|$)", flags), "");
            //if (!letter.isEmpty())
            //    volumeDict.put("Pattern.compile((\\W|^)" + letter + "l\\.?(\\W|$)", flags), "$1" + prefixLiter.get(letter) + "lítrar $2");
        }
        return volumeDict;
    }

    private static final Map<Pattern, String> timeDict = new HashMap<>();

    // TODO: missing explicit tests !!
    public static Map<Pattern, String> getTimeDict() {
        if  (!timeDict.isEmpty())
            return timeDict;
        int flags = 0;
        timeDict.put(Pattern.compile("((\\W|^)(" + prepositions.get(GEN) + ") ((\\d{1,2}\\.)?(\\d{3}\\.?)*(\\d*1|\\d,\\d*1))) klst\\.?(\\W|$)", flags),  "$1 klukkustundar$10");
        timeDict.put(Pattern.compile("((\\W|^)(" + prepositions.get(DAT) + ") ((\\d{1,2}\\.)?(\\d{3}\\.?)*(\\d*[02-9]|\\d,\\d*[02-9]))) klst\\.?(\\W|$)", flags), "$1 klukkustundum$10");
        timeDict.put(Pattern.compile("((\\W|^)(" + prepositions.get(DAT) + ") (((\\d{1,2}\\.)?(\\d{3}\\.?)*|\\d+)(,\\d+)?)?) " + patternSelection.get(AMOUNT) + " klst\\.?(\\W|$)", flags), "$1 1$1 klukkustundum$14");
        timeDict.put(Pattern.compile("((\\W|^)(" + prepositions.get(GEN) + ") ((\\d{1,2}\\.)?(\\d{3}\\.?)*(\\d*[02-9]|\\d,\\d*[02-9]))) klst\\.?(\\W|$)", flags), "$1 klukkustunda$10");
        timeDict.put(Pattern.compile("((\\W|^)(" + prepositions.get(GEN) + ") (((\\d{1,2}\\.)?(\\d{3}\\.?)*|\\d+)(,\\d+)?)?) " + patternSelection.get(AMOUNT) + " klst\\.?(\\W|$)", flags), "$1 1$1 klukkustunda$14");
        timeDict.put(Pattern.compile("(1 )klst\\.?(\\W|$)", flags), "$1 klukkustund$2");
        timeDict.put(Pattern.compile("(\\W|^)klst\\.?(\\W|$)", flags), "$1klukkustundir$2");

        final Map<String, String> prefixTime = new HashMap<>() {{
            put("mín()?", "mínút");
            put("s(ek)?", "sekúnd");
            put("ms(ek)?", "millisekúnd");
        }};

        for (String letters : prefixTime.keySet()) {
            timeDict.put(Pattern.compile("(" + BOS + "(" + prepositions.get(ACC_DAT_GEN_COMB) + ") ((\\d{1,2}\\.)?(\\d{3}\\.?)*(\\d*1|\\d,\\d*1))) " + letters + DOT_ONE_NONE + EOS, flags), "$1 " + prefixTime.get(letters) + "u$11");
            timeDict.put(Pattern.compile("(" + BOS + "(" + prepositions.get(DAT) + ") ((\\d{1,2}\\.)?(\\d{3}\\.?)*(\\d*[02-9]|\\d,\\d*[02-9]))) " + letters + DOT_ONE_NONE + EOS, flags), "$1 " + prefixTime.get(letters) + "um$11");
            timeDict.put(Pattern.compile("(" + BOS + "(" + prepositions.get(DAT) + ") (((\\d{1,2}\\.)?(\\d{3}\\.?)*|\\d+)(,\\d+)?)?) " + patternSelection.get(AMOUNT) + " " + letters + DOT_ONE_NONE + EOS, flags), "$1 1$1 " + prefixTime.get(letters) + "um$15");
            timeDict.put(Pattern.compile("(" + BOS + "(" + prepositions.get(GEN) + ") ((\\d{1,2}\\.)?(\\d{3}\\.?)*(\\d*[02-9]|\\d,\\d*[02-9]))) " + letters + DOT_ONE_NONE + EOS, flags), "$1 " + prefixTime.get(letters) + "na$9"); //changed group from 11 to 9
            timeDict.put(Pattern.compile("(" + BOS + "(" + prepositions.get(GEN) + ") (((\\d{1,2}\\.)?(\\d{3}\\.?)*|\\d+)(,\\d+)?)?) " + patternSelection.get(AMOUNT) + " " + letters + DOT_ONE_NONE + EOS, flags), "$1 1$1 " + prefixTime.get(letters) + "na$15");
            // added ABN: we need 'undir' ('undir x sek/klst/...')
            timeDict.put(Pattern.compile("((\\W|^)(" + prepositions.get(ACC_DAT) + ") ((\\d{1,2}\\.)?(\\d{3}\\.?)*(\\d*[02-9]|\\d,\\d*[02-9]))) " + letters + DOT_ONE_NONE + EOS, flags), "$1 " + prefixTime.get(letters) + "um$9");

            timeDict.put(Pattern.compile("[02-9]?(1 )" + letters + DOT_ONE_NONE + EOS, flags), "$1" + prefixTime.get(letters) + "a$2");
            timeDict.put(Pattern.compile("(11 )" + letters + DOT_ONE_NONE + EOS, flags), "$1" + prefixTime.get(letters) + "ur$2");
            timeDict.put(Pattern.compile("([2-9] )" + letters + DOT_ONE_NONE + EOS, flags), "$1" + prefixTime.get(letters) + "ur$2");
            //TODO: this one messes up, need to give the preposition patterns priority and not allow this one to intervene. But why do they both match after one has been substituted? I.e. " ... sekúndur ..." matches the pattern above with preposition
            //timeDict.put(Pattern.compile("([02-9]|" + patternSelection.get(AMOUNT) + ") " + letters + "\\.?(\\W|$)", flags), "$1 " + prefixTime.get(letters) + "ur $3");
        }
        return timeDict;
    }

    public static final Map<Pattern, String> currencyDict = new HashMap<>();

    // TODO: missing explicit tests !!
    public static Map<Pattern, String> getCurrencyDict() {
        if (!currencyDict.isEmpty())
            return currencyDict;
        int flags = 0;
        // krónur:
        currencyDict.put(Pattern.compile("((\\W|^)(" + prepositions.get(DAT) + ")) kr\\.?-? ?((((\\d{1,2}\\.)?(\\d{3}\\.?)*|\\d+)(,\\d+)?)? " + patternSelection.get(AMOUNT) + ")" + EOS, flags), "$1 6x krónum$15");
        currencyDict.put(Pattern.compile("((\\W|^)(" + prepositions.get(GEN) + ")) kr\\.?-? ?((((\\d{1,2}\\.)?(\\d{3}\\.?)*|\\d+)(,\\d+)?)? " + patternSelection.get(AMOUNT) + ")" + EOS, flags), "$1 6xkróna$15");
        currencyDict.put(Pattern.compile("(\\W|^)[Kk]r\\.? ?((((\\d{1,2}\\.)?(\\d{3}\\.?)*|\\d+)(,\\d+)?)? " + patternSelection.get(AMOUNT) + ")" + EOS, flags), "$1 2x krónur$11");
        currencyDict.put(Pattern.compile("((\\W|^)(" + prepositions.get(ACC_DAT_GEN_COMB) + ") ((\\d{1,2}\\.)?(\\d{3}\\.?)*(\\d*1|\\d,\\d*1))) ?kr\\.?-?" + EOS, flags), "$1 krónu$14");
        currencyDict.put(Pattern.compile("((\\W|^)(" + prepositions.get(ACC_DAT_GEN_COMB) + ")) kr\\.?-? ?((\\d{1,2}\\.)?(\\d{3}\\.?)*(\\d*1|\\d,\\d*1))" + EOS, flags), "$1 10x krónu$14");
        currencyDict.put(Pattern.compile("((\\W|^)(" + prepositions.get(DAT) + ") ((\\d{1,2}\\.)?(\\d{3}\\.?)*(\\d*[02-9]|\\d,\\d*[02-9]))) kr\\.?-?" + EOS, flags), "$1 krónum$10");
        currencyDict.put(Pattern.compile("((\\W|^)(" + prepositions.get(DAT) + ") (((\\d{1,2}\\.)?(\\d{3}\\.?)*|\\d+)(,\\d+)?)? " + patternSelection.get(AMOUNT) + ") kr\\.?-?" + EOS, flags), "$1 8xkrónum$14");
        currencyDict.put(Pattern.compile("((\\W|^)(" + prepositions.get(DAT) + ")) kr\\.?-? ?((\\d{1,2}\\.)?(\\d{3}\\.?)*(\\d*[02-9]|\\d,\\d*[02-9]))" + EOS, flags), "$1 9x krónum$10");
        currencyDict.put(Pattern.compile("((\\W|^)(" + prepositions.get(GEN) + ") ((\\d{1,2}\\.)?(\\d{3}\\.?)*(\\d*[02-9]|\\d,\\d*[02-9]))) kr\\.?-?" + EOS, flags), "$1 króna$10");
        currencyDict.put(Pattern.compile("((\\W|^)(" + prepositions.get(GEN) + ") (((\\d{1,2}\\.)?(\\d{3}\\.?)*|\\d+)(,\\d+)?)? " + patternSelection.get(AMOUNT) + ") kr\\.?-?" + EOS, flags), "$1 8xkróna$14");
        currencyDict.put(Pattern.compile("((\\W|^)(" + prepositions.get(GEN) + ")) kr\\.?-? ?((\\d{1,2}\\.)?(\\d{3}\\.?)*(\\d*[02-9]|\\d,\\d*[02-9]))" + EOS, flags), "$1 9x króna$10");
        currencyDict.put(Pattern.compile("(1 ?)kr\\.?-?(\\W|$)", flags), "$1króna$2");
        currencyDict.put(Pattern.compile("([02-9]|" + patternSelection.get(AMOUNT) + ") ?kr\\.?-?" + EOS, flags), "$1 krónur $3");
        // is this an error? (2 times group 2)
        //currencyDict.put(Pattern.compile("(\\W|^)[Kk]r\\.? ?(\\d)", flags), "$12x krónur$2");
        currencyDict.put(Pattern.compile("(\\W|^)[Kk]r\\.? ?(\\d)", flags), "$1krónur $2");


        // dollars ($)
        currencyDict.put(Pattern.compile("((\\W|^)(" + prepositions.get(ACC_GEN) + ")) \\$((((\\d{1,2}\\.)?(\\d{3}\\.?)*|\\d+)(,\\d+)?)? " + patternSelection.get(AMOUNT) + ")" + EOS, flags), "$1 $8 dollara$17");
        currencyDict.put(Pattern.compile("((\\W|^)(" + prepositions.get(DAT) + ")) \\$((((\\d{1,2}\\.)?(\\d{3}\\.?)*|}\\d+)(,\\d+)?)? " + patternSelection.get(AMOUNT) + ")" + EOS, flags), "$1 $6 dollurum$15");
        currencyDict.put(Pattern.compile("(\\W|^)\\$((((\\d{1,2}\\.)?(\\d{3}\\.?)*|\\d+)(,\\d+)?)? " + patternSelection.get(AMOUNT) + ")" + EOS, flags), "$1 $2 dollarar$11");
        currencyDict.put(Pattern.compile("((\\W|^)(" + prepositions.get(ACC_DAT_GEN_COMB) + ") ((\\d{1,2}\\.)?(\\d{3}\\.?)*(\\d*1|\\d,\\d*1))) ?\\$" + EOS, flags), "$1 dollara$14");
        currencyDict.put(Pattern.compile("((\\W|^)(" + prepositions.get(ACC_DAT_GEN_COMB) + ")) \\$((\\d{1,2}\\.)?(\\d{3}\\.?)*(\\d*1|\\d,\\d*1))" + EOS, flags), "$1 $10 dollara$14");
        currencyDict.put(Pattern.compile("((\\W|^)(" + prepositions.get(ACC_GEN) + ") ((\\d{1,2}\\.)?(\\d{3}\\.?)*(\\d*[02-9]|\\d,\\d*[02-9]))) ?\\$" + EOS, flags), "$1 dollara$12");
        currencyDict.put(Pattern.compile("((\\W|^)(" + prepositions.get(ACC_GEN) + ") (((\\d{1,2}\\.)?(\\d{3}\\.?)*|\\d+)(,\\d+)?)?) " + patternSelection.get(AMOUNT) + " ?\\$" + EOS, flags), "$1 $13 dollara$16");
        currencyDict.put(Pattern.compile("((\\W|^)(" + prepositions.get(ACC_GEN) + ")) \\$((\\d{1,2}\\.)?(\\d{3}\\.?)*(\\d*[02-9]|\\d,\\d*[02-9]))" + EOS, flags), "$1 $11 dollara$12");
        currencyDict.put(Pattern.compile("((\\W|^)(" + prepositions.get(DAT) + ") ((\\d{1,2}\\.)?(\\d{3}\\.?)*(\\d*[02-9]|\\d,\\d*[02-9]))) ?\\$" + EOS, flags), "$1 dollurum$10");
        currencyDict.put(Pattern.compile("((\\W|^)(" + prepositions.get(DAT) + ") (((\\d{1,2}\\.)?(\\d{3}\\.?)*|\\d+)(,\\d+)?)?) " + patternSelection.get(AMOUNT) + " ?\\$" + EOS, flags), "$1 $11 dollurum$14");
        currencyDict.put(Pattern.compile("((\\W|^)(" + prepositions.get(DAT) + ")) \\$((\\d{1,2}\\.)?(\\d{3}\\.?)*(\\d*[02-9]|\\d,\\d*[02-9]))" + EOS, flags), "$1 $9 dollurum$10");
        currencyDict.put(Pattern.compile("(1 ?)\\$" + EOS, flags), "$1 dollari$2");
        currencyDict.put(Pattern.compile("([02-9]|" + patternSelection.get(AMOUNT) + ") ?\\$" + EOS, flags), "$1 dollarar$2");
        currencyDict.put(Pattern.compile("(\\W|^) ?\\$((\\d{1,2}\\.)?(\\d{3}\\.?)*(\\d*1|\\d,\\d*1))" + EOS, flags), "$1$2 dollari$6");
        currencyDict.put(Pattern.compile("(\\W|^) ?\\$((\\d{1,2}\\.)?(\\d{3}\\.?)*(\\d*[02-9]|\\d,\\d*[02-9]))" + EOS, flags), "$1$2 dollarar$6");
        currencyDict.put(Pattern.compile("(\\W|^)\\$" + EOS, flags), "$1dollari$2");

        // pounds (£)
        currencyDict.put(Pattern.compile("((\\W|^)(" + prepositions.get(DAT) + ")) £((((\\d{1,2}\\.)?(\\d{3}\\.?)*|\\d+)(,\\d+)?)? " + patternSelection.get(AMOUNT) + ")" + EOS, flags), "$1 $6 pundum$15");
        currencyDict.put(Pattern.compile("((\\W|^)(" + prepositions.get(GEN) + ")) £((((\\d{1,2}\\.)?(\\d{3}\\.?)*|\\d+)(,\\d+)?)? " + patternSelection.get(AMOUNT) + ")" + EOS, flags), "$1 $6 punda$15");
        currencyDict.put(Pattern.compile("(\\W|^)£((((\\d{1,2}\\.)?(\\d{3}\\.?)*|\\d+)(,\\d+)?)? " + patternSelection.get(AMOUNT) + "?)" + EOS, flags), "$1 $2 pund$11");
        currencyDict.put(Pattern.compile("((\\W|^)(" + prepositions.get(DAT) + ") ((\\d{1,2}\\.)?(\\d{3}\\.?)*(\\d*1|\\d,\\d*1))) ?£" + EOS, flags), "$1 pundi$10");
        currencyDict.put(Pattern.compile("((\\W|^)(" + prepositions.get(DAT) + ")) £((\\d{1,2}\\.)?(\\d{3}\\.?)*(\\d*1|\\d,\\d*1))" + EOS, flags), "$1 $6 pundi$10");
        currencyDict.put(Pattern.compile("((\\W|^)(" + prepositions.get(GEN) + ") ((\\d{1,2}\\.)?(\\d{3}\\.?)*(\\d*1|\\d,\\d*1))) ?£" + EOS, flags), "$1 punds$10");
        currencyDict.put(Pattern.compile("((\\W|^)(" + prepositions.get(GEN) + ")) £((\\d{1,2}\\.)?(\\d{3}\\.?)*(\\d*1|\\d,\\d*1))" + EOS, flags), "$1 $6 punds$10");
        currencyDict.put(Pattern.compile("((\\W|^)(" + prepositions.get(DAT) + ") ((\\d{1,2}\\.)?(\\d{3}\\.?)*(\\d*[02-9]|\\d,\\d*[02-9]))) ?£" + EOS, flags), "$1 pundum$10");
        currencyDict.put(Pattern.compile("((\\W|^)(" + prepositions.get(DAT) + ") (((\\d{1,2}\\.)?(\\d{3}\\.?)*|\\d+)(,\\d+)?)? " + patternSelection.get(AMOUNT) + ") ?£" + EOS, flags), "$1 $8pundum$14");
        currencyDict.put(Pattern.compile("((\\W|^)(" + prepositions.get(DAT) + ")) £((\\d{1,2}\\.)?(\\d{3}\\.?)*(\\d*[02-9]|\\d,\\d*[02-9]))" + EOS, flags), "$1 $9 pundum$10");
        currencyDict.put(Pattern.compile("((\\W|^)(" + prepositions.get(GEN) + ") ((\\d{1,2}\\.)?(\\d{3}\\.?)*(\\d*[02-9]|\\d,\\d*[02-9]))) ?£" + EOS, flags), "$1 punda$10");
        currencyDict.put(Pattern.compile("((\\W|^)(" + prepositions.get(GEN) + ") (((\\d{1,2}\\.)?(\\d{3}\\.?)*|\\d+)(,\\d+)?)? " + patternSelection.get(AMOUNT) + ") ?£" + EOS, flags), "$1 $8punda$14");
        currencyDict.put(Pattern.compile("((\\W|^)(" + prepositions.get(GEN) + ")) £((\\d{1,2}\\.)?(\\d{3}\\.?)*(\\d*[02-9]|\\d,\\d*[02-9]))" + EOS, flags), "$1 $9 punda$10");
        currencyDict.put(Pattern.compile("(\\W|^) ?£(((\\d{1,2}\\.)?(\\d{3}\\.?)*|\\d+)(,\\d+)?)" + EOS, flags), "$1$2 pund$7");
        currencyDict.put(Pattern.compile("(\\W|^)£" + EOS, flags), "$1pund$2");

        // Yen (¥) - TODO

        // Euro (€), ( add later if needed: Ruble and Turkish lira : ₹ and ₤ )
        Map<String, String> currencyList = new HashMap<>();
        currencyList.put("evr", "€");
        //currencyList.put("rúpí", "₹");
        //currencyList.put("lír", "₤");
        for (String word : currencyList.keySet()) {
            currencyDict.put(Pattern.compile("((\\W|^)(" + prepositions.get(DAT) + ")) " + currencyList.get(word) + "((((\\d{1,2}\\.)?(\\d{3}\\.?)*|\\d+)(,\\d+)?)? " + patternSelection.get(AMOUNT) + ")" + EOS, flags), "$1 $6 " + word + "um$15");
            currencyDict.put(Pattern.compile("((\\W|^)(" + prepositions.get(GEN) + ")) " + currencyList.get(word) + "((((\\d{1,2}\\.)?(\\d{3}\\.?)*|\\d+)(,\\d+)?)? " + patternSelection.get(AMOUNT) + ")" + EOS, flags), "$1 $6 " + word + "a$15");
            currencyDict.put(Pattern.compile("(\\W|^)" + currencyList.get(word) + "((((\\d{1,2}\\.)?(\\d{3}\\.?)*|\\d+)(,\\d+)?)? " + patternSelection.get(AMOUNT) + ")" + EOS, flags), "$1 $2 " + word + "ur$11");
            currencyDict.put(Pattern.compile("((\\W|^)(" + prepositions.get(ACC_DAT_GEN_COMB) + ") ((\\d{1,2}\\.)?(\\d{3}\\.?)*(\\d*1|\\d,\\d*1))) ?" + currencyList.get(word) + EOS, flags), "$1 " + word + "u$14");
            currencyDict.put(Pattern.compile("((\\W|^)(" + prepositions.get(ACC_DAT_GEN_COMB) + ")) " + currencyList.get(word) + "((\\d{1,2}\\.)?(\\d{3}\\.?)*(\\d*1|\\d,\\d*1))" + EOS, flags), "$1 $10 " + word + "u$14");
            currencyDict.put(Pattern.compile("((\\W|^)(" + prepositions.get(DAT) + ") ((\\d{1,2}\\.)?(\\d{3}\\.?)*(\\d*[02-9]|\\d,\\d*[02-9]))) ?" + currencyList.get(word) + EOS, flags), "$1 " + word + "um$10");
            currencyDict.put(Pattern.compile("((\\W|^)(" + prepositions.get(DAT) + ") (((\\d{1,2}\\.)?(\\d{3}\\.?)*|\\d+)(,\\d+)?)? " + patternSelection.get(AMOUNT) + ") ?" + currencyList.get(word) + EOS, flags), "$1 $8" + word + "um$14");
            currencyDict.put(Pattern.compile("((\\W|^)(" + prepositions.get(DAT) + ")) " + currencyList.get(word) + "((\\d{1,2}\\.)?(\\d{3}\\.?)*(\\d*[02-9]|\\d,\\d*[02-9]))" + EOS, flags), "$1 $9 " + word + "um$10");
            currencyDict.put(Pattern.compile("((\\W|^)(" + prepositions.get(GEN) + ") ((\\d{1,2}\\.)?(\\d{3}\\.?)*(\\d*[02-9]|\\d,\\d*[02-9]))) ?" + currencyList.get(word) + EOS, flags), "$1 " + word + "a$10");
            currencyDict.put(Pattern.compile("((\\W|^)(" + prepositions.get(GEN) + ") (((\\d{1,2}\\.)?(\\d{3}\\.?)*|\\d+)(,\\d+)?)? " + patternSelection.get(AMOUNT) + ") ?" + currencyList.get(word) + EOS, flags), "$1 $8" + word + "a$14");
            currencyDict.put(Pattern.compile("((\\W|^)(" + prepositions.get(GEN) + ")) " + currencyList.get(word) + "((\\d{1,2}\\.)?(\\d{3}\\.?)*(\\d*[02-9]|\\d,\\d*[02-9]))" + EOS, flags), "$1 $9 " + word + "a$10");
            currencyDict.put(Pattern.compile("(1 ?)" + currencyList.get(word) + EOS, flags), "$1 " + word + "a$2");
            currencyDict.put(Pattern.compile("([02-9]|" + patternSelection.get(AMOUNT) + ") ?" + currencyList.get(word) + EOS, flags), "$1 " + word + "ur$2");
            currencyDict.put(Pattern.compile("(\\W|^) ?" + currencyList.get(word) + "((\\d{1,2}\\.)?(\\d{3}\\.?)*(\\d*1|\\d,\\d*1))" + EOS, flags), "$1$2 " + word + "a$6");
            currencyDict.put(Pattern.compile("(\\W|^) ?" + currencyList.get(word) + "((\\d{1,2}\\.)?(\\d{3}\\.?)*(\\d*[02-9]|\\d,\\d*[02-9]))" + EOS, flags), "$1$2 " + word + "a$6");
            currencyDict.put(Pattern.compile("(\\W|^)" + currencyList.get(word) + EOS, flags), "$1" + word + "ur$2");
        }

        Map<String, String> currencyLetters = new HashMap<>();
        currencyLetters.put("ISK", "íslenskar krónur");
        currencyLetters.put("GBP", "sterlingspund");
        currencyLetters.put("EUR", "evrur");
        currencyLetters.put("USD", "bandaríkjadalir");
        currencyLetters.put("DKK", "danskar krónur");
        currencyLetters.put("AUD", "ástralskir dalir");
        currencyLetters.put("JPY", "japanskt jen");
        currencyLetters.put("CHF", "svissneskir frankar");
        currencyLetters.put("CAD", "kanadískir dalir");
        currencyLetters.put("CZK", "tékkneskar krónur");
        currencyLetters.put("INR", "indverskar rúpíur");
        currencyLetters.put("SEK", "sænskar krónur");
        currencyLetters.put("NOK", "norskar krónur");
        currencyLetters.put("PTE", "portúgalskir skútar");

        for (String letters : currencyLetters.keySet()) {
            currencyDict.put(Pattern.compile("(\\W|^)" + letters + EOS, flags), "$1 " + currencyLetters.get(letters) + "$2");
        }

        // TODO: million-list and billion-list

        return currencyDict;
    }

    public static final Map<Pattern, String> electronicDict = new HashMap<>();

    // TODO: missing explicit tests !!
    public static Map<Pattern, String> getElectronicDict() {
        if (!electronicDict.isEmpty())
            return electronicDict;

        Map<String, String> wattPrefix = new HashMap<>();
        wattPrefix.put("", "");
        wattPrefix.put("k", "kíló");
        wattPrefix.put("M", "Mega");
        wattPrefix.put("G", "Gíga");
        wattPrefix.put("T", "Tera");

        Map<String, String> measurement = new HashMap<>();  // TODO: unused
        measurement.put("V", "volt");
        measurement.put("Hz", "herz");

        int flags = 0;
        for (String letter : wattPrefix.keySet()) {
            electronicDict.put(Pattern.compile("(" + BOS + "(" + prepositions.get(GEN) + ") (" + NUMBER_EOS_1 + ")) " + letter + "[Ww]"
                    + DOT_ONE_NONE + "(st|h)" + DOT_ONE_NONE + EOS, flags), "$1 " + wattPrefix.get(letter) + "vattstundar$11");
            electronicDict.put(Pattern.compile("(" + BOS + "(" + prepositions.get(DAT) + ") (" + NUMBER_EOS_NOT_1 + ") " + letter + "[Ww]"
                    + DOT_ONE_NONE + "(st|h)" + DOT_ONE_NONE + EOS, flags), "$1 " + wattPrefix.get(letter) + "vattstundum$11");
            electronicDict.put(Pattern.compile("(" + BOS + "(" + prepositions.get(DAT) + ") (" + NUMBER_ANY + ") " + patternSelection.get(AMOUNT)
                    + ") " + letter + "[Ww]" + DOT_ONE_NONE + "(st|h)" + DOT_ONE_NONE + EOS, flags), "$1 1$1 " + wattPrefix.get(letter) + "vattstundum$15");
            electronicDict.put(Pattern.compile("([02-9]|" + patternSelection.get(AMOUNT) + ") " + letter + "W" + EOS, flags), "$1 " + wattPrefix.get(letter) + "vött $3");
            electronicDict.put(Pattern.compile("(1 )" + letter + "W" + EOS, flags), "$1 " + wattPrefix.get(letter) + "vatt$2");
            electronicDict.put(Pattern.compile("([02-9]|" + patternSelection.get(AMOUNT) + ") " + letter + "[Ww]" + DOT_ONE_NONE + "(st|h)" +
                    DOT_ONE_NONE + EOS, flags), "$1 " + wattPrefix.get(letter) + "vattstundir $3");
            //electronic_dict.update({"([02-9]|" + amounts + ") " + letter + "[Ww]\.?(st|h)\.?(\W|$)": "\g<1> " + prefix + "vattstundir \g<3>"})
            //TODO: etc. see electronic_dict.py in regina original
        }
        return electronicDict;
    }

    // TODO: missing explicit tests !!
    public static final Map<Pattern, String> restDict = new HashMap<>();
    static {
        int flags = 0;
        restDict.put(Pattern.compile("(" + BOS + "(" + prepositions.get(DAT) + ") (" + NUMBER_EOS_1 + ")) ?%" + EOS, flags), "$1 prósenti$9"); // changed from group 10 to 9 ( in the next entries as well)
        restDict.put(Pattern.compile("(" + BOS + "(" + prepositions.get(GEN) + ") (" + NUMBER_EOS_1 + ")) ?%" + EOS, flags), "$1 prósents$9");
        restDict.put(Pattern.compile("(" + BOS + "(" + prepositions.get(DAT) + ") (" + NUMBER_EOS_NOT_1 + ") ?%" + EOS, flags), "$1 prósentum$9");
        restDict.put(Pattern.compile("(" + BOS + "(" + prepositions.get(DAT) + ") (" + NUMBER_ANY + ") " + patternSelection.get(AMOUNT)
                + ")%" + EOS, flags), "$1 1$1 prósentum$14");
        restDict.put(Pattern.compile("(" + BOS + "(" + prepositions.get(GEN) + ") (" + NUMBER_EOS_NOT_1 + ") ?%" + EOS, flags), "$1 prósenta$8"); // changed from 10 to 8
        restDict.put(Pattern.compile("(" + BOS + "(" + prepositions.get(GEN) + ") (" + NUMBER_ANY + ") " + patternSelection.get(AMOUNT)
                + ")%" + EOS, flags), "$1 1$1 prósenta$14");
        restDict.put(Pattern.compile("%", flags), "prósent");
    }

    // TODO: unused !! missing explicit tests !!
    public static final Map<Pattern, String> periodDict = new HashMap<>();
    static {
        int flags = 0;
        periodDict.put(Pattern.compile(BOS + "mán(ud)?" + DOT_ONE_NONE + EOS, flags), "$1mánudag$3");
        periodDict.put(Pattern.compile(BOS + "þri(ðjud)?" + DOT_ONE_NONE + EOS, flags), "$1þriðjudag$3");
        periodDict.put(Pattern.compile(BOS + "mið(vikud)?" + DOT_ONE_NONE + EOS, flags), "$1miðvikudag$3");
        periodDict.put(Pattern.compile(BOS + "fim(mtud)?" + DOT_ONE_NONE + EOS, flags), "$1fimmtudag$3");
        periodDict.put(Pattern.compile(BOS + "fös(tud)?" + DOT_ONE_NONE + EOS, flags), "$1föstudag$3");
        periodDict.put(Pattern.compile(BOS + "lau(gard)?" + DOT_ONE_NONE + EOS, flags), "$1laugardag$3");
        periodDict.put(Pattern.compile(BOS + "sun(nud)?" + DOT_ONE_NONE + EOS, flags), "$1sunnudag$3");

        periodDict.put(Pattern.compile(BOS + "jan" + DOT_ONE_NONE + EOS, flags), "$1janúar$3");
        periodDict.put(Pattern.compile(BOS + "feb" + DOT_ONE_NONE + EOS, flags), "$1febrúar$3");
        periodDict.put(Pattern.compile(BOS + "mar" + DOT_ONE_NONE + EOS, flags), "$1mars$3");
        periodDict.put(Pattern.compile(BOS + "apr" + DOT_ONE_NONE + EOS, flags), "$1apríl$3");
        periodDict.put(Pattern.compile(BOS + "jún" + DOT_ONE_NONE + EOS, flags), "$1júní$3");
        periodDict.put(Pattern.compile(BOS + "júl" + DOT_ONE_NONE + EOS, flags), "$1júlí$3");
        periodDict.put(Pattern.compile(BOS + "ágú?" + DOT_ONE_NONE + EOS, flags), "$1ágúst$3");
        periodDict.put(Pattern.compile(BOS + "sept?" + DOT_ONE_NONE + EOS, flags), "$1september$3");
        periodDict.put(Pattern.compile(BOS + "okt" + DOT_ONE_NONE + EOS, flags), "$1október$3");
        periodDict.put(Pattern.compile(BOS + "nóv" + DOT_ONE_NONE + EOS, flags), "$1nóvember$3");
        periodDict.put(Pattern.compile(BOS + "des" + DOT_ONE_NONE + EOS, flags), "$1desember$3");

        //TODO: determine if we need this for now
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
    }
}
