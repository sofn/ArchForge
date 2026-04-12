package com.lesofn.appforge.common.utils.i18n

import spock.lang.Specification
import org.springframework.context.MessageSource
import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.context.NoSuchMessageException

/**
 * @author sofn
 */
class MessageUtilsTest extends Specification {

    def "test message retrieves message from MessageSource"() {
        given:
        def messageSource = Mock(MessageSource)
        def originalMessage = MessageUtils.metaClass.getStaticMetaMethod("message", String, Object[])
        MessageUtils.metaClass.static.message = { String msgCode, Object[] msgArgs ->
            messageSource.getMessage(msgCode, msgArgs, Locale.ENGLISH)
        }
        
        def code = "test.code"
        def args = ["arg1", "arg2"] as Object[]
        def expectedMessage = "Test message with arg1 and arg2"
        
        messageSource.getMessage(code, args, Locale.ENGLISH) >> expectedMessage

        when:
        def result = MessageUtils.message(code, args)

        then:
        result == expectedMessage
        
        cleanup:
        if (originalMessage) {
            MessageUtils.metaClass.static.message = originalMessage
        } else {
            MessageUtils.metaClass = null
        }
    }

    def "test message handles NoSuchMessageException"() {
        given:
        def messageSource = Mock(MessageSource)
        def originalMessage = MessageUtils.metaClass.getStaticMetaMethod("message", String, Object[])
        MessageUtils.metaClass.static.message = { String msgCode, Object[] msgArgs ->
            messageSource.getMessage(msgCode, msgArgs, Locale.ENGLISH)
        }
        
        def code = "test.code"
        def args = [] as Object[]
        
        messageSource.getMessage(code, args, Locale.ENGLISH) >> { throw new NoSuchMessageException("test.code") }

        when:
        MessageUtils.message(code, args)

        then:
        thrown(NoSuchMessageException)
        
        cleanup:
        if (originalMessage) {
            MessageUtils.metaClass.static.message = originalMessage
        } else {
            MessageUtils.metaClass = null
        }
    }
}