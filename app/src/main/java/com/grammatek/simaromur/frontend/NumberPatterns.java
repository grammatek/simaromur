package com.grammatek.simaromur.frontend;

/**
 * This class holds the patterns to determine which grammatical form of a number to choose, dependent on the
 * POS-tags of the token following the digit in text.
 *
 * The set of POS-tags is the same as defined for MIM-GOLD in 2020 (the new tagset)
 *
 * Example:
 * [n(noun)l(acjective)][k(masculine)v(feminine)h(neutral)][e(singular)f(plural)][n(nominative)o(accusative)þ(dative)e(genitive)]
 *
 * Further, this class holds patterns that match large digits and decimal patterns
 *
 */
public class NumberPatterns {

    private NumberPatterns() {
    }

    // GRAMMAR
    // ½ skammtur: hálfur skammtur
    // ½ followed by a masculine, singular, nominative word
    public static final String HALFUR = "[nl]ke[n]-?((g?s?)|([svo]?[fme]?))";
    // ½ skammt: hálfan skammt
    // the following word is masculine, singular, accusative
    public static final String HALFAN = "[nl]ke[o]-?((g?s?)|([svo]?[fme]?))";
    // ½ skammti: hálfum skammti
    // whichever gender, singular, dative
    public static final String HALFUM = "[nl][kvh][ef]þ-?((g?s?)|([svo]?[fme]?))";
    // ½ skammts: hálfs skammts
    // masculine or neutral, singular, genitive
    public static final String HALFS = "[nl][kh]ee-?((g?s?)|([svo]?[fme]?))";
    // ½ teskeið: hálf teskeið/hálf epli
    // feminine, singular, nominative or neutral, plural, nominative/accusative
    public static final String HALF = "(([nl]ven)|([nl]hf[no]))-?((g?s?)|([svo]?[fme]?))";
    // ½ teskeið: hálfa teskeið, ½ skammta: hálfa skammta
    public static final String HALFA = "[nl][kv][fe]o-?((g?s?)|([svo]?[fme]?))";
    // ½ teskeið: hálfri teskeið
    public static final String HALFRI = "[nl]veþ-?((g?s?)|([svo]?[fme]?))";
    // ½ teskeiðar: hálfrar teskeiðar
    public static final String HALFRAR = "[nl]vee-?((g?s?)|([svo]?[fme]?))";
    // ½ epli: hálft epli
    public static final String HALFT = "[nl]he[no]-?((g?s?)|([svo]?[fme]?))";
    // ½ epli: hálfu epli
    public static final String HALFU = "[nl]heþ-?((g?s?)|([svo]?[fme]?))";
    // ½ skammtar: hálfir skammtar
    public static final String HALFIR = "[nl]kfn-?((g?s?)|([svo]?[fme]?))";
    // ½ skammta: hálfra skammta, ½ teskeiða: hálfra teskeiða, ½ epla: hálfra epla
    public static final String HALFRA = "[nl][kvh]fe-?((g?s?)|([svo]?[fme]?))";
    // ½ teskeiðar: hálfar teskeiðar
    public static final String HALFAR = "[nl]vf[no]-?((g?s?)|([svo]?[fme]?))";

    // 2/3 hlutar: tveir þriðju hlutar
    public static final String FRACTIONNOM = "[nl][kvh][ef]n-?((g?s?)|([svo]?[fme]?))";
    // 2/3 hluta: tvo þriðju hluta
    public static final String FRACTIONDAT = "[nl][kvh][ef]o-?((g?s?)|([svo]?[fme]?))";
    // 2/3 hlutum: tveimur þriðju hlutum
    public static final String FRACTIONACC = "[nl][kvh][ef]þ-?((g?s?)|([svo]?[fme]?))";
    // 2/3 hluta: tveggja þriðju hluta
    public static final String FRACTIONGEN = "[nl][kvh][ef]e-?((g?s?)|([svo]?[fme]?))";

