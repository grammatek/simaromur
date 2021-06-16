package com.grammatek.simaromur.frontend;

import java.util.ArrayList;
import java.util.List;

/**
 * A list of CategoryTuples to deal with ordinals in the "hundreds", "thousands", and
 * "hundred thousands" positions
 */

public class OrdinalThousandTuples {

    public static final List<CategoryTuple> TUPLES = new ArrayList<>();

    public static List<CategoryTuple> getTuples() {
        if (!TUPLES.isEmpty())
            return TUPLES;

        for (ExpansionTuple t : TupleRules.DOZENS_ORDINAL_LETTERS) {
            TUPLES.add(new CategoryTuple(NumberPatterns.HNDRDS_PTRN_DEF + "1([01][1-9]|10)\\.$",
                    t.getPosPattern(), NumberHelper.HUNDREDS, " eitt hundrað" + t.getNumberWord() + " og"));
            TUPLES.add(new CategoryTuple("^1([01][1-9]|10)\\.$", t.getPosPattern(), NumberHelper.HUNDREDS,
                    "hundrað" + t.getNumberWord() + " og"));
            TUPLES.add(new CategoryTuple(NumberPatterns.HNDRDS_PTRN_DEF + "100\\.$", t.getPosPattern(),
                    NumberHelper.HUNDREDS, " eitt hundrað" + t.getNumberWord()));
            TUPLES.add(new CategoryTuple("100\\.$", t.getPosPattern(), NumberHelper.HUNDREDS,
                    "hundrað" + t.getNumberWord()));
            TUPLES.add(new CategoryTuple(NumberPatterns.ONES_PTRN_NO_11 + "1\\.?0([01][1-9]|10)\\.$",
                    t.getPosPattern(), NumberHelper.THOUSANDS, " eitt þúsund" + t.getNumberWord() + " og"));
            TUPLES.add(new CategoryTuple(NumberPatterns.ONES_PTRN_NO_11 + "1\\.?000\\.$",
                    t.getPosPattern(), NumberHelper.THOUSANDS, " eitt þúsund" + t.getNumberWord()));

            for (ExpansionTuple ten : TupleRules.TENS_ZIP) {
                TUPLES.add(new CategoryTuple("^" + ten.getDigit() + "([01][1-9]|10)\\.$", t.getPosPattern(),
                        NumberHelper.HUNDREDS, ten.getNumberWord() + " hundruð" + t.getNumberWord() + " og"));
                TUPLES.add(new CategoryTuple(NumberPatterns.TNS_PTRN + ten.getDigit() + "\\.?0([01][1-9]|10)\\.$",
                        t.getPosPattern(), NumberHelper.THOUSANDS, ten.getNumberWord() + " þúsund" + t.getNumberWord() + " og"));
                TUPLES.add(new CategoryTuple("^" + ten.getDigit() + "([01][1-9]|10)\\.$", t.getPosPattern(),
                        NumberHelper.THOUSANDS, ""));
            }

            for (ExpansionTuple hun_thous : TupleRules.HUNDRENDS_THOUSANDS_ZIP) {
                TUPLES.add(new CategoryTuple(NumberPatterns.HNDRDS_PTRN + hun_thous.getDigit() + "([01][1-9]|10)\\.$",
                        t.getPosPattern(), NumberHelper.HUNDREDS, hun_thous.getNumberWord() + " hundruð" + t.getNumberWord() + " og"));
                TUPLES.add(new CategoryTuple(NumberPatterns.HNDRDS_PTRN + hun_thous.getDigit() + "00\\.$",
                        t.getPosPattern(), NumberHelper.HUNDREDS, hun_thous.getNumberWord() + " hundruð" + t.getNumberWord()));
                TUPLES.add(new CategoryTuple(NumberPatterns.ONES_PTRN_11 + hun_thous.getDigit() + "\\.?0([01][1-9]|10)\\.$",
                        t.getPosPattern(), NumberHelper.THOUSANDS, hun_thous.getNumberWord() + " þúsund" + t.getNumberWord() + " og"));
                TUPLES.add(new CategoryTuple(NumberPatterns.ONES_PTRN_11 + hun_thous.getDigit() + "\\.?000\\.$",
                        t.getPosPattern(), NumberHelper.THOUSANDS, hun_thous.getNumberWord() + " þúsund" + t.getNumberWord()));
            }
        }
        return TUPLES;
    }
}

