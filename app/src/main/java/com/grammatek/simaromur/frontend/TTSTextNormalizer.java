package com.grammatek.simaromur.frontend;

public class TTSTextNormalizer {

    public String normalize_encoding(String text) {
        String normalized_text = text;
        int len = text.length();
        for (int i = 0; i < text.length(); i++) {
            String repl = get_replacement(text.charAt(i));
            if (!repl.isEmpty()) {
                normalized_text = normalized_text.replace(Character.toString(text.charAt(i)), repl);
            }
            if (should_delete(text.charAt(i)))
                normalized_text = normalized_text.replace(Character.toString(text.charAt(i)), "");
        }
        return normalized_text;
    }

    private boolean should_delete(Character c) {
        return UnicodeMaps.deleteCharsMap.containsKey(c);
    }

    private String get_replacement(Character c) {

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
}
