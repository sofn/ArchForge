package com.lesofn.archforge.infrastructure.web;

import org.jspecify.annotations.Nullable;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import java.util.LinkedHashMap;
import java.util.Map;

public class XssRequestWrapper extends HttpServletRequestWrapper {

    public XssRequestWrapper(HttpServletRequest request) {
        super(request);
    }

    @Override
    public @Nullable String getParameter(String name) {
        return XssSanitizer.sanitize(super.getParameter(name));
    }

    @Override
    public String @Nullable [] getParameterValues(String name) {
        String @Nullable [] values = super.getParameterValues(name);
        if (values == null) {
            return null;
        }
        String[] sanitized = new String[values.length];
        for (int i = 0; i < values.length; i++) {
            sanitized[i] = XssSanitizer.sanitize(values[i]);
        }
        return sanitized;
    }

    @Override
    public Map<String, String[]> getParameterMap() {
        Map<String, String[]> source = super.getParameterMap();
        Map<String, String[]> sanitized = new LinkedHashMap<>();
        source.forEach((key, values) -> sanitized.put(key, getParameterValues(key)));
        return sanitized;
    }

    @Override
    public @Nullable String getHeader(String name) {
        return XssSanitizer.sanitize(super.getHeader(name));
    }
}
