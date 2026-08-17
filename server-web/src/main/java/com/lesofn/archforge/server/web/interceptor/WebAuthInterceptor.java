package com.lesofn.archforge.server.web.interceptor;

import com.lesofn.archforge.infrastructure.auth.stp.StpWebUtil;
import com.lesofn.archforge.server.web.context.WebUserContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import tools.jackson.databind.ObjectMapper;

@Component
@RequiredArgsConstructor
public class WebAuthInterceptor implements HandlerInterceptor {

    private final ObjectMapper objectMapper;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (isPublicRequest(request) || WebUserContext.getUserId() != null) {
            return true;
        }
        if (!StpWebUtil.isLogin()) {
            writeUnauthorized(response);
            return false;
        }
        Long userId = StpWebUtil.getLoginIdAsLong();
        String username = (String) StpWebUtil.getSession().get("username");
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
        if ("GET".equalsIgnoreCase(method) && uri.equals("/web/categories")) {
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

    private void writeUnauthorized(HttpServletResponse response) throws Exception {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        objectMapper.writeValue(response.getWriter(), Map.of("code", 401, "message", "未登录或 token 无效"));
    }
}
