package com.lesofn.matrixboot.frame.spring;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.MethodParameter;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/**
 * json统一拦截处理模板
 * <p>
 * Authors: sofn
 * Version: 1.0  Created at 2015-10-03 00:15.
 */
@Order(1)
@ControllerAdvice(basePackages = "com.lesofn.matrixboot")
public class JsonResultValueHandler implements ResponseBodyAdvice<Object> {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public boolean supports(MethodParameter returnType, Class converterType) {
        return MappingJackson2HttpMessageConverter.class.isAssignableFrom(converterType);
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType, Class selectedConverterType, ServerHttpRequest request, ServerHttpResponse response) {
        //json保证apistatus在最前面
        ObjectNode result = objectMapper.createObjectNode();

        if (body == null) {
            body = objectMapper.createObjectNode();
        }

        if (StringUtils.equals(((ServletServerHttpRequest) request).getServletRequest().getServletPath(), "/error")) {
            result.put("apistatus", 0);
            if (body instanceof String) {
                try {
                    body = objectMapper.readTree((String) body);
                } catch (Exception e) {
                    // If parsing fails, keep the original string
                }
            } else {
                try {
                    body = objectMapper.readTree(body.toString());
                } catch (Exception e) {
                    // If parsing fails, convert to string
                    body = body.toString();
                }
            }
        } else {
            result.put("apistatus", 1);
        }
        result.set("result", objectMapper.valueToTree(body));
        return result;
    }
}
