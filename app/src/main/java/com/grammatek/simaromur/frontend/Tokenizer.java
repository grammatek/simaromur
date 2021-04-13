package com.grammatek.simaromur.frontend;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * The Tokenizer is a basic white space tokenizer, that takes abbreviations and digits into account,
 * when splitting and determining end of sentences.
 * Algorithm:
 *      a. split on white spaces
 *      b. insert spaces before punctuation where appropriate (see processSpecialCharacters)
 *      c. determine end of sentence (EOS) with the help of context (see detectSentences)
 *      d. collect sentences in a list to return
 */
public class Tokenizer {
    private Set<String> mAbbreviations;
    private Set<String> mAbbreviationsNonending;

    private String mAlphabetic = "[A-Za-záéíóúýðþæöÁÉÍÓÚÝÐÞÆÖ]+";
    private String mUpperCase = "[A-ZÁÉÍÓÚÝÐÞÆÖ]";

    public Tokenizer(Context context) {
        Abbreviations abbr = new Abbreviations(context);
        mAbbreviations = abbr.getAbbreviations();
        mAbbreviationsNonending = abbr.getNonEndingAbbr();

    }

    /**
     * Takes a unicode-normalized text as input (see @TTSNormalizer) and returns a list of sentences
     * as strings. A white space split on these strings gives a token list, where punctuation has been
     * separated from word tokens, except from digits and abbreviations. However, a full stop at the
     * end of a sentence is always separated from the last token, even if the last token is an
     * abbreviation.
     * @param text a string that has been unicode-normalized
     * @return a list of sentences as strings
     */
    public List<String> detectSentences(String text) {
        List<String> sentences = new ArrayList<>();
        String[] tokensArr = text.split("\\s");
        StringBuilder sb = new StringBuilder();
        String lastToken = "";
        // loop through all tokens in text and determine sentence boundaries,
        // store tokens ending with '.' in the 'lastToken' variable
        for (String t : tokensArr) {
            String tokenized = t;
            // we don't need to do anything with alphabetic-only tokens
            if (!t.matches(mAlphabetic)) {
                tokenized = processSpecialCharacters(t.trim());
            }
            if (!lastToken.isEmpty()) {
                if (!isFullStopEOS(tokenized, lastToken))
                    addToSent(sb, lastToken);
                else {
                    String sentence = finishSent(sb, lastToken);
                    sentences.add(sentence);
                    sb = new StringBuilder();
                }
            }
            // keep tokens ending with '.' for the next iteration
            if (endsWithDot(tokenized)) {
                lastToken = tokenized;
                continue;
            }
            else
                lastToken = "";

            sb.append(tokenized).append(" ");
            if (isEOS(tokenized)) {
                sentences.add(sb.toString().trim());
                sb = new StringBuilder();
            }
        }

        // we might still have a dangling last token
        if (!lastToken.isEmpty()) {
            String sent = finishSent(sb, lastToken);
            sentences.add(sent);
            sb = new StringBuilder();
        }
        // last token of text might not have ended with an EOS symbol, we still want to
        // collect the last tokens into a sentence and return
        if (!sb.toString().trim().isEmpty()) {
            sentences.add (sb.toString().trim() + " .");
        }
        return sentences;
    }

    // 'token' might end with " .", delete the space, because we are dealing with an
    // abbreviation or digits, that should not contain a space before the "."
    private void addToSent(StringBuilder sb, String token) {
        token = token.replace(" ", "");
        sb.append(token + " ");
    }

    // finish a sentence, take a look if if the sb content has a correct sentence ending,
    // if not, add a " ." to the sentence and return.
    private String finishSent(StringBuilder sb, String token) {
        sb.append(token);
        String sent = sb.toString().trim();
        if (!sent.endsWith(" .") && !sent.endsWith(" . \""))
            sent = sent.substring(0, sent.length() - 1) + " .";
        return sent;
    }

