package com.lesofn.archforge.server.web.auth;

import com.lesofn.archforge.infrastructure.auth.LoginContext;
import com.lesofn.archforge.user.api.domain.SysUser;
import com.lesofn.archforge.user.api.service.SysUserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@Profile({
        "dev", "test"
})
@ConditionalOnProperty(prefix = "arch-forge.mock-auth", name = "enabled", havingValue = "true")
@RequiredArgsConstructor
public class MockWebAuthInterceptor implements HandlerInterceptor, Ordered {

    public static final String MOCK_USERID_HEADER = "x-archforge-userid";

    private final SysUserService sysUserService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String mockUserId = request.getHeader(MOCK_USERID_HEADER);
        if (mockUserId == null || mockUserId.isBlank()) {
            return true;
        }
        try {
            Long userId = Long.parseLong(mockUserId.trim());
            String username = sysUserService.findById(userId)
                    .map(SysUser::getUsername)
                    .orElse("mock-user-" + userId);
            LoginContext.setWebUser(userId, username);
        } catch (NumberFormatException ignored) {
            return true;
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        LoginContext.clearWebUser();
    }

    @Override
    public int getOrder() { return Ordered.HIGHEST_PRECEDENCE; }
}
