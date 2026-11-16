package com.madr.madr_app_backend.util;

import java.util.Locale;

public final class StringSanitizer {

    private StringSanitizer(){}

    public static String sanitize(String input){
        if (input == null) return null;
        return input
                .replaceAll("[^\\p{L}\\p{N}\\s]", "")
                .replaceAll("\\s+", " ")
                .strip()
                .toLowerCase(Locale.ROOT);
    }
}
