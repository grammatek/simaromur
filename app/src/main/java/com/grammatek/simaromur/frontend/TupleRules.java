package com.grammatek.simaromur.frontend;

import java.util.ArrayList;
import java.util.List;

/**
 * Multiple lists of expansionTuples to use when expanding digits to their correct word format.
 */

public class TupleRules {
    private TupleRules(){}

    private static final String ANYTHING = "^.*$";
    private static final String HALF_PATTERN = "(1\\/2|½)";
    private static final String THIRD_PATTERN = "(1\\/3|⅓)";
    private static final String FOURTH_PATTERN = "(1\\/4|¼)";
    private static final String TWO_THIRD_PATTERN = "(2\\/3|⅔)";
    private static final String THREE_FOURTH_PATTERN = "(3\\/4|¾)";

    private static final String ONES = "ones";
    private static final String DOZENS = "dozens";

    public static final List<ExpansionTuple> ONES_ZIP = new ArrayList<>();
    static {
        ONES_ZIP.add(new ExpansionTuple(NumberPatterns.EINN, " einn", "1"));
        ONES_ZIP.add(new ExpansionTuple(NumberPatterns.EINUM, " einum", "1"));
        ONES_ZIP.add(new ExpansionTuple(NumberPatterns.EINS, " eins", "1"));
        ONES_ZIP.add(new ExpansionTuple(NumberPatterns.EIN, " ein", "1"));
        ONES_ZIP.add(new ExpansionTuple(NumberPatterns.EINA, " eina", "1"));
        ONES_ZIP.add(new ExpansionTuple(NumberPatterns.EINN, " eina", "1"));
        ONES_ZIP.add(new ExpansionTuple(NumberPatterns.EINNI, " einni", "1"));
        ONES_ZIP.add(new ExpansionTuple(NumberPatterns.EINNAR, " einnar", "1"));
        ONES_ZIP.add(new ExpansionTuple(NumberPatterns.EITT, " eitt", "1"));
        ONES_ZIP.add(new ExpansionTuple(NumberPatterns.EINU, " einu", "1"));
        ONES_ZIP.add(new ExpansionTuple(NumberPatterns.NO_NOUN, " eitt", "1"));

        ONES_ZIP.add(new ExpansionTuple(NumberPatterns.TVEIR, " tveir", "2"));
        ONES_ZIP.add(new ExpansionTuple(NumberPatterns.TVO, " tvo", "2"));
        ONES_ZIP.add(new ExpansionTuple(NumberPatterns.TVEIMUR, " tveimur", "2"));
        ONES_ZIP.add(new ExpansionTuple(NumberPatterns.TVEGGJA, " tveggja", "2"));
        ONES_ZIP.add(new ExpansionTuple(NumberPatterns.TVAER, " tvær", "2"));
        ONES_ZIP.add(new ExpansionTuple(NumberPatterns.TVOE, " tvö", "2"));
        ONES_ZIP.add(new ExpansionTuple(NumberPatterns.NO_NOUN, " tvö", "2"));

        ONES_ZIP.add(new ExpansionTuple(NumberPatterns.THRIR, " þrír", "3"));
        ONES_ZIP.add(new ExpansionTuple(NumberPatterns.THRJAR, " þrjár", "3"));
        ONES_ZIP.add(new ExpansionTuple(NumberPatterns.THRJA, " þrjá", "3"));
        ONES_ZIP.add(new ExpansionTuple(NumberPatterns.THREMUR, " þremur", "3"));
        ONES_ZIP.add(new ExpansionTuple(NumberPatterns.THRIGGJA, " þriggja", "3"));
        ONES_ZIP.add(new ExpansionTuple(NumberPatterns.THRJU, " þrjú", "3"));
        ONES_ZIP.add(new ExpansionTuple(NumberPatterns.NO_NOUN, " þrjú", "3"));

        ONES_ZIP.add(new ExpansionTuple(NumberPatterns.FJORIR, " fjórir", "4"));
        ONES_ZIP.add(new ExpansionTuple(NumberPatterns.FJORA, " fjóra", "4"));
        ONES_ZIP.add(new ExpansionTuple(NumberPatterns.FJORUM, " fjórum", "4"));
        ONES_ZIP.add(new ExpansionTuple(NumberPatterns.FJOGURRA, " fjögurra", "4"));
        ONES_ZIP.add(new ExpansionTuple(NumberPatterns.FJORAR, " fjórar", "4"));
        ONES_ZIP.add(new ExpansionTuple(NumberPatterns.FJOGUR, " fjögur", "4"));
        ONES_ZIP.add(new ExpansionTuple(NumberPatterns.NO_NOUN, " fjögur", "4"));

        ONES_ZIP.add(new ExpansionTuple(ANYTHING, " fimm", "5"));
        ONES_ZIP.add(new ExpansionTuple(ANYTHING, " sex", "6"));
        ONES_ZIP.add(new ExpansionTuple(ANYTHING, " sjö", "7"));
        ONES_ZIP.add(new ExpansionTuple(ANYTHING, " átta", "8"));
        ONES_ZIP.add(new ExpansionTuple(ANYTHING, " níu", "9"));
    }

