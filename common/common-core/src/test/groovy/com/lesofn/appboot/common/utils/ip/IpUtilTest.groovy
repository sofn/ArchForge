package com.lesofn.appboot.common.utils.ip

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

    def "test getRealIpAddr returns default IP when request is null"() {
        when:
        def result = IpUtil.getRealIpAddr(null)

        then:
        result == "127.0.0.1"
    }

    def "test getRealIpAddr returns IP from x-forwarded-for header"() {
        given:
        def request = Mock(HttpServletRequest)
        request.getHeader("x-forwarded-for") >> "192.168.1.1"
        request.getHeader("Proxy-Client-IP") >> null
        request.getHeader("WL-Proxy-Client-IP") >> null
        request.getRemoteAddr() >> "127.0.0.1"

        when:
        def result = IpUtil.getRealIpAddr(request)

        then:
        result == "192.168.1.1"
    }

    def "test getRealIpAddr returns IP from Proxy-Client-IP header when x-forwarded-for is unknown"() {
        given:
        def request = Mock(HttpServletRequest)
        request.getHeader("x-forwarded-for") >> "unknown"
        request.getHeader("Proxy-Client-IP") >> "192.168.1.2"
        request.getHeader("WL-Proxy-Client-IP") >> null
        request.getRemoteAddr() >> "127.0.0.1"

        when:
        def result = IpUtil.getRealIpAddr(request)

        then:
        result == "192.168.1.2"
    }

    def "test getRealIpAddr returns IP from WL-Proxy-Client-IP header when others are unknown"() {
        given:
        def request = Mock(HttpServletRequest)
        request.getHeader("x-forwarded-for") >> "unknown"
        request.getHeader("Proxy-Client-IP") >> "unknown"
        request.getHeader("WL-Proxy-Client-IP") >> "192.168.1.3"
        request.getRemoteAddr() >> "127.0.0.1"

        when:
        def result = IpUtil.getRealIpAddr(request)

        then:
        result == "192.168.1.3"
    }

    def "test getRealIpAddr returns remote address when all headers are unknown"() {
        given:
        def request = Mock(HttpServletRequest)
        request.getHeader("x-forwarded-for") >> "unknown"
        request.getHeader("Proxy-Client-IP") >> "unknown"
        request.getHeader("WL-Proxy-Client-IP") >> "unknown"
        request.getRemoteAddr() >> "192.168.1.4"

        when:
        def result = IpUtil.getRealIpAddr(request)

        then:
        result == "192.168.1.4"
    }

    def "test getRealIpAddr handles comma-separated IPs from x-forwarded-for"() {
        given:
        def request = Mock(HttpServletRequest)
        request.getHeader("x-forwarded-for") >> "192.168.1.10, 192.168.1.20"
        request.getHeader("Proxy-Client-IP") >> null
        request.getHeader("WL-Proxy-Client-IP") >> null
        request.getRemoteAddr() >> "127.0.0.1"

        when:
        def result = IpUtil.getRealIpAddr(request)

        then:
        result == "192.168.1.20"
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