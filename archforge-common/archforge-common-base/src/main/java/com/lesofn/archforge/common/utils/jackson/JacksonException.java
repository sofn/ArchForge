package com.lesofn.archforge.common.utils.jackson;

/**
 * Jackson 序列化/反序列化异常。
 *
 * @author sofn
 */
public class JacksonException extends RuntimeException {

    public JacksonException(String message, Exception e) {
        super(message, e);
    }
}
