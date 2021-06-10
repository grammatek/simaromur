package com.grammatek.simaromur.frontend;

import java.util.ArrayList;
import java.util.List;

/**
 * A list of category tuples to deal with sport results
 */
public class SportTuples {

    public static final List<CategoryTuple> TUPLES = new ArrayList<>();

    public static List<CategoryTuple> getTuples() {
        if (!TUPLES.isEmpty())
            return TUPLES;

        TUPLES.add(new CategoryTuple("^[1-9]\\d?\\/[1-9]?\\d$", NormalizationDictionaries.MATCH_ANY,
                NumberHelper.BETWEEN_TEAMS, " <sil>")); //TODO: move <sil> to a central class as a constant

        for (ExpansionTuple tuple : TupleRules.ONES_ZIP) {
            TUPLES.add(new CategoryTuple("^[1-9]?" + tuple.getDigit() + "\\/[1-9]\\d?$", tuple.getPosPattern(),
                    NumberHelper.FIRST_ONE, tuple.getNumberWord()));
            TUPLES.add(new CategoryTuple("^[1-9]\\d?\\/[1-9]?" + tuple.getDigit() + "$", tuple.getPosPattern(),
                    NumberHelper.SECOND_ONE, tuple.getNumberWord()));
        }

        for (ExpansionTuple tuple : TupleRules.TENS_ZIP) {
            TUPLES.add(new CategoryTuple("^" + tuple.getDigit() + "\\/[1-9]\\d?$", NormalizationDictionaries.MATCH_ANY,
                    NumberHelper.FIRST_ONE, tuple.getNumberWord()));
            TUPLES.add(new CategoryTuple("^[1-9]\\d?\\/" + tuple.getDigit(), NormalizationDictionaries.MATCH_ANY,
                    NumberHelper.SECOND_ONE, tuple.getNumberWord()));
        }

        for (ExpansionTuple tuple : TupleRules.DOZENS_ZIP) {
            TUPLES.add(new CategoryTuple("^" + tuple.getDigit() + "0\\/[1-9]\\d?$", NormalizationDictionaries.MATCH_ANY,
                    NumberHelper.FIRST_TEN, tuple.getNumberWord()));
            TUPLES.add(new CategoryTuple("^[1-9]\\d?\\/" + tuple.getDigit() + "0$", NormalizationDictionaries.MATCH_ANY,
                    NumberHelper.SECOND_TEN, tuple.getNumberWord()));
            TUPLES.add(new CategoryTuple("^" + tuple.getDigit() + "[1-9]\\/[1-9]\\d?$", NormalizationDictionaries.MATCH_ANY,
                    NumberHelper.FIRST_TEN, tuple.getNumberWord() + " og"));
            TUPLES.add(new CategoryTuple("^[1-9]\\d?\\/" + tuple.getDigit() + "[1-9]$", NormalizationDictionaries.MATCH_ANY,
                    NumberHelper.SECOND_TEN, tuple.getNumberWord() + " og"));
        }

        return TUPLES;
    }
}
