package com.lesofn.archsmith.server.admin.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lesofn.archsmith.common.errors.SystemErrorCode;
import com.lesofn.archsmith.infrastructure.frame.response.model.ResponseResult;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

/**
 * 认证失败处理器 当用户未认证时访问需要认证的资源，会调用此处理器
 *
 * @author sofn
 */
@Slf4j
@Component
public class AuthenticationEntryPointImpl implements AuthenticationEntryPoint {

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException)
            throws IOException, ServletException {

        log.warn(
                "认证失败，无法访问系统资源 - URI: {}, Error: {}",
                request.getRequestURI(),
                authException.getMessage());

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        ResponseResult<String> result =
                ResponseResult.error(
                        SystemErrorCode.COMMON_REQUEST_FORBIDDEN.getCode(),
                        SystemErrorCode.COMMON_REQUEST_FORBIDDEN.getMsg());

        ObjectMapper objectMapper = new ObjectMapper();
        String jsonResult = objectMapper.writeValueAsString(result);

        response.getWriter().write(jsonResult);
    }
}
