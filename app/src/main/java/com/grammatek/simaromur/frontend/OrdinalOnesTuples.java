package com.grammatek.simaromur.frontend;

import java.util.ArrayList;
import java.util.List;

public class OrdinalOnesTuples {

    public static final List<OrdinalTuple> TUPLES = new ArrayList<>();

    public static List<OrdinalTuple> getTuples() {
        if (!TUPLES.isEmpty())
            return TUPLES;

        for (Tuple tuple : TupleRules.TWO_ORDINAL_ZIP) {
            TUPLES.add(new OrdinalTuple(NumberPatterns.ONES_PTRN_NO_11 + "2\\.$", tuple.getNumberPattern(),
                    NumberHelper.ONES, tuple.getNumberWord()));
        }
        for (Tuple tuple : TupleRules.ORDINAL_LETTERS) {
            TUPLES.add(new OrdinalTuple("^\\.$", tuple.getNumberPattern(), NumberHelper.ONES,
                    "núllt" + tuple.getNumberWord()));
            for (Tuple t : TupleRules.ORDINALS_ONES_ZIP) {
                TUPLES.add(new OrdinalTuple(NumberPatterns.ONES_PTRN_NO_11 + t.getDigit() + "\\.$",
                        tuple.getNumberPattern(), t.getNumberPattern(), t.getNumberWord() + tuple.getNumberWord()));
            }
        }
        for (Tuple tuple : TupleRules.DOZENS_ORDINAL_LETTERS) {
            for (Tuple t : TupleRules.DOZENS_ORDINAL_ZIP) {
                TUPLES.add(new OrdinalTuple(NumberPatterns.TNS_PTRN + t.getDigit() + "0\\.$",
                        tuple.getNumberPattern(), NumberHelper.DOZENS, t.getNumberWord() + tuple.getNumberWord()));
                TUPLES.add(new OrdinalTuple(NumberPatterns.TNS_PTRN + t.getDigit() + "[1-9]\\.$",
                        tuple.getNumberPattern(), NumberHelper.DOZENS, t.getNumberWord() + tuple.getNumberWord() + " og"));
            }
        }
        return TUPLES;
    }
}

