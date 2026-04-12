package com.lesofn.appforge.common.spring

import org.springframework.context.ApplicationContext
import org.springframework.core.env.Environment
import spock.lang.Specification

/**
 * Spock test for SpringContextHolder
 * @author sofn
 */
class SpringContextHolderTest extends Specification {

    def "test getApplicationContext throws exception when not initialized"() {
        given:
        // Clear the application context
        def field = SpringContextHolder.class.getDeclaredField("applicationContext")
        field.setAccessible(true)
        field.set(null, null)

        when:
        SpringContextHolder.getApplicationContext()

        then:
        thrown(IllegalStateException)
    }

    def "test getApplicationContext returns context when initialized"() {
        given:
        def mockContext = Mock(ApplicationContext)
        def field = SpringContextHolder.class.getDeclaredField("applicationContext")
        field.setAccessible(true)
        field.set(null, mockContext)

        when:
        def result = SpringContextHolder.getApplicationContext()

        then:
        result == mockContext
    }

    def "test getBean by class returns bean from context"() {
        given:
        def mockContext = Mock(ApplicationContext)
        def mockBean = new Object()
        def field = SpringContextHolder.class.getDeclaredField("applicationContext")
        field.setAccessible(true)
        field.set(null, mockContext)

        when:
        def result = SpringContextHolder.getBean(Object)

        then:
        1 * mockContext.getBean(Object) >> mockBean
        result == mockBean
    }

    def "test getBean by name returns bean from context"() {
        given:
        def mockContext = Mock(ApplicationContext)
        def mockBean = new Object()
        def field = SpringContextHolder.class.getDeclaredField("applicationContext")
        field.setAccessible(true)
        field.set(null, mockContext)

        when:
        def result = SpringContextHolder.getBean("testBean")

        then:
        1 * mockContext.getBean("testBean") >> mockBean
        result == mockBean
    }

    def "test getBeansOfType returns map of beans"() {
        given:
        def mockContext = Mock(ApplicationContext)
        def mockBeans = ["bean1": new Object(), "bean2": new Object()]
        def field = SpringContextHolder.class.getDeclaredField("applicationContext")
        field.setAccessible(true)
        field.set(null, mockContext)

        when:
        def result = SpringContextHolder.getBeansOfType(Object)

        then:
        1 * mockContext.getBeansOfType(Object) >> mockBeans
        result == mockBeans
    }

    def "test isInjectedApplicationContext returns false when not initialized"() {
        given:
        def field = SpringContextHolder.class.getDeclaredField("applicationContext")
        field.setAccessible(true)
        field.set(null, null)

        when:
        def result = SpringContextHolder.isInjectedApplicationContext()

        then:
        result == false
    }

    def "test isInjectedApplicationContext returns true when initialized"() {
        given:
        def mockContext = Mock(ApplicationContext)
        def field = SpringContextHolder.class.getDeclaredField("applicationContext")
        field.setAccessible(true)
        field.set(null, mockContext)

        when:
        def result = SpringContextHolder.isInjectedApplicationContext()

        then:
        result == true
    }

    def "test cleanup"() {
        // Clean up after tests
        def field = SpringContextHolder.class.getDeclaredField("applicationContext")
        field.setAccessible(true)
        field.set(null, null)
    }
}