package com.lesofn.matrixboot.frame.spring.context;

import com.lesofn.matrixboot.frame.context.RequestContext;
import com.lesofn.matrixboot.frame.context.ThreadLocalContext;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

/**
 * Authors: sofn
 * Version: 1.0  Created at 2015-09-06 22:56.
 */
public class RequestContextMethodArgumentResolver implements HandlerMethodArgumentResolver {
    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.getParameterType() == RequestContext.class;
    }

    @Override
    public Object resolveArgument(MethodParameter methodParameter, ModelAndViewContainer modelAndViewContainer, NativeWebRequest nativeWebRequest,
                                  WebDataBinderFactory webDataBinderFactory) throws Exception {
        return ThreadLocalContext.getRequestContext();
    }
}
