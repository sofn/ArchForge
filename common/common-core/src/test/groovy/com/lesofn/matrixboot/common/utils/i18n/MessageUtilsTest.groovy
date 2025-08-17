package com.lesofn.matrixboot.common.utils.i18n

import spock.lang.Specification
import com.lesofn.matrixboot.common.spring.SpringContextHolder
import org.springframework.context.MessageSource
import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.context.NoSuchMessageException
import java.util.Locale

/**
 * @author sofn
 */
class MessageUtilsTest extends Specification {

    def "test message retrieves message from MessageSource"() {
        given:
        def messageSource = Mock(MessageSource)
        SpringContextHolder.metaClass.static.getBean = { Class clazz -> messageSource }
        LocaleContextHolder.metaClass.static.getLocale = { -> Locale.ENGLISH }
        
        def code = "test.code"
        def args = ["arg1", "arg2"] as Object[]
        def expectedMessage = "Test message with arg1 and arg2"
        
        messageSource.getMessage(code, args, Locale.ENGLISH) >> expectedMessage

        when:
        def result = MessageUtils.message(code, args)

        then:
        result == expectedMessage
    }

    def "test message handles NoSuchMessageException"() {
        given:
        def messageSource = Mock(MessageSource)
        SpringContextHolder.metaClass.static.getBean = { Class clazz -> messageSource }
        LocaleContextHolder.metaClass.static.getLocale = { -> Locale.ENGLISH }
        
        def code = "test.code"
        def args = [] as Object[]
        
        messageSource.getMessage(code, args, Locale.ENGLISH) >> { throw new NoSuchMessageException("test.code") }

        when:
        MessageUtils.message(code, args)

        then:
        thrown(NoSuchMessageException)
    }
}