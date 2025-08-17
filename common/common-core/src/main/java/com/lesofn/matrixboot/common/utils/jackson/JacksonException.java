package com.lesofn.matrixboot.common.utils.jackson;

/**
 * @author sofn
 */
public class JacksonException extends RuntimeException {

    public JacksonException(String message, Exception e) {
        super(message, e);
    }

}
