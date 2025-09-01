package com.lesofn.appboot.common.utils.ip

import spock.lang.Specification

/**
 * @author sofn
 */
class IpRegionUtilTest extends Specification {

    def "test getIpRegion returns empty region for null ip"() {
        when:
        def result = IpRegionUtil.getIpRegion(null)

        then:
        result != null
        result.getCountry() == null
        result.getArea() == null
    }

    def "test getIpRegion returns empty region for empty ip"() {
        when:
        def result = IpRegionUtil.getIpRegion("")

        then:
        result != null
        result.getCountry() == null
        result.getArea() == null
    }

    def "test getIpRegion returns inner IP region for inner ip"() {
        given:
        def ip = "192.168.1.1"

        when:
        def result = IpRegionUtil.getIpRegion(ip)

        then:
        result != null
        result.getCountry() == ""
        result.getArea() == "内网IP"
    }

    def "test getBriefLocationByIp returns brief location for inner ip"() {
        given:
        def ip = "192.168.1.1"

        when:
        def result = IpRegionUtil.getBriefLocationByIp(ip)

        then:
        result == "内网IP"
    }

    def "test getBriefLocationByIp returns brief location for null ip"() {
        when:
        def result = IpRegionUtil.getBriefLocationByIp(null)

        then:
        result == ""
    }
}