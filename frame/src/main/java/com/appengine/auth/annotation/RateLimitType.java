package com.appengine.auth.annotation;

import com.appengine.auth.model.AuthExcepFactor;
import com.appengine.common.exception.ExcepFactor;
import com.appengine.frame.context.RequestContext;

import java.util.Map;

/**
 * @author sofn
 * @version 1.0 Created at: 2015-10-08 12:03
 */
public enum RateLimitType {
    IP(AuthExcepFactor.E_IP_RATE_LIMIT),
    IPMETHOD(AuthExcepFactor.E_IP_RATE_LIMIT),
    USER(AuthExcepFactor.E_USER_RATE_LIMIT),
    USERMETHOD(AuthExcepFactor.E_USER_RATE_LIMIT),
    PARAM(AuthExcepFactor.E_API_RATE_LIMIT);

    ExcepFactor excepFactor;

    RateLimitType(ExcepFactor excepFactor) {
        this.excepFactor = excepFactor;
    }

    public ExcepFactor getExcepFactor() {
        return excepFactor;
    }

    public String getParamString(RequestContext rc) {
        switch (this) {
            case IP:
                return "i_" + rc.getIp();
            case IPMETHOD:
                return "i_" + rc.getOriginRequest().getRequestURI() + rc.getIp();
            case USER:
                return "u_" + rc.getCurrentUid();
            case USERMETHOD:
                return "u_" + rc.getOriginRequest().getRequestURI() + rc.getCurrentUid();
            case PARAM:
                return "p_" + getRequestParamString(rc.getOriginRequest().getParameterMap());
            default:
                return "error";
        }
    }

    private String getRequestParamString(Map<String, String[]> parameters) {
        StringBuilder paramBuf = new StringBuilder();

        for (Map.Entry<String, String[]> e : parameters.entrySet()) {
            String key = e.getKey();
            String[] values = e.getValue();
            for (String value : values) {
                paramBuf.append(key).append(":").append(value);
                paramBuf.append("_");
            }
        }
        if (paramBuf.length() > 0 && paramBuf.charAt(paramBuf.length() - 1) == '_') {
            paramBuf.deleteCharAt(paramBuf.length() - 1);
        }
        return paramBuf.toString();
    }


}
