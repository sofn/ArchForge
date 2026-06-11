package com.lesofn.archsmith.common.utils.excel

import spock.lang.Specification

class FastExcelUtilSpec extends Specification {

    def "write then read round-trip preserves rows"() {
        given:
        def out = new ByteArrayOutputStream()
        def headers = ["id", "name", "score"]
        def rows = [[1L, "alice", 90], [2L, "bob", 85]]

        when:
        FastExcelUtil.write(out, "Sheet1", headers, rows)
        def read = FastExcelUtil.readFirstSheet(new ByteArrayInputStream(out.toByteArray()))

        then:
        read.size() == 2
        read[0] == ["1", "alice", "90"]
        read[1] == ["2", "bob", "85"]
    }

    def "header row is written and skipped on read"() {
        given:
        def out = new ByteArrayOutputStream()
        FastExcelUtil.write(out, "S", ["a"], [["x"], ["y"]])

        when:
        def read = FastExcelUtil.readFirstSheet(new ByteArrayInputStream(out.toByteArray()))

        then:
        read*.first() == ["x", "y"]
    }

    def "null cell value is skipped, not crashed"() {
        given:
        def out = new ByteArrayOutputStream()

        when:
        FastExcelUtil.write(out, "S", ["a", "b"], [["x", null], [null, "y"]])
        def read = FastExcelUtil.readFirstSheet(new ByteArrayInputStream(out.toByteArray()))

        then:
        read.size() == 2
    }
}
