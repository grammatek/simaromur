package com.grammatek.simaromur.frontend;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * This class contains:
 *      - Arrays with column names of the decimal system (hundreds, dozens, ones, etc.)
 *      - Regular expressions that match different verisons of ordinals, cardinals, decimals, etc.
 *      - simple digits - word map with default expansions
 *      - simple ordinal digits - word map with default expansions
 *      - web-link replacement map
 */

public class NumberHelper {

    public static final String HUNDRED_MILLIONS = "hundred millions";
    public static final String TEN_MILLIONS = "ten millions";
    public static final String MILLIONS = "millions";
    public static final String HUNDRED_THOUSANDS = "hundred thousands";
    public static final String TEN_THOUSANDS = "ten thousands";
    public static final String THOUSANDS = "thousands";
    public static final String HUNDREDS = "hundreds";
    public static final String DOZENS = "dozens";
    public static final String ONES = "ones";

    //sport
    public static final String FIRST_TEN = "first_ten";
    public static final String FIRST_ONE = "first_one";
    public static final String BETWEEN_TEAMS = "between_teams";
    public static final String SECOND_TEN = "second_ten";
    public static final String SECOND_ONE = "second_one";

    // decimal
    public static final String POINTS = "points";
    public static final String P2 = "point2";
    public static final String P3 = "point3";
    public static final String P4 = "point4";
    public static final String P5 = "point5";
    public static final String P6 = "point6";
    public static final String P7 = "point7";
    public static final String P8 = "point8";
    public static final String P9 = "point9";
    public static final String P10 = "point10";

    private NumberHelper() {}

    public static final String[] INT_COLS_THOUSAND = new String[]{THOUSANDS, HUNDREDS, DOZENS, ONES};
    public static final String[] INT_COLS_MILLION = new String[] {HUNDRED_THOUSANDS, TEN_THOUSANDS, THOUSANDS, HUNDREDS, DOZENS, ONES};

    public static final String [] INT_COLS_BIG = new String[] {"hundred billions", "ten billions", "billions", "hundred millions", "ten millions", "millions",
            HUNDRED_THOUSANDS, TEN_THOUSANDS, THOUSANDS, HUNDREDS, DOZENS, ONES};

    public static final String [] DECIMAL_COLS_THOUSAND = new String[] {THOUSANDS, HUNDREDS, DOZENS, ONES, POINTS ,P2 ,P3, P4,
            P5, P6, P7, P8, P9, P10};

    public static final String [] DECIMAL_COLS_BIG = new String[] {"hundred billions", "ten billions","billions", "hundred millions","ten millions","millions",
            HUNDRED_THOUSANDS, TEN_THOUSANDS, THOUSANDS, HUNDREDS, DOZENS, ONES,POINTS ,P2 ,P3, P4,
            P5, P6, P7, P8, P9, P10};

    public static final String[] TIME_SPORT_COLS = new String[] {FIRST_TEN, FIRST_ONE, BETWEEN_TEAMS, SECOND_TEN, SECOND_ONE};

    //1.234. or 1. or 12. or 123.
    public static final Pattern ORDINAL_THOUSAND_PTRN = Pattern.compile("^([1-9]\\.?\\d{3}|[1-9]\\d{0,2})\\.$");
    public static final String ORDINAL_MILLION_PTRN = "^[1-9]\\d{0,2}\\.\\d{3}\\.$";
    public static final String ORDINAL_BIG_PTRN = "^[1-9]\\d{0,2}(\\.\\d{3}){2,3}\\.$";

    //1.234 or 1 or 12 or 123
    public static final Pattern CARDINAL_THOUSAND_PTRN = Pattern.compile("^([1-9]\\.?\\d{3}|[1-9]\\d{0,2})$");
    //1.234 or 12.345 or 123.456 or 123468
    public static final Pattern CARDINAL_MILLION_PTRN = Pattern.compile("^[1-9]\\d{0,2}\\.?\\d{3}$");
    public static final Pattern CARDINAL_BIG_PTRN = Pattern.compile("^[1-9]\\d{0,2}(\\.\\d{3}){2,3}$");

    //1.123,4 or 1232,4 or 123,4 or 12,42345 or 1,489
    public static final Pattern DECIMAL_THOUSAND_PTRN = Pattern.compile("^([1-9]\\.?\\d{3}|[1-9]\\d{0,2}),\\d+$");
    public static final String DECIMAL_BIG_PTRN = "^[1-9]\\d{0,2}\\.?(\\d{3}){1,3},\\d+$";

