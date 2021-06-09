package com.grammatek.simaromur.frontend;

import java.util.ArrayList;
import java.util.List;

public class CardinalThousandTuples {
    private static final String BOS = "^";
    private static final String MATCH_ALL = ".*";
    public static final List<OrdinalTuple> TUPLES = new ArrayList<>();
    //TODO: create constants from regexes and words like "hundrað" etc.
    public static List<OrdinalTuple> getTuples() {
        if (!TUPLES.isEmpty())
            return TUPLES;

        TUPLES.add(new OrdinalTuple(NumberPatterns.HNDRDS_PTRN_DEF + "1([01][1-9]|[1-9]0)" + NumberPatterns.DEC_PTRN,
                MATCH_ALL, NumberHelper.HUNDREDS, " eitt hundrað og"));
        TUPLES.add(new OrdinalTuple(NumberPatterns.HNDRDS_PTRN_DEF + "1[2-9]0\\.$",
                MATCH_ALL, NumberHelper.HUNDREDS, " eitt hundrað og"));
        TUPLES.add(new OrdinalTuple("^1([01][1-9]|[1-9]0)" + NumberPatterns.DEC_PTRN,
                MATCH_ALL, NumberHelper.HUNDREDS, "hundrað og"));
        TUPLES.add(new OrdinalTuple("^1([2-9]0)\\.$",
                MATCH_ALL, NumberHelper.HUNDREDS, "hundrað og"));
        TUPLES.add(new OrdinalTuple(NumberPatterns.HNDRDS_PTRN_DEF + "1([2-9][1-9]|00)" + NumberPatterns.DEC_PTRN_ORDINAL,
                MATCH_ALL, NumberHelper.HUNDREDS, " eitt hundrað"));
        TUPLES.add(new OrdinalTuple("^100" + NumberPatterns.DEC_PTRN,
                MATCH_ALL, NumberHelper.HUNDREDS, "hundrað"));
        TUPLES.add(new OrdinalTuple("^1([2-9][1-9])" + NumberPatterns.DEC_PTRN_ORDINAL,
                MATCH_ALL, NumberHelper.HUNDREDS, "hundrað"));
        TUPLES.add(new OrdinalTuple(NumberPatterns.ONES_PTRN_NO_11 + "1" + NumberPatterns.THSNDS_AND_PTRN_CARDINAL + NumberPatterns.DEC_PTRN,
                MATCH_ALL, NumberHelper.THOUSANDS, " eitt þúsund og"));
        TUPLES.add(new OrdinalTuple(NumberPatterns.ONES_PTRN_NO_11 + "1" + NumberPatterns.THSNDS_AND_PTRN_ORDINAL,
                MATCH_ALL, NumberHelper.THOUSANDS, " eitt þúsund og"));
        TUPLES.add(new OrdinalTuple(NumberPatterns.ONES_PTRN_NO_11 + "1\\.?((000$)|(([1-9](?!00)\\d{2})|(0[2-9][1-9]))" +
                NumberPatterns.DEC_PTRN_ORDINAL + ")",
                MATCH_ALL, NumberHelper.THOUSANDS, " eitt þúsund"));
        TUPLES.add(new OrdinalTuple("^1\\.?000$",
                MATCH_ALL, NumberHelper.THOUSANDS, " þúsund"));

        for (Tuple tuple : TupleRules.DOZENS_ZIP) {
            TUPLES.add(new OrdinalTuple(NumberPatterns.TNS_PTRN + tuple.getDigit() + "0" + NumberPatterns.DEC_PTRN,
                    MATCH_ALL, NumberHelper.DOZENS, tuple.getNumberWord()));
            TUPLES.add(new OrdinalTuple(NumberPatterns.TNS_PTRN + tuple.getDigit() + "[1-9]" + NumberPatterns.DEC_PTRN,
                    MATCH_ALL, NumberHelper.DOZENS, tuple.getNumberWord() + " og"));
        }

        for (Tuple tuple : TupleRules.HUNDRENDS_THOUSANDS_ZIP) {
            TUPLES.add(new OrdinalTuple(NumberPatterns.HNDRDS_PTRN + tuple.getDigit() + "([01][1-9]|[1-9]0)" + NumberPatterns.DEC_PTRN,
                    MATCH_ALL, NumberHelper.HUNDREDS, tuple.getNumberWord() + " hundruð og"));
            TUPLES.add(new OrdinalTuple(NumberPatterns.HNDRDS_PTRN + tuple.getDigit() + "[2-9]0\\.$",
                    MATCH_ALL, NumberHelper.HUNDREDS, tuple.getNumberWord() + " hundruð og"));
            TUPLES.add(new OrdinalTuple(NumberPatterns.HNDRDS_PTRN + tuple.getDigit() + "00" + NumberPatterns.DEC_PTRN,
                    MATCH_ALL, NumberHelper.HUNDREDS, tuple.getNumberWord() + " hundruð"));
            TUPLES.add(new OrdinalTuple(NumberPatterns.HNDRDS_PTRN + tuple.getDigit() + "[2-9][1-9]" + NumberPatterns.DEC_PTRN_ORDINAL,
                    MATCH_ALL, NumberHelper.HUNDREDS, tuple.getNumberWord() + " hundruð"));
            TUPLES.add(new OrdinalTuple(NumberPatterns.ONES_PTRN_NO_11 + tuple.getDigit() +
                    NumberPatterns.THSNDS_AND_PTRN_CARDINAL + NumberPatterns.DEC_PTRN,
                    MATCH_ALL, NumberHelper.THOUSANDS, tuple.getNumberWord() + " þúsund og"));
            TUPLES.add(new OrdinalTuple(NumberPatterns.ONES_PTRN_NO_11 + tuple.getDigit() +
                    NumberPatterns.THSNDS_AND_PTRN_CARDINAL,
                    MATCH_ALL, NumberHelper.THOUSANDS, tuple.getNumberWord() + " þúsund og"));
            TUPLES.add(new OrdinalTuple(NumberPatterns.ONES_PTRN_NO_11 + tuple.getDigit() +
                    "\\.?((000$)|(([1-9](?!00)\\d{2})|(0[2-9][1-9]))" + NumberPatterns.DEC_PTRN_ORDINAL + ")",
                    MATCH_ALL, NumberHelper.THOUSANDS, tuple.getNumberWord() + " þúsund"));
        }
        for (Tuple tuple : TupleRules.TENS_ZIP) {
            if (tuple.getDigit().equals("10"))
                continue;
            TUPLES.add(new OrdinalTuple(BOS + tuple.getDigit() + "([01][1-9]|[1-9]0)" + NumberPatterns.DEC_PTRN,
                    MATCH_ALL, NumberHelper.HUNDREDS, tuple.getNumberWord() + " hundruð og"));
            TUPLES.add(new OrdinalTuple(BOS + tuple.getDigit() + "00" + NumberPatterns.DEC_PTRN,
                    MATCH_ALL, NumberHelper.HUNDREDS, tuple.getNumberWord() + " hundruð"));
            TUPLES.add(new OrdinalTuple(BOS + tuple.getDigit() + "([2-9][1-9])" + NumberPatterns.DEC_PTRN_ORDINAL,
                    MATCH_ALL, NumberHelper.HUNDREDS, tuple.getNumberWord() + " hundruð"));
            TUPLES.add(new OrdinalTuple(BOS + tuple.getDigit() + "([01][1-9]|[1-9]0)" + NumberPatterns.DEC_PTRN,
                    MATCH_ALL, NumberHelper.THOUSANDS, ""));
            TUPLES.add(new OrdinalTuple(BOS + tuple.getDigit() + "00" + NumberPatterns.DEC_PTRN,
                    MATCH_ALL, NumberHelper.THOUSANDS, ""));
            TUPLES.add(new OrdinalTuple(BOS + tuple.getDigit() + "([2-9][1-9])" + NumberPatterns.DEC_PTRN_ORDINAL,
                    MATCH_ALL, NumberHelper.THOUSANDS, ""));
        }
        for (Tuple tuple : TupleRules.TENS_ZIP) {
            TUPLES.add(new OrdinalTuple(NumberPatterns.TNS_PTRN + tuple.getDigit() + NumberPatterns.THSNDS_AND_PTRN_CARDINAL + NumberPatterns.DEC_PTRN,
                    MATCH_ALL, NumberHelper.THOUSANDS, tuple.getNumberWord() + " þúsund og"));
            TUPLES.add(new OrdinalTuple(NumberPatterns.TNS_PTRN + tuple.getDigit() + NumberPatterns.THSNDS_AND_PTRN_ORDINAL,
                    MATCH_ALL, NumberHelper.THOUSANDS, tuple.getNumberWord() + " þúsund og"));
            TUPLES.add(new OrdinalTuple(NumberPatterns.TNS_PTRN + tuple.getDigit() + NumberPatterns.THSNDS_PTRN_AFTER + NumberPatterns.DEC_PTRN_ORDINAL,
                    MATCH_ALL, NumberHelper.THOUSANDS, tuple.getNumberWord() + " þúsund"));
        }
        return TUPLES;
    }
}
