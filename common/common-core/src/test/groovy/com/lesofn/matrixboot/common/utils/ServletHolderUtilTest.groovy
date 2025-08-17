package com.lesofn.matrixboot.common.utils

import spock.lang.Specification
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import jakarta.servlet.ServletContext

/**
 * @author sofn
 */
class ServletHolderUtilTest extends Specification {

    def "test renderString renders string to response"() {
        given:
        def response = Mock(HttpServletResponse)
        def writer = Mock(java.io.PrintWriter)
        response.getWriter() >> writer

        when:
        ServletHolderUtil.renderString(response, "test string")

        then:
        1 * response.setStatus(200)
        1 * response.setContentType("application/json")
        1 * response.setCharacterEncoding("utf-8")
        1 * writer.print("test string")
    }

    def "test renderString handles IOException"() {
        given:
        def response = Mock(HttpServletResponse)
        response.getWriter() >> { throw new IOException("Test exception") }

        when:
        ServletHolderUtil.renderString(response, "test string")

        then:
        noExceptionThrown()
    }
}