    public static final List<ExpansionTuple> DEC_ONES_MALE = new ArrayList<>();
    static {
        DEC_ONES_MALE.add(new ExpansionTuple(NumberPatterns.NO_NOUN, " einn", "1"));
        DEC_ONES_MALE.add(new ExpansionTuple(NumberPatterns.NO_NOUN, " tveir", "2"));
        DEC_ONES_MALE.add(new ExpansionTuple(NumberPatterns.NO_NOUN, " þrír", "3"));
        DEC_ONES_MALE.add(new ExpansionTuple(NumberPatterns.NO_NOUN, " fjórir", "4"));
    }

    public static final List<ExpansionTuple> TENS_ZIP = new ArrayList<>();
    static {
        TENS_ZIP.add(new ExpansionTuple(" tíu", "10"));
        TENS_ZIP.add(new ExpansionTuple(" ellefu", "11"));
        TENS_ZIP.add(new ExpansionTuple(" tólf", "12"));
        TENS_ZIP.add(new ExpansionTuple(" þrettán", "13"));
        TENS_ZIP.add(new ExpansionTuple(" fjórtán", "14"));
        TENS_ZIP.add(new ExpansionTuple(" fimmtán", "15"));
        TENS_ZIP.add(new ExpansionTuple(" sextán", "16"));
        TENS_ZIP.add(new ExpansionTuple(" sautján", "17"));
        TENS_ZIP.add(new ExpansionTuple(" átján", "18"));
        TENS_ZIP.add(new ExpansionTuple(" nítján", "19"));
    }

    public static final List<ExpansionTuple> ONES_NUMERATOR = new ArrayList<>();
    static {
        ONES_NUMERATOR.add(new ExpansionTuple(" einn", "1"));
        ONES_NUMERATOR.add(new ExpansionTuple(" tveir", "2"));
        ONES_NUMERATOR.add(new ExpansionTuple(" þrír", "3"));
        ONES_NUMERATOR.add(new ExpansionTuple(" fjórir", "4"));
        ONES_NUMERATOR.add(new ExpansionTuple(" fimm", "5"));
        ONES_NUMERATOR.add(new ExpansionTuple(" sex", "6"));
        ONES_NUMERATOR.add(new ExpansionTuple(" sjö", "7"));
        ONES_NUMERATOR.add(new ExpansionTuple(" átta", "8"));
        ONES_NUMERATOR.add(new ExpansionTuple(" níu", "9"));
    }

    public static final List<ExpansionTuple> DOZENS_NUMERATOR = new ArrayList<>();
    static {
        DOZENS_NUMERATOR.add(new ExpansionTuple(" tuttugu", "2"));
        DOZENS_NUMERATOR.add(new ExpansionTuple(" þrjátíu", "3"));
        DOZENS_NUMERATOR.add(new ExpansionTuple(" fjörutíu", "4"));
        DOZENS_NUMERATOR.add(new ExpansionTuple(" fimmtíu", "5"));
        DOZENS_NUMERATOR.add(new ExpansionTuple(" sextíu", "6"));
        DOZENS_NUMERATOR.add(new ExpansionTuple(" sjötíu", "7"));
        DOZENS_NUMERATOR.add(new ExpansionTuple(" áttatíu", "8"));
        DOZENS_NUMERATOR.add(new ExpansionTuple(" níutíu", "9"));
    }

