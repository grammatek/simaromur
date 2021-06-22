package com.grammatek.simaromur.frontend;

public class Util {

    public static String join(String[] textArr) {
        StringBuilder sb = new StringBuilder();
        for (String s : textArr) {
            sb.append(s);
            sb.append(" ");
        }
        return sb.toString().trim();
    }
}
