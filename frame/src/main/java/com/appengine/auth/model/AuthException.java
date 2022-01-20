package com.appengine.auth.model;


import com.appengine.common.exception.ExcepFactor;
import com.appengine.common.exception.EngineException;

/**
 * @author sofn
 */
public class AuthException extends EngineException {

    private static final long serialVersionUID = 7420542113978073952L;

    public AuthException(ExcepFactor factor, Object[] args) {
        super(factor, args);
    }

    public AuthException(ExcepFactor factor) {
        super(factor);
    }

    public AuthException(ExcepFactor factor, String msg) {
        super(factor, msg);
    }

    public AuthException(Exception e) {
        super(e);
    }

    public AuthException() {
        super(AuthExcepFactor.E_USER_AUTHFAIL);
    }

}
