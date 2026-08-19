package com.lesofn.archforge.infrastructure.web;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

class XssSanitizerTest {

    @Test
    void escapesScriptTags() {
        assertEquals("&lt;script&gt;alert&#40;1&#41;&lt;/script&gt;", XssSanitizer.sanitize("<script>alert(1)</script>"));
    }

    @Test
    void leavesNullAlone() {
        assertNull(XssSanitizer.sanitize(null));
    }
}
