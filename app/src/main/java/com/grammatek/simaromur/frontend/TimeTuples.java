package com.grammatek.simaromur.frontend;

import java.util.ArrayList;
import java.util.List;

public class TimeTuples {

    public static final List<OrdinalTuple> TUPLES = new ArrayList<>();

    public static List<OrdinalTuple> getTuples() {
        if (!TUPLES.isEmpty())
            return TUPLES;

        TUPLES.add(new OrdinalTuple("^0\\d[:\\.][0-5]\\d$", NormalizationDictionaries.MATCH_ANY,
                NumberHelper.FIRST_TEN, DecimalThousandTuples.ZERO));
        TUPLES.add(new OrdinalTuple("^00[:\\.][0-5]\\d$", NormalizationDictionaries.MATCH_ANY,
                NumberHelper.FIRST_ONE, DecimalThousandTuples.ZERO));
        TUPLES.add(new OrdinalTuple("^([01]?[0-9]|2[0-4])[:\\.]0\\d$", NormalizationDictionaries.MATCH_ANY,
                NumberHelper.SECOND_TEN, DecimalThousandTuples.ZERO));
        TUPLES.add(new OrdinalTuple("^([01]?[0-9]|2[0-4])[:\\.]00$", NormalizationDictionaries.MATCH_ANY,
                NumberHelper.SECOND_ONE, DecimalThousandTuples.ZERO));

        for (Tuple tuple : TupleRules.ONES_ZIP_TIME) {
            TUPLES.add(new OrdinalTuple("^[02]?" + tuple.getDigit() + "[:\\.][0-5]\\d$", NormalizationDictionaries.MATCH_ANY,
                    NumberHelper.FIRST_ONE, tuple.getNumberWord()));
            TUPLES.add(new OrdinalTuple("^([01]?[0-9]|2[0-4])[:\\.][02-5]" + tuple.getDigit() + "$", NormalizationDictionaries.MATCH_ANY,
                    NumberHelper.SECOND_ONE, tuple.getNumberWord()));
            TUPLES.add(new OrdinalTuple("^0" + tuple.getDigit() + "$", NormalizationDictionaries.MATCH_ANY,
                    NumberHelper.SECOND_ONE, DecimalThousandTuples.ZERO + " " + tuple.getNumberWord()));
        }

        for (Tuple tuple : TupleRules.TENS_ZIP) {
            TUPLES.add(new OrdinalTuple("^" + tuple.getDigit() + "[:\\.][0-5]\\d$", NormalizationDictionaries.MATCH_ANY,
                    NumberHelper.FIRST_TEN, tuple.getNumberWord()));
            TUPLES.add(new OrdinalTuple("^([01]?[0-9]|2[0-4])[:\\.]" + tuple.getDigit() + "$", NormalizationDictionaries.MATCH_ANY,
                    NumberHelper.SECOND_TEN, tuple.getNumberWord()));
            TUPLES.add(new OrdinalTuple("^2[1-4][:\\.][0-5]\\d$", NormalizationDictionaries.MATCH_ANY,
                    NumberHelper.FIRST_TEN, "tuttugu og"));
        }
        TUPLES.add(new OrdinalTuple("^20[:\\.][0-5]\\d$", NormalizationDictionaries.MATCH_ANY,
                NumberHelper.FIRST_TEN, "tuttugu"));
        TUPLES.add(new OrdinalTuple("^([01]?[0-9]|2[0-4])[:\\.]20$", NormalizationDictionaries.MATCH_ANY,
                NumberHelper.SECOND_TEN, "tuttugu"));

        for (Tuple tuple : TupleRules.DOZENS_ZIP) {
            TUPLES.add(new OrdinalTuple("^([01]?\\d|2[0-4])[:\\.]" + tuple.getDigit() + "0$", NormalizationDictionaries.MATCH_ANY,
                    NumberHelper.SECOND_TEN, tuple.getNumberWord()));
            TUPLES.add(new OrdinalTuple("^([01]?\\d|2[0-4])[:\\.]" + tuple.getDigit() + "[1-9]$", NormalizationDictionaries.MATCH_ANY,
                    NumberHelper.SECOND_TEN, tuple.getNumberWord() + " og"));
        }
        return TUPLES;
    }
}

/*
time_tuples = [

for string, number in dozens_zip[:4]:
    time_tuples.append(("^([01]?\d|2[0-4])[:\.]" + number + "0$", '.*', 'second_ten', string))
    time_tuples.append(("^([01]?\d|2[0-4])[:\.]" + number + "[1-9]$", '.*', 'second_ten', string + " og"))

 */

