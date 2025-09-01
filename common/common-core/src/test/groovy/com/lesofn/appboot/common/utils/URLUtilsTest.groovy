package com.lesofn.appboot.common.utils

import spock.lang.Specification

/**
 * @author sofn
 */
class URLUtilsTest extends Specification {

    def "test encode encodes string with UTF-8"() {
        given:
        def input = "hello world"

        when:
        def result = URLUtils.encode(input)

        then:
        result == "hello+world"
    }

    def "test encode handles special characters"() {
        given:
        def input = "hello@world.com"

        when:
        def result = URLUtils.encode(input)

        then:
        result == "hello%40world.com"
    }

    def "test decode decodes URL encoded string"() {
        given:
        def input = "hello+world"

        when:
        def result = URLUtils.decode(input)

        then:
        result == "hello world"
    }

    def "test decode handles special characters"() {
        given:
        def input = "hello%40world.com"

        when:
        def result = URLUtils.decode(input)

        then:
        result == "hello@world.com"
    }

    def "test parseQuery parses simple query string"() {
        given:
        def uri = "http://example.com?name=test&value=123"

        when:
        def result = URLUtils.parseQuery(uri)

        then:
        result.size() == 2
        result["name"] == ["test"]
        result["value"] == ["123"]
    }

    def "test parseQuery handles query string without URL scheme"() {
        given:
        def uri = "name=test&value=123"

        when:
        def result = URLUtils.parseQuery(uri)

        then:
        result.size() == 2
        result["name"] == ["test"]
        result["value"] == ["123"]
    }

    def "test parseQuery handles empty query string"() {
        given:
        def uri = ""

        when:
        def result = URLUtils.parseQuery(uri)

        then:
        result.size() == 0
    }

    def "test parseQuery handles query string with empty value"() {
        given:
        def uri = "name=&value=123"

        when:
        def result = URLUtils.parseQuery(uri)

        then:
        result.size() == 2
        result["name"] == [""]
        result["value"] == ["123"]
    }

    def "test parseQuery handles query string with no value"() {
        given:
        def uri = "name&value=123"

        when:
        def result = URLUtils.parseQuery(uri)

        then:
        result.size() == 2
        result["name"] == [""]
        result["value"] == ["123"]
    }

    def "test parseQuery handles query string with multiple values for same key"() {
        given:
        def uri = "name=test1&name=test2&value=123"

        when:
        def result = URLUtils.parseQuery(uri)

        then:
        result.size() == 2
        result["name"] == ["test1", "test2"]
        result["value"] == ["123"]
    }

    def "test parseQuery handles query string with encoded characters"() {
        given:
        def uri = "name=hello%40world.com&value=123"

        when:
        def result = URLUtils.parseQuery(uri)

        then:
        result.size() == 2
        result["name"] == ["hello@world.com"]
        result["value"] == ["123"]
    }
}