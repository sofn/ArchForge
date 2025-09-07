package com.lesofn.appboot.infrastructure.frame.response;

import com.lesofn.appboot.infrastructure.frame.response.model.Result;
import com.lesofn.appboot.infrastructure.frame.response.model.ResponseResult;
import com.lesofn.appboot.common.error.system.SystemErrorCode;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.MethodParameter;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import javax.annotation.Nullable;
import javax.annotation.Nonnull;

/**
 * @author sofn
 * @version 2019-07-11 16:44
 */
@Order(0)
@RestControllerAdvice(basePackages = "com.lesofn.appboot")
public class ResultValueWrapper implements ResponseBodyAdvice<Object> {

    @Override
    public boolean supports(@Nullable MethodParameter returnType, @Nonnull Class converterType) {
        return MappingJackson2HttpMessageConverter.class.isAssignableFrom(converterType);
    }

    @Override
    public Object beforeBodyWrite(@Nullable Object body, @Nullable  MethodParameter returnType, @Nullable MediaType selectedContentType,
                                  @Nullable Class selectedConverterType, @Nonnull ServerHttpRequest request, @Nonnull ServerHttpResponse response) {
        if (body == null) {
            return ResponseResult.success(null);
        } else if (body instanceof ResponseResult) {
            return body;
        } else if (body instanceof Result) {
            Result<?> result = (Result<?>) body;
            return ResponseResult.success(result.getData());
        }

        if (StringUtils.equals(((ServletServerHttpRequest) request).getServletRequest().getServletPath(), "/error")) {
            return ResponseResult.error(SystemErrorCode.SYSTEM_ERROR.getCode(), body.toString());
        } else {
            return ResponseResult.success(body);
        }
    }
}
