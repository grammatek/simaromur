package com.grammatek.simaromur.frontend;

import java.util.ArrayList;
import java.util.List;

public class CardinalOnesTuples {

    public static final List<OrdinalTuple> TUPLES = new ArrayList<>();

    public static List<OrdinalTuple> getTuples() {
        if (!TUPLES.isEmpty())
            return TUPLES;

        // TODO: is this correct? regina shows only a tuple of three for this first entry, all other are tuples of four
        TUPLES.add(new OrdinalTuple("^0(,\\d+)?$", "", NumberHelper.ONES, "núll"));

        for (Tuple tuple : TupleRules.ONES_ZIP) {
            TUPLES.add(new OrdinalTuple((NumberPatterns.ONES_PTRN_NO_11 + tuple.getDigit() + NumberPatterns.DEC_PTRN),
                    tuple.getNumberPattern(), NumberHelper.ONES, tuple.getNumberWord()));
        }
        for (Tuple tuple : TupleRules.TENS_ZIP) {
            TUPLES.add(new OrdinalTuple((NumberPatterns.TNS_PTRN + tuple.getDigit() + NumberPatterns.DEC_PTRN),
                    ".*", NumberHelper.DOZENS, tuple.getNumberWord()));
        }

        return TUPLES;
    }
}
