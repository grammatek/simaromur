package com.grammatek.simaromur.frontend;

import android.content.Context;
import android.content.res.Resources;

import com.grammatek.simaromur.R;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This class handles Unicode cleaning and unicode normalizing of text. To simplify further
 * processing, text normalizing and grapheme-to-phoneme conversion, we clean the text of most
 * unicode characters not contained in the Icelandic alphabet, and also delete or substitute a
 * number of punctuation characters and special symbols.
 */
public class TTSUnicodeNormalizer {

    public static Map<String, PronDictEntry> mLexicon = new HashMap<>();

    // The Icelandic alphabet, the grapheme set valid for automatic g2p
    private final static Set<Character> CHAR_SET = new HashSet<>();
    static {
        CHAR_SET.add('a');
        CHAR_SET.add('á');
        CHAR_SET.add('b');
        CHAR_SET.add('d');
        CHAR_SET.add('ð');
        CHAR_SET.add('e');
        CHAR_SET.add('é');
        CHAR_SET.add('f');
        CHAR_SET.add('g');
        CHAR_SET.add('h');
        CHAR_SET.add('i');
        CHAR_SET.add('í');
        CHAR_SET.add('j');
        CHAR_SET.add('k');
        CHAR_SET.add('l');
        CHAR_SET.add('m');
        CHAR_SET.add('n');
        CHAR_SET.add('o');
        CHAR_SET.add('ó');
        CHAR_SET.add('p');
        CHAR_SET.add('r');
        CHAR_SET.add('s');
        CHAR_SET.add('t');
        CHAR_SET.add('u');
        CHAR_SET.add('ú');
        CHAR_SET.add('v');
        CHAR_SET.add('y');
        CHAR_SET.add('ý');
        CHAR_SET.add('þ');
        CHAR_SET.add('æ');
        CHAR_SET.add('ö');
        CHAR_SET.add('x');
    }

    // we want to keep punctuation marks still present in the normalized
    // string, but delete the unknown character otherwise
    private final String DONT_DELETE = "[.,\":?!-]";

    public TTSUnicodeNormalizer(Context context, Map<String, PronDictEntry> pronDict) {
        mLexicon = pronDict;
    }

    /**
     * Normalize the unicode encoding of the input text. This includes deleting and substituting
     * certain characters and symbols, as defined in @UnicodeMaps
     * @param text raw input text
     * @return cleaned version of @text as String
     */
    public String normalizeEncoding(String text) {
        String normalizedText = text;
        for (int i = 0; i < text.length(); i++) {
            String repl = getReplacement(text.charAt(i));
            if (!repl.isEmpty()) {
                normalizedText = normalizedText.replace(Character.toString(text.charAt(i)), repl);
            }
            if (shouldDelete(text.charAt(i)))
                normalizedText = normalizedText.replace(Character.toString(text.charAt(i)), "");
        }
        return normalizedText;
    }

