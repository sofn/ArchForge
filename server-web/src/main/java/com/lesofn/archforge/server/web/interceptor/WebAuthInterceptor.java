package com.lesofn.archforge.server.web.interceptor;

import tools.jackson.databind.ObjectMapper;
import com.lesofn.archforge.server.web.context.WebUserContext;
import com.lesofn.archforge.server.web.util.WebJwtTokenUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@RequiredArgsConstructor
public class WebAuthInterceptor implements HandlerInterceptor {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final WebJwtTokenUtil webJwtTokenUtil;
    private final ObjectMapper objectMapper;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (isPublicRequest(request)) {
            return true;
        }
        String token = extractToken(request);
        if (token == null || !webJwtTokenUtil.validateToken(token)) {
            writeUnauthorized(response);
            return false;
        }
        Long userId = webJwtTokenUtil.getUserId(token);
        String username = webJwtTokenUtil.getUsername(token);
        WebUserContext.set(userId, username);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
            throws Exception {
        WebUserContext.clear();
    }

    private boolean isPublicRequest(HttpServletRequest request) {
        String method = request.getMethod();
        String uri = request.getRequestURI();
        if ("OPTIONS".equalsIgnoreCase(method)) {
            return true;
        }
        if ("GET".equalsIgnoreCase(method) && isPublicArticlePath(uri)) {
            return true;
        }
        if ("GET".equalsIgnoreCase(method) && uri.startsWith("/web/file/")) {
            return true;
        }
        return "POST".equalsIgnoreCase(method) && uri.equals("/web/login");
    }

    private boolean isPublicArticlePath(String uri) {
        if (uri.equals("/web/articles")) {
            return true;
        }
        if (!uri.startsWith("/web/articles/")) {
            return false;
        }
        return !uri.equals("/web/articles/me") && !uri.startsWith("/web/articles/me/");
    }

    private String extractToken(HttpServletRequest request) {
        String header = request.getHeader(AUTHORIZATION_HEADER);
        if (header != null && header.startsWith(BEARER_PREFIX)) {
            return header.substring(BEARER_PREFIX.length());
        }
        return null;
    }

    private void writeUnauthorized(HttpServletResponse response) throws Exception {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        objectMapper.writeValue(response.getWriter(), Map.of("code", 401, "message", "未登录或 token 无效"));
    }
}
