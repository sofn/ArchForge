package com.appengine.common.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author sofn
 */
public class URLUtils {

    public static final String DEFAULT_CHARSET = "utf-8";
    public static final Logger LOGGER = LoggerFactory.getLogger(URLUtils.class);

    public static String encode(String src) {
        try {
            return URLEncoder.encode(src, DEFAULT_CHARSET);
        } catch (UnsupportedEncodingException e) {
            return src;
        }
    }

    public static String decode(String src) {
        try {
            return URLDecoder.decode(src, DEFAULT_CHARSET);
        } catch (UnsupportedEncodingException e) {
            return src;
        }
    }

    public static Map<String, List<String>> parseQuery(String uri) {
        int pathEndPos = uri.indexOf('?');
        String s = pathEndPos < 0 ? uri : uri.substring(pathEndPos + 1);

        Map<String, List<String>> params = new LinkedHashMap<String, List<String>>();
        String name = null;
        int pos = 0; // Beginning of the unprocessed region
        int i; // End of the unprocessed region
        char c = 0; // Current character
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

        if (pos != i) { // Are there characters we haven't dealt with?
            if (name == null) { // Yes and we haven't seen any `='.
                addParam(params, decode(s.substring(pos, i)), "");
            } else { // Yes and this must be the last value.
                addParam(params, name, decode(s.substring(pos, i)));
            }
        } else if (name != null) { // Have we seen a name without value?
            addParam(params, name, "");
        }

        return params;
    }

    private static void addParam(Map<String, List<String>> params, String name, String value) {
        List<String> values = params.get(name);
        if (values == null) {
            values = new ArrayList<>(1); // Often there's only 1 value.
            params.put(name, values);
        }
        values.add(value);
    }

    public static final String encodeURL(String str) {
        try {
            return URLEncoder.encode(str, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            LOGGER.error("encodeUrl", e);
        }
        return str;
    }
}
