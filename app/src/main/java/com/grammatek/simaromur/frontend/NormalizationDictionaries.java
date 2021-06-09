package com.grammatek.simaromur.frontend;

import org.apache.commons.collections4.OrderedMap;
import org.apache.commons.collections4.map.ListOrderedMap;

import java.util.HashMap;
import java.util.Map;

/**
 * Normalization dictionaries for abbreviations, digits and other non-standard-words.
 * This class still needs refactoring!
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
    public static final String LETTERS = "[A-ZÁÉÍÓÚÝÐÞÆÖa-záéíóúýðþæö]";
    public static final String LETTER_OR_DIGIT = "[A-ZÁÉÍÓÚÝÐÞÆÖa-záéíóúýðþæö\\d]";
    public static final String NOT_LETTERS = "[^A-ZÁÉÍÓÚÝÐÞÆÖa-záéíóúýðþæö]";
    public static final String ALL_MONTHS = "jan(úar)?|feb(rúar)?|mars?|apr(íl)?|maí|jú[nl]í?|ág(úst)?|sep(t(ember)?)?|okt(óber)?|nóv(ember)?|des(ember)?";
    public static final String THE_CLOCK = "(núll|eitt|tvö|þrjú|fjögur|fimm|sex|sjö|átta|níu|tíu|ellefu|tólf" +
            "|((þret|fjór|fimm|sex)tán)|((sau|á|ní)tján)|tuttugu( og (eitt|tvö|þrjú|fjögur))?)";
    public static final String MEASURE_PREFIX_DIGITS = "(\\d{1,2}" + DOT + ")?(\\d{3}" + DOT + "?)*\\d+(,\\d+)?";
    public static final String MEASURE_PREFIX_WORDS = "([Hh]undr[au]ð|HUNDR[AU]Ð|[Þþ]úsund|ÞÚSUND|[Mm]illjón(ir)?|MILLJÓN(IR)?) ";

    // any number, (large, small, with or without thousand separators, with or without a decimal point) that does NOT end with a "1"
    public static final String NUMBER_EOS_NOT_1 = "(\\d{1,2}\\.)?(\\d{3}\\.?)*(\\d*[02-9]|\\d,\\d*[02-9]))";
    // any number, (large, small, with or without thousand separators, with or without a decimal point) that DOES end with a "1"
    // TODO: ask Helga: this does not quite match, since a larger number than one digit plus decimal point does not match
    // e.g. 21,1 does not match, whereas 1,1 matches
    public static final String NUMBER_EOS_1 = "(\\d{1,2}\\.)?(\\d{3}\\.?)*(\\d*1|\\d,\\d*1)";
    // any number (large, small, with or without thousand separators, with or without a decimal point)
    public static final String NUMBER_ANY = "(((\\d{1,2}\\.)?(\\d{3}\\.?)*|\\d+)(,\\d+)?)?";

    private NormalizationDictionaries() {}

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

    //The link patterns handle external patterns like https://mbl.is/innlent, internal patterns like https://localholst:8888,
    //mail patterns like name@address.com, twitter handles like @handle and hashtags, e.g. #thisrules2021
    public static Map<String, String> patternSelection = new HashMap<>();
    static {
        patternSelection.put(AMOUNT, "(hundr[au]ð|þúsund|milljón(ir)?)");
        patternSelection.put(LINK_PTRN_EXTERNAL, "((https?:\\/\\/)?(www\\.)?([A-ZÁÐÉÍÓÚÝÞÆÖa-záðéíóúýþæö\\d\\-_\\.\\/]+)?\\.[A-ZÁÐÉÍÓÚÝÞÆÖa-záðéíóúýþæö\\d\\-_\\.\\/]+)");
        patternSelection.put(LINK_PTRN_INTERNAL, "((file|(https?:\\/\\/)?localhost):[A-ZÁÐÉÍÓÚÝÞÆÖa-záðéíóúýþæö\\d_\\?\\/\\.=\\-\\&\\%\\#]+)");
        patternSelection.put(LINK_PTRN_MAIL, "([A-ZÁÐÉÍÓÚÝÞÆÖa-záðéíóúýþæö\\d\\-_\\.]*@[A-ZÁÐÉÍÓÚÝÞÆÖa-záðéíóúýþæö\\d\\-_\\.]+(\\.[A-Za-z])?)");
        patternSelection.put(LINK_PTRN_HASHTAG, "(# ?[A-ZÁÐÉÍÓÚÝÞÆÖa-záðéíóúýþæö\\d\\-_]+)");
    }
    public static Map<String, String> links = new HashMap<>();
    static {
        links.put(LINK_PTRN_ALL, "^(" + patternSelection.get(LINK_PTRN_EXTERNAL) + "|" + patternSelection.get(LINK_PTRN_INTERNAL)
                + "|" + patternSelection.get(LINK_PTRN_MAIL) + "|" + patternSelection.get(LINK_PTRN_HASHTAG) + ")$");
    }

    // TODO: some seemingly random replacements, need to categorize and rename, 'preHelp' does not tell us anything
    public static OrderedMap<String, String> preHelpDict = new ListOrderedMap<>();
    static {
        preHelpDict.put(BOS + "(?i)2ja" + EOS, "$1tveggja$2");
        // is this save? couldn't it be 'þriðja/þriðju/þriðji'?
        preHelpDict.put(BOS + "(?i)3ja" + EOS, "$1þriggja$2");

        preHelpDict.put(BOS + "(?i)4ð(a|i|u)" + EOS, "$1fjórð$2$3");
        preHelpDict.put(BOS + "(?i)5t(a|i|u)" + EOS, "$1fimmt$2$3");
        preHelpDict.put(BOS + "(?i)6t(a|i|u)" + EOS, "$1sjött$2$3");
        preHelpDict.put(BOS + "(?i)7d(a|i|u)" + EOS, "$1sjöund$2$3");
        preHelpDict.put(BOS + "(?i)8d(a|i|u)" + EOS, "$1áttund$2$3");
        preHelpDict.put(BOS + "(?i)9d(a|i|u)" + EOS, "$1níund$2$3");

        // separate digits and letters
        preHelpDict.put("(?i)([a-záðéíóúýþæö]+)(\\d+)", "$1 $2");
        preHelpDict.put("(?i)(\\d+)([a-záðéíóúýþæö]+)", "$1 $2");

        // what are these? degrees and percent with letters?
        preHelpDict.put("(?i)([\\da-záðéíóúýþæö]+)(°)", "$1 $2");
        //this causes normalization errors at later stages, analyze why
        //preHelpDict.put("(?i)([\\da-záðéíóúýþæö]+)(%)", "$1 $2");
        // dates
        preHelpDict.put(BOS + "(0?[1-9]|[12]\\d|3[01])\\.(0?[1-9]|1[012])\\.(\\d{3,4})" + EOS, "$1$2. $3. $4$5");
        preHelpDict.put(BOS + "(0?[1-9]|[12]\\d|3[01])\\.(0?[1-9]|1[012])\\." + EOS, "$1$2. $3.$4");
        // insert a hyphen between digit groups of 3 and 4, most likely a telephone number
        preHelpDict.put("(\\d{3})( )(\\d{4})", "$1-$3");
    }

    public static Map<String, String> hyphenDict = new HashMap<>();
    static {
        //TODO: explain what this pattern should do: why insert a space on the one side of the hyphen,
        // depending on which side has upper case only? (abcdEF-GHJ -> abcdEF- GHJ) What are real world examples here?
        hyphenDict.put(BOS + "([A-ZÁÐÉÍÓÚÝÞÆÖ]+)(\\-[A-ZÁÐÉÍÓÚÝÞÆÖa-záðéíóúýþæö]+)" + EOS, "$1$2 $3$4");
        hyphenDict.put(BOS + "([A-ZÁÐÉÍÓÚÝÞÆÖa-záðéíóúýþæö]+\\-)([A-ZÁÐÉÍÓÚÝÞÆÖ]+)" + EOS, "$1$2 $3$4");
    }

    // See a list of examples for the below abbreviations in "abbreviation_examples.txt"
    // It is recommended to update that file when this dictionary is updated to keep an overview of covered
    // abbreviations
    public static final Map<String, String> abbreviationDict = new HashMap<>();
    static {
        abbreviationDict.put("(\\d+\\.) gr" + DOT + EOS, "$1 grein$2");
        abbreviationDict.put("(\\d+\\.) mgr" + DOT + EOS, "$1 málsgrein2");
        abbreviationDict.put(BOS + "[Ii]nnsk" + DOT + "(blm" + DOT + "|blaðamanns)" + EOS, "$1innskot $2");
        abbreviationDict.put(BOS + "([Ii]nnsk(" + DOT + "|ot) )(blm" + DOT + ")" + EOS, "$12xblaðamanns $5");
        abbreviationDict.put("([Ff]" + DOT + "[Kk]r" + DOT + "?)" + EOS, "fyrir Krist$2");
        abbreviationDict.put("([Ee]" + DOT + "[Kk]r" + DOT + "?)" + EOS, "eftir Krist$2");
        abbreviationDict.put(BOS + "([Cc]a|CA)" + DOT_ONE_NONE + EOS, "$1sirka$3");
        abbreviationDict.put("(\\d+" + DOT + ") [Ss]ek" + DOT_ONE_NONE + EOS, "$1 sekúnda$2");
        abbreviationDict.put("(\\d+" + DOT + ") [Mm]ín" + DOT_ONE_NONE + EOS, "$1 mínúta$2");

        abbreviationDict.put(BOS + "(\\d{1,2}\\/\\d{1,2} )frák" + DOT_ONE_NONE + EOS, "$12x fráköst$3");
        abbreviationDict.put(BOS + "(\\d{1,2}\\/\\d{1,2} )stoðs" + DOT_ONE_NONE + EOS, "$12x stoðsendingar$3");
        abbreviationDict.put(BOS + "([Nn]" + DOT_ONE_NONE + "k|N" + DOT_ONE_NONE + "K)" + DOT_ONE_NONE + EOS, "$1næstkomandi$3");
        abbreviationDict.put(BOS + "(?i)(atr" + DOT_ONE_NONE + ")" + EOS, "$1 atriði$3");
        abbreviationDict.put(BOS + "(?i)(ath" + DOT_ONE_NONE + ")" + EOS, "$1 athugið$3");
        abbreviationDict.put(BOS + "(?i)(aths" + DOT_ONE_NONE + ")" + EOS, "$1 athugasemd$3");
        abbreviationDict.put(BOS + "([Ff]" + DOT_ONE_NONE + "hl|F" + DOT + "HL)" + DOT_ONE_NONE + EOS, "$1 fyrri hluti$3");
        abbreviationDict.put(BOS + "([Ss]" + DOT_ONE_NONE + "hl|S" + DOT + "HL)" + DOT_ONE_NONE + EOS, "$1 síðari hluti$3");

        abbreviationDict.put("(?i)" + BOS + "(e" + DOT_ONE_NONE + "h" + DOT_ONE_NONE + "f" + DOT_ONE_NONE + ")" + EOS, "$1E H F$3");
        abbreviationDict.put("(?i)" + BOS + "(o" + DOT_ONE_NONE + "h" + DOT_ONE_NONE + "f" + DOT_ONE_NONE + ")" + EOS, "$1O H F$3");
        abbreviationDict.put("(?i)" + BOS + "(h" + DOT_ONE_NONE + "f" + DOT_ONE_NONE + ")" + EOS, "$1H F$3");
        abbreviationDict.put("(?i)" + BOS + "(s" + DOT_ONE_NONE + "f" + DOT_ONE_NONE + ")" + EOS, "$1S F$3");
        abbreviationDict.put("(?i)" + BOS + "(q" + DOT_ONE_NONE + "e" + DOT_ONE_NONE + "d" + DOT_ONE_NONE + ")" + EOS, "$1Q E D$3");
        abbreviationDict.put(BOS + "([Pp]" + DOT_ONE_NONE + "s" + DOT_ONE_NONE + ")" + EOS, "$1P S$3");

        abbreviationDict.put(BOS + "([Aa]" + DOT_ONE_NONE + "m" + DOT_ONE_NONE + "k|A" + DOT_ONE_NONE + "M" + DOT_ONE_NONE + "K)" + DOT_ONE_NONE + EOS, "$1að minnsta kosti$3");
        abbreviationDict.put(BOS + "([Aa]" + DOT + "m" + DOT + "m" + DOT_ONE_NONE + ")" + EOS, "$1að mínu mati$3");
        abbreviationDict.put(BOS + "([Aa]" + DOT + "n" + DOT + "l" + DOT_ONE_NONE + ")" + EOS, "$1að nokkru leyti$3");
        abbreviationDict.put(BOS + "([Aa]lþm" + DOT_ONE_NONE + ")" + EOS, "$1alþingismaður$3");
        abbreviationDict.put(BOS + "([Aa]lm|ALM)" + DOT_ONE_NONE + EOS, "$1almennt$3");
        abbreviationDict.put(BOS + "(bls" + DOT_ONE_NONE + ") (" + NUMBER_ANY + "|" + MEASURE_PREFIX_WORDS +
                "( )?)" + EOS, "$1blaðsíða $3$12"); // realistic? million pages? 45,3 pages?
        abbreviationDict.put("(?i)" + BOS + "(B" + DOT + "S[Cc]" + DOT + "?)" + EOS, "$1B S C$3");
        abbreviationDict.put("(?i)" + BOS + "(M" + DOT + "S[Cc]" + DOT + "?)" + EOS, "$1M S C$3");

        abbreviationDict.put(BOS + "([Dd]r" + DOT_ONE_NONE + ")" + EOS, "$1doktor$3");
        abbreviationDict.put("(" + BOS + "\\() ?(e" + DOT + ")" + EOS, "$1enska$4");
        abbreviationDict.put(BOS + "([Ee]" + DOT + "k|E" + DOT + "K)" + DOT_ONE_NONE + EOS, "$1 einhvers konar$3");
        abbreviationDict.put(BOS + "([Ee]\\-s konar)" + DOT_ONE_NONE+ EOS, "$1 einhvers konar$3");
        abbreviationDict.put(BOS + "([Ee]" + DOT + "t" + DOT + "v" + DOT_ONE_NONE + ")" + EOS, "$1 ef til vill$3");

        abbreviationDict.put(BOS + "([Ee]\\-ð)" + EOS, "$1eitthvað$3");
        abbreviationDict.put(BOS + "([Ee]\\-ju)" + EOS, "$1einhverju$3");
        abbreviationDict.put(BOS + "([Ee]\\-s)" + EOS, "$1einhvers$3");
        abbreviationDict.put(BOS + "([Ee]\\-r)" + EOS, "$1einhvers$3");
        abbreviationDict.put(BOS + "([Ee]\\-n)" + EOS, "$1einhvern$3");
        abbreviationDict.put(BOS + "([Ee]\\-um)" + EOS, "$1einhverjum$3");
        // do we have "fædd" somewhere or how do we know this should be "fæddur"?
        abbreviationDict.put(BOS + "(f" + DOT + ") (^((([012]?[1-9]|3[01])" + DOT + " ?)?(" + ALL_MONTHS + ") )\\d{2,4}$)" + EOS, "$1fæddur $3$4");
        abbreviationDict.put(BOS + "([Ff]él" + DOT_ONE_NONE + ")" + EOS, "$1félag$3");
        abbreviationDict.put(BOS + "([Ff]rh|FRH)" + DOT_ONE_NONE + EOS, "$1framhald$3");
        abbreviationDict.put(BOS + "([Ff]rt|FRT)" + DOT_ONE_NONE + EOS, "$1framtíð$3");
        abbreviationDict.put(BOS + "([Ff]" + DOT + "o" + DOT + "t" + DOT +")" + EOS, "$1fyrir okkar tímatal$3");
        abbreviationDict.put(BOS + "([Ff]" + DOT + "h" + DOT_ONE_NONE + ")" + EOS, "$1fyrir hönd$3");
        abbreviationDict.put(BOS + "([Gg]" + DOT_ONE_NONE + "r" + DOT_ONE_NONE + "f|G" + DOT_ONE_NONE +"R" + DOT_ONE_NONE + "F)"
                + DOT_ONE_NONE + EOS, "$1gerum ráð fyrir$3");
        abbreviationDict.put(BOS + "([Gg]" + DOT_ONE_NONE + "m" + DOT_ONE_NONE + "g|G" + DOT_ONE_NONE +"M" + DOT_ONE_NONE + "G)"
                + DOT_ONE_NONE + EOS, "$1guð minn góður$3");

        abbreviationDict.put(BOS + "([Hh]dl" + DOT_ONE_NONE + ")" + EOS, "$1héraðsdómslögmaður$3");
        abbreviationDict.put(BOS + "([Hh]rl" + DOT_ONE_NONE + ")" + EOS, "$1hæstarréttarlögmaður$3");
        abbreviationDict.put(BOS + "([Hh]öf|HÖF)" + DOT_ONE_NONE + EOS, "$1höfundur$3");
        abbreviationDict.put(BOS + "([Hh]v?k)" + EOS, "$1hvorugkyn$3");
        abbreviationDict.put(BOS + "([Hh]r" + DOT_ONE_NONE + ")" + EOS, "$1herra$3");
        abbreviationDict.put(BOS + "([Hh]v" + DOT_ONE_NONE + ")" + EOS, "$1hæstvirtur$3"); //case?
        abbreviationDict.put(BOS + "([Hh]" + DOT + "u" + DOT + "b|H" + DOT + "U" + DOT + "B)" + DOT_ONE_NONE + EOS, "$1hér um bil$3");

        abbreviationDict.put("(" + LETTERS + "+ )([Jj]r)" + DOT_ONE_NONE + EOS, "$1junior$3");

        abbreviationDict.put(BOS + "([Kk]k)" + EOS, "$1karlkyn$3");
        abbreviationDict.put(BOS + "([Kk]vk)" + EOS, "$1kvenkyn$3");
        abbreviationDict.put(BOS + "([Kk]t" + DOT_ONE_NONE + ")(:| \\d{6}\\-?\\d{4})" + EOS, "$1kennitala$3");
        abbreviationDict.put(BOS + "([Kk]l ?(" + DOT + "|\\:)?)(\\s?\\d{2}([:" + DOT + "]\\d{2})?)", "$1klukkan$4");
        // how do we get this, ef number normalizing is done after abbreviation normalizing?
        abbreviationDict.put("(?i)" + BOS + "kl ?(" + DOT + "|\\:)? ?(" + THE_CLOCK + " )", "klukkan$2");

        abbreviationDict.put(BOS + "([Kk]höfn|[Kk]bh|KBH)" + EOS, "$1Kaupmannahöfn$2"); // inflections?
        abbreviationDict.put(BOS + "([Ll]h" + DOT_ONE_NONE + "nt" + DOT_ONE_NONE + ")" + EOS, "$1lýsingarháttur nútíðar$3");
        abbreviationDict.put(BOS + "([Ll]h" + DOT_ONE_NONE + "þt" + DOT_ONE_NONE + ")" + EOS, "$1lýsingarháttur þátíðar$3");
        abbreviationDict.put(BOS + "([Ll]td|LTD)" + DOT_ONE_NONE + EOS, "$1limited$3");

        abbreviationDict.put(BOS + "([Mm]" + DOT + "a" + DOT + ")" + EOS, "$1meðal annars$3");
        abbreviationDict.put(BOS + "([Mm]" + DOT + "a" + DOT + "s|M" + DOT + "A" + DOT + "S)" + DOT_ONE_NONE + EOS, "$1meira að segja$3");
        abbreviationDict.put(BOS + "([Mm]" + DOT + "a" + DOT + "o|M" + DOT + "A" + DOT + "O)" + DOT_ONE_NONE + EOS, "$1meðal annarra orða$3");
        abbreviationDict.put("(" + MEASURE_PREFIX_DIGITS + "|" + MEASURE_PREFIX_WORDS + ")?([Mm]" + DOT + "y" + DOT + "s" + DOT_ONE_NONE + ")" + EOS, "$1metra yfir sjávarmáli$9"); // do we really need the millions here? million metres above n.n.?
        abbreviationDict.put(BOS + "([Mm]" + DOT_ONE_NONE + "v" + DOT_ONE_NONE + ")" + EOS, "$1miðað við$3");
        abbreviationDict.put(BOS + "([Mm]" + DOT_ONE_NONE + "t" + DOT_ONE_NONE + "t|M" + DOT_ONE_NONE + "T" + DOT_ONE_NONE + "T)"
                + DOT_ONE_NONE + EOS, "$1með tilliti til$3");
        abbreviationDict.put(BOS +  "([Mm]" + DOT_ONE_NONE + "ö" + DOT_ONE_NONE + "o|M" + DOT_ONE_NONE + "Ö" + DOT_ONE_NONE + "O)"
                + DOT_ONE_NONE + EOS, "$1með öðrum orðum$3");
        abbreviationDict.put(BOS + "([Mm]fl|MFL)" + DOT_ONE_NONE + EOS, "$1meistaraflokkur$3");
        abbreviationDict.put("(" + MEASURE_PREFIX_DIGITS + "|" + MEASURE_PREFIX_WORDS + ")?(m ?\\^ ?2)" + EOS, "$1 fermetrar$9");
        abbreviationDict.put("(" + MEASURE_PREFIX_DIGITS + "|" + MEASURE_PREFIX_WORDS + ")?(m ?\\^ ?3)" + EOS, "$1 rúmmetrar$9");

        abbreviationDict.put(BOS + "([Nn]úv" + DOT_ONE_NONE + ")" + EOS, "$1núverandi$3");
        abbreviationDict.put(BOS + "([Nn]" + DOT_ONE_NONE + "t" + DOT_ONE_NONE + "t|N" + DOT_ONE_NONE + "T" + DOT + "T)"
                + DOT_ONE_NONE + "($)" + EOS, "$1nánar tiltekið$3");
        abbreviationDict.put(BOS + "([Nn]kl|NKL)" + EOS, "nákvæmlega");
        abbreviationDict.put(BOS + "([Oo]" + DOT_ONE_NONE + "fl|O" + DOT_ONE_NONE + "FL)" + DOT_ONE_NONE + EOS, "$1og fleira$3");
        abbreviationDict.put(BOS + "([Oo]" + DOT_ONE_NONE + "m" + DOT_ONE_NONE + "fl|O" + DOT_ONE_NONE + "M" + DOT_ONE_NONE + "FL)"
                + DOT_ONE_NONE + EOS, "$1og margt fleira$3");
        abbreviationDict.put(BOS + "([Oo]" + DOT_ONE_NONE + "s" + DOT_ONE_NONE + "frv?|O" + DOT_ONE_NONE + "S" + DOT_ONE_NONE + "FRV?)"
                + DOT_ONE_NONE + EOS, "$1og svo framvegis$3");
        abbreviationDict.put(BOS + "([Oo]" + DOT_ONE_NONE + "þ" + DOT_ONE_NONE + "h|O" + DOT_ONE_NONE + "Þ" + DOT_ONE_NONE + "H)"
                + DOT_ONE_NONE + EOS, "$1og þess háttar$3");
        abbreviationDict.put(BOS + "([Oo]" + DOT_ONE_NONE + "þ" + DOT_ONE_NONE + "u" + DOT_ONE_NONE + "l|O" + DOT_ONE_NONE + "Þ"
                + DOT_ONE_NONE + "U" + DOT_ONE_NONE + "L)" + DOT_ONE_NONE + EOS, "$1og því um líkt$3");
        abbreviationDict.put(BOS + "([Pp]" + DOT_ONE_NONE + ")" + EOS, "$1pakki$3");

        abbreviationDict.put(BOS + "([Rr]n" + DOT_ONE_NONE + ")(:| [\\d\\-]+)" + EOS, "$1reikningsnúmer$3$4");
        abbreviationDict.put(BOS + "([Rr]itstj" + DOT_ONE_NONE + ")" + EOS, "$1ritstjóri$3");
        abbreviationDict.put(BOS + "([Rr]slm" + DOT_ONE_NONE + ")" + EOS, "$1rannsóknarlögreglumaður$3");
        abbreviationDict.put(BOS + "([Rr]ví?k|RVÍ?K)" + EOS, "$1Reykjavík$3");

        abbreviationDict.put(BOS + "(([Ss]íma)?nr" + DOT_ONE_NONE + ")", "$13xnúmer ");
        abbreviationDict.put(BOS + "([Ss]"+ DOT_ONE_NONE + ")(:| \\d{3}\\-?\\d{4}|\\d{7})" + EOS, "$1sími$3$4");
        abbreviationDict.put(BOS + "([Ss]br" + DOT_ONE_NONE + ")" + EOS, "$1samanber$3");
        abbreviationDict.put(BOS + "([Ss]" + DOT_ONE_NONE + "l" + DOT_ONE_NONE + "|SL" + DOT + ")" + EOS, "síðastliðinn");
        abbreviationDict.put(BOS + "([Ss]" + DOT_ONE_NONE + "k" + DOT_ONE_NONE + ")" + EOS, "$1svokallað$3");
        abbreviationDict.put(BOS + "([Ss]kv|SKV)" + DOT_ONE_NONE + EOS, "$1samkvæmt$3");
        abbreviationDict.put(BOS + "([Ss]" + DOT + " ?s" + DOT_ONE_NONE + ")" + EOS, "$1svo sem$3");
        abbreviationDict.put(BOS + "([Ss]amþ|SAMÞ)" + DOT_ONE_NONE + EOS, "$1samþykki$3"); // ekki "samþykkt"?
        abbreviationDict.put(BOS + "([Ss]gt" + DOT_ONE_NONE + ")" + EOS, "$1sergeant$3");
        abbreviationDict.put(BOS + "([Ss]t" + DOT_ONE_NONE + ")" + EOS, "$1saint$3");
        abbreviationDict.put(BOS + "([Ss]ltjn|SLTJN)" + DOT_ONE_NONE + EOS, "$1Seltjarnarnes$3");
        abbreviationDict.put(BOS + "([Ss]thlm|STHLM)" + EOS, "$1Stokkhólmur$3");

        abbreviationDict.put(BOS + "([Tt]bl|TBL)" + DOT_ONE_NONE + EOS, "$1tölublað$3"); //beyging?
        abbreviationDict.put(BOS + "([Tt]l" + DOT_ONE_NONE + ")" + EOS, "$1tengiliður$3");
        abbreviationDict.put(BOS + "([Tt]" + DOT + "h" + DOT_ONE_NONE + ")" + EOS, "$1til hægri$3");
        abbreviationDict.put(BOS + "([Tt]" + DOT + "v" + DOT_ONE_NONE + ")" + EOS, "$1til vinstri$3");
        abbreviationDict.put(BOS + "([Tt]" + DOT + "a" + DOT + "m|T" + DOT + "A" + DOT + "M)" + DOT_ONE_NONE + EOS, "$1til að mynda$3");
        abbreviationDict.put(BOS + "([Tt]" + DOT_ONE_NONE + "d|T" + DOT_ONE_NONE + "D)" + DOT_ONE_NONE + EOS, "$1til dæmis$3");

        abbreviationDict.put(BOS + "([Uu]" + DOT_ONE_NONE + "þ" + DOT_ONE_NONE + "b|U" + DOT_ONE_NONE + "Þ" + DOT_ONE_NONE + "B)"
                + DOT_ONE_NONE + EOS, "$1um það bil$3");
        abbreviationDict.put(BOS + "([Uu]ppl|UPPL)" + DOT_ONE_NONE + EOS, "$1upplýsingar$3");
        abbreviationDict.put(BOS + "([Uu]td|UTD)" + DOT_ONE_NONE + EOS, "$1united$3");
        abbreviationDict.put(BOS + "([Vv]s|VS)" + DOT_ONE_NONE + EOS, "$1versus$3");
        abbreviationDict.put(BOS + "([Vv]sk" + DOT_ONE_NONE + ")" + EOS, "$1virðisaukaskatt$3"); // beyginar? ekki "skattur"?

        abbreviationDict.put(BOS + "([Þþ]f" + DOT_ONE_NONE+ ")" + EOS, "$1þolfall$3");
        abbreviationDict.put(BOS + "([Þþ]gf" + DOT_ONE_NONE+ ")" + EOS, "$1þágufall$3");
        abbreviationDict.put(BOS + "([Þþ]lt" + DOT_ONE_NONE+ ")" + EOS, "$1þáliðin tíð$3");
        abbreviationDict.put(BOS + "([Þþ]" + DOT + "á" + DOT + ")" + EOS, "$1þessa árs$3");
        abbreviationDict.put(BOS + "([Þþ]" + DOT + "h" + DOT + ")" + EOS, "$1þess háttar$3");
        abbreviationDict.put(BOS + "([Þþ]" + DOT + "m" + DOT + ")" + EOS, "$1þessa mánaðar$3");
        abbreviationDict.put(BOS + "([Þþ]" + DOT + "a" + DOT_ONE_NONE + ")" + EOS, "$1þannig að$3");
        abbreviationDict.put(BOS + "([Þþ]" + DOT_ONE_NONE + "e" + DOT_ONE_NONE + "a" + DOT_ONE_NONE + "s|" +
                "Þ" + DOT_ONE_NONE + "E" + DOT_ONE_NONE + "A" + DOT_ONE_NONE + "S)" + DOT_ONE_NONE + EOS, "$1það er að segja$3");
        abbreviationDict.put(BOS + "([Þþ]" + DOT_ONE_NONE + "a" + DOT_ONE_NONE + DOT_ONE_NONE + "a|Þ" + DOT_ONE_NONE + "A"
                + DOT_ONE_NONE + "A)" + DOT_ONE_NONE + EOS, "$1þá og því aðeins að$3");
        abbreviationDict.put(BOS + "([Þþ]" + DOT + "u" + DOT + "l" + DOT + "|Þ" + DOT + "U" + DOT + "L)" + DOT_ONE_NONE + EOS,
                "$1því um líkt$3");
        abbreviationDict.put(BOS + "([Þþ]" + DOT + "a" + DOT + "l" + DOT + "|Þ" + DOT + "A" + DOT + "L)" + DOT_ONE_NONE + EOS,
                "$1þar af leiðandi$3");
        abbreviationDict.put(BOS + "([Þþ]" + DOT + "á(" + DOT + "| )m" + DOT + "|Þ" + DOT + "Á(" + DOT + "| )M)" + DOT_ONE_NONE + EOS,
                "$1þar á meðal$3");
        abbreviationDict.put(BOS + "([Þþ]" + DOT + "m" + DOT + "t" + DOT + "|Þ" + DOT + "M" + DOT + "T)" + DOT_ONE_NONE + EOS,
                "$1þar með talið$3");

        abbreviationDict.put(" ((" + MEASURE_PREFIX_DIGITS + "|" + MEASURE_PREFIX_WORDS + ")( )?\\s)(þú" + DOT_ONE_NONE + ")" +
                "( " + LETTERS + "*)?", "$1þúsund$11"); // changed from group 13 to 11
        abbreviationDict.put(" ([Mm]örg )þús" + DOT_ONE_NONE + "( " + LETTERS + "*)?", "$1þúsund$2");

        abbreviationDict.put("(\\d+" + DOT + ") [Áá]rg" + DOT + EOS, "$1 árgangur$2");
        abbreviationDict.put(BOS + "([Óó]ákv" + DOT + "gr" + DOT + ")" + EOS, "$1óáveðinn greinir$3");
        abbreviationDict.put("(\\d+" + DOT + ") útg" + DOT + EOS, "$1 útgáfa$2");
        abbreviationDict.put(BOS + "([Íí]sl|ÍSL)" + DOT_ONE_NONE + EOS, "$1íslenska$3");

        abbreviationDict.put("([02-9])( )?°C" + EOS, "$1 gráður selsíus $2"); // 3 -> 2, all entries below
        abbreviationDict.put("(1)( )?°C", "$1 gráða selsíus$2");
        abbreviationDict.put("([02-9])( )?°F" + EOS, "$1 gráður farenheit $2");
        abbreviationDict.put("(1)( )?°F", "$1 gráða farenheit$2");
        abbreviationDict.put("([02-9])( )?°W" + EOS, "$1 gráður vestur $2");
        abbreviationDict.put("(1)( )?°W", "$1 gráða vestur$2");
        abbreviationDict.put("([02-9])( )?°N" + EOS, "$1 gráður norður $2");
        abbreviationDict.put("(1)( )?°N", "$1 gráða norður$2");
        abbreviationDict.put("([02-9])( )?°E" + EOS, "$1 gráður austur $2");
        abbreviationDict.put("(1)( )?°E", "$1 gráða austur$2");
        abbreviationDict.put("([02-9])( )?°S" + EOS, "$1 gráður suður $2");
        abbreviationDict.put("(1)( )?°S", "$1 gráða suður $2");
    }


    public static final Map<String, String> directionDict = new HashMap<>();
    static {
        // we don't accept dashes (u2013 or u2014), only standard hyphenation.
        // see more patterns in directiondict.txt
        directionDict.put(BOS + "(SV-(:?(til|lands|átt|verðu|vert)))" + EOS, "$1suðvestan$3$4");
        directionDict.put(BOS + "(NV-(:?(til|lands|átt|verðu|vert)))" + EOS, "$1norðvestan$3$4");
        directionDict.put(BOS + "(NA-(:?(til|lands|átt|verðu|vert)))" + EOS, "$1norðaustan$3$4");
        directionDict.put(BOS + "(SA-(:?(til|lands|átt|verðu|vert)))" + EOS, "$1suðaustan$4$5");
        directionDict.put(BOS + "(A-(:?(til|lands|átt|verðu|vert)))" + EOS, "$1austan$4");
        directionDict.put(BOS + "(S-(:?(til|lands|átt|verðu|vert)))" + EOS, "$1sunnan$4");
        directionDict.put(BOS + "(V-(:?(til|lands|átt|verðu|vert)))" + EOS, "$1vestan$4");
        directionDict.put(BOS + "(N-(:?(til|lands|átt|verðu|vert)))" + EOS, "$1norðan$4");
    }

    public static final Map<String, String> denominatorDict = new HashMap<>();
    static {
        // not complete, see denominatordict.txt for further patterns
        denominatorDict.put("\\/kg" + DOT_ONE_NONE + EOS, " á kílóið$1");
        denominatorDict.put("\\/t" + DOT_ONE_NONE + EOS, " á tonnið$1");
        denominatorDict.put("\\/ha" + DOT_ONE_NONE + EOS, " á hektarann$1");
        denominatorDict.put("\\/mg" + DOT_ONE_NONE + EOS, " á milligrammið$1");
        denominatorDict.put("\\/gr" + DOT_ONE_NONE + EOS, " á grammið$1");
        denominatorDict.put("\\/ml" + DOT_ONE_NONE + EOS, " á millilítrann$1");
        denominatorDict.put("\\/dl" + DOT_ONE_NONE + EOS, " á desilítrann$1");
        denominatorDict.put("\\/l" + DOT_ONE_NONE + EOS, " á lítrann$1");
        denominatorDict.put("\\/km" + DOT_ONE_NONE + EOS, " á kílómetra$1");
        denominatorDict.put("\\/klst" + DOT_ONE_NONE + EOS, " á klukkustund$1");
        denominatorDict.put("\\/kw" + DOT_ONE_NONE + "(st|h)" + DOT_ONE_NONE + EOS, " á kílóvattstund$2");
        denominatorDict.put("\\/Mw" + DOT_ONE_NONE + "(st|h)" + DOT_ONE_NONE + EOS, " á megavattstund$2");
        denominatorDict.put("\\/Gw" + DOT_ONE_NONE + "(st|h)" + DOT_ONE_NONE + EOS, " á gígavattstund$2");
        denominatorDict.put("\\/Tw" + DOT_ONE_NONE + "(st|h)" + DOT_ONE_NONE + EOS, " á teravattstund$2");
        denominatorDict.put("\\/s(ek)?" + DOT_ONE_NONE + EOS, " á sekúndu$2");
        denominatorDict.put("\\/mín" + DOT_ONE_NONE + EOS, " á mínútu$1");
        denominatorDict.put("\\/fm" + DOT_ONE_NONE + EOS, " á fermetra$1");
        denominatorDict.put("\\/ferm" + DOT_ONE_NONE + EOS, " á fermetra$1");
    }


    public static Map<String, String> weightDict = new HashMap<>();
    static {
        weightDict.put("(" + BOS + "(" + prepositions.get(DAT) + ") ((\\d{1,2}\\.)?(\\d{3}\\.?)*(\\d*1|\\d,\\d*1))) t" + DOT_ONE_NONE + EOS, "$1 tonni$10");
        weightDict.put("(" + BOS + "(" + prepositions.get(GEN) + ") ((\\d{1,2}\\.)?(\\d{3}\\.?)*(\\d*1|\\d,\\d*1))) t" + DOT_ONE_NONE + EOS, "$1 tonns$10");
        weightDict.put("(" + BOS + "(" + prepositions.get(DAT) + ") ((\\d{1,2}\\.)?(\\d{3}\\.?)*(\\d*[02-9]|\\d,\\d*[02-9]))) t" + DOT_ONE_NONE + EOS, "$1 tonnum$9"); // changed from group 10 to 9
        weightDict.put("(" + BOS + "(" + prepositions.get(DAT) + ") (((\\d{1,2}\\.)?(\\d{3}\\.?)*|\\d+)(,\\d+)?)?) " + patternSelection.get(AMOUNT) + " t" + DOT_ONE_NONE + EOS, "1 $1$1 tonnum$13");
        // usw. three more, and the same for grams
        weightDict.put("(" + BOS + "(" + prepositions.get(DAT) + ") ((\\d{1,2}\\.)?(\\d{3}\\.?)*(\\d*1|\\d,\\d*1))) g" + DOT_ONE_NONE + EOS, "$1 grammi$10");
        weightDict.put("(" + BOS + "(" + prepositions.get(GEN) + ") ((\\d{1,2}\\.)?(\\d{3}\\.?)*(\\d*1|\\d,\\d*1))) g" + DOT_ONE_NONE + EOS, "$1 gramms$10");
        weightDict.put("(" + BOS + "(" + prepositions.get(DAT) + ") ((\\d{1,2}\\.)?(\\d{3}\\.?)*(\\d*[02-9]|\\d,\\d*[02-9]))) g\\.?(\\W|$)", "$1 grömmum$10");
        weightDict.put("(" + BOS + "(" + prepositions.get(DAT) + ") (((\\d{1,2}\\.)?(\\d{3}\\.?)*|\\d+)(,\\d+)?)?) " + patternSelection.get(AMOUNT) + " g"+ DOT_ONE_NONE + EOS, "1 $1$1 grömmum$13");
        weightDict.put("(1 )gr?" + DOT_ONE_NONE + EOS, "$1gramm$2");
        weightDict.put("([02-9]|" + patternSelection.get(AMOUNT) + ") gr?\\.?(\\W|$)", "$1 grömm$3");


        // another section for nanó/milli/míkró/píkó/attó/zeptó/yoktó-kíló/pund + grammi/gramms/grömmum, ...
        // see class weight_dict.py in regina
    }

    public static OrderedMap<String, String> distanceDict = new ListOrderedMap<>();
    public static Map<String, String> getDistanceDict() {
        if (!distanceDict.isEmpty())
            return distanceDict;

        Map<String, String> prefixMap = new HashMap<>();
        // first initialized with "fet", "tomma", and some cryptic patterns for "metri"

        distanceDict.put("(" + BOS + "(" + prepositions.get(ACC_DAT_GEN_COMB) + ") " + NUMBER_EOS_1 + " )m" + DOT_ONE_NONE +
                "( (?![kmgyabefstvö]" + DOT + ")" + LETTER_OR_DIGIT + "*" + EOS  + ")", "$1 metra$10"); // from 14 to 10
        distanceDict.put("(" + BOS + "(" + prepositions.get(ACC_GEN) + ") (" + NUMBER_EOS_NOT_1 + " )m" + DOT_ONE_NONE +
                "( (?![kmgyabefstvö]" + DOT + ")" + LETTER_OR_DIGIT + "*" + EOS  + ")", "$1 metra$9$10");
        distanceDict.put("(" + BOS + "(" + prepositions.get(ACC_GEN) + ") " + NUMBER_ANY + " " + patternSelection.get(AMOUNT) + " )m" + DOT_ONE_NONE +
                "( (?![kmgyabefstvö]" + DOT + ")" + LETTER_OR_DIGIT + "*" + EOS  + ")", "$1 $13 metra$16");
        distanceDict.put("(" + BOS + "(" + prepositions.get(DAT) + ") (" + NUMBER_EOS_NOT_1 + " )m" + DOT_ONE_NONE +
                "( (?![kmgyabefstvö]" + DOT + ")" + LETTER_OR_DIGIT + "*" + EOS + ")", "$1 metrum$10");
        distanceDict.put("(" + BOS + "(" + prepositions.get(DAT) + ") " + NUMBER_ANY + " " + patternSelection.get(AMOUNT) + " )m" + DOT_ONE_NONE +
                "( (?![kmgyabefstvö]" + DOT + ")(?![kmgyabefstvö]" + DOT + ")" + LETTER_OR_DIGIT + "*" + EOS  + ")", "$1 1$1 metrum$14");
        distanceDict.put("(1 )m" + DOT_ONE_NONE + "( (?![kmgyabefstvö]" + DOT + ")" + LETTER_OR_DIGIT + "*" + EOS + ")",
                "$1metri$2");
        distanceDict.put("([02-9] )m" + DOT_ONE_NONE + "( (?![kmgyabefstvö]" + DOT + ")" + LETTER_OR_DIGIT + "*" + EOS  + ")",
                "$1metrar $2");

        // also píkó, nanó, míkró, njúton, in original regina
        prefixMap.put("m", "milli");
        prefixMap.put("[cs]", "senti");
        prefixMap.put("d", "desi");
        prefixMap.put("k", "kíló");

        for (String letter : prefixMap.keySet()) {
            distanceDict.put("(" + BOS + "(" + prepositions.get(ACC_DAT_GEN_COMB) + ") ((\\d{1,2}" + DOT + ")?(\\d{3}"
                    + DOT + ")*(\\d*1|\\d,\\d*1))) " + letter + "m" + DOT_ONE_NONE + EOS, "$1 " + prefixMap.get(letter) + "metra$10"); //14->10
            distanceDict.put("(" + BOS + "(" + prepositions.get(ACC_GEN) + ") " + NUMBER_EOS_NOT_1 + " " + letter + "m"
                    + DOT_ONE_NONE + EOS, "$1 " + prefixMap.get(letter) + "metra$8"); // different group count from regina! (12)
            distanceDict.put("(" + BOS + "(" + prepositions.get(ACC_GEN) + ") " + NUMBER_ANY + ") " + patternSelection.get(AMOUNT) +
                    " " + letter + "m" + DOT_ONE_NONE + EOS, "$1 $10 " + prefixMap.get(letter) + "metra$12");
            distanceDict.put("(" + BOS + "(" + prepositions.get(DAT) + ") " + NUMBER_EOS_NOT_1 + " " + letter + "m" +
                    DOT_ONE_NONE + EOS, "$1 " + prefixMap.get(letter) + "metrum$8"); //10 -> 8
            distanceDict.put("(" + BOS + "(" + prepositions.get(DAT) + ") " + NUMBER_ANY + ") " + patternSelection.get(AMOUNT) +
                    " " + letter + "m" + DOT_ONE_NONE + EOS, "$1 $11" + prefixMap.get(letter) + "metrum$14");
            distanceDict.put("(1 )" + letter + "m" + DOT_ONE_NONE + EOS, "$1 " + prefixMap.get(letter) + "metri $2");
            distanceDict.put("([0-9]|" + patternSelection.get(AMOUNT) + ") " + letter + "m" + DOT_ONE_NONE + EOS, "$1 " + prefixMap.get(letter) + "metrar $3");
        }

        return distanceDict;
    }

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

        areaDict.put("((\\W|^)(" + prepositions.get(ACC_DAT_GEN_COMB) + ") ((\\d{1,2}\\.)?(\\d{3}\\.?)*(\\d*1|\\d,\\d*1))) ha\\.?(\\W|$)", "$1 hektara$14");
        areaDict.put("((\\W|^)(" + prepositions.get(ACC_GEN) + ") ((\\d{1,2}\\.)?(\\d{3}\\.?)*(\\d*[02-9]|\\d,\\d*[02-9]))) ha\\.?(\\W|$)", "$1 hektara$12");
        areaDict.put("((\\W|^)(" + prepositions.get(ACC_GEN) + ") (((\\d{1,2}\\.)?(\\d{3}\\.?)*|\\d+)(,\\d+)?)?) " + patternSelection.get(AMOUNT) + " ha\\.?(\\W|$)", "$1 13x hektara$16x");
        areaDict.put("((\\W|^)(" + prepositions.get(DAT)+ ") ((\\d{1,2}\\.)?(\\d{3}\\.?)*(\\d*[02-9]|\\d,\\d*[02-9]))) ha\\.?(\\W|$)","$1 hekturum$10");
        areaDict.put("((\\W|^)(" + prepositions.get(DAT) + ") (((\\d{1,2}\\.)?(\\d{3}\\.?)*|\\d+)(,\\d+)?)?) " + patternSelection.get(AMOUNT) + " ha\\.?(\\W|$)", "$1 1$1 hekturum$14");
        areaDict.put("(1) ha\\.?(\\W|$)", "$1 hektari$2");
        // does this change later? at the moment we get: "reisa 15 ha ..." -> "reisa 15 hektarar ..."
        areaDict.put("([02-9]|" + patternSelection.get(AMOUNT) + ") ha\\.?(\\W|$)","$1 hektarar $3");

        for (String letter : prefixMeterDimension.keySet()) {
            for (String superscript : dimensionAfter.keySet()) {
                areaDict.put("((\\W|^)(" + prepositions.get(ACC_DAT_GEN_COMB) + ") ((\\d{1,2}\\.)?(\\d{3}\\.?)*(\\d*1|\\d,\\d*1)) " + letter + "m" + superscript + "(\\W|$))",
                        "$1 " + dimensionAfter.get(superscript) + prefixMeterDimension.get(letter) + "metra$14");
                areaDict.put("((\\W|^)(" + prepositions.get(ACC_GEN) + ") ((\\d{1,2}\\.)?(\\d{3}\\.?)*(\\d*[02-9]|\\d,\\d*[02-9]))) " + letter + "m" + superscript + "(\\W|$)",
                        "$1 " + dimensionAfter.get(superscript) + prefixMeterDimension.get(letter) + "metra$12");
                areaDict.put("((\\W|^)(" + prepositions.get(ACC_GEN) + ") (((\\d{1,2}\\.)?(\\d{3}\\.?)*|\\d+)(,\\d+)?)?) " + patternSelection.get(AMOUNT) + " " + letter + "m" + superscript + "(\\W|$)",
                        "$1 13x " + dimensionAfter.get(superscript) + prefixMeterDimension.get(letter) + "metra$16");
                areaDict.put("((\\W|^)(" + prepositions.get(DAT) + ") ((\\d{1,2}\\.)?(\\d{3}\\.?)*(\\d*[02-9]|\\d,\\d*[02-9]))) " + letter + "m" + superscript + "(\\W|$)",
                        "$1 " + dimensionAfter.get(superscript) + prefixMeterDimension.get(letter) + "metrum$10");
                areaDict.put("((\\W|^)(" + prepositions.get(DAT) + ") (((\\d{1,2}\\.)?(\\d{3}\\.?)*|\\d+)(,\\d+)?)?) " + patternSelection.get(AMOUNT) + " " + letter + "m" + superscript + "(\\W|$)",
                        "$1 1$1 " + dimensionAfter.get(superscript) + prefixMeterDimension.get(letter) + "metrum$14");
                areaDict.put("(1 )" + letter + "m" + superscript + "(\\W|$)", "$1" + dimensionAfter.get(superscript) + prefixMeterDimension.get(letter) + "metri$2");
                areaDict.put("([02-9]|" + patternSelection.get(AMOUNT) + ") " + letter + "m" + superscript + "(\\W|$)", "$1 " + dimensionAfter.get(superscript) + prefixMeterDimension.get(letter) + "metrar $3");

            }
        }

        for (String letter : prefixMeterDimension.keySet()) {
            for (String preprefix : dimensionBefore.keySet()) {
                areaDict.put("((\\W|^)(" + prepositions.get(ACC_DAT_GEN_COMB) + ") ((\\d{1,2}\\.)?(\\d{3}\\.?)*(\\d*1|\\d,\\d*1))) " + preprefix + letter + "m\\.?(\\W|$)",
                        "$1 " + dimensionBefore.get(preprefix) + prefixMeterDimension.get(letter) + "metra$14");
                areaDict.put("((\\W|^)(" + prepositions.get(ACC_GEN) + ") ((\\d{1,2}\\.)?(\\d{3}\\.?)*(\\d*[02-9]|\\d,\\d*[02-9]))) " + preprefix + letter + "m\\.?(\\W|$)",
                        "$1 " + dimensionBefore.get(preprefix) + prefixMeterDimension.get(letter) + "metra$12");
                areaDict.put("((\\W|^)(" + prepositions.get(ACC_GEN) + ") (((\\d{1,2}\\.)?(\\d{3}\\.?)*|\\d+)(,\\d+)?)?) " + patternSelection.get(AMOUNT) + " " + preprefix + letter + "m\\.?(\\W|$)",
                        "$1 13x " + dimensionBefore.get(preprefix) + prefixMeterDimension.get(letter) + "metra$16");
                areaDict.put("((\\W|^)(" + prepositions.get(DAT) + ") ((\\d{1,2}\\.)?(\\d{3}\\.?)*(\\d*[02-9]|\\d,\\d*[02-9]))) " + preprefix + letter + "m\\.?(\\W|$)",
                        "$1 " + dimensionBefore.get(preprefix) + prefixMeterDimension.get(letter) + "metrum$8"); //10 -> 8
                areaDict.put("((\\W|^)(" + prepositions.get(DAT) + ") (((\\d{1,2}\\.)?(\\d{3}\\.?)*|\\d+)(,\\d+)?)?) " + patternSelection.get(AMOUNT) + " " + preprefix + letter + "m\\.?(\\W|$)",
                        "$1 1$1" + dimensionBefore.get(preprefix) + prefixMeterDimension.get(letter) + "metrum$14");
                areaDict.put("(1 )" + preprefix + letter + "m\\.?(\\W|$)", "$1 " + dimensionBefore.get(preprefix) + prefixMeterDimension.get(letter) + "metri$2");
                areaDict.put("([02-9]|" + patternSelection.get(AMOUNT) + ") " + preprefix + letter + "m\\.?(\\W|$)", "$1 " + dimensionBefore.get(preprefix) + prefixMeterDimension.get(letter) + "metrar$3");
                //added ABN, previous patterns did not capture " ... ( 150.000 m² ) ..." - still need to figure that out
                areaDict.put("((\\W|^)(\\d{3}\\.\\d{3})) " + letter + "m" + preprefix + "(\\W|$)", "$1 " + dimensionBefore.get(preprefix) + prefixMeterDimension.get(letter) + "metrar$4");
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
            volumeDict.put("((\\W|^)(" + prepositions.get(ACC_DAT_GEN_COMB)+ ") ((\\d{1,2}\\.)?(\\d{3}\\.?)*(\\d*1|\\d,\\d*1))) " + letter + "[Ll]\\.?(\\W|$)", "$1 " + prefixLiter.get(letter) + "lítra$14x");
            volumeDict.put("((\\W|^)(" + prepositions.get(ACC_GEN) + ") ((\\d{1,2}\\.)?(\\d{3}\\.?)*(\\d*[02-9]|\\d,\\d*[02-9]))) " + letter + "[Ll]\\.?(\\W|$)", "1 " + prefixLiter.get(letter) + "lítra$9"); // 12->9
            volumeDict.put("((\\W|^)(" + prepositions.get(ACC_GEN) + ") (((\\d{1,2}\\.)?(\\d{3}\\.?)*|\\d+)(,\\d+)?)?) " + patternSelection.get(AMOUNT) + " " + letter + "[Ll]\\.?(\\W|$)", "$1 13x " + prefixLiter.get(letter) + "lítra$16");
            volumeDict.put("((\\W|^)(" + prepositions.get(DAT) + ") ((\\d{1,2}\\.)?(\\d{3}\\.?)*(\\d*[02-9]|\\d,\\d*[02-9]))) " + letter + "[Ll]\\.?(\\W|$)", "$1 " + prefixLiter.get(letter) + "lítrum$10");
            volumeDict.put("((\\W|^)(" + prepositions.get(DAT) + ") (((\\d{1,2}\\.)?(\\d{3}\\.?)*|\\d+)(,\\d+)?)?) " + patternSelection.get(AMOUNT) + " " + letter + "[Ll]\\.?(\\W|$)", "$1 1$1 " + prefixLiter.get(letter) + "lítrum$14");
            volumeDict.put("(1 )" + letter + "[Ll]\\.?(\\W|$)", "$1" + prefixLiter.get(letter) + "lítri$2");
            volumeDict.put("([02-9]|" + patternSelection.get(AMOUNT) + ") " + letter + "[Ll]\\.?(\\W|$)", "");
            //if (!letter.isEmpty())
            //    volumeDict.put("(\\W|^)" + letter + "l\\.?(\\W|$)", "$1" + prefixLiter.get(letter) + "lítrar $2");
        }

        return volumeDict;
    }

    private static Map<String, String> timeDict = new HashMap<>();

    public static Map<String, String> getTimeDict() {
        if  (!timeDict.isEmpty())
            return timeDict;

        timeDict.put("((\\W|^)(" + prepositions.get(GEN) + ") ((\\d{1,2}\\.)?(\\d{3}\\.?)*(\\d*1|\\d,\\d*1))) klst\\.?(\\W|$)",  "$1 klukkustundar$10");
        timeDict.put("((\\W|^)(" + prepositions.get(DAT) + ") ((\\d{1,2}\\.)?(\\d{3}\\.?)*(\\d*[02-9]|\\d,\\d*[02-9]))) klst\\.?(\\W|$)", "$1 klukkustundum$10");
        timeDict.put("((\\W|^)(" + prepositions.get(DAT) + ") (((\\d{1,2}\\.)?(\\d{3}\\.?)*|\\d+)(,\\d+)?)?) " + patternSelection.get(AMOUNT) + " klst\\.?(\\W|$)", "$1 1$1 klukkustundum$14");
        timeDict.put("((\\W|^)(" + prepositions.get(GEN) + ") ((\\d{1,2}\\.)?(\\d{3}\\.?)*(\\d*[02-9]|\\d,\\d*[02-9]))) klst\\.?(\\W|$)", "$1 klukkustunda$10");
        timeDict.put("((\\W|^)(" + prepositions.get(GEN) + ") (((\\d{1,2}\\.)?(\\d{3}\\.?)*|\\d+)(,\\d+)?)?) " + patternSelection.get(AMOUNT) + " klst\\.?(\\W|$)", "$1 1$1 klukkustunda$14");
        timeDict.put("(1 )klst\\.?(\\W|$)", "$1 klukkustund$2");
        timeDict.put("(\\W|^)klst\\.?(\\W|$)", "$1klukkustundir$2x");

        Map<String, String> prefixTime = new HashMap<String, String>() {{
            put("mín()?", "mínút");
            put("s(ek)?", "sekúnd");
            put("ms(ek)?", "millisekúnd");
        }};

        for (String letters : prefixTime.keySet()) {
            timeDict.put("(" + BOS + "(" + prepositions.get(ACC_DAT_GEN_COMB) + ") ((\\d{1,2}\\.)?(\\d{3}\\.?)*(\\d*1|\\d,\\d*1))) " + letters + DOT_ONE_NONE + EOS, "$1 " + prefixTime.get(letters) + "u$11");
            timeDict.put("(" + BOS + "(" + prepositions.get(DAT) + ") ((\\d{1,2}\\.)?(\\d{3}\\.?)*(\\d*[02-9]|\\d,\\d*[02-9]))) " + letters + DOT_ONE_NONE + EOS, "$1 " + prefixTime.get(letters) + "um$11");
            timeDict.put("(" + BOS + "(" + prepositions.get(DAT) + ") (((\\d{1,2}\\.)?(\\d{3}\\.?)*|\\d+)(,\\d+)?)?) " + patternSelection.get(AMOUNT) + " " + letters + DOT_ONE_NONE + EOS, "$1 1$1 " + prefixTime.get(letters) + "um$15");
            timeDict.put("(" + BOS + "(" + prepositions.get(GEN) + ") ((\\d{1,2}\\.)?(\\d{3}\\.?)*(\\d*[02-9]|\\d,\\d*[02-9]))) " + letters + DOT_ONE_NONE + EOS, "$1 " + prefixTime.get(letters) + "na$9"); //changed group from 11 to 9
            timeDict.put("(" + BOS + "(" + prepositions.get(GEN) + ") (((\\d{1,2}\\.)?(\\d{3}\\.?)*|\\d+)(,\\d+)?)?) " + patternSelection.get(AMOUNT) + " " + letters + DOT_ONE_NONE + EOS, "$1 1$1 " + prefixTime.get(letters) + "na$15");
            // added ABN: we need 'undir' ('undir x sek/klst/...')
            timeDict.put("((\\W|^)(" + prepositions.get(ACC_DAT) + ") ((\\d{1,2}\\.)?(\\d{3}\\.?)*(\\d*[02-9]|\\d,\\d*[02-9]))) " + letters + DOT_ONE_NONE + EOS, "$1 " + prefixTime.get(letters) + "um$9");

            timeDict.put("(1 )" + letters + DOT_ONE_NONE + EOS, "$1" + prefixTime.get(letters) + "a$2");
            //TODO: this one messes up, need to give the preposition patterns priority and not allow this one to intervene. But why do they both match after one has been substituted? I.e. " ... sekúndur ..." matches the pattern above with preposition
            //timeDict.put("([02-9]|" + patternSelection.get(AMOUNT) + ") " + letters + "\\.?(\\W|$)", "$1 " + prefixTime.get(letters) + "ur $3");
        }

        return timeDict;
    }

    public static Map<String, String> currencyDict = new HashMap<>();

    public static Map<String, String> getCurrencyDict() {
        if (!currencyDict.isEmpty())
            return currencyDict;
        // krónur:
        currencyDict.put("((\\W|^)(" + prepositions.get(DAT) + ")) kr\\.?\\-? ?((((\\d{1,2}\\.)?(\\d{3}\\.?)*|\\d+)(,\\d+)?)? " + patternSelection.get(AMOUNT) + ")" + EOS, "$1 6x krónum$15");
        currencyDict.put("((\\W|^)(" + prepositions.get(GEN) + ")) kr\\.?\\-? ?((((\\d{1,2}\\.)?(\\d{3}\\.?)*|\\d+)(,\\d+)?)? " + patternSelection.get(AMOUNT) + ")" + EOS, "$1 6xkróna$15");
        currencyDict.put("(\\W|^)[Kk]r\\.? ?((((\\d{1,2}\\.)?(\\d{3}\\.?)*|\\d+)(,\\d+)?)? " + patternSelection.get(AMOUNT) + ")" + EOS, "$1 2x krónur$11");
        currencyDict.put("((\\W|^)(" + prepositions.get(ACC_DAT_GEN_COMB) + ") ((\\d{1,2}\\.)?(\\d{3}\\.?)*(\\d*1|\\d,\\d*1))) ?kr\\.?\\-?" + EOS, "$1 krónu$14");
        currencyDict.put("((\\W|^)(" + prepositions.get(ACC_DAT_GEN_COMB) + ")) kr\\.?\\-? ?((\\d{1,2}\\.)?(\\d{3}\\.?)*(\\d*1|\\d,\\d*1))" + EOS, "$1 10x krónu$14");
        currencyDict.put("((\\W|^)(" + prepositions.get(DAT) + ") ((\\d{1,2}\\.)?(\\d{3}\\.?)*(\\d*[02-9]|\\d,\\d*[02-9]))) kr\\.?\\-?" + EOS, "$1 krónum$10");
        currencyDict.put("((\\W|^)(" + prepositions.get(DAT) + ") (((\\d{1,2}\\.)?(\\d{3}\\.?)*|\\d+)(,\\d+)?)? " + patternSelection.get(AMOUNT) + ") kr\\.?\\-?" + EOS, "$1 8xkrónum$14");
        currencyDict.put("((\\W|^)(" + prepositions.get(DAT) + ")) kr\\.?\\-? ?((\\d{1,2}\\.)?(\\d{3}\\.?)*(\\d*[02-9]|\\d,\\d*[02-9]))" + EOS, "$1 9x krónum$10");
        currencyDict.put("((\\W|^)(" + prepositions.get(GEN) + ") ((\\d{1,2}\\.)?(\\d{3}\\.?)*(\\d*[02-9]|\\d,\\d*[02-9]))) kr\\.?\\-?" + EOS, "$1 króna$10");
        currencyDict.put("((\\W|^)(" + prepositions.get(GEN) + ") (((\\d{1,2}\\.)?(\\d{3}\\.?)*|\\d+)(,\\d+)?)? " + patternSelection.get(AMOUNT) + ") kr\\.?\\-?" + EOS, "$1 8xkróna$14");
        currencyDict.put("((\\W|^)(" + prepositions.get(GEN) + ")) kr\\.?\\-? ?((\\d{1,2}\\.)?(\\d{3}\\.?)*(\\d*[02-9]|\\d,\\d*[02-9]))" + EOS, "$1 9x króna$10");
        currencyDict.put("(1 ?)kr\\.?\\-?(\\W|$)", "$1króna$2");
        currencyDict.put("([02-9]|" + patternSelection.get(AMOUNT) + ") ?kr\\.?\\-?(\\W|$)", "$1 krónur $3");
        // is this an error? (2 times group 2)
        //currencyDict.put("(\\W|^)[Kk]r\\.? ?(\\d)", "$12x krónur$2");
        currencyDict.put("(\\W|^)[Kk]r\\.? ?(\\d)", "$1krónur $2");

        // MUCH more here! other currencies, etc.

        return currencyDict;

    }

    public static Map<String, String> electronicDict = new HashMap<>();

    public static Map<String, String> getElectronicDict() {
        if (!electronicDict.isEmpty())
            return electronicDict;

        Map<String, String> wattPrefix = new HashMap<>();
        wattPrefix.put("", "");
        wattPrefix.put("k", "kíló");
        wattPrefix.put("M", "Mega");
        wattPrefix.put("G", "Gíga");
        wattPrefix.put("T", "Tera");


        Map<String, String> measurement = new HashMap<>();
        measurement.put("V", "volt");
        measurement.put("Hz", "herz");


        for (String letter : wattPrefix.keySet()) {
            electronicDict.put("(" + BOS + "(" + prepositions.get(GEN) + ") (" + NUMBER_EOS_1 + ")) " + letter + "[Ww]"
                    + DOT_ONE_NONE + "(st|h)" + DOT_ONE_NONE + EOS, "$1 " + wattPrefix.get(letter) + "vattstundar$11");
            electronicDict.put("(" + BOS + "(" + prepositions.get(DAT) + ") (" + NUMBER_EOS_NOT_1 + ") " + letter + "[Ww]"
                    + DOT_ONE_NONE + "(st|h)" + DOT_ONE_NONE + EOS, "$1 " + wattPrefix.get(letter) + "vattstundum$11");
            electronicDict.put("(" + BOS + "(" + prepositions.get(DAT) + ") (" + NUMBER_ANY + ") " + patternSelection.get(AMOUNT)
                    + ") " + letter + "[Ww]" + DOT_ONE_NONE + "(st|h)" + DOT_ONE_NONE + EOS, "$1 1$1 " + wattPrefix.get(letter) + "vattstundum$15");
            electronicDict.put("([02-9]|" + patternSelection.get(AMOUNT) + ") " + letter + "W" + EOS, "$1 " + wattPrefix.get(letter) + "vött $3");
            electronicDict.put("(1 )" + letter + "W" + EOS, "$1 " + wattPrefix.get(letter) + "vatt$2");
            electronicDict.put("([02-9]|" + patternSelection.get(AMOUNT) + ") " + letter + "[Ww]" + DOT_ONE_NONE + "(st|h)" +
                    DOT_ONE_NONE + EOS, "$1 " + wattPrefix.get(letter) + "vattstundir $3");
            //electronic_dict.update({"([02-9]|" + amounts + ") " + letter + "[Ww]\.?(st|h)\.?(\W|$)": "\g<1> " + prefix + "vattstundir \g<3>"})
            // etc. see electronic_dict.py in regina original
        }
        return electronicDict;
    }

    public static Map<String, String> restDict = new HashMap<>();
    static {
        restDict.put("(" + BOS + "(" + prepositions.get(DAT) + ") (" + NUMBER_EOS_1 + ")) ?\\%" + EOS, "$1 prósenti$9"); // changed from group 10 to 9 ( in the next entries as well)
        restDict.put("(" + BOS + "(" + prepositions.get(GEN) + ") (" + NUMBER_EOS_1 + ")) ?\\%" + EOS, "$1 prósents$9");
        restDict.put("(" + BOS + "(" + prepositions.get(DAT) + ") (" + NUMBER_EOS_NOT_1 + ") ?\\%" + EOS, "$1 prósentum$9");
        restDict.put("(" + BOS + "(" + prepositions.get(DAT) + ") (" + NUMBER_ANY + ") " + patternSelection.get(AMOUNT)
                + ")\\%" + EOS, "$1 1$1 prósentum$14");
        restDict.put("(" + BOS + "(" + prepositions.get(GEN) + ") (" + NUMBER_EOS_NOT_1 + ") ?\\%" + EOS, "$1 prósenta$8"); // changed from 10 to 8
        restDict.put("(" + BOS + "(" + prepositions.get(GEN) + ") (" + NUMBER_ANY + ") " + patternSelection.get(AMOUNT)
                + ")\\%" + EOS, "$1 1$1 prósenta$14");
        restDict.put("\\%", " prósent");

    }

    public static Map<String, String> periodDict = new HashMap<String, String>() {{
        put("(\\W|^)mán(ud)?\\.?(\\W|$)", "$1mánudag$3");
        put("(\\W|^)þri(ðjud)?\\.?(\\W|$)", "$1þriðjudag$3");
        put("(\\W|^)mið(vikud)?\\.?(\\W|$)", "$1miðvikudag$3");
        put("(\\W|^)fim(mtud)?\\.?(\\W|$)", "$1fimmtudag$3");
        put("(\\W|^)fös(tud)?\\.?(\\W|$)", "$1föstudag$3");
        put("(\\W|^)lau(gard)?\\.?(\\W|$)", "$1laugardag$3");
        put("(\\W|^)sun(nud)?\\.?(\\W|$)", "$1sunnudag$3");

        put("(\\W|^)jan\\.?(\\W|$)", "$1janúar$3");
        put("(\\W|^)feb\\.?(\\W|$)", "$1febrúar$3");
        put("(\\W|^)mar\\.?(\\W|$)", "$1mars$3");
        put("(\\W|^)apr\\.?(\\W|$)", "$1apríl$3");
        put("(\\W|^)jún\\.?(\\W|$)", "$1júní$3");
        put("(\\W|^)júl\\.?(\\W|$)", "$1júlí$3");
        put("(\\W|^)ágú?\\.?(\\W|$)", "$1ágúst$3");
        put("(\\W|^)sept?\\.?(\\W|$)", "$1september$3");
        put("(\\W|^)okt\\.?(\\W|$)", "$1október$3");
        put("(\\W|^)nóv\\.?(\\W|$)", "$1nóvember$3");
        put("(\\W|^)des\\.?(\\W|$)", "$1desember$3");

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
