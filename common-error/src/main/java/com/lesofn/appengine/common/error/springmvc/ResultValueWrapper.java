package com.lesofn.appengine.common.error.springmvc;

import com.lesofn.appengine.common.error.response.CustomResult;
import com.lesofn.appengine.common.error.response.Result;
import com.lesofn.appengine.common.error.system.SystemErrorCodes;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.MethodParameter;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import javax.annotation.Nullable;

/**
 * @author sofn
 * @version 2019-07-11 16:44
 */
@Order(0)
@RestControllerAdvice
public class ResultValueWrapper implements ResponseBodyAdvice<Object> {

    @Override
    public boolean supports(MethodParameter returnType, Class converterType) {
        return HttpMessageConverter.class.isAssignableFrom(converterType);
    }

    @Override
    public Object beforeBodyWrite(@Nullable Object body, MethodParameter returnType, MediaType selectedContentType,
                                  Class selectedConverterType, ServerHttpRequest request, ServerHttpResponse response) {
        if (body instanceof Result) {
            return body;
        }

        if (body instanceof CustomResult) {
            return ((CustomResult<?>) body).getData();
        }

        if (StringUtils.equals(((ServletServerHttpRequest) request).getServletRequest().getServletPath(), "/error")) {
            return Result.error(SystemErrorCodes.SYSTEM_ERROR.getCode(), body.toString());
        } else {
            return Result.success(body);
        }
    }
}