    // similar to before
    // 1 followed by noun or adjective, masc., nom. or acc.
    // TODO: do we need all the different patterns? isn't TVEIR == THRIR? etc.
    public static final String EINN = "[nl]k[ef][no]-?((g?s?)|([svo]?[fme]?))";
    public static final String EINUM = "[nl]k[ef]þ-?((g?s?)|([svo]?[fme]?))";
    public static final String EINS = "[nl][kh][ef]e-?((g?s?)|([svo]?[fme]?))";
    public static final String EIN = "(([nl]v[ef]n)|([n|l]hf[n|o]))-?((g?s?)|([svo]?[fme]?))";
    public static final String EINA = "[nl]v[ef]o-?((g?s?)|([svo]?[fme]?))";
    public static final String EINNI = "[nl]v[ef]þ-?((g?s?)|([svo]?[fme]?))";
    public static final String EINNAR = "[nl]v[ef]e-?((g?s?)|([svo]?[fme]?))";
    public static final String EITT = "[nl]h[ef][no]-?((g?s?)|([svo]?[fme]?))";
    public static final String EINU = "[nl]h[ef]þ-?((g?s?)|([svo]?[fme]?))";

    // 2
    public static final String TVEIR = "[nl]k[ef]n-?((g?s?)|([svo]?[fme]?))";
    public static final String TVO = "[nl]k[ef]o-?((g?s?)|([svo]?[fme]?))";
    public static final String TVEIMUR = "[nl][kvh][ef]þ-?((g?s?)|([svo]?[fme]?))";
    public static final String TVEGGJA = "[nl][kvh][ef]e-?((g?s?)|([svo]?[fme]?))";
    public static final String TVAER = "[nl]v[ef][no]-?((g?s?)|([svo]?[fme]?))";
    public static final String TVOE = "[nl]h[ef][no]-?((g?s?)|([svo]?[fme]?))";

    // 3
    public static final String THRIR = "[nl]k[ef]n-?((g?s?)|([svo]?[fme]?))";
    public static final String THRJA = "[nl]k[ef]o-?((g?s?)|([svo]?[fme]?))";
    public static final String THREMUR = "[nl][kvh][ef]þ-?((g?s?)|([svo]?[fme]?))";
    public static final String THRIGGJA = "[nl][kvh][ef]e-?((g?s?)|([svo]?[fme]?))";
    public static final String THRJAR = "[nl]v[ef][no]-?((g?s?)|([svo]?[fme]?))";
    public static final String THRJU = "[nl]h[ef][no]-?((g?s?)|([svo]?[fme]?))";

    // 4
    public static final String FJORIR = "[nl]k[ef]n-?((g?s?)|([svo]?[fme]?))";
    public static final String FJORA = "[nl]k[ef]o-?((g?s?)|([svo]?[fme]?))";
    public static final String FJORUM = "[nl][kvh][ef]þ-?((g?s?)|([svo]?[fme]?))";
    public static final String FJOGURRA = "[nl][kvh][ef]e-?((g?s?)|([svo]?[fme]?))";
    public static final String FJORAR = "[nl]v[ef][no]-?((g?s?)|([svo]?[fme]?))";
    public static final String FJOGUR = "[nl]h[ef][no]-?((g?s?)|([svo]?[fme]?))";

    // patterns for the ordinal number 2.
    // 2. followed by noun or adjective, masculine, singular, nominative
    public static final String ANNAR = "[nl]ken-?(g?s?|[svo]?[fme]?)";
    public static final String ANNAN = "[nl]keo-?(g?s?|[svo]?[fme]?)";
    public static final String ODRUM = "[nl]((ke)|([kvh]f))þ-?(g?s?|[svo]?[fme]?)";
    public static final String ADRIR = "[nl]kfn-?(g?s?|[svo]?[fme]?)";
    public static final String ADRA = "[nl](kf|ve)o-?((g?s?)|([svo]?[fme]?))";
    public static final String ANNARRA = "[nl][kvh]fe-?(g?s?|[svo]?[fme]?)";
    public static final String ONNUR = "[nl](ven|hf[no])-?(g?s?|[svo]?[fme]?)";
    public static final String ANNARRI = "[nl]veþ-?(g?s?|[svo]?[fme]?)";
    public static final String ANNARRAR = "[nl]vee-?(g?s?|[svo]?[fme]?)";
    public static final String ADRAR = "[nl]vf[no]-?(g?s?|[svo]?[fme]?)";
    public static final String ANNAD = "[nl]he[no]-?(g?s?|[svo]?[fme]?)";
    public static final String ODRU = "[nl]heþ-?(g?s?|[svo]?[fme]?)";
    public static final String ANNARS = "[nl][kh]ee-?(g?s?|[svo]?[fme]?)";

