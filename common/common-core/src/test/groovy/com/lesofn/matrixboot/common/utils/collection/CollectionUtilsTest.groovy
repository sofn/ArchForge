package com.lesofn.matrixboot.common.utils.collection

import spock.lang.Specification
import spock.lang.Unroll

/**
 * @author sofn
 */
class CollectionUtilsTest extends Specification {

    def "test strListSplitter with valid string"() {
        given:
        def input = "a,b,c"

        when:
        def result = CollectionUtils.strListSplitter(input)

        then:
        result == ["a", "b", "c"]
    }

    def "test strListSplitter with empty string"() {
        given:
        def input = ""

        when:
        def result = CollectionUtils.strListSplitter(input)

        then:
        result == []
    }

    def "test strListSplitter with null input"() {
        when:
        def result = CollectionUtils.strListSplitter(null)

        then:
        result == []
    }

    def "test strListSplitter with string containing spaces"() {
        given:
        def input = "a, b , c"

        when:
        def result = CollectionUtils.strListSplitter(input)

        then:
        result == ["a", "b", "c"]
    }

    def "test strListSplitter with string containing empty elements"() {
        given:
        def input = "a,,b,c,"

        when:
        def result = CollectionUtils.strListSplitter(input)

        then:
        result == ["a", "b", "c"]
    }

    def "test longListSplitter converts string to list of longs"() {
        given:
        def input = "1,2,3"

        when:
        def result = CollectionUtils.longListSplitter(input)

        then:
        result == [1L, 2L, 3L]
    }

    def "test longListSplitter with invalid long values"() {
        given:
        def input = "1,abc,3"

        when:
        def result = CollectionUtils.longListSplitter(input)

        then:
        result == [1L, 0L, 3L]
    }

    def "test intListSplitter converts string to list of integers"() {
        given:
        def input = "1,2,3"

        when:
        def result = CollectionUtils.intListSplitter(input)

        then:
        result == [1, 2, 3]
    }

    def "test intListSplitter with invalid integer values"() {
        given:
        def input = "1,abc,3"

        when:
        def result = CollectionUtils.intListSplitter(input)

        then:
        result == [1, 0, 3]
    }

    def "test strLineSplitter with valid string"() {
        given:
        def input = "a\nb\nc"

        when:
        def result = CollectionUtils.strLineSplitter(input)

        then:
        result == ["a", "b", "c"]
    }

    def "test strLineSplitter with Windows line endings"() {
        given:
        def input = "a\r\nb\r\nc"

        when:
        def result = CollectionUtils.strLineSplitter(input)

        then:
        result == ["a", "b", "c"]
    }

    def "test strLineSplitter with empty string"() {
        given:
        def input = ""

        when:
        def result = CollectionUtils.strLineSplitter(input)

        then:
        result == []
    }

    def "test strLineSplitter with null input"() {
        when:
        def result = CollectionUtils.strLineSplitter(null)

        then:
        result == []
    }
}