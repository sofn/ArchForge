package com.lesofn.appboot.common.utils.jackson

import spock.lang.Specification
import com.fasterxml.jackson.core.type.TypeReference

/**
 * @author sofn
 */
class JacksonUtilTest extends Specification {

    def "test to serializes object to JSON string"() {
        given:
        def obj = [name: "test", value: 123]

        when:
        def result = JacksonUtil.to(obj)

        then:
        result == '{"name":"test","value":123}'
    }

    def "test to serializes list to JSON string"() {
        given:
        def list = [[name: "test1", value: 123], [name: "test2", value: 456]]

        when:
        def result = JacksonUtil.to(list)

        then:
        result == '[{"name":"test1","value":123},{"name":"test2","value":456}]'
    }

    def "test from deserializes JSON string to object"() {
        given:
        def json = '{"name":"test","value":123}'

        when:
        def result = JacksonUtil.from(json, new TypeReference<Map<String, Object>>() {})

        then:
        result.name == "test"
        result.value == 123
    }

    def "test fromList deserializes JSON string to list"() {
        given:
        def json = '[{"name":"test1","value":123},{"name":"test2","value":456}]'
        def type = Map

        when:
        def result = JacksonUtil.fromList(json, type)

        then:
        result.size() == 2
        result[0].name == "test1"
        result[0].value == 123
        result[1].name == "test2"
        result[1].value == 456
    }

    def "test fromMap deserializes JSON string to map"() {
        given:
        def json = '{"name":"test","value":123}'

        when:
        def result = JacksonUtil.fromMap(json)

        then:
        result.name == "test"
        result.value == 123
    }

    def "test getAsString extracts string field from JSON"() {
        given:
        def json = '{"name":"test","value":123}'

        when:
        def result = JacksonUtil.getAsString(json, "name")

        then:
        result == "test"
    }

    def "test getAsInt extracts int field from JSON"() {
        given:
        def json = '{"name":"test","value":123}'

        when:
        def result = JacksonUtil.getAsInt(json, "value")

        then:
        result == 123
    }

    def "test getAsLong extracts long field from JSON"() {
        given:
        def json = '{"name":"test","value":1234567890}'

        when:
        def result = JacksonUtil.getAsLong(json, "value")

        then:
        result == 1234567890L
    }

    def "test getAsDouble extracts double field from JSON"() {
        given:
        def json = '{"name":"test","value":123.45}'

        when:
        def result = JacksonUtil.getAsDouble(json, "value")

        then:
        result == 123.45
    }

    def "test getAsBoolean extracts boolean field from JSON"() {
        given:
        def json = '{"name":"test","value":true}'

        when:
        def result = JacksonUtil.getAsBoolean(json, "value")

        then:
        result == true
    }

    def "test isJson validates valid JSON string"() {
        given:
        def json = '{"name":"test","value":123}'

        when:
        def result = JacksonUtil.isJson(json)

        then:
        result == true
    }

    def "test isJson validates invalid JSON string"() {
        given:
        def json = '{"name":"test","value":}'

        when:
        def result = JacksonUtil.isJson(json)

        then:
        result == false
    }

    def "test remove removes field from JSON"() {
        given:
        def json = '{"name":"test","value":123}'

        when:
        def result = JacksonUtil.remove(json, "value")

        then:
        result == '{"name":"test"}'
    }

    def "test update updates field in JSON"() {
        given:
        def json = '{"name":"test","value":123}'

        when:
        def result = JacksonUtil.update(json, "value", 456)

        then:
        result == '{"name":"test","value":456}'
    }

    def "test format formats JSON string"() {
        given:
        def json = '{"name":"test","value":123}'

        when:
        def result = JacksonUtil.format(json)

        then:
        result == '{\
  "name" : "test",\
  "value" : 123\
}'
    }
}
