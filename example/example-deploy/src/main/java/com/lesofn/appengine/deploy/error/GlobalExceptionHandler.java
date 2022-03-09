package com.lesofn.appengine.deploy.error;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Authors: sofn
 * Version: 1.0  Created at 15-6-7 20:55.
 */
@Component
public class GlobalExceptionHandler implements HandlerExceptionResolver {

    public static final String GlobalExceptionAttribute = GlobalExceptionHandler.class.getName() + ".ERROR";

    public ModelAndView resolveException(HttpServletRequest request, HttpServletResponse response, Object handler,
                                         Exception ex) {
        request.setAttribute(GlobalExceptionAttribute, ex);
        return null;
    }
}
