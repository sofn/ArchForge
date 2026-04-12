package com.lesofn.appforge.common.utils.jackson

import spock.lang.Specification

/**
 * @author sofn
 */
class JsonUtilTest extends Specification {

    def "test to serializes object to JSON string"() {
        given:
        def obj = [name: "test", value: 123]

        when:
        def result = JsonUtil.to(obj)

        then:
        result == '{"name":"test","value":123}'
    }

    def "test to serializes list to JSON string"() {
        given:
        def list = [[name: "test1", value: 123], [name: "test2", value: 456]]

        when:
        def result = JsonUtil.to(list)

        then:
        result == '[{"name":"test1","value":123},{"name":"test2","value":456}]'
    }

    def "test from deserializes JSON string to object"() {
        given:
        def json = '{"name":"test","value":123}'

        when:
        def result = JsonUtil.fromMap(json)

        then:
        result.name == "test"
        result.value == 123
    }

    def "test fromList deserializes JSON string to list"() {
        given:
        def json = '[{"name":"test1","value":123},{"name":"test2","value":456}]'
        def type = Map

        when:
        def result = JsonUtil.fromList(json, type)

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
        def result = JsonUtil.fromMap(json)

        then:
        result.name == "test"
        result.value == 123
    }

    def "test getAsString extracts string field from JSON"() {
        given:
        def json = '{"name":"test","value":123}'

        when:
        def result = JsonUtil.getAsString(json, "name")

        then:
        result == "test"
    }

    def "test getAsInt extracts int field from JSON"() {
        given:
        def json = '{"name":"test","value":123}'

        when:
        def result = JsonUtil.getAsInt(json, "value")

        then:
        result == 123
    }

    def "test getAsLong extracts long field from JSON"() {
        given:
        def json = '{"name":"test","value":1234567890}'

        when:
        def result = JsonUtil.getAsLong(json, "value")

        then:
        result == 1234567890L
    }

    def "test getAsDouble extracts double field from JSON"() {
        given:
        def json = '{"name":"test","value":123.45}'

        when:
        def result = JsonUtil.getAsDouble(json, "value")

        then:
        result == 123.45
    }

    def "test getAsBoolean extracts boolean field from JSON"() {
        given:
        def json = '{"name":"test","value":true}'

        when:
        def result = JsonUtil.getAsBoolean(json, "value")

        then:
        result == true
    }

    def "test isJson validates valid JSON string"() {
        given:
        def json = '{"name":"test","value":123}'

        when:
        def result = JsonUtil.isJson(json)

        then:
        result == true
    }

    def "test isJson validates invalid JSON string"() {
        given:
        def json = '{"name":"test","value":}'

        when:
        def result = JsonUtil.isJson(json)

        then:
        result == false
    }

    def "test remove removes field from JSON"() {
        given:
        def json = '{"name":"test","value":123}'

        when:
        def result = JsonUtil.remove(json, "value")

        then:
        result == '{"name":"test"}'
    }

    def "test update updates field in JSON"() {
        given:
        def json = '{"name":"test","value":123}'

        when:
        def result = JsonUtil.update(json, "value", 456)

        then:
        result == '{"name":"test","value":456}'
    }

    def "test format formats JSON string"() {
        given:
        def json = '{"name":"test","value":123}'

        when:
        def result = JsonUtil.format(json)

        then:
        result.contains('"name" : "test"')
        result.contains('"value" : 123')
    }

    def "test null fields are excluded from JSON serialization"() {
        given:
        def obj = [name: "test", value: 123, description: null, emptyString: ""]

        when:
        def result = JsonUtil.to(obj)

        then:
        result == '{"name":"test","value":123,"emptyString":""}'
        !result.contains('description')
    }

    def "test null fields are excluded from list serialization"() {
        given:
        def list = [
                [name: "test1", value: 123, description: null],
                [name: "test2", value: 456, description: "valid"]
        ]

        when:
        def result = JsonUtil.to(list)

        then:
        result == '[{"name":"test1","value":123},{"name":"test2","value":456,"description":"valid"}]'
        !result.contains('null')
    }
}
