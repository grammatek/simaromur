package com.grammatek.simaromur.frontend;

import android.content.Context;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * The Tokenizer is a basic white space tokenizer, that takes abbreviations and digits into account,
 * when splitting and determining end of sentences.
 * Algorithm:
 * a. split on white spaces
 * b. insert spaces before punctuation where appropriate (see processSpecialCharacters)
 * c. determine end of sentence (EOS) with the help of context (see detectSentences)
 * d. collect sentences in a list to return
 */
public class Tokenizer {
    public static final String EOS_SYMBOLS = "[.:?!;]";

    private final Set<String> mAbbreviations;
    private final Set<String> mAbbreviationsNonending;

    private final String mAlphabeticRaw = "[A-Za-záéíóúýðþæöÁÉÍÓÚÝÐÞÆÖ]+";
    private final Pattern mAlphabetic = Pattern.compile(mAlphabeticRaw);
    private final Pattern mAlphabeticExt = Pattern.compile(".*[A-Za-záéíóúýðþæöÁÉÍÓÚÝÐÞÆÖ]+.*|.*\\d+.*");
    private final Pattern mUpperCase = Pattern.compile("[A-ZÁÉÍÓÚÝÐÞÆÖ]");
    private final Pattern mLowerCase = Pattern.compile("[a-záéíóúýðþæö]");
    private final String mWordChars = "[A-Za-záéíóúýðþæöÁÉÍÓÚÝÐÞÆÖ\\d.µ]";
    private final Pattern mWordChar = Pattern.compile(mWordChars);
    private final Pattern mEOS = Pattern.compile("[.:?!;]");

    private final Pattern mFinalDot = Pattern.compile("[^\\s]\\.");
    private final Pattern mEnclosingParenthesis = Pattern.compile("(\\()(.+)(\\))");
    private final Pattern mOpenParenthesis = Pattern.compile("(\\()");
    private final Pattern mClosingParenthesis = Pattern.compile("(?<=[^:;])(\\))");

    private final Pattern mInsertSpaceAfterAnywhere = Pattern.compile("([(\\[{\\-/_+]|[.]{2,})");
    private final Pattern mInsertSpaceBeforeAnywhere = Pattern.compile("([)\\]}\\-/_%+!?]|[.]{2,})");
    private final Pattern mInsertSpaceAround = Pattern.compile("(\")");
    private final Pattern mInsertSpaceIfDigit = Pattern.compile("(.*)(')(\\d+)");
    private final Pattern mInsertSpaceIfChar = Pattern.compile("(,)(\\D)");
    private final Pattern mInsertSpaceBeforeIfEnd = Pattern.compile("(.+)([\":;,.!?])$");
    private final Pattern mInsertSpaceBeforeIfEndAndPunct = Pattern.compile("(.+)([\":;,.!?)])(\\s[\":;,.!?)])$");

    private final Pattern mYearAtEOS = Pattern.compile("(1\\d{3})|(20\\d{2})\\.");
    private final Pattern mSimpleCardinalOrdinal = Pattern.compile("\\d+\\.?");
    private final Pattern mComplexDigitsPunct = Pattern.compile("(\\d+[.,:]\\d+)+[,.%]?");

    private final Pattern mTelephone = Pattern.compile("\\d{3}-\\d{4}[,.?:;]?");
    private final Pattern mKennitala = Pattern.compile("\\d{6}-\\d{4}[,.?:;]?");

    private final Pattern mNegativeNumber = Pattern.compile("-\\d+.*");
    private final Pattern mDigitsMultipleHyphens = Pattern.compile("(\\d+-){2,}\\d+");
    private final Pattern mHyphenAlphabetic = Pattern.compile("[A-Za-záéíóúýðþæöÁÉÍÓÚÝÐÞÆÖ]{1,2}-[A-Za-záéíóúýðþæöÁÉÍÓÚÝÐÞÆÖ]+");
    private final Pattern mSlashDates = Pattern.compile("([0-9]|[0-2][0-9]|3[01])/(0?[1-9]|1[0-2])/\\d{2,4}");
    private final Pattern mSlashSmallNumbers = Pattern.compile(mWordChars + "{1,3}/" + mWordChars + "{1,3}");
    private final Pattern mSpecialCases = Pattern.compile("(millj./)|(.+/klst)|(.+/kwst)|" +
            "(.+/gwst)|(.+/gw\\.st)|(.+/mwst)|(.+/twst)|(.+/m²)|(.+/m³)|(.+/mm²)|(.+/mm³)|(.+/cm²)|(.+/cm³)|(.+/ferm)");
    private final Pattern mSmileys = Pattern.compile("(:\\))|(:\\()|(;\\))");
    private final Pattern mWebPattern = Pattern.compile("(www)|(http)|@");
    private final Pattern mHTMLEntities = Pattern.compile("&.+;");
    private final Pattern mClosingTags = Pattern.compile("</.+>");
    private final Pattern mUppercaseAbbrev = Pattern.compile("([A-ZÁÉÍÓÚÝÐÞÆÖ]\\.)+");

