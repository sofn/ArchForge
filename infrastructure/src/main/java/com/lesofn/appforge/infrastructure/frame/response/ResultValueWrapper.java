package com.lesofn.appforge.infrastructure.frame.response;

import com.lesofn.appforge.common.errors.SystemErrorCode;
import com.lesofn.appforge.infrastructure.frame.response.model.ResponseResult;
import com.lesofn.appforge.infrastructure.frame.response.model.Result;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.apache.commons.lang3.Strings;
import org.springframework.core.MethodParameter;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/**
 * @author sofn
 * @version 2019-07-11 16:44
 */
@Order(0)
@RestControllerAdvice(basePackages = "com.lesofn.appforge")
public class ResultValueWrapper implements ResponseBodyAdvice<Object> {

    @Override
    public boolean supports(@Nullable MethodParameter returnType, @Nonnull Class converterType) {
        return JacksonJsonHttpMessageConverter.class.isAssignableFrom(converterType);
    }

    @Override
    public Object beforeBodyWrite(
            @Nullable Object body,
            @Nullable MethodParameter returnType,
            @Nullable MediaType selectedContentType,
            @Nullable Class selectedConverterType,
            @Nonnull ServerHttpRequest request,
            @Nonnull ServerHttpResponse response) {

        String requestPath =
                ((ServletServerHttpRequest) request).getServletRequest().getServletPath();

        // Skip response wrapping for OpenAPI/Swagger endpoints
        if (Strings.CS.startsWith(requestPath, "/v3/api-docs")
                || Strings.CS.startsWith(requestPath, "/swagger-ui")
                || Strings.CS.equals(requestPath, "/swagger-ui.html")
                || Strings.CS.startsWith(requestPath, "/swagger-resources")) {
            return body;
        }

        if (body == null) {
            return ResponseResult.success(null);
        } else if (body instanceof ResponseResult) {
            return body;
        } else if (body instanceof Result) {
            Result<?> result = (Result<?>) body;
            return ResponseResult.success(result.getData());
        }

        if (Strings.CS.equals(requestPath, "/error")) {
            return ResponseResult.error(SystemErrorCode.SYSTEM_ERROR.getCode(), body.toString());
        } else {
            return ResponseResult.success(body);
        }
    }
}