    // 4/8 or ⅓ , etc.
    public static final Pattern FRACTION_PTRN = Pattern.compile("^([1-9]\\d{0,2} ?)?([1-9]\\d*\\/([2-9]|[1-9]\\d+)|(½|⅓|⅔|¼|¾))$");
    // 01:55 or 01.55
    public static final Pattern TIME_PTRN = Pattern.compile("^(([01]?\\d|2[0-4])[:\\.][0-5]|0)\\d$");
    public static final String SPORT_PTRN = "^(?!1\\/2)([1-9]\\d?\\/[1-9]\\d?)$";

    public static final Pattern LETTERS_PTRN = Pattern.compile("^(?!^(RÚV|SPRON|NATO|-|\\.)$)[\\-.A-ZÁÐÉÍÓÚÝÞÆÖ_]+$");
    // original pattern, keep the line until we have tested that the above pattern works as expected
    //public static final String LETTERS_PTRN = "^(?!^(RÚV|SPRON|\\-|\\.)$)[\\-\\.A-ZÁÐÉÍÓÚÝÞÆÖ]{1,5}$";
    public static final String ROMAN_LETTERS_PTRN = "[IVXLCDM]{5,20}";
    public static final String SYMBOL_PTRN = "^[^A-ZÁÐÉÍÓÚÝÞÆÖa-záðéíóúýþæö\\d]$";

    public static final Map<String, String> DIGIT_NUMBERS = new HashMap<>();
    static {
        DIGIT_NUMBERS.put("0", " núll");
        DIGIT_NUMBERS.put("1", " einn");
        DIGIT_NUMBERS.put("2", " tveir");
        DIGIT_NUMBERS.put("3", " þrír");
        DIGIT_NUMBERS.put("4", " fjórir");
        DIGIT_NUMBERS.put("5", " fimm");
        DIGIT_NUMBERS.put("6", " sex");
        DIGIT_NUMBERS.put("7", " sjö");
        DIGIT_NUMBERS.put("8", " átta");
        DIGIT_NUMBERS.put("9", " níu");
        //TODO: not sure about this, without context difficult to say if it should be "sil" or not
        // DIGIT_NUMBERS.put("\\-", " <sil>");
        //DIGIT_NUMBERS.put("\\-", " #");
        DIGIT_NUMBERS.put("\\+", " plús");
        //TODO: if we have more sentences being normalized, this replaces end-of-sentence dot as well. We don't want that
        DIGIT_NUMBERS.put("\\.", " punktur");
        DIGIT_NUMBERS.put(":", " tvípunktur");
        //TODO: converts normal sentence commas, ask what this is supposed to do
        //DIGIT_NUMBERS.put(",", " komma");
        DIGIT_NUMBERS.put("\\/", " skástrik");
    }

    public static final Map<String, String> DIGITS_ORD = new HashMap<>();
    static {
        DIGITS_ORD.put("1", "fyrsta");
        DIGITS_ORD.put("2", "annan");
        DIGITS_ORD.put("3", "þriðja");
        DIGITS_ORD.put("4", "fjórða");
        DIGITS_ORD.put("5", "fimmta");
        DIGITS_ORD.put("6", "sjötta");
        DIGITS_ORD.put("7", "sjöunda");
        DIGITS_ORD.put("8", "áttunda");
        DIGITS_ORD.put("9", "níunda");
    }

    public static final Map<String ,String> WLINK_NUMBERS = new HashMap<>();
    static {
        WLINK_NUMBERS.put("0", "núll");
        WLINK_NUMBERS.put("1", "einn");
        WLINK_NUMBERS.put("2", "tveir");
        WLINK_NUMBERS.put("3", "þrír");
        WLINK_NUMBERS.put("4", "fjórir");
        WLINK_NUMBERS.put("5", "fimm");
        WLINK_NUMBERS.put("6", "sex");
        WLINK_NUMBERS.put("7", "sjö");
        WLINK_NUMBERS.put("8", "átta");
        WLINK_NUMBERS.put("9", "níu");
        WLINK_NUMBERS.put("\\.", "punktur");
        WLINK_NUMBERS.put("\\-", "bandstrik");
        WLINK_NUMBERS.put("\\/", "skástrik");
        WLINK_NUMBERS.put("_", "undirstrik");
        WLINK_NUMBERS.put("@", "hjá");
        WLINK_NUMBERS.put(":", "tvípunktur");
        WLINK_NUMBERS.put("=", "jafnt og");
        WLINK_NUMBERS.put("\\?", "spurningarmerki");
        WLINK_NUMBERS.put("!", "upphrópunarmerki");
        WLINK_NUMBERS.put("&", "og");
        WLINK_NUMBERS.put("%", "prósent");
        WLINK_NUMBERS.put("#", "myllumerki");
    }
}



