package com.grammatek.simaromur.frontend;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class Tokenizer {
    private Set<String> abbreviations;
    private Set<String> abbreviations_nonending;

    private String alphabetic = "[A-Za-záéíóúýðþæöÁÉÍÓÚÝÐÞÆÖ]+";
    private String upperCase = "[A-ZÁÉÍÓÚÝÐÞÆÖ]";
    private String punct = "[\\s.,]+";

    public Tokenizer(Context context) {
        Abbreviations abbr = new Abbreviations(context);
        abbreviations = abbr.getAbbreviations();
        abbreviations_nonending = abbr.getNonEndingAbbr();

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
        String[] tokens_arr = text.split("\\s");
        StringBuilder sb = new StringBuilder();
        String last_token = "";
        // loop through all tokens in text and determine sentence boundaries,
        // store tokens ending with '.' in the 'last_token' variable
        for (String t : tokens_arr) {
            String tokenized = t;
            // we don't need to do anything with alphabetic-only tokens
            if (!t.matches(alphabetic)) {
                tokenized = processSpecialCharacters(t.trim());
            }
            if (!last_token.isEmpty()) {
                if (!isFullStopEOS(tokenized, last_token))
                    addToSent(sb, last_token);
                else {
                    String sentence = finishSent(sb, last_token);
                    sentences.add(sentence);
                    sb = new StringBuilder();
                }
            }
            // keep tokens ending with '.' for the next iteration
            if (endsWithDot(tokenized)) {
                last_token = tokenized;
                continue;
            }
            else
                last_token = "";

            sb.append(tokenized + " ");
            if (isEOS(tokenized)) {
                sentences.add(sb.toString().trim());
                sb = new StringBuilder();
            }
        }

        // we might still have a dangling last token
        if (!last_token.isEmpty()) {
            String sent = finishSent(sb, last_token);
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

    private void addToSent(StringBuilder sb, String token) {
        token = token.replace(" ", "");
        sb.append(token + " ");
    }

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
            if (isUpperCaseAbbr(last) || abbreviations_nonending.contains(last.toLowerCase())) {
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

    /*
    If a token contains some other character(s) than alphabetic characters, we take a closer look.
     */
    private String processSpecialCharacters(String token) {

        // first, check if we need to process the token, several categories do not need
        // further processing, just return the token as is:
        if (!shouldProcess(token))
            return token;

        // for all kinds of punctuation we need to insert spaces at the correct positions
        // Patterns
        String processed_token = token;
        String insert_space_after_anywhere = "(.*)([(\\[{])(.*)";
        String insert_space_before_anywhere = "(.+)([)\\[}])(.*)";
        String insert_space_after_if_beginning = "^(\")(.+)";
        String insert_space_before_if_end = "(.+)([\":,.!?])$";
        String insert_space_before_if_end_and_punct = "(.+)([\":,.!?])(\\s[\":,.!?])$";

        // replacements
        processed_token = processed_token.replaceAll(insert_space_after_anywhere, "$1$2" + " " + "$3");
        processed_token = processed_token.replaceAll(insert_space_before_anywhere, "$1" + " " + "$2$3");
        processed_token = processed_token.replaceAll(insert_space_after_if_beginning, "$1" + " " + "$2");
        processed_token = processed_token.replaceAll(insert_space_before_if_end, "$1" + " " + "$2");
        processed_token = processed_token.replaceAll(insert_space_before_if_end_and_punct, "$1" + " " + "$2$3");

        return processed_token;
    }

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
        if (token.matches("(" + upperCase + "\\.)+") && !isAbbreviation(token))
            return true;
        return false;
    }

    private boolean isAbbreviation(String token) {
        if (abbreviations.contains(token.toLowerCase()))
            return true;
        if (abbreviations_nonending.contains(token.toLowerCase()))
            return true;
        return false;
    }

}