    private final Pattern mDate = Pattern.compile("([0-9]|[0-2][0-9]|3[01])\\. (Jan.*|Feb.*|Mar.*|Apr.*|Maí|Jún.*|Júl.*|Ág.*|Sep.*|Okt.*|Nóv.*|Des.*)");

    private final Pattern mMultipleSpaces = Pattern.compile("\\s{2,}");

    private final List<Character> mDetachAtEnd = Arrays.asList(',', '\"');

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
     *
     * @param text a string that has been unicode-normalized
     * @return a list of sentences as strings
     */
    public List<String> detectSentences(String text) {
        // TODO: if the sentence ends with an abbreviation and a dot, then the dot is
        //       separated from the abbreviation. We should add a heuristics to check
        //       if the abbreviation is at the end of a sentence, and if so, keep the dot where
        //       it is. Example: ".... kl."
        List<String> sentences = new ArrayList<>();
        String[] tokensArr = text.split("\\s");
        StringBuilder sb = new StringBuilder();
        String lastToken = "";
        // loop through all tokens in text and determine sentence boundaries,
        // store tokens ending with '.' in the 'lastToken' variable
        for (String t : tokensArr) {
            if (t.isEmpty())
                continue;
            String tokenized = t;
            // we don't need to do anything with alphabetic-only tokens
            if (!mAlphabetic.matcher(t).matches()) {
                tokenized = processSpecialCharacters(t.trim());
            }
            sb = checkLastToken(sentences, sb, lastToken, tokenized);
            // keep tokens ending with '.' for the next iteration
            lastToken = updateLastToken(tokenized);
            if (!lastToken.isEmpty()) continue;

            sb = updateStringBuilder(sentences, sb, tokenized);
        }
        finishSentence(sentences, sb, lastToken);

        return sentences;
    }

    /*
     * Check the content of 'sb' and 'lastToken' and finish the sentence contained in 'sb'.
     * 'sentences' is the list of sentences already detected from the input text (see @detectSentences)
     * After processing 'sb' and 'lastToken' we create a new sentence string to add to 'sentences'
     */
    private void finishSentence(List<String> sentences, StringBuilder sb, String lastToken) {
        // we might still have a dangling last token
        if (!lastToken.isEmpty()) {
            String sent = ensureFullStop(sb, lastToken);
            sent = mMultipleSpaces.matcher(sent).replaceAll(" ");
            sentences.add(sent);
            sb = new StringBuilder();
        }
        // last token of text might not have ended with an EOS symbol, we still want to
        // collect the last tokens into a sentence and return
        if (!sb.toString().trim().isEmpty()) {
            String lastSentence = sb.toString().trim();
            lastSentence = mMultipleSpaces.matcher(lastSentence).replaceAll(" ");
            if (!lastSentence.matches(".*" + mAlphabetic + ".*|.*\\d+.*") && !sentences.isEmpty()) {
                // we don't want to add a sentence only consisting of symbols, do we?
                // rather add to last sentence, was probably a mistake to finish that one
                String sent = sentences.get(sentences.size() - 1);
                sent = sent + " " + lastSentence;
                sentences.set(sentences.size() - 1, sent);
            } else if (!Character.toString(lastSentence.charAt(lastSentence.length() - 1)).matches(EOS_SYMBOLS))
                sentences.add(lastSentence + " .");
            else
                sentences.add(lastSentence.trim());
        }
    }

    /*
     * Append 'tokenized' to 'sb', check if 'tokenized' represents an end of a sentence, if yes, create a
     * new sentence from 'sb' and add to 'sentences'. Create new StringBuilder object.
     * Return the 'sb', either we have the old 'sb' with 'tokenized' appended, or a new StringBuilder object.
     */
    private StringBuilder updateStringBuilder(List<String> sentences, StringBuilder sb, String tokenized) {
        sb.append(tokenized).append(" ");
        if (isEOS(tokenized)) {
            sentences.add(sb.toString().trim());
            sb = new StringBuilder();
        }
        return sb;
    }

    private String updateLastToken(String tokenized) {
        if (endsWithDot(tokenized)) {
            return tokenized;
        } else
            return "";
    }

    private StringBuilder checkLastToken(List<String> sentences, StringBuilder sb, String lastToken, String tokenized) {
        if (!lastToken.isEmpty()) {
            if (!isFullStopEOS(tokenized, lastToken))
                appendToken(sb, lastToken);
            else {
                String sentence = ensureFullStop(sb, lastToken);
                sentences.add(sentence);
                sb = new StringBuilder();
            }
        }
        return sb;
    }

