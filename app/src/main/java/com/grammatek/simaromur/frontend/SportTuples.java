package com.grammatek.simaromur.frontend;

import java.util.ArrayList;
import java.util.List;

public class SportTuples {

    public static final List<OrdinalTuple> TUPLES = new ArrayList<>();

    public static List<OrdinalTuple> getTuples() {
        if (!TUPLES.isEmpty())
            return TUPLES;

        TUPLES.add(new OrdinalTuple("^[1-9]\\d?\\/[1-9]?\\d$", NormalizationDictionaries.MATCH_ANY,
                NumberHelper.BETWEEN_TEAMS, " <sil>")); //TODO: move <sil> to a central class as a constant

        for (Tuple tuple : TupleRules.ONES_ZIP) {
            TUPLES.add(new OrdinalTuple("^[1-9]?" + tuple.getDigit() + "\\/[1-9]\\d?$", tuple.getNumberPattern(),
                    NumberHelper.FIRST_ONE, tuple.getNumberWord()));
            TUPLES.add(new OrdinalTuple("^[1-9]\\d?\\/[1-9]?" + tuple.getDigit() + "$", tuple.getNumberPattern(),
                    NumberHelper.SECOND_ONE, tuple.getNumberWord()));
        }

        for (Tuple tuple : TupleRules.TENS_ZIP) {
            TUPLES.add(new OrdinalTuple("^" + tuple.getDigit() + "\\/[1-9]\\d?$", NormalizationDictionaries.MATCH_ANY,
                    NumberHelper.FIRST_ONE, tuple.getNumberWord()));
            TUPLES.add(new OrdinalTuple("^[1-9]\\d?\\/" + tuple.getDigit(), NormalizationDictionaries.MATCH_ANY,
                    NumberHelper.SECOND_ONE, tuple.getNumberWord()));
        }

        for (Tuple tuple : TupleRules.DOZENS_ZIP) {
            TUPLES.add(new OrdinalTuple("^" + tuple.getDigit() + "0\\/[1-9]\\d?$", NormalizationDictionaries.MATCH_ANY,
                    NumberHelper.FIRST_TEN, tuple.getNumberWord()));
            TUPLES.add(new OrdinalTuple("^[1-9]\\d?\\/" + tuple.getDigit() + "0$", NormalizationDictionaries.MATCH_ANY,
                    NumberHelper.SECOND_TEN, tuple.getNumberWord()));
            TUPLES.add(new OrdinalTuple("^" + tuple.getDigit() + "[1-9]\\/[1-9]\\d?$", NormalizationDictionaries.MATCH_ANY,
                    NumberHelper.FIRST_TEN, tuple.getNumberWord() + " og"));
            TUPLES.add(new OrdinalTuple("^[1-9]\\d?\\/" + tuple.getDigit() + "[1-9]$", NormalizationDictionaries.MATCH_ANY,
                    NumberHelper.SECOND_TEN, tuple.getNumberWord() + " og"));
        }

        return TUPLES;
    }
}
