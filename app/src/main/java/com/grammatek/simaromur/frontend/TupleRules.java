package com.grammatek.simaromur.frontend;

import java.util.ArrayList;
import java.util.List;

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

    public static final List<Tuple> ONES_ZIP = new ArrayList<>();
    static {
        ONES_ZIP.add(new Tuple(NumberPatterns.EINN, " einn", "1"));
        ONES_ZIP.add(new Tuple(NumberPatterns.EINUM, " einum", "1"));
        ONES_ZIP.add(new Tuple(NumberPatterns.EINS, " eins", "1"));
        ONES_ZIP.add(new Tuple(NumberPatterns.EIN, " ein", "1"));
        ONES_ZIP.add(new Tuple(NumberPatterns.EINA, " eina", "1"));
        ONES_ZIP.add(new Tuple(NumberPatterns.EINN, " eina", "1"));
        ONES_ZIP.add(new Tuple(NumberPatterns.EINNI, " einni", "1"));
        ONES_ZIP.add(new Tuple(NumberPatterns.EINNAR, " einnar", "1"));
        ONES_ZIP.add(new Tuple(NumberPatterns.EITT, " eitt", "1"));
        ONES_ZIP.add(new Tuple(NumberPatterns.EINU, " einu", "1"));
        ONES_ZIP.add(new Tuple(NumberPatterns.NO_NOUN, " eitt", "1"));

        ONES_ZIP.add(new Tuple(NumberPatterns.TVEIR, " tveir", "2"));
        ONES_ZIP.add(new Tuple(NumberPatterns.TVO, " tvo", "2"));
        ONES_ZIP.add(new Tuple(NumberPatterns.TVEIMUR, " tveimur", "2"));
        ONES_ZIP.add(new Tuple(NumberPatterns.TVEGGJA, " tveggja", "2"));
        ONES_ZIP.add(new Tuple(NumberPatterns.TVAER, " tvær", "2"));
        ONES_ZIP.add(new Tuple(NumberPatterns.TVOE, " tvö", "2"));
        ONES_ZIP.add(new Tuple(NumberPatterns.NO_NOUN, " tvö", "2"));

        ONES_ZIP.add(new Tuple(NumberPatterns.THRIR, " þrír", "3"));
        ONES_ZIP.add(new Tuple(NumberPatterns.THRJAR, " þrjár", "3"));
        ONES_ZIP.add(new Tuple(NumberPatterns.THRJA, " þrjá", "3"));
        ONES_ZIP.add(new Tuple(NumberPatterns.THREMUR, " þremur", "3"));
        ONES_ZIP.add(new Tuple(NumberPatterns.THRIGGJA, " þriggja", "3"));
        ONES_ZIP.add(new Tuple(NumberPatterns.THRJU, " þrjú", "3"));
        ONES_ZIP.add(new Tuple(NumberPatterns.NO_NOUN, " þrjú", "3"));

        ONES_ZIP.add(new Tuple(NumberPatterns.FJORIR, " fjórir", "4"));
        ONES_ZIP.add(new Tuple(NumberPatterns.FJORA, " fjóra", "4"));
        ONES_ZIP.add(new Tuple(NumberPatterns.FJORUM, " fjórum", "4"));
        ONES_ZIP.add(new Tuple(NumberPatterns.FJOGURRA, " fjögurra", "4"));
        ONES_ZIP.add(new Tuple(NumberPatterns.FJORAR, " fjórar", "4"));
        ONES_ZIP.add(new Tuple(NumberPatterns.FJOGUR, " fjögur", "4"));
        ONES_ZIP.add(new Tuple(NumberPatterns.NO_NOUN, " fjögur", "4"));

        ONES_ZIP.add(new Tuple(ANYTHING, " fimm", "5"));
        ONES_ZIP.add(new Tuple(ANYTHING, " sex", "6"));
        ONES_ZIP.add(new Tuple(ANYTHING, " sjö", "7"));
        ONES_ZIP.add(new Tuple(ANYTHING, " átta", "8"));
        ONES_ZIP.add(new Tuple(ANYTHING, " níu", "9"));
    }

    public static final List<Tuple> DEC_ONES_MALE = new ArrayList<>();
    static {
        DEC_ONES_MALE.add(new Tuple(NumberPatterns.NO_NOUN, " einn", "1"));
        DEC_ONES_MALE.add(new Tuple(NumberPatterns.NO_NOUN, " tveir", "2"));
        DEC_ONES_MALE.add(new Tuple(NumberPatterns.NO_NOUN, " þrír", "3"));
        DEC_ONES_MALE.add(new Tuple(NumberPatterns.NO_NOUN, " fjórir", "4"));
    }

    public static final List<Tuple> TENS_ZIP = new ArrayList<>();
    static {
        TENS_ZIP.add(new Tuple(" tíu", "10"));
        TENS_ZIP.add(new Tuple(" ellefu", "11"));
        TENS_ZIP.add(new Tuple(" tólf", "12"));
        TENS_ZIP.add(new Tuple(" þrettán", "13"));
        TENS_ZIP.add(new Tuple(" fjórtán", "14"));
        TENS_ZIP.add(new Tuple(" fimmtán", "15"));
        TENS_ZIP.add(new Tuple(" sextán", "16"));
        TENS_ZIP.add(new Tuple(" sautján", "17"));
        TENS_ZIP.add(new Tuple(" átján", "18"));
        TENS_ZIP.add(new Tuple(" nítján", "19"));
    }

    public static final List<Tuple> ONES_NUMERATOR = new ArrayList<>();
    static {
        ONES_NUMERATOR.add(new Tuple(" einn", "1"));
        ONES_NUMERATOR.add(new Tuple(" tveir", "2"));
        ONES_NUMERATOR.add(new Tuple(" þrír", "3"));
        ONES_NUMERATOR.add(new Tuple(" fjórir", "4"));
        ONES_NUMERATOR.add(new Tuple(" fimm", "5"));
        ONES_NUMERATOR.add(new Tuple(" sex", "6"));
        ONES_NUMERATOR.add(new Tuple(" sjö", "7"));
        ONES_NUMERATOR.add(new Tuple(" átta", "8"));
        ONES_NUMERATOR.add(new Tuple(" níu", "9"));
    }

    public static final List<Tuple> DOZENS_NUMERATOR = new ArrayList<>();
    static {
        DOZENS_NUMERATOR.add(new Tuple(" tuttugu", "2"));
        DOZENS_NUMERATOR.add(new Tuple(" þrjátíu", "3"));
        DOZENS_NUMERATOR.add(new Tuple(" fjörutíu", "4"));
        DOZENS_NUMERATOR.add(new Tuple(" fimmtíu", "5"));
        DOZENS_NUMERATOR.add(new Tuple(" sextíu", "6"));
        DOZENS_NUMERATOR.add(new Tuple(" sjötíu", "7"));
        DOZENS_NUMERATOR.add(new Tuple(" áttatíu", "8"));
        DOZENS_NUMERATOR.add(new Tuple(" níutíu", "9"));
    }

    public static final List<Tuple> ONES_DENOMINATOR = new ArrayList<>();
    static {
        ONES_DENOMINATOR.add(new Tuple(" aðrir", "2"));
        ONES_DENOMINATOR.add(new Tuple(" þriðju", "3"));
        ONES_DENOMINATOR.add(new Tuple(" fjórðu", "4"));
        ONES_DENOMINATOR.add(new Tuple(" fimmtu", "5"));
        ONES_DENOMINATOR.add(new Tuple(" sjöttu", "6"));
        ONES_DENOMINATOR.add(new Tuple(" sjöundu", "7"));
        ONES_DENOMINATOR.add(new Tuple(" áttundu", "8"));
        ONES_DENOMINATOR.add(new Tuple(" níundu", "9"));
    }

    public static final List<Tuple> TENS_DENOMINATOR = new ArrayList<>();
    static {
        TENS_DENOMINATOR.add(new Tuple(" tíundu", "10"));
        TENS_DENOMINATOR.add(new Tuple(" elleftu", "11"));
        TENS_DENOMINATOR.add(new Tuple(" tólftu", "12"));
        TENS_DENOMINATOR.add(new Tuple(" þrettándu", "13"));
        TENS_DENOMINATOR.add(new Tuple(" fjórtándu", "14"));
        TENS_DENOMINATOR.add(new Tuple(" fimmtándu", "15"));
        TENS_DENOMINATOR.add(new Tuple(" sextándu", "16"));
        TENS_DENOMINATOR.add(new Tuple(" sautjándu", "17"));
        TENS_DENOMINATOR.add(new Tuple(" átjándu", "18"));
        TENS_DENOMINATOR.add(new Tuple(" nítjándu", "19"));
    }

    public static final List<Tuple> DOZENS_DENOMINATOR = new ArrayList<>();
    static {
        DOZENS_DENOMINATOR.add(new Tuple(" tuttugustu", "2"));
        DOZENS_DENOMINATOR.add(new Tuple(" þrítugustu", "3"));
        DOZENS_DENOMINATOR.add(new Tuple(" fertugustu", "4"));
        DOZENS_DENOMINATOR.add(new Tuple(" fimmtugustu", "5"));
        DOZENS_DENOMINATOR.add(new Tuple(" sextugustu", "6"));
        DOZENS_DENOMINATOR.add(new Tuple(" sjötugustu", "7"));
        DOZENS_DENOMINATOR.add(new Tuple(" áttugustu", "8"));
        DOZENS_DENOMINATOR.add(new Tuple(" nítugustu", "9"));
    }

    public static final List<Tuple> HALF_ZIP = new ArrayList<>();
    static {
        HALF_ZIP.add(new Tuple(NumberPatterns.HALFUR, " hálfur", HALF_PATTERN));
        HALF_ZIP.add(new Tuple(NumberPatterns.HALFAN, " hálfan", HALF_PATTERN));
        HALF_ZIP.add(new Tuple(NumberPatterns.HALFUM, " hálfum", HALF_PATTERN));
        HALF_ZIP.add(new Tuple(NumberPatterns.HALFS, " hálfs", HALF_PATTERN));
        HALF_ZIP.add(new Tuple(NumberPatterns.HALF, " hálf", HALF_PATTERN));
        HALF_ZIP.add(new Tuple(NumberPatterns.HALFA, " hálfa", HALF_PATTERN));
        HALF_ZIP.add(new Tuple(NumberPatterns.HALFRI, " hálfri", HALF_PATTERN));
        HALF_ZIP.add(new Tuple(NumberPatterns.HALFRAR, " hálfrar", HALF_PATTERN));
        HALF_ZIP.add(new Tuple(NumberPatterns.HALFT, " hálft", HALF_PATTERN));
        HALF_ZIP.add(new Tuple(NumberPatterns.HALFU, " hálfu", HALF_PATTERN));
        HALF_ZIP.add(new Tuple(NumberPatterns.HALFIR, " hálfir", HALF_PATTERN));
        HALF_ZIP.add(new Tuple(NumberPatterns.HALFRA, " hálfra", HALF_PATTERN));
        HALF_ZIP.add(new Tuple(NumberPatterns.NO_NOUN, " hálfur", HALF_PATTERN));

        HALF_ZIP.add(new Tuple(NumberPatterns.FRACTIONNOM, " einn þriðji", THIRD_PATTERN));
        HALF_ZIP.add(new Tuple(NumberPatterns.FRACTIONDAT, " einn þriðja", THIRD_PATTERN)); //TODO: isn't this wrong? acc and dat switched? Same in following blocks
        HALF_ZIP.add(new Tuple(NumberPatterns.FRACTIONACC, " einum þriðja", THIRD_PATTERN));
        HALF_ZIP.add(new Tuple(NumberPatterns.FRACTIONGEN, " eins þriðja", THIRD_PATTERN));
        HALF_ZIP.add(new Tuple(NumberPatterns.NO_NOUN, " einn þriðji", THIRD_PATTERN));

        HALF_ZIP.add(new Tuple(NumberPatterns.FRACTIONNOM, " einn fjórði", FOURTH_PATTERN));
        HALF_ZIP.add(new Tuple(NumberPatterns.FRACTIONDAT, " einn fjórða", FOURTH_PATTERN));
        HALF_ZIP.add(new Tuple(NumberPatterns.FRACTIONACC, " einum fjórða", FOURTH_PATTERN));
        HALF_ZIP.add(new Tuple(NumberPatterns.FRACTIONGEN, " eins fjórða", FOURTH_PATTERN));
        HALF_ZIP.add(new Tuple(NumberPatterns.NO_NOUN, " einn fjórði", FOURTH_PATTERN));

        HALF_ZIP.add(new Tuple(NumberPatterns.FRACTIONNOM, " tveir þriðju", TWO_THIRD_PATTERN));
        HALF_ZIP.add(new Tuple(NumberPatterns.FRACTIONDAT, " tvo þriðju", TWO_THIRD_PATTERN));
        HALF_ZIP.add(new Tuple(NumberPatterns.FRACTIONACC, " tveimur þriðju", TWO_THIRD_PATTERN));
        HALF_ZIP.add(new Tuple(NumberPatterns.FRACTIONGEN, " tveggja þriðju", TWO_THIRD_PATTERN));
        HALF_ZIP.add(new Tuple(NumberPatterns.NO_NOUN, " tveir þriðju", TWO_THIRD_PATTERN));

        HALF_ZIP.add(new Tuple(NumberPatterns.FRACTIONNOM, " þrír fjórðu", THREE_FOURTH_PATTERN));
        HALF_ZIP.add(new Tuple(NumberPatterns.FRACTIONDAT, " þrjá fjórðu", THREE_FOURTH_PATTERN));
        HALF_ZIP.add(new Tuple(NumberPatterns.FRACTIONACC, " þremur fjórðu", THREE_FOURTH_PATTERN));
        HALF_ZIP.add(new Tuple(NumberPatterns.FRACTIONGEN, " þriggja fjórðu", THREE_FOURTH_PATTERN));
        HALF_ZIP.add(new Tuple(NumberPatterns.NO_NOUN, " þrír fjórðu", THREE_FOURTH_PATTERN));
    }

    public static final List<Tuple> TWO_ORDINAL_ZIP = new ArrayList<>();
    static {
        TWO_ORDINAL_ZIP.add(new Tuple(NumberPatterns.ANNAR, " annar", ""));
        TWO_ORDINAL_ZIP.add(new Tuple(NumberPatterns.ANNAN, " annan", ""));
        TWO_ORDINAL_ZIP.add(new Tuple(NumberPatterns.ODRUM, " öðrum", ""));
        TWO_ORDINAL_ZIP.add(new Tuple(NumberPatterns.ANNARS, " annars", ""));
        TWO_ORDINAL_ZIP.add(new Tuple(NumberPatterns.ADRIR, " aðrir", ""));
        TWO_ORDINAL_ZIP.add(new Tuple(NumberPatterns.ADRA, " aðra", ""));
        TWO_ORDINAL_ZIP.add(new Tuple(NumberPatterns.ANNARRA, " annarra", ""));
        TWO_ORDINAL_ZIP.add(new Tuple(NumberPatterns.ONNUR, " önnur", ""));
        TWO_ORDINAL_ZIP.add(new Tuple(NumberPatterns.ANNARRI, " annarri", ""));
        TWO_ORDINAL_ZIP.add(new Tuple(NumberPatterns.ANNARRAR, " annarrar", ""));
        TWO_ORDINAL_ZIP.add(new Tuple(NumberPatterns.ADRAR, " aðrar", ""));
        TWO_ORDINAL_ZIP.add(new Tuple(NumberPatterns.ANNAD, " annað", ""));
        TWO_ORDINAL_ZIP.add(new Tuple(NumberPatterns.ODRU, " öðru", ""));
        TWO_ORDINAL_ZIP.add(new Tuple(NumberPatterns.NO_NOUN, " annan", ""));
    }

    public static final List<Tuple> ORDINALS_ONES_ZIP = new ArrayList<>();
    static {
        ORDINALS_ONES_ZIP.add(new Tuple(ONES, " fyrst", "1")); // note: different order than in original Regina: fyrst, 1, ones
        ORDINALS_ONES_ZIP.add(new Tuple(ONES, " þriðj", "3"));
        ORDINALS_ONES_ZIP.add(new Tuple(ONES, " fjórð", "4"));
        ORDINALS_ONES_ZIP.add(new Tuple(ONES, " fimmt", "5"));
        ORDINALS_ONES_ZIP.add(new Tuple(ONES, " sjött", "6"));
        ORDINALS_ONES_ZIP.add(new Tuple(ONES, " sjöund", "7"));
        ORDINALS_ONES_ZIP.add(new Tuple(ONES, " áttund", "8"));
        ORDINALS_ONES_ZIP.add(new Tuple(ONES, " níund", "9"));
        ORDINALS_ONES_ZIP.add(new Tuple(DOZENS, " tíund", "10"));
        ORDINALS_ONES_ZIP.add(new Tuple(DOZENS, " elleft", "11"));
        ORDINALS_ONES_ZIP.add(new Tuple(DOZENS, " tólft", "12"));
        ORDINALS_ONES_ZIP.add(new Tuple(DOZENS, " þrettánd", "13"));
        ORDINALS_ONES_ZIP.add(new Tuple(DOZENS, " fjórtánd", "14"));
        ORDINALS_ONES_ZIP.add(new Tuple(DOZENS, " fimmtánd", "15"));
        ORDINALS_ONES_ZIP.add(new Tuple(DOZENS, " sextánd", "16"));
        ORDINALS_ONES_ZIP.add(new Tuple(DOZENS, " sautjánd", "17"));
        ORDINALS_ONES_ZIP.add(new Tuple(DOZENS, " átjánd", "18"));
        ORDINALS_ONES_ZIP.add(new Tuple(DOZENS, " nítjánd", "19"));
    }
    public static final List<Tuple> ORDINAL_LETTERS = new ArrayList<>();
    static {
        ORDINAL_LETTERS.add(new Tuple(NumberPatterns.FYRSTI, "i", ""));
        ORDINAL_LETTERS.add(new Tuple(NumberPatterns.FYRSTA, "a", ""));
        ORDINAL_LETTERS.add(new Tuple(NumberPatterns.FYRSTU, "u", ""));
        ORDINAL_LETTERS.add(new Tuple(NumberPatterns.NO_NOUN, "a", ""));
    }
    public static final List<Tuple> DOZENS_ORDINAL_ZIP = new ArrayList<>();
    static {
        DOZENS_ORDINAL_ZIP.add(new Tuple("", " tuttug", "2"));
        DOZENS_ORDINAL_ZIP.add(new Tuple("", " þrítug", "3"));
        DOZENS_ORDINAL_ZIP.add(new Tuple("", " fertug", "4"));
        DOZENS_ORDINAL_ZIP.add(new Tuple("", " fimmtug", "5"));
        DOZENS_ORDINAL_ZIP.add(new Tuple("", " sextug", "6"));
        DOZENS_ORDINAL_ZIP.add(new Tuple("", " sjötug", "7"));
        DOZENS_ORDINAL_ZIP.add(new Tuple("", " áttug", "8"));
        DOZENS_ORDINAL_ZIP.add(new Tuple("", " nítug", "9"));
    }
    public static final List<Tuple> DOZENS_ORDINAL_LETTERS = new ArrayList<>();
    static {
        DOZENS_ORDINAL_LETTERS.add(new Tuple(NumberPatterns.FYRSTI, "asti", ""));
        DOZENS_ORDINAL_LETTERS.add(new Tuple(NumberPatterns.FYRSTA, "asta", ""));
        DOZENS_ORDINAL_LETTERS.add(new Tuple(NumberPatterns.FYRSTU, "ustu", ""));
        DOZENS_ORDINAL_LETTERS.add(new Tuple(NumberPatterns.NO_NOUN, "asta", ""));
    }
    public static final List<Tuple> HUNDRENDS_THOUSANDS_ZIP = new ArrayList<>();
    static {
        HUNDRENDS_THOUSANDS_ZIP.add(new Tuple("", " tvö", "2"));
        HUNDRENDS_THOUSANDS_ZIP.add(new Tuple("", " þrjú", "3"));
        HUNDRENDS_THOUSANDS_ZIP.add(new Tuple("", " fjögur", "4"));
        HUNDRENDS_THOUSANDS_ZIP.add(new Tuple("", " fimm", "5"));
        HUNDRENDS_THOUSANDS_ZIP.add(new Tuple("", " sex", "6"));
        HUNDRENDS_THOUSANDS_ZIP.add(new Tuple("", " sjö", "7"));
        HUNDRENDS_THOUSANDS_ZIP.add(new Tuple("", " átta", "8"));
        HUNDRENDS_THOUSANDS_ZIP.add(new Tuple("", " níu", "9"));
    }
    public static final List<Tuple> MILLIONS_ZIP = new ArrayList<>();
    static {
        MILLIONS_ZIP.add(new Tuple("", " tvær", "2"));
        MILLIONS_ZIP.add(new Tuple("", " þrjár", "3"));
        MILLIONS_ZIP.add(new Tuple("", " fjórar", "4"));
        MILLIONS_ZIP.add(new Tuple("", " fimm", "5"));
        MILLIONS_ZIP.add(new Tuple("", " sex", "6"));
        MILLIONS_ZIP.add(new Tuple("", " sjö", "7"));
        MILLIONS_ZIP.add(new Tuple("", " átta", "8"));
        MILLIONS_ZIP.add(new Tuple("", " níu", "9"));
    }
    public static final List<Tuple> BILLIONS_ZIP = new ArrayList<>();
    static {
        BILLIONS_ZIP.add(new Tuple("", " tveir", "2"));
        BILLIONS_ZIP.add(new Tuple("", " tveir", "3"));
        BILLIONS_ZIP.add(new Tuple("", " tveir", "4"));
        BILLIONS_ZIP.add(new Tuple("", " tveir", "5"));
        BILLIONS_ZIP.add(new Tuple("", " tveir", "6"));
        BILLIONS_ZIP.add(new Tuple("", " tveir", "7"));
        BILLIONS_ZIP.add(new Tuple("", " tveir", "8"));
        BILLIONS_ZIP.add(new Tuple("", " tveir", "9"));
    }
    public static final List<Tuple> MB_ORDINAL_ZIP = new ArrayList<>();
    static {
        MB_ORDINAL_ZIP.add(new Tuple("", " tví", "2"));
        MB_ORDINAL_ZIP.add(new Tuple("", " þrí", "3"));
        MB_ORDINAL_ZIP.add(new Tuple("", " fer", "4"));
        MB_ORDINAL_ZIP.add(new Tuple("", " fimm", "5"));
        MB_ORDINAL_ZIP.add(new Tuple("", " sex", "6"));
        MB_ORDINAL_ZIP.add(new Tuple("", " sjö", "7"));
        MB_ORDINAL_ZIP.add(new Tuple("", " átt", "8"));
        MB_ORDINAL_ZIP.add(new Tuple("", " ní", "9"));
    }
    public static final List<Tuple> ONES_ZIP_TIME = new ArrayList<>();
    static {
        ONES_ZIP_TIME.add(new Tuple("", " eitt", "1"));
        ONES_ZIP_TIME.add(new Tuple("", " tvö", "1"));
        ONES_ZIP_TIME.add(new Tuple("", " þrjú", "1"));
        ONES_ZIP_TIME.add(new Tuple("", " fjögur", "1"));
        ONES_ZIP_TIME.add(new Tuple("", " fimm", "1"));
        ONES_ZIP_TIME.add(new Tuple("", " sex", "1"));
        ONES_ZIP_TIME.add(new Tuple("", " sjö", "1"));
        ONES_ZIP_TIME.add(new Tuple("", " átta", "1"));
        ONES_ZIP_TIME.add(new Tuple("", " níu", "1"));
    }
    public static final List<Tuple> DOZENS_ZIP = new ArrayList<>();
    static {
        DOZENS_ZIP.add(new Tuple("", " tuttugu", "2"));
        DOZENS_ZIP.add(new Tuple("", " þrjátíu", "3"));
        DOZENS_ZIP.add(new Tuple("", " fjörutíu", "4"));
        DOZENS_ZIP.add(new Tuple("", " fimmtíu", "5"));
        DOZENS_ZIP.add(new Tuple("", " sextíu", "6"));
        DOZENS_ZIP.add(new Tuple("", " sjötíu", "7"));
        DOZENS_ZIP.add(new Tuple("", " áttatíu", "8"));
        DOZENS_ZIP.add(new Tuple("", " níutíu", "9"));
    }

}

