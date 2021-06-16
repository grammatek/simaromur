package com.grammatek.simaromur.frontend;

/**
 * This class holds information on how to expand a digit according to the following POS-tag pattern.
 *
 * Example:
 *      new Tuple("[nl]k[ef]þ-?((g?s?)|([svo]?[fme]?))", "einum", "1")
 *
 * Meaning: when the pos-tag pattern follows the digit "1" it should be expanded to "einum".
 */

public class ExpansionTuple {

    private final String posPattern;
    private final String numberWord;
    private final String digit;

    public ExpansionTuple(String pattern, String word, String digit) {
        this.posPattern = pattern;
        this.numberWord = word;
        this.digit = digit;
    }

    public ExpansionTuple(String word, String digit) {
        this("", word, digit);
    }

    public String getPosPattern() {
        return this.posPattern;
    }
    public  String getNumberWord() {
        return this.numberWord;
    }
    public String getDigit() {
        return this.digit;
    }

}

