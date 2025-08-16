package com.lesofn.matrixboot.frame.spring;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.support.spring.FastJsonHttpMessageConverter4;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.MethodParameter;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
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

    @Override
    public boolean supports(MethodParameter returnType, Class converterType) {
        return FastJsonHttpMessageConverter4.class.isAssignableFrom(converterType);
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType, Class selectedConverterType, ServerHttpRequest request, ServerHttpResponse response) {
        //json保证apistatus在最前面
        JSONObject result = new JSONObject(true);

        if (body == null) {
            body = new JSONObject();
        }

        if (StringUtils.equals(((ServletServerHttpRequest) request).getServletRequest().getServletPath(), "/error")) {
            result.put("apistatus", 0);
            if (body instanceof String) {
                body = JSON.parse((String) body);
            } else {
                body = JSON.parse(body.toString());
            }
        } else {
            result.put("apistatus", 1);
        }
        result.put("result", body);
        return result;
    }
}
