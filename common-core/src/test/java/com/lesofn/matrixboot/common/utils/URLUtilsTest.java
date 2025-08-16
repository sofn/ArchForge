package com.lesofn.matrixboot.common.utils;

import com.google.common.collect.Lists;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author sofn
 * @version 1.0 Created at: 2022-03-11 13:10
 */
class URLUtilsTest {

    @Test
    void encode() {
        String encode = URLUtils.encode("https://lesofn.com");
        assertEquals("https%3A%2F%2Flesofn.com", encode);
    }

    @Test
    void decode() {
        String decode = URLUtils.decode("https%3A%2F%2Flesofn.com");
        assertEquals("https://lesofn.com", decode);
    }

    @Test
    void parseQuery() {
        Map<String, List<String>> params = URLUtils.parseQuery("a=1&b=2&b=3");
        assertEquals(2, params.size());
        assertEquals(Lists.newArrayList("1"), params.get("a"));
        assertEquals(Lists.newArrayList("2", "3"), params.get("b"));
    }
}