    /**
     * This method is the last in the normalization process. That is, we already have
     * normalized the text with regards to abbreviations, digits, etc., but as last procedure
     * we need to ensure that no non-valid characters are delivered to the g2p system.
     * <p>
     * Before replaceing possible non-valid characters, we make a lexicon-lookup, since
     * words with non-Icelandic characters might be stored there, even if automatic g2p
     * would fail.
     * <p>
     * TODO: this needs more careful handling and a "contract" with the g2p module: which
     *       characters should be allowed?
     * @param sentences normalized sentences
     * @return the list of 'sentences', cleaned of any non-valid characters for g2p
     */
    public List<String> normalizeAlphabet(List<String> sentences) {
        List<String> normalizedSentences = new ArrayList<>();
        for (String sent : sentences) {
            StringBuilder sb = new StringBuilder();
            String[] sentArr = sent.split(" ");

            for (String wrd : sentArr) {
                if (isTag(wrd)) {
                    sb.append(wrd).append(" ");
                    continue;
                }
                if (sentArr.length == 1 || sentArr.length == 2) {
                    // If we have a "sentence" consisting of one symbol that hasn't been
                    // normalized yet, we take care of that here.
                    // Example: "( ." becomes: "vinstri svigi ."
                    String wrdCopy = UnicodeMaps.SymbolsMap.get(wrd);
                    if (wrdCopy != null) {
                        sb.append(wrdCopy).append(" ");
                        continue;
                    }
                }
                if (!inDictionary(wrd) && !wrd.contains(" ")) {
                    StringBuilder newWrd = new StringBuilder();
                    for (int i = 0; i < wrd.length(); i++) {
                        char currentChar = wrd.charAt(i);
                        String charValue = String.valueOf(Character.toChars(wrd.codePointAt(i)));
                        String repl = getIceAlphaReplacement(currentChar);

                        if (!CHAR_SET.contains(Character.toLowerCase(currentChar))) {
                            // Check if a replacement exists
                            if (!repl.isEmpty()) {
                                newWrd.append(repl);
                            } else if (charValue.length() > 1) {
                                // Handle 16-bit unicode like an emoji, delete all of it
                                newWrd = new StringBuilder();
                                break;
                            } else if (wrd.charAt(i) == '(' || wrd.charAt(i) == ')') {
                                // sounds odd if parenthesis are ignored and don't cause the tts voice
                                // to pause a little, try <sil>
                                // TODO: we might need a more general approach to this, i.e. which
                                //  symbols and punctuation chars should cause the voice to pause?
                                newWrd.append("<sil>");
                            } else if (!Character.toString(currentChar).matches(DONT_DELETE)) {
                                // If it doesn't match DONT_DELETE, exclude it (do nothing)
                            } else {
                                newWrd.append(currentChar);
                            }
                        } else {
                            newWrd.append(currentChar);
                        }
                    }
                    wrd = newWrd.toString();
                }
                // we restore the original string with valid words / characters only
                sb.append(wrd);
                // don't add an extra space if we deleted the word
                if (!wrd.isEmpty())
                    sb.append(" ");
            }
            // a comma without context should be transcribed as 'komma'
            if (sb.toString().trim().equals(", ."))
                normalizedSentences.add("komma .");
            else
                normalizedSentences.add(sb.toString().trim().toLowerCase());
        }
        return normalizedSentences;
    }

    public static boolean inDictionary(String wrd) {
        return mLexicon.containsKey(wrd.toLowerCase());
    }

    private boolean isTag(String wrd) {
        return wrd.matches("^<\\w+>$");
    }

    private boolean shouldDelete(Character c) {
        return UnicodeMaps.deleteCharsMap.containsKey(c);
    }

    private String getReplacement(Character c) {
        if (UnicodeMaps.insertSpaceMap.containsKey(c))
            return UnicodeMaps.insertSpaceMap.get(c);
        if (UnicodeMaps.otherSubstMap.containsKey(c))
            return UnicodeMaps.otherSubstMap.get(c);
        if (UnicodeMaps.arabicAlphabet.containsKey(c))
            return UnicodeMaps.arabicAlphabet.get(c);
        if (UnicodeMaps.cyrillicAlphabet.containsKey(c))
            return UnicodeMaps.cyrillicAlphabet.get(c);
        if (UnicodeMaps.greekAlphabet.containsKey(c))
            return UnicodeMaps.greekAlphabet.get(c);
        if (UnicodeMaps.hebrewAlphabet.containsKey(c))
            return UnicodeMaps.hebrewAlphabet.get(c);
        if (UnicodeMaps.ipaMap.containsKey(c))
            return UnicodeMaps.ipaMap.get(c);

        return "";
    }

    private String getIceAlphaReplacement(Character c) {
        if (UnicodeMaps.postDictLookupMap.containsKey(c))
            return UnicodeMaps.postDictLookupMap.get(c);

        return "";
    }
}
