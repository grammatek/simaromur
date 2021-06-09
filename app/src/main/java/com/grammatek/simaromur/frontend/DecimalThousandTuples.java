package com.grammatek.simaromur.frontend;

import java.util.ArrayList;
import java.util.List;

public class DecimalThousandTuples {

    public static final String ANYTHING = ".*"; //TODO: don't we have this in another class somewhere?
    public static final String ZERO = " núll";

    public static final List<OrdinalTuple> TUPLES = new ArrayList<>();

    public static List<OrdinalTuple> getTuples() {
        if (!TUPLES.isEmpty())
            return TUPLES;

        TUPLES.add(new OrdinalTuple(NumberPatterns.ZEROPNT_PTRN + "\\d{9}0\\d*$", ANYTHING, NumberHelper.P10, ZERO));
        TUPLES.add(new OrdinalTuple(NumberPatterns.ZEROPNT_PTRN + "\\d{8}0\\d*$", ANYTHING, NumberHelper.P9, ZERO));
        TUPLES.add(new OrdinalTuple(NumberPatterns.ZEROPNT_PTRN + "\\d{7}0\\d*$", ANYTHING, NumberHelper.P8, ZERO));
        TUPLES.add(new OrdinalTuple(NumberPatterns.ZEROPNT_PTRN + "\\d{6}0\\d*$", ANYTHING, NumberHelper.P7, ZERO));
        TUPLES.add(new OrdinalTuple(NumberPatterns.ZEROPNT_PTRN + "\\d{5}0\\d*$", ANYTHING, NumberHelper.P6, ZERO));
        TUPLES.add(new OrdinalTuple(NumberPatterns.ZEROPNT_PTRN + "\\d{4}0\\d*$", ANYTHING, NumberHelper.P5, ZERO));
        TUPLES.add(new OrdinalTuple(NumberPatterns.ZEROPNT_PTRN + "\\d{3}0\\d*$", ANYTHING, NumberHelper.P4, ZERO));
        TUPLES.add(new OrdinalTuple(NumberPatterns.ZEROPNT_PTRN + "\\d{2}0\\d*$", ANYTHING, NumberHelper.P3, ZERO));
        TUPLES.add(new OrdinalTuple(NumberPatterns.ZEROPNT_PTRN + "0\\d*$", ANYTHING, NumberHelper.POINTS, " komma " + ZERO));


        for (Tuple tuple : TupleRules.ONES_ZIP) {
            TUPLES.add(new OrdinalTuple(NumberPatterns.ZEROPNT_PTRN + tuple.getDigit() + "\\d*$", tuple.getNumberPattern(),
                    NumberHelper.POINTS, " komma" + tuple.getNumberWord()));
            TUPLES.add(new OrdinalTuple(NumberPatterns.ZEROPNT_PTRN + "\\d" + tuple.getDigit() + "\\d*$", tuple.getNumberPattern(),
                    NumberHelper.P2, tuple.getNumberWord()));
            TUPLES.add(new OrdinalTuple(NumberPatterns.ZEROPNT_PTRN + "\\d{2}" + tuple.getDigit() + "\\d*$", tuple.getNumberPattern(),
                    NumberHelper.P3, tuple.getNumberWord()));
            TUPLES.add(new OrdinalTuple(NumberPatterns.ZEROPNT_PTRN + "\\d{3}" + tuple.getDigit() + "\\d*$", tuple.getNumberPattern(),
                    NumberHelper.P4, tuple.getNumberWord()));
            TUPLES.add(new OrdinalTuple(NumberPatterns.ZEROPNT_PTRN + "\\d{4}" + tuple.getDigit() + "\\d*$", tuple.getNumberPattern(),
                    NumberHelper.P5, tuple.getNumberWord()));
            TUPLES.add(new OrdinalTuple(NumberPatterns.ZEROPNT_PTRN + "\\d{5}" + tuple.getDigit() + "\\d*$", tuple.getNumberPattern(),
                    NumberHelper.P6, tuple.getNumberWord()));
            TUPLES.add(new OrdinalTuple(NumberPatterns.ZEROPNT_PTRN + "\\d{6}" + tuple.getDigit() + "\\d*$", tuple.getNumberPattern(),
                    NumberHelper.P7, tuple.getNumberWord()));
            TUPLES.add(new OrdinalTuple(NumberPatterns.ZEROPNT_PTRN + "\\d{7}" + tuple.getDigit() + "\\d*$", tuple.getNumberPattern(),
                    NumberHelper.P8, tuple.getNumberWord()));
            TUPLES.add(new OrdinalTuple(NumberPatterns.ZEROPNT_PTRN + "\\d{8}" + tuple.getDigit() + "\\d*$", tuple.getNumberPattern(),
                    NumberHelper.P9, tuple.getNumberWord()));
            TUPLES.add(new OrdinalTuple(NumberPatterns.ZEROPNT_PTRN + "\\d{9}" + tuple.getDigit() + "\\d*$", tuple.getNumberPattern(),
                    NumberHelper.P10, tuple.getNumberWord()));
        }

        for (Tuple tuple : TupleRules.DEC_ONES_MALE) {
            TUPLES.add(new OrdinalTuple(NumberPatterns.ONES_PTRN_NO_11 + tuple.getDigit() + NumberPatterns.DEC_PTRN_DEF,
                    tuple.getNumberPattern(), NumberHelper.ONES, tuple.getNumberWord()));
        }

        return TUPLES;

    }
}
