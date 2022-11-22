package com.grammatek.simaromur.frontend;

import java.util.ArrayList;
import java.util.List;

public class CardinalBigTuples {
    //TODO see regina original
    //TODO: add billions
    public static final List<CategoryTuple> TUPLES = new ArrayList<>();

    public static List<CategoryTuple> getTuples() {
        if (!TUPLES.isEmpty())
            return TUPLES;

        TUPLES.add(new CategoryTuple(NumberPatterns.ONES_PTRN_NO_11 + "1" + NumberPatterns.MILLION_AND_CARDINAL + NumberPatterns.DEC_PTRN,
                NormalizationDictionaries.MATCH_ANY, NumberHelper.MILLIONS, "ein milljón og"));
        TUPLES.add(new CategoryTuple(NumberPatterns.ONES_PTRN_NO_11 + "1" + NumberPatterns.MILLION_AND_ORDINAL + NumberPatterns.DEC_PTRN,
                NormalizationDictionaries.MATCH_ANY, NumberHelper.MILLIONS, "ein milljón og"));
        TUPLES.add(new CategoryTuple(NumberPatterns.ONES_PTRN_NO_11 + "1" + NumberPatterns.MILLION_PTRN_AFTER + NumberPatterns.DEC_PTRN_ORDINAL,
                NormalizationDictionaries.MATCH_ANY, NumberHelper.MILLIONS, "ein milljón"));

        TUPLES.add(new CategoryTuple(NumberPatterns.HNDRDS_PTRN + "100" + NumberPatterns.MILLION_AND_CARDINAL + NumberPatterns.DEC_PTRN,
                NormalizationDictionaries.MATCH_ANY, NumberHelper.HUNDRED_MILLIONS, "eitt hundrað milljónir og"));
        TUPLES.add(new CategoryTuple(NumberPatterns.HNDRDS_PTRN + "100" + NumberPatterns.MILLION_AND_ORDINAL,
                NormalizationDictionaries.MATCH_ANY, NumberHelper.HUNDRED_MILLIONS, "hundrað milljónir og"));
        TUPLES.add(new CategoryTuple("^100" + NumberPatterns.MILLION_AND_CARDINAL + NumberPatterns.DEC_PTRN,
                NormalizationDictionaries.MATCH_ANY, NumberHelper.HUNDRED_MILLIONS, "hundrað milljónir og"));
        TUPLES.add(new CategoryTuple("^100" + NumberPatterns.MILLION_AND_ORDINAL,
                NormalizationDictionaries.MATCH_ANY, NumberHelper.HUNDRED_MILLIONS, "hundrað milljónir og"));
        TUPLES.add(new CategoryTuple(NumberPatterns.HNDRDS_PTRN + "100" + NumberPatterns.MILLION_PTRN_AFTER + NumberPatterns.DEC_PTRN_ORDINAL,
                NormalizationDictionaries.MATCH_ANY, NumberHelper.HUNDRED_MILLIONS, "eitt hundrað milljónir"));
        TUPLES.add(new CategoryTuple("^100" + NumberPatterns.MILLION_PTRN_AFTER + NumberPatterns.DEC_PTRN_ORDINAL,
                NormalizationDictionaries.MATCH_ANY, NumberHelper.HUNDRED_MILLIONS, "hundrað milljónir"));
        TUPLES.add(new CategoryTuple(NumberPatterns.HNDRDS_PTRN + "1" + NumberPatterns.HNDRD_AND_MILLION + NumberPatterns.DEC_PTRN_ORDINAL,
                NormalizationDictionaries.MATCH_ANY, NumberHelper.HUNDRED_MILLIONS, "eitt hundrað og"));
        TUPLES.add(new CategoryTuple("^1" + NumberPatterns.HNDRD_AND_MILLION + NumberPatterns.DEC_PTRN_ORDINAL,
                NormalizationDictionaries.MATCH_ANY, NumberHelper.HUNDRED_MILLIONS, "hundrað og"));
        TUPLES.add(new CategoryTuple(NumberPatterns.HNDRDS_PTRN + "1" + NumberPatterns.HNDRD_MILLION + NumberPatterns.DEC_PTRN_ORDINAL,
                NormalizationDictionaries.MATCH_ANY, NumberHelper.HUNDRED_MILLIONS, "eitt hundrað"));
        TUPLES.add(new CategoryTuple("^1" + NumberPatterns.HNDRD_MILLION + NumberPatterns.DEC_PTRN_ORDINAL,
                NormalizationDictionaries.MATCH_ANY, NumberHelper.HUNDRED_MILLIONS, "hundrað"));


        for (ExpansionTuple tuple : TupleRules.HUNDRENDS_THOUSANDS_ZIP) {
            TUPLES.add(new CategoryTuple(NumberPatterns.HNDRDS_PTRN + tuple.getDigit() + "00" + NumberPatterns.MILLION_AND_CARDINAL + NumberPatterns.DEC_PTRN,
                    NormalizationDictionaries.MATCH_ANY, NumberHelper.HUNDRED_MILLIONS, tuple.getNumberWord() + " hundruð milljónir og"));
            TUPLES.add(new CategoryTuple(NumberPatterns.HNDRDS_PTRN + tuple.getDigit() + "00" + NumberPatterns.MILLION_AND_ORDINAL,
                    NormalizationDictionaries.MATCH_ANY, NumberHelper.HUNDRED_MILLIONS, tuple.getNumberWord() + " hundruð milljónir og"));
            TUPLES.add(new CategoryTuple(NumberPatterns.HNDRDS_PTRN + tuple.getDigit() + "00" + NumberPatterns.MILLION_PTRN_AFTER + NumberPatterns.DEC_PTRN_ORDINAL,
                    NormalizationDictionaries.MATCH_ANY, NumberHelper.HUNDRED_MILLIONS, tuple.getNumberWord() + " hundruð milljónir"));
            TUPLES.add(new CategoryTuple(NumberPatterns.HNDRDS_PTRN + tuple.getDigit() + NumberPatterns.HNDRD_AND_MILLION + NumberPatterns.DEC_PTRN_ORDINAL,
                    NormalizationDictionaries.MATCH_ANY, NumberHelper.HUNDRED_MILLIONS, tuple.getNumberWord() + " hundruð og"));
            TUPLES.add(new CategoryTuple(NumberPatterns.HNDRDS_PTRN + tuple.getDigit() + NumberPatterns.HNDRD_MILLION + NumberPatterns.DEC_PTRN_ORDINAL,
                    NormalizationDictionaries.MATCH_ANY, NumberHelper.HUNDRED_MILLIONS, tuple.getNumberWord() + " hundruð"));

        }
        for (ExpansionTuple tuple : TupleRules.DOZENS_ZIP) {
            TUPLES.add(new CategoryTuple(NumberPatterns.TNS_PTRN + tuple.getDigit() + "0" + NumberPatterns.MILLION_AND_CARDINAL + NumberPatterns.DEC_PTRN,
                    NormalizationDictionaries.MATCH_ANY, NumberHelper.TEN_MILLIONS, tuple.getNumberWord() + " milljónir og"));
            TUPLES.add(new CategoryTuple(NumberPatterns.TNS_PTRN + tuple.getDigit() + "0" + NumberPatterns.MILLION_AND_ORDINAL,
                    NormalizationDictionaries.MATCH_ANY, NumberHelper.TEN_MILLIONS, tuple.getNumberWord() + " milljónir og"));
            TUPLES.add(new CategoryTuple(NumberPatterns.TNS_PTRN + tuple.getDigit() + "0" + NumberPatterns.MILLION_PTRN_AFTER + NumberPatterns.DEC_PTRN_ORDINAL,
                    NormalizationDictionaries.MATCH_ANY, NumberHelper.TEN_MILLIONS, tuple.getNumberWord() + " milljónir"));
            TUPLES.add(new CategoryTuple(NumberPatterns.TNS_PTRN + tuple.getDigit() + "[1-9](\\.\\d{3}){2}" + NumberPatterns.DEC_PTRN_ORDINAL,
                    NormalizationDictionaries.MATCH_ANY, NumberHelper.TEN_MILLIONS, tuple.getNumberWord() + " og"));
        }
        for (ExpansionTuple tuple : TupleRules.MILLIONS_ZIP) {
            TUPLES.add(new CategoryTuple(NumberPatterns.ONES_PTRN_NO_11 + tuple.getDigit() + NumberPatterns.MILLION_AND_CARDINAL + NumberPatterns.DEC_PTRN,
                    NormalizationDictionaries.MATCH_ANY, NumberHelper.MILLIONS, tuple.getNumberWord() + " milljónir og"));
            TUPLES.add(new CategoryTuple(NumberPatterns.ONES_PTRN_NO_11 + tuple.getDigit() + NumberPatterns.MILLION_AND_ORDINAL,
                    NormalizationDictionaries.MATCH_ANY, NumberHelper.MILLIONS, tuple.getNumberWord() + " milljónir og"));
            TUPLES.add(new CategoryTuple(NumberPatterns.ONES_PTRN_NO_11 + tuple.getDigit() + NumberPatterns.MILLION_PTRN_AFTER + NumberPatterns.DEC_PTRN_ORDINAL,
                    NormalizationDictionaries.MATCH_ANY, NumberHelper.MILLIONS, tuple.getNumberWord() + " milljónir"));
        }
        for (ExpansionTuple tuple : TupleRules.TENS_ZIP) {
            TUPLES.add(new CategoryTuple("^[1-9]?" + tuple.getDigit() + NumberPatterns.MILLION_AND_CARDINAL + NumberPatterns.DEC_PTRN,
                    NormalizationDictionaries.MATCH_ANY, NumberHelper.MILLIONS, tuple.getNumberWord() + " milljónir og"));
            TUPLES.add(new CategoryTuple("^[1-9]?" + tuple.getDigit() + NumberPatterns.MILLION_AND_ORDINAL,
                    NormalizationDictionaries.MATCH_ANY, NumberHelper.MILLIONS, tuple.getNumberWord() + " milljónir og"));
            TUPLES.add(new CategoryTuple("^[1-9]?" + tuple.getDigit() + NumberPatterns.MILLION_PTRN_AFTER + NumberPatterns.DEC_PTRN_ORDINAL,
                    NormalizationDictionaries.MATCH_ANY, NumberHelper.MILLIONS, tuple.getNumberWord() + " milljónir"));
        }

        /*


         */

        return TUPLES;
    }
}
