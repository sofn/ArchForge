package com.lesofn.archforge.infrastructure.web;

import org.jspecify.annotations.Nullable;

public final class XssSanitizer {

    private XssSanitizer() {
    }

    public static @Nullable String sanitize(@Nullable String value) {
        if (value == null || value.isEmpty()) {
            return value;
        }
        return value.replace("<", "&lt;").replace(">", "&gt;");
    }
}