    // a token might end with a dot or a dot plus a quotation mark
    // might have to add more possibilities here
    private boolean endsWithDot(String token) {
        return token.endsWith(".") || token.endsWith(". \"");
    }

    // If last token ended with a dot, we look at if the current token starts with an
    // upper case letter and if last token is non sentence ending abbreviation.
    // Generally, if next token starts with an upper case letter, we have an EOS, unless
    // the last token (the dot-token) is a one-letter upper case abbr. or a defined non-sentence
    // ending abbreviation (like 'Hr.', which should always be followed by a name).
    private boolean isFullStopEOS(String current, String last) {
        if (Character.isUpperCase(current.charAt(0)) || current.charAt(0) == '"') {
            if (isUpperCaseAbbr(last) || mAbbreviationsNonending.contains(last.toLowerCase())) {
                return false;
            }
            return true;
        }
        return false;
    }

    // Most EOS symbols are not as ambiguous like the dot, check for them here.
    // The ':' is a matter of definition, we define it as EOS for now at least.
    private boolean isEOS(String token) {
        return (token.endsWith(" ?") || token.endsWith("? \"")
                || token.endsWith(" !") || token.endsWith("! \"")
                || token.endsWith(" :"));
    }


    //If a token contains some other character(s) than alphabetic characters, we take a closer look.
    private String processSpecialCharacters(String token) {

        // first, check if we need to process the token, several categories do not need
        // further processing, just return the token as is:
        if (!shouldProcess(token))
            return token;

        // for all kinds of punctuation we need to insert spaces at the correct positions
        // Patterns
        String processedToken = token;
        String insertSpaceAfterAnywhere = "(.*)([(\\[{])(.*)";
        String insertSpaceBeforeAnywhere = "(.+)([)\\[}])(.*)";
        String insertSpaceAfterIfBeginning = "^(\")(.+)";
        String insertSpaceBeforeIfEnd = "(.+)([\":,.!?])$";
        String insertSpaceBeforeIfEndAndPunct = "(.+)([\":,.!?])(\\s[\":,.!?])$";

        // replacements
        processedToken = processedToken.replaceAll(insertSpaceAfterAnywhere, "$1$2" + " " + "$3");
        processedToken = processedToken.replaceAll(insertSpaceBeforeAnywhere, "$1" + " " + "$2$3");
        processedToken = processedToken.replaceAll(insertSpaceAfterIfBeginning, "$1" + " " + "$2");
        processedToken = processedToken.replaceAll(insertSpaceBeforeIfEnd, "$1" + " " + "$2");
        processedToken = processedToken.replaceAll(insertSpaceBeforeIfEndAndPunct, "$1" + " " + "$2$3");

        return processedToken;
    }

    // We assume 'token' contains some non-alphabetic characters and we test
    // if it needs further processing. Tokens of size 0 or 1 do not need processing, and digits (with
    // or without punctuation) and defined abbreviations are also not to be processed further.
    // For all other tokens the method returns 'true'.
    private boolean shouldProcess(String token) {
        if (token.length() <= 1)
            return false;
        // a simple cardinal or ordinal number
        if (token.matches("\\d+\\.?"))
            return false;
        // a more complex combination of digits and punctuations, e.g. dates and large numbers
        if (token.matches("(\\d+[.,:]\\d+)+[,.]?"))
            return false;
        if (isAbbreviation(token))
            return false;
        if (isUpperCaseAbbr(token))
            return false;
        return true;
    }

    private boolean isUpperCaseAbbr(String token) {
        if (token.matches("(" + mUpperCase + "\\.)+") && !isAbbreviation(token))
            return true;
        return false;
    }

    private boolean isAbbreviation(String token) {
        if (mAbbreviations.contains(token.toLowerCase()))
            return true;
        if (mAbbreviationsNonending.contains(token.toLowerCase()))
            return true;
        return false;
    }

}