    // patterns for all ordinal numbers except 2. (see above)
    public static final String FYRSTI = "[nl]ken-?(g?s?|[svo]?[fme]?)";
    public static final String FYRSTA = "[nl](ke[oþe]|ven|he[noþe])-?(g?s?|[svo]?[fme]?)";
    public static final String FYRSTU = "[nl](([kvh]f[noþe])|(ve[oþe]))-?(g?s?|[svo]?[fme]?)";

    // when a number is not followed by a noun or an adjective
    public static final String NO_NOUN = "^(?![nl][kvh][ef][noþe]-?((g?s?)|([svo]?[fme]?)))[a-záðéíóúýþæö\\d\\-]+$";

    // NUMBER RULES

    // digits in decimal numbers preceding the decimal place, can be any positive number and 0
    // for example {0,}35 or {13.234,}2342
    public static final String ZEROPNT_PTRN = "^(([1-9]((\\d{0,2}(\\.\\d{3})*\\.)\\d{3}))|\\d+|0),";

    // a possibility of digits in decimal numbers succeeding the decimal place, can be any positive number or fraction
    // for example 1{,24} or 4{½}
    public static final String DEC_PTRN = "((,\\d*)|(\\s1\\/2|\\s?(½|⅓|¼|⅔|¾)))?$";

    // definitely digits in decimal numbers succeeding the decimal place
    // 210{,432}
    public static final String DEC_PTRN_DEF = "(,\\d*)|(\\s1\\/2|\\s?(½|⅓|¼|⅔|¾))$";

    // a possibility of digits in decimal numbers succeeding the decimal place, can also be an ordinal number
    // 210{.}, 243{,543}, 243
    public static final String DEC_PTRN_ORDINAL = "(\\.|(,\\d*)|(\\s1\\/2|\\s?(½|⅓|¼|⅔|¾)))?$";

    // before fraction {123 }3/4
    public static final String FRACTION_PTRN_BEFORE = "^(([1-9]((\\d{0,2}(\\.\\d{3})*))|\\d+))\\s";

    // pattern before a number that is not 11-19, so the number becomes "and number"
    // legal - 1: einn
    // illegal - 11: ellefu
    // legal - 21: tuttugu og einn
    public static final String ONES_PTRN_NO_11 = "^((([1-9]((\\d{0,2}(\\.\\d{3})*\\.\\d)|\\d*))[02-9])|[2-9]?)?";

    // 11 is legal
    public static final String ONES_PTRN_11 = "^([1-9]((\\d{0,2}(\\.\\d{3})*\\.\\d{2}|\\d*))|[1-9]?)?";

    // pattern before a dozens place: {135.235.2}13
    public static final String TNS_PTRN = "^((([1-9]((\\d{0,2}(\\.\\d{3})*\\.)|\\d*))\\d)|[1-9])?";

    // pattern before a hundreds place: {135.235.}213
    public static final String HNDRDS_PTRN = "^((([1-9]((\\d{0,2}(\\.\\d{3})*\\.)|\\d*)))|[1-9]?)";

    // must occur
    public static final String HNDRDS_PTRN_DEF = "^((([1-9]((\\d{0,2}(\\.\\d{3})*\\.)|\\d*)))|[1-9])";

    // where you say "tvö þúsund ... "
    // {2.000}, {2.021}, {2421}
    public static final String THSNDS_PTRN_AFTER = "(\\.?(000|(0[2-9][1-9])|([1-9](?!00)\\d{2})))";

    // where you say "tvö þúsund og"
    // {2008}, {2019}, {2090}
    public static final String THSNDS_AND_PTRN_CARDINAL = "\\.?(0([01][1-9]|[1-9]0)|[1-9]00)";

    // ordinal, where you say "tvö þúsund og"
    // {2008.}: tvö þúsundasti og áttundi
    public static final String THSNDS_AND_PTRN_ORDINAL = "\\.?(0[2-9]0|[1-9]00)\\.$";

    //hndrds_ptrn_after = r"(0([01][1-9]|[2-9]0)|00)"
    // "hundruð"
    //{2}21.342
    public static final String HNDRD_THSND_AFTER = "([2-9][1-9])(\\.?\\d{3})";

