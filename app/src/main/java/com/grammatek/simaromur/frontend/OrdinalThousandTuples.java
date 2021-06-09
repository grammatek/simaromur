package com.grammatek.simaromur.frontend;

import java.util.ArrayList;
import java.util.List;

public class OrdinalThousandTuples {

    public static final List<OrdinalTuple> TUPLES = new ArrayList<>();

    public static List<OrdinalTuple> getTuples() {
        if (!TUPLES.isEmpty())
            return TUPLES;

        for (Tuple t : TupleRules.DOZENS_ORDINAL_LETTERS) {
            TUPLES.add(new OrdinalTuple(NumberPatterns.HNDRDS_PTRN_DEF + "1([01][1-9]|10)\\.$",
                    t.getNumberPattern(), NumberHelper.HUNDREDS, " eitt hundrað" + t.getNumberWord() + " og"));
            TUPLES.add(new OrdinalTuple("^1([01][1-9]|10)\\.$", t.getNumberPattern(), NumberHelper.HUNDREDS,
                    "hundrað" + t.getNumberWord() + " og"));
            TUPLES.add(new OrdinalTuple(NumberPatterns.HNDRDS_PTRN_DEF + "100\\.$", t.getNumberPattern(),
                    NumberHelper.HUNDREDS, " eitt hundrað" + t.getNumberWord()));
            TUPLES.add(new OrdinalTuple("100\\.$", t.getNumberPattern(), NumberHelper.HUNDREDS,
                    "hundrað" + t.getNumberWord()));
            TUPLES.add(new OrdinalTuple(NumberPatterns.ONES_PTRN_NO_11 + "1\\.?0([01][1-9]|10)\\.$",
                    t.getNumberPattern(), NumberHelper.THOUSANDS, " eitt þúsund" + t.getNumberWord() + " og"));
            TUPLES.add(new OrdinalTuple(NumberPatterns.ONES_PTRN_NO_11 + "1\\.?000\\.$",
                    t.getNumberPattern(), NumberHelper.THOUSANDS, " eitt þúsund" + t.getNumberWord()));

            for (Tuple ten : TupleRules.TENS_ZIP) {
                TUPLES.add(new OrdinalTuple("^" + ten.getDigit() + "([01][1-9]|10)\\.$", t.getNumberPattern(),
                        NumberHelper.HUNDREDS, ten.getNumberWord() + " hundruð" + t.getNumberWord() + " og"));
                TUPLES.add(new OrdinalTuple(NumberPatterns.TNS_PTRN + ten.getDigit() + "\\.?0([01][1-9]|10)\\.$",
                        t.getNumberPattern(), NumberHelper.THOUSANDS, ten.getNumberWord() + " þúsund" + t.getNumberWord() + " og"));
                TUPLES.add(new OrdinalTuple("^" + ten.getDigit() + "([01][1-9]|10)\\.$", t.getNumberPattern(),
                        NumberHelper.THOUSANDS, ""));
            }

            for (Tuple hun_thous : TupleRules.HUNDRENDS_THOUSANDS_ZIP) {
                TUPLES.add(new OrdinalTuple(NumberPatterns.HNDRDS_PTRN + hun_thous.getDigit() + "([01][1-9]|10)\\.$",
                        t.getNumberPattern(), NumberHelper.HUNDREDS, hun_thous.getNumberWord() + " hundruð" + t.getNumberWord() + " og"));
                TUPLES.add(new OrdinalTuple(NumberPatterns.HNDRDS_PTRN + hun_thous.getDigit() + "00\\.$",
                        t.getNumberPattern(), NumberHelper.HUNDREDS, hun_thous.getNumberWord() + " hundruð" + t.getNumberWord()));
                TUPLES.add(new OrdinalTuple(NumberPatterns.ONES_PTRN_11 + hun_thous.getDigit() + "\\.?0([01][1-9]|10)\\.$",
                        t.getNumberPattern(), NumberHelper.THOUSANDS, hun_thous.getNumberWord() + " þúsund" + t.getNumberWord() + " og"));
                TUPLES.add(new OrdinalTuple(NumberPatterns.ONES_PTRN_11 + hun_thous.getDigit() + "\\.?000\\.$",
                        t.getNumberPattern(), NumberHelper.THOUSANDS, hun_thous.getNumberWord() + " þúsund" + t.getNumberWord()));
            }
        }
        return TUPLES;
    }
}

