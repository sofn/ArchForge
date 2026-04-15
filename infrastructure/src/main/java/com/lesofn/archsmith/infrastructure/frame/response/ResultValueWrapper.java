package com.lesofn.archsmith.infrastructure.frame.response;

import com.lesofn.archsmith.common.errors.SystemErrorCode;
import com.lesofn.archsmith.infrastructure.frame.response.model.ResponseResult;
import com.lesofn.archsmith.infrastructure.frame.response.model.Result;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
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
@RestControllerAdvice(basePackages = "com.lesofn.archsmith")
public class ResultValueWrapper implements ResponseBodyAdvice<Object> {

    @Override
    public boolean supports(@Nullable MethodParameter returnType, @NonNull Class converterType) {
        return JacksonJsonHttpMessageConverter.class.isAssignableFrom(converterType);
    }

    @Override
    public Object beforeBodyWrite(
            @Nullable Object body,
            @Nullable MethodParameter returnType,
            @Nullable MediaType selectedContentType,
            @Nullable Class selectedConverterType,
            @NonNull ServerHttpRequest request,
            @NonNull ServerHttpResponse response) {

        String requestPath =
                ((ServletServerHttpRequest) request).getServletRequest().getServletPath();

        // Skip response wrapping for OpenAPI/Swagger endpoints
        if (requestPath.startsWith("/v3/api-docs")
                || requestPath.startsWith("/swagger-ui")
                || requestPath.equals("/swagger-ui.html")
                || requestPath.startsWith("/swagger-resources")) {
            return body;
        }

        return switch (body) {
            case null -> ResponseResult.success(null);
            case ResponseResult<?> r -> r;
            case Result<?> r -> ResponseResult.success(r.getData());
            default -> {
                if (requestPath.equals("/error")) {
                    yield ResponseResult.error(
                            SystemErrorCode.SYSTEM_ERROR.getCode(), body.toString());
                } else {
                    yield ResponseResult.success(body);
                }
            }
        };
    }
}