    public static final List<ExpansionTuple> ONES_DENOMINATOR = new ArrayList<>();
    static {
        ONES_DENOMINATOR.add(new ExpansionTuple(" aðrir", "2"));
        ONES_DENOMINATOR.add(new ExpansionTuple(" þriðju", "3"));
        ONES_DENOMINATOR.add(new ExpansionTuple(" fjórðu", "4"));
        ONES_DENOMINATOR.add(new ExpansionTuple(" fimmtu", "5"));
        ONES_DENOMINATOR.add(new ExpansionTuple(" sjöttu", "6"));
        ONES_DENOMINATOR.add(new ExpansionTuple(" sjöundu", "7"));
        ONES_DENOMINATOR.add(new ExpansionTuple(" áttundu", "8"));
        ONES_DENOMINATOR.add(new ExpansionTuple(" níundu", "9"));
    }

    public static final List<ExpansionTuple> TENS_DENOMINATOR = new ArrayList<>();
    static {
        TENS_DENOMINATOR.add(new ExpansionTuple(" tíundu", "10"));
        TENS_DENOMINATOR.add(new ExpansionTuple(" elleftu", "11"));
        TENS_DENOMINATOR.add(new ExpansionTuple(" tólftu", "12"));
        TENS_DENOMINATOR.add(new ExpansionTuple(" þrettándu", "13"));
        TENS_DENOMINATOR.add(new ExpansionTuple(" fjórtándu", "14"));
        TENS_DENOMINATOR.add(new ExpansionTuple(" fimmtándu", "15"));
        TENS_DENOMINATOR.add(new ExpansionTuple(" sextándu", "16"));
        TENS_DENOMINATOR.add(new ExpansionTuple(" sautjándu", "17"));
        TENS_DENOMINATOR.add(new ExpansionTuple(" átjándu", "18"));
        TENS_DENOMINATOR.add(new ExpansionTuple(" nítjándu", "19"));
    }

    public static final List<ExpansionTuple> DOZENS_DENOMINATOR = new ArrayList<>();
    static {
        DOZENS_DENOMINATOR.add(new ExpansionTuple(" tuttugustu", "2"));
        DOZENS_DENOMINATOR.add(new ExpansionTuple(" þrítugustu", "3"));
        DOZENS_DENOMINATOR.add(new ExpansionTuple(" fertugustu", "4"));
        DOZENS_DENOMINATOR.add(new ExpansionTuple(" fimmtugustu", "5"));
        DOZENS_DENOMINATOR.add(new ExpansionTuple(" sextugustu", "6"));
        DOZENS_DENOMINATOR.add(new ExpansionTuple(" sjötugustu", "7"));
        DOZENS_DENOMINATOR.add(new ExpansionTuple(" áttugustu", "8"));
        DOZENS_DENOMINATOR.add(new ExpansionTuple(" nítugustu", "9"));
    }

