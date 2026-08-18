package com.lesofn.archforge.infrastructure.frame.response;

import com.lesofn.archforge.common.error.SystemErrorCode;
import com.lesofn.archforge.infrastructure.frame.response.model.ResponseResult;
import com.lesofn.archforge.infrastructure.frame.response.model.Result;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.core.MethodParameter;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/**
 * @author sofn
 * @version 2019-07-11 16:44
 */
@Order(0)
@RestControllerAdvice(basePackages = "com.lesofn.archforge")
public class ResultValueWrapper implements ResponseBodyAdvice<Object> {

    @Override
    public boolean supports(@Nullable MethodParameter returnType, @NonNull Class converterType) {
        if (returnType != null) {
            Class<?> parameterType = returnType.getParameterType();
            // Skip HTTP entity wrappers (ResponseEntity, HttpEntity) so void/binary bodies are
            // handled by Spring's HttpEntityMethodProcessor without interference.
            if (HttpEntity.class.isAssignableFrom(parameterType)) {
                return false;
            }
        }
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

        // Skip already committed responses and non-JSON/binary content types
        if (response instanceof ServletServerHttpResponse servletResponse
                && servletResponse.getServletResponse().isCommitted()) {
            return body;
        }
        if (selectedContentType != null && !isJsonCompatible(selectedContentType)) {
            return body;
        }

        if (body == null) {
            return ResponseResult.success(null);
        }

        return switch (body) {
            case ProblemDetail p -> p; // RFC 9457: pass through without wrapping
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

    private static boolean isJsonCompatible(@NonNull MediaType mediaType) {
        return mediaType.isCompatibleWith(MediaType.APPLICATION_JSON) || mediaType.isCompatibleWith(MediaType.TEXT_PLAIN) ||
                mediaType.isCompatibleWith(MediaType.TEXT_HTML);
    }
}
