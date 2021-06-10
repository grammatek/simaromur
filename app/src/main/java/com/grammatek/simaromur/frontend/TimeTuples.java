package com.grammatek.simaromur.frontend;

import java.util.ArrayList;
import java.util.List;

public class TimeTuples {

    public static final List<CategoryTuple> TUPLES = new ArrayList<>();

    public static List<CategoryTuple> getTuples() {
        if (!TUPLES.isEmpty())
            return TUPLES;

        TUPLES.add(new CategoryTuple("^0\\d[:\\.][0-5]\\d$", NormalizationDictionaries.MATCH_ANY,
                NumberHelper.FIRST_TEN, DecimalThousandTuples.ZERO));
        TUPLES.add(new CategoryTuple("^00[:\\.][0-5]\\d$", NormalizationDictionaries.MATCH_ANY,
                NumberHelper.FIRST_ONE, DecimalThousandTuples.ZERO));
        TUPLES.add(new CategoryTuple("^([01]?[0-9]|2[0-4])[:\\.]0\\d$", NormalizationDictionaries.MATCH_ANY,
                NumberHelper.SECOND_TEN, DecimalThousandTuples.ZERO));
        TUPLES.add(new CategoryTuple("^([01]?[0-9]|2[0-4])[:\\.]00$", NormalizationDictionaries.MATCH_ANY,
                NumberHelper.SECOND_ONE, DecimalThousandTuples.ZERO));

        for (ExpansionTuple tuple : TupleRules.ONES_ZIP_TIME) {
            TUPLES.add(new CategoryTuple("^[02]?" + tuple.getDigit() + "[:\\.][0-5]\\d$", NormalizationDictionaries.MATCH_ANY,
                    NumberHelper.FIRST_ONE, tuple.getNumberWord()));
            TUPLES.add(new CategoryTuple("^([01]?[0-9]|2[0-4])[:\\.][02-5]" + tuple.getDigit() + "$", NormalizationDictionaries.MATCH_ANY,
                    NumberHelper.SECOND_ONE, tuple.getNumberWord()));
            TUPLES.add(new CategoryTuple("^0" + tuple.getDigit() + "$", NormalizationDictionaries.MATCH_ANY,
                    NumberHelper.SECOND_ONE, DecimalThousandTuples.ZERO + " " + tuple.getNumberWord()));
        }

        for (ExpansionTuple tuple : TupleRules.TENS_ZIP) {
            TUPLES.add(new CategoryTuple("^" + tuple.getDigit() + "[:\\.][0-5]\\d$", NormalizationDictionaries.MATCH_ANY,
                    NumberHelper.FIRST_TEN, tuple.getNumberWord()));
            TUPLES.add(new CategoryTuple("^([01]?[0-9]|2[0-4])[:\\.]" + tuple.getDigit() + "$", NormalizationDictionaries.MATCH_ANY,
                    NumberHelper.SECOND_TEN, tuple.getNumberWord()));
            TUPLES.add(new CategoryTuple("^2[1-4][:\\.][0-5]\\d$", NormalizationDictionaries.MATCH_ANY,
                    NumberHelper.FIRST_TEN, "tuttugu og"));
        }
        TUPLES.add(new CategoryTuple("^20[:\\.][0-5]\\d$", NormalizationDictionaries.MATCH_ANY,
                NumberHelper.FIRST_TEN, "tuttugu"));
        TUPLES.add(new CategoryTuple("^([01]?[0-9]|2[0-4])[:\\.]20$", NormalizationDictionaries.MATCH_ANY,
                NumberHelper.SECOND_TEN, "tuttugu"));

        for (ExpansionTuple tuple : TupleRules.DOZENS_ZIP) {
            TUPLES.add(new CategoryTuple("^([01]?\\d|2[0-4])[:\\.]" + tuple.getDigit() + "0$", NormalizationDictionaries.MATCH_ANY,
                    NumberHelper.SECOND_TEN, tuple.getNumberWord()));
            TUPLES.add(new CategoryTuple("^([01]?\\d|2[0-4])[:\\.]" + tuple.getDigit() + "[1-9]$", NormalizationDictionaries.MATCH_ANY,
                    NumberHelper.SECOND_TEN, tuple.getNumberWord() + " og"));
        }
        return TUPLES;
    }
}