    public static final List<ExpansionTuple> HALF_ZIP = new ArrayList<>();
    static {
        HALF_ZIP.add(new ExpansionTuple(NumberPatterns.HALFUR, " hálfur", HALF_PATTERN));
        HALF_ZIP.add(new ExpansionTuple(NumberPatterns.HALFAN, " hálfan", HALF_PATTERN));
        HALF_ZIP.add(new ExpansionTuple(NumberPatterns.HALFUM, " hálfum", HALF_PATTERN));
        HALF_ZIP.add(new ExpansionTuple(NumberPatterns.HALFS, " hálfs", HALF_PATTERN));
        HALF_ZIP.add(new ExpansionTuple(NumberPatterns.HALF, " hálf", HALF_PATTERN));
        HALF_ZIP.add(new ExpansionTuple(NumberPatterns.HALFA, " hálfa", HALF_PATTERN));
        HALF_ZIP.add(new ExpansionTuple(NumberPatterns.HALFRI, " hálfri", HALF_PATTERN));
        HALF_ZIP.add(new ExpansionTuple(NumberPatterns.HALFRAR, " hálfrar", HALF_PATTERN));
        HALF_ZIP.add(new ExpansionTuple(NumberPatterns.HALFT, " hálft", HALF_PATTERN));
        HALF_ZIP.add(new ExpansionTuple(NumberPatterns.HALFU, " hálfu", HALF_PATTERN));
        HALF_ZIP.add(new ExpansionTuple(NumberPatterns.HALFIR, " hálfir", HALF_PATTERN));
        HALF_ZIP.add(new ExpansionTuple(NumberPatterns.HALFRA, " hálfra", HALF_PATTERN));
        HALF_ZIP.add(new ExpansionTuple(NumberPatterns.NO_NOUN, " hálfur", HALF_PATTERN));

        HALF_ZIP.add(new ExpansionTuple(NumberPatterns.FRACTIONNOM, " einn þriðji", THIRD_PATTERN));
        HALF_ZIP.add(new ExpansionTuple(NumberPatterns.FRACTIONDAT, " einn þriðja", THIRD_PATTERN)); //TODO: isn't this wrong? acc and dat switched? Same in following blocks
        HALF_ZIP.add(new ExpansionTuple(NumberPatterns.FRACTIONACC, " einum þriðja", THIRD_PATTERN));
        HALF_ZIP.add(new ExpansionTuple(NumberPatterns.FRACTIONGEN, " eins þriðja", THIRD_PATTERN));
        HALF_ZIP.add(new ExpansionTuple(NumberPatterns.NO_NOUN, " einn þriðji", THIRD_PATTERN));

        HALF_ZIP.add(new ExpansionTuple(NumberPatterns.FRACTIONNOM, " einn fjórði", FOURTH_PATTERN));
        HALF_ZIP.add(new ExpansionTuple(NumberPatterns.FRACTIONDAT, " einn fjórða", FOURTH_PATTERN));
        HALF_ZIP.add(new ExpansionTuple(NumberPatterns.FRACTIONACC, " einum fjórða", FOURTH_PATTERN));
        HALF_ZIP.add(new ExpansionTuple(NumberPatterns.FRACTIONGEN, " eins fjórða", FOURTH_PATTERN));
        HALF_ZIP.add(new ExpansionTuple(NumberPatterns.NO_NOUN, " einn fjórði", FOURTH_PATTERN));

        HALF_ZIP.add(new ExpansionTuple(NumberPatterns.FRACTIONNOM, " tveir þriðju", TWO_THIRD_PATTERN));
        HALF_ZIP.add(new ExpansionTuple(NumberPatterns.FRACTIONDAT, " tvo þriðju", TWO_THIRD_PATTERN));
        HALF_ZIP.add(new ExpansionTuple(NumberPatterns.FRACTIONACC, " tveimur þriðju", TWO_THIRD_PATTERN));
        HALF_ZIP.add(new ExpansionTuple(NumberPatterns.FRACTIONGEN, " tveggja þriðju", TWO_THIRD_PATTERN));
        HALF_ZIP.add(new ExpansionTuple(NumberPatterns.NO_NOUN, " tveir þriðju", TWO_THIRD_PATTERN));

        HALF_ZIP.add(new ExpansionTuple(NumberPatterns.FRACTIONNOM, " þrír fjórðu", THREE_FOURTH_PATTERN));
        HALF_ZIP.add(new ExpansionTuple(NumberPatterns.FRACTIONDAT, " þrjá fjórðu", THREE_FOURTH_PATTERN));
        HALF_ZIP.add(new ExpansionTuple(NumberPatterns.FRACTIONACC, " þremur fjórðu", THREE_FOURTH_PATTERN));
        HALF_ZIP.add(new ExpansionTuple(NumberPatterns.FRACTIONGEN, " þriggja fjórðu", THREE_FOURTH_PATTERN));
        HALF_ZIP.add(new ExpansionTuple(NumberPatterns.NO_NOUN, " þrír fjórðu", THREE_FOURTH_PATTERN));
    }

