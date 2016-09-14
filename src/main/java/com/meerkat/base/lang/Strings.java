package com.meerkat.base.lang;


public final class Strings {

    private Strings() {
    }

    public static boolean isBlank(String s) {
        return s == null || s.isEmpty();
    }

    public static String blankToNull(String s) {
        if (isBlank(s)) {
            return null;
        }

        return s;
    }

    public static String join(String[] parts, String joiner) {
        StringBuilder ss = new StringBuilder();
        for (String s : parts) {
            if (ss.length() > 0) {
                ss.append(joiner);
            }
            ss.append(s == null ? "" : s);
        }
        return ss.toString();
    }
}