    // 'token' might end with " .", delete the space, because we are dealing with an
    // abbreviation or digits, that should not contain a space before the "."
    private void appendToken(StringBuilder sb, String token) {
        token = token.replace(" .", ".");
        sb.append(token).append(" ");
    }

    // finish a sentence, take a look if if the sb content has a correct sentence ending,
    // if not, add a " ." to the sentence and return.
    private String ensureFullStop(StringBuilder sb, String token) {
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
        if (current.isEmpty())
            return false;
        if (Character.isUpperCase(current.charAt(0)) || current.charAt(0) == '"') {
            return !isUpperCaseAbbr(last) && !mAbbreviationsNonending.contains(last.toLowerCase());
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
        if (!shouldProcess(token)) {
            token = mOpenParenthesis.matcher(token).replaceAll("$1 ");
            token = mClosingParenthesis.matcher(token).replaceAll(" $1");
            token = token.replaceAll("%", " %");
            token = separateFinalSymbols(token);
            return token;
        }

        // for all kinds of punctuation we need to insert spaces at the correct positions
        // Patterns
        String processedToken = token;
        // Matches a dot followed by a double quote
        // Example: "text."
        final String swapDotDoubleQuote = "(\\.)\"";
        // Matches a comma followed by a double quote
        final String swapCommaDoubleQuote = "(,)\"";

        // replacements
        processedToken = processedToken.replaceAll(swapDotDoubleQuote, "\".");
        processedToken = processedToken.replaceAll(swapCommaDoubleQuote, "\",");
        processedToken = mInsertSpaceAfterAnywhere.matcher(processedToken).replaceAll("$1 ");
        processedToken = mInsertSpaceBeforeAnywhere.matcher(processedToken).replaceAll(" $1");
        processedToken = mInsertSpaceBeforeIfEnd.matcher(processedToken).replaceAll("$1 $2");
        processedToken = mInsertSpaceBeforeIfEndAndPunct.matcher(processedToken).replaceAll("$1 $2$3");
        processedToken = mInsertSpaceAround.matcher(processedToken).replaceAll(" $1 ");
        processedToken = mInsertSpaceIfDigit.matcher(processedToken).replaceAll("$1 $2 $3");
        processedToken = mInsertSpaceIfChar.matcher(processedToken).replaceAll(" $1 $2 ");

        return processedToken.replaceAll("\\s+", " ").trim();
    }

    private String separateFinalSymbols(String token) {
        // Make sure we insert a space between the symbols in DETACH_AT_END if one or more of them are
        // at the end of a token.

        String resToken = token;
        int counter = token.length() - 1;
        boolean lastChar = true;
        for (int i = counter; i >= 0; i--) {
            if (!mDetachAtEnd.contains(token.charAt(i)))
                break;
            else {
                String tmpTok = resToken.substring(0, counter) + " " + token.charAt(i);
                if (!lastChar)
                    tmpTok = tmpTok + resToken.substring(counter + 1);
                lastChar = false;
                resToken = tmpTok;
                counter--;
            }
        }
        return resToken;
    }

    /* We assume 'token' contains some non-alphabetic characters and we test
     * if it needs further processing. Tokens of size 0 or 1 do not need processing, and digits (with
     * or without punctuation) and defined abbreviations are also not to be processed further.
     * For all other tokens the method returns 'true'.
     */
    private boolean shouldProcess(String token) {
        if (token.length() <= 1)
            return false;
        // a simple cardinal or ordinal number
        if (mSimpleCardinalOrdinal.matcher(token).matches())
            return false;
        // a more complex combination of digits and punctuations, e.g. dates and large numbers
        if (mComplexDigitsPunct.matcher(token).matches())
            return false;
        // telephone number, don't split on hyphen
        if (mTelephone.matcher(token).matches())
            return false;
        if (mKennitala.matcher(token).matches())
            return false;
        if (mNegativeNumber.matcher(token).matches())
            return false;
        if (mDigitsMultipleHyphens.matcher(token).matches())
            return false;
        if (mHyphenAlphabetic.matcher(token).matches())
            return false;
        if (token.equals("og/eða"))
            return true;
        if (mSlashSmallNumbers.matcher(token).matches())
            return false;
        if (mSpecialCases.matcher(token).matches())
            return false;
        if (mSmileys.matcher(token).matches())
            return false;
        if (mWebPattern.matcher(token).find())
            return false;
        if (mHTMLEntities.matcher(token).matches())
            return false;
        if (isAbbreviation(token))
            return false;
        return !isUpperCaseAbbr(token);
    }

    private boolean isUpperCaseAbbr(String token) {
        return token.matches("(" + mUpperCase + "\\.)+") && !isAbbreviation(token);
    }

    private boolean isAbbreviation(String token) {
        if (mAbbreviations.contains(token.toLowerCase()))
            return true;
        return mAbbreviationsNonending.contains(token.toLowerCase());
    }
}

