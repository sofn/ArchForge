package com.lesofn.appengine.common.encrypt;

/**
 * 加密解密异常
 *
 * @author sofn
 *         Date: 10/15/13
 *         Time: 2:15 PM
 */
public class EncrypterException extends RuntimeException {

    public EncrypterException() {
        super();
    }

    public EncrypterException(String message) {
        super(message);
    }

    public EncrypterException(String message, Throwable cause) {
        super(message, cause);
    }

    public EncrypterException(Throwable cause) {
        super(cause);
    }

    protected EncrypterException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
