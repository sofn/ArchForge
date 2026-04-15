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
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

/**
 * 访问拒绝处理器 当用户已认证但没有权限访问某个资源时，会调用此处理器
 *
 * @author sofn
 */
@Slf4j
@Component
public class AccessDeniedHandlerImpl implements AccessDeniedHandler {

    @Override
    public void handle(
            HttpServletRequest request,
            HttpServletResponse response,
            AccessDeniedException accessDeniedException)
            throws IOException, ServletException {

        log.warn(
                "访问权限不足，拒绝访问 - URI: {}, User: {}, Error: {}",
                request.getRequestURI(),
                request.getRemoteUser(),
                accessDeniedException.getMessage());

        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
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
