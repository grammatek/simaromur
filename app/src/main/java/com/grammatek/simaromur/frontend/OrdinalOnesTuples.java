package com.grammatek.simaromur.frontend;

import java.util.ArrayList;
import java.util.List;

/**
 * A list of CategoryTuples to deal with ordinals in the "ones" and "dozens" positions
 */
public class OrdinalOnesTuples {

    public static final List<CategoryTuple> TUPLES = new ArrayList<>();

    public static List<CategoryTuple> getTuples() {
        if (!TUPLES.isEmpty())
            return TUPLES;

        for (ExpansionTuple tuple : TupleRules.TWO_ORDINAL_ZIP) {
            TUPLES.add(new CategoryTuple(NumberPatterns.ONES_PTRN_NO_11 + "2\\.$", tuple.getPosPattern(),
                    NumberHelper.ONES, tuple.getNumberWord()));
        }
        for (ExpansionTuple tuple : TupleRules.ORDINAL_LETTERS) {
            TUPLES.add(new CategoryTuple("^\\.$", tuple.getPosPattern(), NumberHelper.ONES,
                    "núllt" + tuple.getNumberWord()));
            for (ExpansionTuple t : TupleRules.ORDINALS_ONES_ZIP) {
                TUPLES.add(new CategoryTuple(NumberPatterns.ONES_PTRN_NO_11 + t.getDigit() + "\\.$",
                        tuple.getPosPattern(), t.getPosPattern(), t.getNumberWord() + tuple.getNumberWord()));
            }
        }
        for (ExpansionTuple tuple : TupleRules.DOZENS_ORDINAL_LETTERS) {
            for (ExpansionTuple t : TupleRules.DOZENS_ORDINAL_ZIP) {
                TUPLES.add(new CategoryTuple(NumberPatterns.TNS_PTRN + t.getDigit() + "0\\.$",
                        tuple.getPosPattern(), NumberHelper.DOZENS, t.getNumberWord() + tuple.getNumberWord()));
                TUPLES.add(new CategoryTuple(NumberPatterns.TNS_PTRN + t.getDigit() + "[1-9]\\.$",
                        tuple.getPosPattern(), NumberHelper.DOZENS, t.getNumberWord() + tuple.getNumberWord() + " og"));
            }
        }
        return TUPLES;
    }
}

