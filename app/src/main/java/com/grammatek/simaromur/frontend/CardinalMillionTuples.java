package com.grammatek.simaromur.frontend;

import java.util.ArrayList;
import java.util.List;

/**
 * A list of categoryTuples to deal with cardinals in the positions between ten thousands and hundred thousands
 */
public class CardinalMillionTuples {

    public static final List<CategoryTuple> TUPLES = new ArrayList<>();

    public static List<CategoryTuple> getTuples() {
        if (!TUPLES.isEmpty())
            return TUPLES;

        TUPLES.add(new CategoryTuple(NumberPatterns.TNS_PTRN + "10" + NumberPatterns.THSNDS_AND_PTRN_CARDINAL + NumberPatterns.DEC_PTRN_ORDINAL,
                NormalizationDictionaries.MATCH_ANY, NumberHelper.TEN_THOUSANDS, "tíu þúsund og"));
        TUPLES.add(new CategoryTuple(NumberPatterns.TNS_PTRN + "10" + NumberPatterns.THSNDS_AND_PTRN_CARDINAL,
                NormalizationDictionaries.MATCH_ANY, NumberHelper.TEN_THOUSANDS, "tíu þúsund og"));
        TUPLES.add(new CategoryTuple(NumberPatterns.TNS_PTRN + "10" + NumberPatterns.THSNDS_PTRN_AFTER + NumberPatterns.DEC_PTRN_ORDINAL,
                NormalizationDictionaries.MATCH_ANY, NumberHelper.TEN_THOUSANDS, " tíu þúsund"));
        TUPLES.add(new CategoryTuple(NumberPatterns.HNDRDS_PTRN + "100" + NumberPatterns.THSNDS_AND_PTRN_CARDINAL + NumberPatterns.DEC_PTRN,
                NormalizationDictionaries.MATCH_ANY, NumberHelper.HUNDRED_THOUSANDS, " eitt hundrað þúsund og"));
        TUPLES.add(new CategoryTuple("^100" + NumberPatterns.THSNDS_AND_PTRN_CARDINAL + NumberPatterns.DEC_PTRN,
                NormalizationDictionaries.MATCH_ANY, NumberHelper.HUNDRED_THOUSANDS, "hundrað þúsund og"));
        TUPLES.add(new CategoryTuple(NumberPatterns.HNDRDS_PTRN + "100" + NumberPatterns.THSNDS_AND_PTRN_ORDINAL,
                NormalizationDictionaries.MATCH_ANY, NumberHelper.HUNDRED_THOUSANDS, " eitt hundrað þúsund og"));
        TUPLES.add(new CategoryTuple("^100" + NumberPatterns.THSNDS_AND_PTRN_ORDINAL,
                NormalizationDictionaries.MATCH_ANY, NumberHelper.HUNDRED_THOUSANDS, "hundrað þúsund og"));
        TUPLES.add(new CategoryTuple(NumberPatterns.HNDRDS_PTRN_DEF + "100" + NumberPatterns.THSNDS_PTRN_AFTER + NumberPatterns.DEC_PTRN_ORDINAL,
                NormalizationDictionaries.MATCH_ANY, NumberHelper.HUNDRED_THOUSANDS, "hundrað þúsund"));
        TUPLES.add(new CategoryTuple("^100" + NumberPatterns.THSNDS_PTRN_AFTER + NumberPatterns.DEC_PTRN_ORDINAL,
                NormalizationDictionaries.MATCH_ANY, NumberHelper.HUNDRED_THOUSANDS, " eitt hundrað og"));
        TUPLES.add(new CategoryTuple(NumberPatterns.HNDRDS_PTRN + "1" + NumberPatterns.HNDRD_AND_THSND + NumberPatterns.DEC_PTRN_ORDINAL,
                NormalizationDictionaries.MATCH_ANY, NumberHelper.HUNDRED_THOUSANDS, " eitt hundrað og"));
        TUPLES.add(new CategoryTuple("^1" + NumberPatterns.HNDRD_AND_THSND + NumberPatterns.DEC_PTRN_ORDINAL,
                NormalizationDictionaries.MATCH_ANY, NumberHelper.HUNDRED_THOUSANDS, " hundrað og"));
        TUPLES.add(new CategoryTuple(NumberPatterns.HNDRDS_PTRN + "1" + NumberPatterns.HNDRD_THSND_AFTER + NumberPatterns.DEC_PTRN_ORDINAL,
                NormalizationDictionaries.MATCH_ANY, NumberHelper.HUNDRED_THOUSANDS, " eitt hundrað"));
        TUPLES.add(new CategoryTuple("^1" + NumberPatterns.HNDRD_THSND_AFTER + NumberPatterns.DEC_PTRN_ORDINAL,
                NormalizationDictionaries.MATCH_ANY, NumberHelper.HUNDRED_THOUSANDS, "hundrað"));

        for (ExpansionTuple tuple : TupleRules.DOZENS_ZIP) {
            TUPLES.add(new CategoryTuple(NumberPatterns.TNS_PTRN + tuple.getDigit() + "0" + NumberPatterns.THSNDS_AND_PTRN_CARDINAL + NumberPatterns.DEC_PTRN,
                    NormalizationDictionaries.MATCH_ANY, NumberHelper.TEN_THOUSANDS, tuple.getNumberWord() + " þúsund og"));
            TUPLES.add(new CategoryTuple(NumberPatterns.TNS_PTRN + tuple.getDigit() + "0" + NumberPatterns.THSNDS_AND_PTRN_CARDINAL,
                    NormalizationDictionaries.MATCH_ANY, NumberHelper.TEN_THOUSANDS, tuple.getNumberWord() + " þúsund og"));
            TUPLES.add(new CategoryTuple(NumberPatterns.TNS_PTRN + tuple.getDigit() + "0" + NumberPatterns.THSNDS_PTRN_AFTER + NumberPatterns.DEC_PTRN_ORDINAL,
                    NormalizationDictionaries.MATCH_ANY, NumberHelper.TEN_THOUSANDS, tuple.getNumberWord() + " þúsund"));
            TUPLES.add(new CategoryTuple(NumberPatterns.TNS_PTRN + tuple.getDigit() + "[1-9]\\.?\\d{3}" + NumberPatterns.DEC_PTRN_ORDINAL,
                    NormalizationDictionaries.MATCH_ANY, NumberHelper.TEN_THOUSANDS, tuple.getNumberWord() + " og"));
        }

        for (ExpansionTuple tuple : TupleRules.HUNDRENDS_THOUSANDS_ZIP) {
            TUPLES.add(new CategoryTuple(NumberPatterns.HNDRDS_PTRN + tuple.getDigit() + "00" + NumberPatterns.THSNDS_PTRN_AFTER + NumberPatterns.DEC_PTRN_ORDINAL,
                    NormalizationDictionaries.MATCH_ANY, NumberHelper.HUNDRED_THOUSANDS, tuple.getNumberWord() + " hundruð þúsund"));
            TUPLES.add(new CategoryTuple(NumberPatterns.HNDRDS_PTRN + tuple.getDigit() + "00" + NumberPatterns.THSNDS_AND_PTRN_CARDINAL + NumberPatterns.DEC_PTRN,
                    NormalizationDictionaries.MATCH_ANY, NumberHelper.HUNDRED_THOUSANDS, tuple.getNumberWord() + " hundruð þúsund og"));
            TUPLES.add(new CategoryTuple(NumberPatterns.HNDRDS_PTRN + tuple.getDigit() + "00" + NumberPatterns.THSNDS_AND_PTRN_ORDINAL,
                    NormalizationDictionaries.MATCH_ANY, NumberHelper.HUNDRED_THOUSANDS, tuple.getNumberWord() + " hundruð þúsund og"));
            TUPLES.add(new CategoryTuple(NumberPatterns.HNDRDS_PTRN + tuple.getDigit() + NumberPatterns.HNDRD_AND_THSND + NumberPatterns.DEC_PTRN_ORDINAL,
                    NormalizationDictionaries.MATCH_ANY, NumberHelper.HUNDRED_THOUSANDS, tuple.getNumberWord() + " hundruð og"));
            TUPLES.add(new CategoryTuple(NumberPatterns.HNDRDS_PTRN + tuple.getDigit() + NumberPatterns.HNDRD_THSND_AFTER + NumberPatterns.DEC_PTRN_ORDINAL,
                    NormalizationDictionaries.MATCH_ANY, NumberHelper.HUNDRED_THOUSANDS, tuple.getNumberWord() + " hundruð"));
        }

        return TUPLES;
    }
}

