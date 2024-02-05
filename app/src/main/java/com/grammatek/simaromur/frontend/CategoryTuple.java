package com.grammatek.simaromur.frontend;

import java.util.regex.Pattern;

/**
 * A CategoryTuple typically holds information on the expansion of a certain regex pattern
 * belonging to a certain category, according to a rule (pos-tag pattern)
 *
 * Example:
 *
 * new CategoryTuple("^((([1-9]((\d{0,2}(\.\d{3})*\.\d)|\d*))[02-9])|[2-9]?)?1((,\d*)|(\s1\/2|\s?(½|⅓|¼|⅔|¾)))?$",
 *                  "[nl]v[ef]o-?((g?s?)|([svo]?[fme]?))",
 *                  "ones",
 *                  "eina")
 *
 * Meaning: when we have a digit matching the numberPattern, and a pos-tag pattern matching the rule,
 * the digit "1" should be expanded to "eina" in the category (column) "ones"
 */

public class CategoryTuple {

    private final Pattern numberPattern;
    private final Pattern rule;
    private final String category;
    private final String expansion;

    public CategoryTuple(String pattern, String rule, String category, String expansion) {
        this.numberPattern = Pattern.compile(".*" + pattern + ".*");
        this.rule = Pattern.compile(".*" + rule);
        this.category = category;
        this.expansion = expansion;
    }

    public Pattern getNumberPattern() {
        return this.numberPattern;
    }
    public  Pattern getRule() {
        return this.rule;
    }
    public String getCategory() {
        return this.category;
    }
    public String getExpansion() {
        return this.expansion;
    }
}

