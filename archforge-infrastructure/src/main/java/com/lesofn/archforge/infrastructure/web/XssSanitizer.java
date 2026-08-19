package com.lesofn.archforge.infrastructure.web;

public final class XssSanitizer {

    private XssSanitizer() {
    }

    public static String sanitize(String value) {
        if (value == null || value.isEmpty()) {
            return value;
        }
        return value.replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#x27;")
                .replace("(", "&#40;")
                .replace(")", "&#41;");
    }
}
