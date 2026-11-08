package com.madr.madr_app_backend.util;

public final class StringSanitizer {

    private StringSanitizer(){}

    public static String sanitize(String input){
        if (input == null) return null;
        return input.strip().toLowerCase();
    }
}
