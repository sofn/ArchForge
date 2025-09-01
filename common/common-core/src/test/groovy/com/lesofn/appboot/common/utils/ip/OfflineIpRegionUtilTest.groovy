package com.lesofn.appboot.common.utils.ip

import spock.lang.Specification

/**
 * @author sofn
 */
class OfflineIpRegionUtilTest extends Specification {

    def "test getIpRegion returns null for null ip"() {
        when:
        def result = OfflineIpRegionUtil.getIpRegion(null)

        then:
        result == null
    }

    def "test getIpRegion returns null for empty ip"() {
        when:
        def result = OfflineIpRegionUtil.getIpRegion("")

        then:
        result == null
    }

    def "test getIpRegion returns null for blank ip"() {
        when:
        def result = OfflineIpRegionUtil.getIpRegion("   ")

        then:
        result == null
    }

    def "test getIpRegion returns null for IPv6 address"() {
        given:
        def ip = "2001:0db8:85a3:0000:0000:8a2e:0370:7334"

        when:
        def result = OfflineIpRegionUtil.getIpRegion(ip)

        then:
        result == null
    }

    def "test getIpRegion returns null for invalid IPv4 address"() {
        given:
        def ip = "999.999.999.999"

        when:
        def result = OfflineIpRegionUtil.getIpRegion(ip)

        then:
        result == null
    }
}