    // "hundruð og"
    //{2}14.342
    public static final String HNDRD_AND_THSND = "(([01][1-9]\\.?\\d{3})|([2-9]0\\.?\\d{3}))";

    // milljón(ir) og
    // {2.000.002}: tvær milljónir og tveir
    public static final String MILLION_AND_CARDINAL = "\\.(([1-9]00\\.000)|(0[1-9]0\\.000)|(00[1-9]\\.000)|(000\\.[1-9]00)|(000\\.0([01][1-9]|[2-9]0)))";

    // milljónasti og
    // {2.000.002.}: tvímilljónasti og annar
    public static final String MILLION_AND_ORDINAL = "\\.(([1-9]00\\.000)|(0[1-9]0\\.000)|(00[1-9]\\.000)|(000\\.[1-9]00)|(000\\.0[2-9]0))\\.$";

    // milljónir ..
    // {2.034.435} tvær milljónir þrjátíu og fjögur þúsund fjögur hundruð þrjátíu og fimm
    public static final String MILLION_PTRN_AFTER = "\\.((000\\.000)|([1-9](?!00\\.000)\\d{2}\\.\\d{3})|(0[1-9](?!0\\.000)\\d\\.\\d{3})|(00[1-9]\\.(?!0{3})\\d{3})|(0{3}\\.0[2-9][1-9]))";
    public static final String HNDRD_AND_MILLION = "([01][1-9]|[2-9]0)(\\.\\d{3}){2}";
    public static final String HNDRD_MILLION = "([2-9][1-9])(\\.\\d{3}){2}";

    // milljarðar og
    // {2.000.000.003}: tveir milljarðar og þrír
    public static final String BILLION_AND_CARDINAL = "\\.((([1-9]00)|(0[1-9]0)|(00[1-9]))(\\.0{3}){2}|(0{3}\\.([1-9]00|0[1-9]0|00[1-9])\\.0{3})|((0{3}\\.){2}([1-9]00|0[1-9]0|0[01][1-9])))";

    // milljarðasti og
    // {2.000.000.003.}: tvímilljarðasti og þrír
    public static final String BILLION_AND_ORDINAL = "\\.((([1-9]00)|(0[1-9]0)|(00[1-9]))(\\.0{3}){2}|(0{3}\\.([1-9]00|0[1-9]0|00[1-9])\\.0{3})|((0{3}\\.){2}([1-9]00|0[1-9]0)|10))\\.$";

    // Patterns to make up "milljarðar .."
    // {2.001.023.425}: tveir milljarðar ein milljón tuttugu og þrjú þúsund fjögur hundruð tuttugu og fimm
    public static final String BILLION_1 = "([1-9](?!00(\\.000){2})\\d{2}(\\.\\d{3}){2})";
    public static final String BILLION_2 = "(0[1-9](?!0(\\.000){2})\\d(\\.\\d{3}){2})";
    public static final String BILLION_3 = "(00[1-9](?!(\\.000){2})(\\.\\d{3}){2})";
    public static final String BILLION_4 = "(0{3}\\.[1-9](?!00\\.000)\\d{2}\\.\\d{3})";
    public static final String BILLION_5 = "(0{3}\\.0[1-9](?!0\\.000)\\d\\.\\d{3})";
    public static final String BILLION_6 = "(0{3}\\.00[1-9]\\.(?!000)\\d{3})";
    public static final String BILLION_7 = "((0{3}\\.){2}[1-9](?!00)\\d{2})";
    public static final String BILLION_8 = "((0{3}\\.){2}0[2-9][1-9])";

    public static final String BILLION_AFTER = "\\.((000(\\.0{3}){2})|" + BILLION_1 + "|" + BILLION_2 + "|" + BILLION_3
            + "|" + BILLION_4 + "|" + BILLION_5 + "|" + BILLION_6 + "|" + BILLION_7 + "|" + BILLION_8 + ")";

    // 203.000.321.023
    public static final String HNDRD_AND_BILLION = "([01][1-9]|[2-9]0)(\\.\\d{3}){3}";

    // 234.234.123.234
    public static final String HNDRD_BILLION = "([2-9][1-9])(\\.\\d{3}){3}";
}
