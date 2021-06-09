package com.grammatek.simaromur.frontend;

/**
 * A class to rebuild the tuple data structure in Python.
 */
public class Tuple {

    private final String numberPattern;
    private final String numberWord;
    private final String digit;

    public Tuple(String pattern, String word, String digit) {
        this.numberPattern = pattern;
        this.numberWord = word;
        this.digit = digit;
    }

    public Tuple(String word, String digit) {
        this("", word, digit);
    }

    public String getNumberPattern() {
        return this.numberPattern;
    }
    public  String getNumberWord() {
        return this.numberWord;
    }
    public String getDigit() {
        return this.digit;
    }

}

