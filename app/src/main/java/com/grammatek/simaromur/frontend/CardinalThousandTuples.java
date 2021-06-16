package com.grammatek.simaromur.frontend;

import java.util.ArrayList;
import java.util.List;

/**
 * A list of categoryTuples for cardinals in the positions dozens to thousands
 */
public class CardinalThousandTuples {
    private static final String BOS = "^";
    private static final String MATCH_ALL = ".*";
    public static final List<CategoryTuple> TUPLES = new ArrayList<>();
    //TODO: create constants from regexes and words like "hundrað" etc.
    public static List<CategoryTuple> getTuples() {
        if (!TUPLES.isEmpty())
            return TUPLES;

        TUPLES.add(new CategoryTuple(NumberPatterns.HNDRDS_PTRN_DEF + "1([01][1-9]|[1-9]0)" + NumberPatterns.DEC_PTRN,
                MATCH_ALL, NumberHelper.HUNDREDS, " eitt hundrað og"));
        TUPLES.add(new CategoryTuple(NumberPatterns.HNDRDS_PTRN_DEF + "1[2-9]0\\.$",
                MATCH_ALL, NumberHelper.HUNDREDS, " eitt hundrað og"));
        TUPLES.add(new CategoryTuple("^1([01][1-9]|[1-9]0)" + NumberPatterns.DEC_PTRN,
                MATCH_ALL, NumberHelper.HUNDREDS, "hundrað og"));
        TUPLES.add(new CategoryTuple("^1([2-9]0)\\.$",
                MATCH_ALL, NumberHelper.HUNDREDS, "hundrað og"));
        TUPLES.add(new CategoryTuple(NumberPatterns.HNDRDS_PTRN_DEF + "1([2-9][1-9]|00)" + NumberPatterns.DEC_PTRN_ORDINAL,
                MATCH_ALL, NumberHelper.HUNDREDS, " eitt hundrað"));
        TUPLES.add(new CategoryTuple("^100" + NumberPatterns.DEC_PTRN,
                MATCH_ALL, NumberHelper.HUNDREDS, "hundrað"));
        TUPLES.add(new CategoryTuple("^1([2-9][1-9])" + NumberPatterns.DEC_PTRN_ORDINAL,
                MATCH_ALL, NumberHelper.HUNDREDS, "hundrað"));
        TUPLES.add(new CategoryTuple(NumberPatterns.ONES_PTRN_NO_11 + "1" + NumberPatterns.THSNDS_AND_PTRN_CARDINAL + NumberPatterns.DEC_PTRN,
                MATCH_ALL, NumberHelper.THOUSANDS, " eitt þúsund og"));
        TUPLES.add(new CategoryTuple(NumberPatterns.ONES_PTRN_NO_11 + "1" + NumberPatterns.THSNDS_AND_PTRN_ORDINAL,
                MATCH_ALL, NumberHelper.THOUSANDS, " eitt þúsund og"));
        TUPLES.add(new CategoryTuple(NumberPatterns.ONES_PTRN_NO_11 + "1\\.?((000$)|(([1-9](?!00)\\d{2})|(0[2-9][1-9]))" +
                NumberPatterns.DEC_PTRN_ORDINAL + ")",
                MATCH_ALL, NumberHelper.THOUSANDS, " eitt þúsund"));
        TUPLES.add(new CategoryTuple("^1\\.?000$",
                MATCH_ALL, NumberHelper.THOUSANDS, " þúsund"));

        for (ExpansionTuple tuple : TupleRules.DOZENS_ZIP) {
            TUPLES.add(new CategoryTuple(NumberPatterns.TNS_PTRN + tuple.getDigit() + "0" + NumberPatterns.DEC_PTRN,
                    MATCH_ALL, NumberHelper.DOZENS, tuple.getNumberWord()));
            TUPLES.add(new CategoryTuple(NumberPatterns.TNS_PTRN + tuple.getDigit() + "[1-9]" + NumberPatterns.DEC_PTRN,
                    MATCH_ALL, NumberHelper.DOZENS, tuple.getNumberWord() + " og"));
        }

        for (ExpansionTuple tuple : TupleRules.HUNDRENDS_THOUSANDS_ZIP) {
            TUPLES.add(new CategoryTuple(NumberPatterns.HNDRDS_PTRN + tuple.getDigit() + "([01][1-9]|[1-9]0)" + NumberPatterns.DEC_PTRN,
                    MATCH_ALL, NumberHelper.HUNDREDS, tuple.getNumberWord() + " hundruð og"));
            TUPLES.add(new CategoryTuple(NumberPatterns.HNDRDS_PTRN + tuple.getDigit() + "[2-9]0\\.$",
                    MATCH_ALL, NumberHelper.HUNDREDS, tuple.getNumberWord() + " hundruð og"));
            TUPLES.add(new CategoryTuple(NumberPatterns.HNDRDS_PTRN + tuple.getDigit() + "00" + NumberPatterns.DEC_PTRN,
                    MATCH_ALL, NumberHelper.HUNDREDS, tuple.getNumberWord() + " hundruð"));
            TUPLES.add(new CategoryTuple(NumberPatterns.HNDRDS_PTRN + tuple.getDigit() + "[2-9][1-9]" + NumberPatterns.DEC_PTRN_ORDINAL,
                    MATCH_ALL, NumberHelper.HUNDREDS, tuple.getNumberWord() + " hundruð"));
            TUPLES.add(new CategoryTuple(NumberPatterns.ONES_PTRN_NO_11 + tuple.getDigit() +
                    NumberPatterns.THSNDS_AND_PTRN_CARDINAL + NumberPatterns.DEC_PTRN,
                    MATCH_ALL, NumberHelper.THOUSANDS, tuple.getNumberWord() + " þúsund og"));
            TUPLES.add(new CategoryTuple(NumberPatterns.ONES_PTRN_NO_11 + tuple.getDigit() +
                    NumberPatterns.THSNDS_AND_PTRN_CARDINAL,
                    MATCH_ALL, NumberHelper.THOUSANDS, tuple.getNumberWord() + " þúsund og"));
            TUPLES.add(new CategoryTuple(NumberPatterns.ONES_PTRN_NO_11 + tuple.getDigit() +
                    "\\.?((000$)|(([1-9](?!00)\\d{2})|(0[2-9][1-9]))" + NumberPatterns.DEC_PTRN_ORDINAL + ")",
                    MATCH_ALL, NumberHelper.THOUSANDS, tuple.getNumberWord() + " þúsund"));
        }
        for (ExpansionTuple tuple : TupleRules.TENS_ZIP) {
            if (tuple.getDigit().equals("10"))
                continue;
            TUPLES.add(new CategoryTuple(BOS + tuple.getDigit() + "([01][1-9]|[1-9]0)" + NumberPatterns.DEC_PTRN,
                    MATCH_ALL, NumberHelper.HUNDREDS, tuple.getNumberWord() + " hundruð og"));
            TUPLES.add(new CategoryTuple(BOS + tuple.getDigit() + "00" + NumberPatterns.DEC_PTRN,
                    MATCH_ALL, NumberHelper.HUNDREDS, tuple.getNumberWord() + " hundruð"));
            TUPLES.add(new CategoryTuple(BOS + tuple.getDigit() + "([2-9][1-9])" + NumberPatterns.DEC_PTRN_ORDINAL,
                    MATCH_ALL, NumberHelper.HUNDREDS, tuple.getNumberWord() + " hundruð"));
            TUPLES.add(new CategoryTuple(BOS + tuple.getDigit() + "([01][1-9]|[1-9]0)" + NumberPatterns.DEC_PTRN,
                    MATCH_ALL, NumberHelper.THOUSANDS, ""));
            TUPLES.add(new CategoryTuple(BOS + tuple.getDigit() + "00" + NumberPatterns.DEC_PTRN,
                    MATCH_ALL, NumberHelper.THOUSANDS, ""));
            TUPLES.add(new CategoryTuple(BOS + tuple.getDigit() + "([2-9][1-9])" + NumberPatterns.DEC_PTRN_ORDINAL,
                    MATCH_ALL, NumberHelper.THOUSANDS, ""));
        }
        for (ExpansionTuple tuple : TupleRules.TENS_ZIP) {
            TUPLES.add(new CategoryTuple(NumberPatterns.TNS_PTRN + tuple.getDigit() + NumberPatterns.THSNDS_AND_PTRN_CARDINAL + NumberPatterns.DEC_PTRN,
                    MATCH_ALL, NumberHelper.THOUSANDS, tuple.getNumberWord() + " þúsund og"));
            TUPLES.add(new CategoryTuple(NumberPatterns.TNS_PTRN + tuple.getDigit() + NumberPatterns.THSNDS_AND_PTRN_ORDINAL,
                    MATCH_ALL, NumberHelper.THOUSANDS, tuple.getNumberWord() + " þúsund og"));
            TUPLES.add(new CategoryTuple(NumberPatterns.TNS_PTRN + tuple.getDigit() + NumberPatterns.THSNDS_PTRN_AFTER + NumberPatterns.DEC_PTRN_ORDINAL,
                    MATCH_ALL, NumberHelper.THOUSANDS, tuple.getNumberWord() + " þúsund"));
        }
        return TUPLES;
    }
}
