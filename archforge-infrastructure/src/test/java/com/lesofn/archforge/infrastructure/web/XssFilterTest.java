package com.lesofn.archforge.infrastructure.web;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

class XssFilterTest {

    @Test
    void skipsMultipartUploads() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setContentType("multipart/form-data; boundary=----x");
        assertTrue(XssFilter.shouldSkip(request));
    }

    @Test
    void sanitizesJsonAndFormPosts() {
        MockHttpServletRequest json = new MockHttpServletRequest();
        json.setContentType("application/json");
        assertFalse(XssFilter.shouldSkip(json));

        MockHttpServletRequest form = new MockHttpServletRequest();
        form.setContentType("application/x-www-form-urlencoded");
        assertFalse(XssFilter.shouldSkip(form));
    }
}
