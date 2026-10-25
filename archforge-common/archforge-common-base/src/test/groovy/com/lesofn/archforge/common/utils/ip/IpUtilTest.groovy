package com.lesofn.archforge.common.utils.ip

import jakarta.servlet.http.HttpServletRequest
import spock.lang.Specification

/**
 * @author sofn
 */
class IpUtilTest extends Specification {


    def "test isIntranetIP with intranet IP addresses"() {
        expect:
        IpUtil.isIntranetIP(ip) == expected

        where:
        ip              | expected
        "10.0.0.1"      | true
        "172.16.0.1"    | true
        "192.168.1.1"   | true
        "8.8.8.8"       | false
        "127.0.0.1"     | false
    }

    def "test ipToInt converts IP to integer correctly"() {
        expect:
        IpUtil.ipToInt(ip) == expected

        where:
        ip              | expected
        "0.0.0.0"       | 0
        "127.0.0.1"     | 2130706433
        "192.168.1.1"   | -1062731519
        "255.255.255.255"| -1
    }

    def "test availablePort with valid port"() {
        when:
        def result = IpUtil.availablePort(8080)

        then:
        // This might fail if port 8080 is actually in use
        // We're just testing that the method doesn't throw an exception
        noExceptionThrown()
        result instanceof Boolean
    }

    def "test availablePort with invalid port"() {
        when:
        IpUtil.availablePort(-1)

        then:
        thrown(IllegalArgumentException)

        when:
        IpUtil.availablePort(65537)

        then:
        thrown(IllegalArgumentException)
    }
    
    def "test isValidIpv4 with valid IPv4 address"() {
        given:
        def validIp = "192.168.1.1"

        when:
        def result = IpUtil.isValidIpv4(validIp)

        then:
        result == true
    }
    
    def "test isValidIpv4 with invalid IPv4 address"() {
        given:
        def invalidIp = "999.999.999.999"

        when:
        def result = IpUtil.isValidIpv4(invalidIp)

        then:
        result == false
    }
    
    def "test isValidIpv6 with valid IPv6 address"() {
        given:
        def validIp = "2001:0db8:85a3:0000:0000:8a2e:0370:7334"

        when:
        def result = IpUtil.isValidIpv6(validIp)

        then:
        result == true
    }
    
    def "test isInnerIp with inner IP address"() {
        given:
        def innerIp = "192.168.1.1"

        when:
        def result = IpUtil.isInnerIp(innerIp)

        then:
        result == true
    }
    
    def "test isInnerIp with outer IP address"() {
        given:
        def outerIp = "8.8.8.8"

        when:
        def result = IpUtil.isInnerIp(outerIp)

        then:
        result == false
    }
    
    def "test getLocalIp returns local IP"() {
        when:
        def result = IpUtil.getLocalIp()

        then:
        // We can't assert a specific value since it depends on the environment
        result != null
        result instanceof String
    }
    
    def "test getSingleLocalIp returns local IP"() {
        when:
        def result = IpUtil.getSingleLocalIp()

        then:
        // We can't assert a specific value since it depends on the environment
        result != null
        result instanceof String
    }
    
    def "test randomAvailablePort returns valid port"() {
        when:
        def result = IpUtil.randomAvailablePort()

        then:
        result >= 1024
        result <= 65536
    }
}