    public static final List<ExpansionTuple> TWO_ORDINAL_ZIP = new ArrayList<>();
    static {
        TWO_ORDINAL_ZIP.add(new ExpansionTuple(NumberPatterns.ANNAR, " annar", ""));
        TWO_ORDINAL_ZIP.add(new ExpansionTuple(NumberPatterns.ANNAN, " annan", ""));
        TWO_ORDINAL_ZIP.add(new ExpansionTuple(NumberPatterns.ODRUM, " öðrum", ""));
        TWO_ORDINAL_ZIP.add(new ExpansionTuple(NumberPatterns.ANNARS, " annars", ""));
        TWO_ORDINAL_ZIP.add(new ExpansionTuple(NumberPatterns.ADRIR, " aðrir", ""));
        TWO_ORDINAL_ZIP.add(new ExpansionTuple(NumberPatterns.ADRA, " aðra", ""));
        TWO_ORDINAL_ZIP.add(new ExpansionTuple(NumberPatterns.ANNARRA, " annarra", ""));
        TWO_ORDINAL_ZIP.add(new ExpansionTuple(NumberPatterns.ONNUR, " önnur", ""));
        TWO_ORDINAL_ZIP.add(new ExpansionTuple(NumberPatterns.ANNARRI, " annarri", ""));
        TWO_ORDINAL_ZIP.add(new ExpansionTuple(NumberPatterns.ANNARRAR, " annarrar", ""));
        TWO_ORDINAL_ZIP.add(new ExpansionTuple(NumberPatterns.ADRAR, " aðrar", ""));
        TWO_ORDINAL_ZIP.add(new ExpansionTuple(NumberPatterns.ANNAD, " annað", ""));
        TWO_ORDINAL_ZIP.add(new ExpansionTuple(NumberPatterns.ODRU, " öðru", ""));
        TWO_ORDINAL_ZIP.add(new ExpansionTuple(NumberPatterns.NO_NOUN, " annan", ""));
    }

