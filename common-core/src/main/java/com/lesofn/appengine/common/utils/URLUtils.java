package com.lesofn.appengine.common.utils;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author sofn
 */
@Slf4j
public class URLUtils {

    @SneakyThrows
    public static String encode(String src) {
        return URLEncoder.encode(src, StandardCharsets.UTF_8.name());
    }

    @SneakyThrows
    public static String decode(String src) {
        return URLDecoder.decode(src, StandardCharsets.UTF_8.name());
    }

    public static Map<String, List<String>> parseQuery(String uri) {
        int pathEndPos = uri.indexOf('?');
        String s = pathEndPos < 0 ? uri : uri.substring(pathEndPos + 1);

        Map<String, List<String>> params = new LinkedHashMap<String, List<String>>();
        String name = null;
        // Beginning of the unprocessed region
        int pos = 0;
        // End of the unprocessed region
        int i;
        // Current character
        char c = 0;
        for (i = 0; i < s.length(); i++) {
            c = s.charAt(i);
            if (c == '=' && name == null) {
                if (pos != i) {
                    name = decode(s.substring(pos, i));
                }
                pos = i + 1;
            } else if (c == '&') {
                if (name == null && pos != i) {
                    // We haven't seen an `=' so far but moved forward.
                    // Must be a param of the form '&a&' so add it with
                    // an empty value.
                    addParam(params, decode(s.substring(pos, i)), "");
                } else if (name != null) {
                    addParam(params, name, decode(s.substring(pos, i)));
                    name = null;
                }
                pos = i + 1;
            }
        }

        // Are there characters we haven't dealt with?
        if (pos != i) {
            // Yes and we haven't seen any `='.
            if (name == null) {
                addParam(params, decode(s.substring(pos, i)), "");
            }
            // Yes and this must be the last value.
            else {
                addParam(params, name, decode(s.substring(pos, i)));
            }
        }
        // Have we seen a name without value?
        else if (name != null) {
            addParam(params, name, "");
        }

        return params;
    }

    private static void addParam(Map<String, List<String>> params, String name, String value) {
        List<String> values = params.computeIfAbsent(name, it -> new ArrayList<>(1));
        values.add(value);
    }

}
