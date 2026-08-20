package com.lesofn.archforge.infrastructure.web;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

class XssSanitizerTest {

    @Test
    void escapesScriptTagsWithoutTouchingParentheses() {
        assertEquals("&lt;script&gt;alert(1)&lt;/script&gt;", XssSanitizer.sanitize("<script>alert(1)</script>"));
    }

    @Test
    void keepsApostrophesAndParenthesesInNormalText() {
        assertEquals("don't (call)", XssSanitizer.sanitize("don't (call)"));
    }

    @Test
    void leavesNullAlone() {
        assertNull(XssSanitizer.sanitize(null));
    }
}