    public static final List<ExpansionTuple> ORDINALS_ONES_ZIP = new ArrayList<>();
    static {
        ORDINALS_ONES_ZIP.add(new ExpansionTuple(ONES, " fyrst", "1")); // note: different order than in original Regina: fyrst, 1, ones
        ORDINALS_ONES_ZIP.add(new ExpansionTuple(ONES, " þriðj", "3"));
        ORDINALS_ONES_ZIP.add(new ExpansionTuple(ONES, " fjórð", "4"));
        ORDINALS_ONES_ZIP.add(new ExpansionTuple(ONES, " fimmt", "5"));
        ORDINALS_ONES_ZIP.add(new ExpansionTuple(ONES, " sjött", "6"));
        ORDINALS_ONES_ZIP.add(new ExpansionTuple(ONES, " sjöund", "7"));
        ORDINALS_ONES_ZIP.add(new ExpansionTuple(ONES, " áttund", "8"));
        ORDINALS_ONES_ZIP.add(new ExpansionTuple(ONES, " níund", "9"));
        ORDINALS_ONES_ZIP.add(new ExpansionTuple(DOZENS, " tíund", "10"));
        ORDINALS_ONES_ZIP.add(new ExpansionTuple(DOZENS, " elleft", "11"));
        ORDINALS_ONES_ZIP.add(new ExpansionTuple(DOZENS, " tólft", "12"));
        ORDINALS_ONES_ZIP.add(new ExpansionTuple(DOZENS, " þrettánd", "13"));
        ORDINALS_ONES_ZIP.add(new ExpansionTuple(DOZENS, " fjórtánd", "14"));
        ORDINALS_ONES_ZIP.add(new ExpansionTuple(DOZENS, " fimmtánd", "15"));
        ORDINALS_ONES_ZIP.add(new ExpansionTuple(DOZENS, " sextánd", "16"));
        ORDINALS_ONES_ZIP.add(new ExpansionTuple(DOZENS, " sautjánd", "17"));
        ORDINALS_ONES_ZIP.add(new ExpansionTuple(DOZENS, " átjánd", "18"));
        ORDINALS_ONES_ZIP.add(new ExpansionTuple(DOZENS, " nítjánd", "19"));
    }
    public static final List<ExpansionTuple> ORDINAL_LETTERS = new ArrayList<>();
    static {
        ORDINAL_LETTERS.add(new ExpansionTuple(NumberPatterns.FYRSTI, "i", ""));
        ORDINAL_LETTERS.add(new ExpansionTuple(NumberPatterns.FYRSTA, "a", ""));
        ORDINAL_LETTERS.add(new ExpansionTuple(NumberPatterns.FYRSTU, "u", ""));
        ORDINAL_LETTERS.add(new ExpansionTuple(NumberPatterns.NO_NOUN, "a", ""));
    }
    public static final List<ExpansionTuple> DOZENS_ORDINAL_ZIP = new ArrayList<>();
    static {
        DOZENS_ORDINAL_ZIP.add(new ExpansionTuple("", " tuttug", "2"));
        DOZENS_ORDINAL_ZIP.add(new ExpansionTuple("", " þrítug", "3"));
        DOZENS_ORDINAL_ZIP.add(new ExpansionTuple("", " fertug", "4"));
        DOZENS_ORDINAL_ZIP.add(new ExpansionTuple("", " fimmtug", "5"));
        DOZENS_ORDINAL_ZIP.add(new ExpansionTuple("", " sextug", "6"));
        DOZENS_ORDINAL_ZIP.add(new ExpansionTuple("", " sjötug", "7"));
        DOZENS_ORDINAL_ZIP.add(new ExpansionTuple("", " áttug", "8"));
        DOZENS_ORDINAL_ZIP.add(new ExpansionTuple("", " nítug", "9"));
    }
    public static final List<ExpansionTuple> DOZENS_ORDINAL_LETTERS = new ArrayList<>();
    static {
        DOZENS_ORDINAL_LETTERS.add(new ExpansionTuple(NumberPatterns.FYRSTI, "asti", ""));
        DOZENS_ORDINAL_LETTERS.add(new ExpansionTuple(NumberPatterns.FYRSTA, "asta", ""));
        DOZENS_ORDINAL_LETTERS.add(new ExpansionTuple(NumberPatterns.FYRSTU, "ustu", ""));
        DOZENS_ORDINAL_LETTERS.add(new ExpansionTuple(NumberPatterns.NO_NOUN, "asta", ""));
    }
    public static final List<ExpansionTuple> HUNDRENDS_THOUSANDS_ZIP = new ArrayList<>();
    static {
        HUNDRENDS_THOUSANDS_ZIP.add(new ExpansionTuple("", " tvö", "2"));
        HUNDRENDS_THOUSANDS_ZIP.add(new ExpansionTuple("", " þrjú", "3"));
        HUNDRENDS_THOUSANDS_ZIP.add(new ExpansionTuple("", " fjögur", "4"));
        HUNDRENDS_THOUSANDS_ZIP.add(new ExpansionTuple("", " fimm", "5"));
        HUNDRENDS_THOUSANDS_ZIP.add(new ExpansionTuple("", " sex", "6"));
        HUNDRENDS_THOUSANDS_ZIP.add(new ExpansionTuple("", " sjö", "7"));
        HUNDRENDS_THOUSANDS_ZIP.add(new ExpansionTuple("", " átta", "8"));
        HUNDRENDS_THOUSANDS_ZIP.add(new ExpansionTuple("", " níu", "9"));
    }
    public static final List<ExpansionTuple> MILLIONS_ZIP = new ArrayList<>();
    static {
        MILLIONS_ZIP.add(new ExpansionTuple("", " tvær", "2"));
        MILLIONS_ZIP.add(new ExpansionTuple("", " þrjár", "3"));
        MILLIONS_ZIP.add(new ExpansionTuple("", " fjórar", "4"));
        MILLIONS_ZIP.add(new ExpansionTuple("", " fimm", "5"));
        MILLIONS_ZIP.add(new ExpansionTuple("", " sex", "6"));
        MILLIONS_ZIP.add(new ExpansionTuple("", " sjö", "7"));
        MILLIONS_ZIP.add(new ExpansionTuple("", " átta", "8"));
        MILLIONS_ZIP.add(new ExpansionTuple("", " níu", "9"));
    }
    public static final List<ExpansionTuple> BILLIONS_ZIP = new ArrayList<>();
    static {
        BILLIONS_ZIP.add(new ExpansionTuple("", " tveir", "2"));
        BILLIONS_ZIP.add(new ExpansionTuple("", " tveir", "3"));
        BILLIONS_ZIP.add(new ExpansionTuple("", " tveir", "4"));
        BILLIONS_ZIP.add(new ExpansionTuple("", " tveir", "5"));
        BILLIONS_ZIP.add(new ExpansionTuple("", " tveir", "6"));
        BILLIONS_ZIP.add(new ExpansionTuple("", " tveir", "7"));
        BILLIONS_ZIP.add(new ExpansionTuple("", " tveir", "8"));
        BILLIONS_ZIP.add(new ExpansionTuple("", " tveir", "9"));
    }
    public static final List<ExpansionTuple> MB_ORDINAL_ZIP = new ArrayList<>();
    static {
        MB_ORDINAL_ZIP.add(new ExpansionTuple("", " tví", "2"));
        MB_ORDINAL_ZIP.add(new ExpansionTuple("", " þrí", "3"));
        MB_ORDINAL_ZIP.add(new ExpansionTuple("", " fer", "4"));
        MB_ORDINAL_ZIP.add(new ExpansionTuple("", " fimm", "5"));
        MB_ORDINAL_ZIP.add(new ExpansionTuple("", " sex", "6"));
        MB_ORDINAL_ZIP.add(new ExpansionTuple("", " sjö", "7"));
        MB_ORDINAL_ZIP.add(new ExpansionTuple("", " átt", "8"));
        MB_ORDINAL_ZIP.add(new ExpansionTuple("", " ní", "9"));
    }
    public static final List<ExpansionTuple> ONES_ZIP_TIME = new ArrayList<>();
    static {
        ONES_ZIP_TIME.add(new ExpansionTuple("", " eitt", "1"));
        ONES_ZIP_TIME.add(new ExpansionTuple("", " tvö", "1"));
        ONES_ZIP_TIME.add(new ExpansionTuple("", " þrjú", "1"));
        ONES_ZIP_TIME.add(new ExpansionTuple("", " fjögur", "1"));
        ONES_ZIP_TIME.add(new ExpansionTuple("", " fimm", "1"));
        ONES_ZIP_TIME.add(new ExpansionTuple("", " sex", "1"));
        ONES_ZIP_TIME.add(new ExpansionTuple("", " sjö", "1"));
        ONES_ZIP_TIME.add(new ExpansionTuple("", " átta", "1"));
        ONES_ZIP_TIME.add(new ExpansionTuple("", " níu", "1"));
    }
    public static final List<ExpansionTuple> DOZENS_ZIP = new ArrayList<>();
    static {
        DOZENS_ZIP.add(new ExpansionTuple("", " tuttugu", "2"));
        DOZENS_ZIP.add(new ExpansionTuple("", " þrjátíu", "3"));
        DOZENS_ZIP.add(new ExpansionTuple("", " fjörutíu", "4"));
        DOZENS_ZIP.add(new ExpansionTuple("", " fimmtíu", "5"));
        DOZENS_ZIP.add(new ExpansionTuple("", " sextíu", "6"));
        DOZENS_ZIP.add(new ExpansionTuple("", " sjötíu", "7"));
        DOZENS_ZIP.add(new ExpansionTuple("", " áttatíu", "8"));
        DOZENS_ZIP.add(new ExpansionTuple("", " níutíu", "9"));
    }

}

