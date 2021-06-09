package com.grammatek.simaromur.frontend;

/**
 * A class to rebuild the tuple data structure in Python.
 */
public class OrdinalTuple {

    private final String numberPattern;
    private final String rule;
    private final String categorie;
    private final String expansion;

    public OrdinalTuple(String pattern, String rule, String categorie, String expansion) {
        this.numberPattern = pattern;
        this.rule = rule;
        this.categorie = categorie;
        this.expansion = expansion;
    }

    public String getNumberPattern() {
        return this.numberPattern;
    }
    public  String getRule() {
        return this.rule;
    }
    public String getCategorie() {
        return this.categorie;
    }
    public String getExpansion() {
        return this.expansion;
    }

}

