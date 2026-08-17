package com.lesofn.archforge.server.admin.filter;

import com.lesofn.archforge.infrastructure.auth.LoginContext;
import com.lesofn.archforge.infrastructure.auth.model.SystemLoginUser;
import com.lesofn.archforge.infrastructure.auth.stp.StpAdminUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Temporary bridge: populate Spring Security context from sa-token session
 * until method security is fully migrated.
 */
@Slf4j
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        if (StpAdminUtil.isLogin() && SecurityContextHolder.getContext().getAuthentication() == null) {
            SystemLoginUser loginUser = LoginContext.findAdminUser().orElse(null);
            if (loginUser != null) {
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(loginUser, null, loginUser
                        .getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }
        chain.doFilter(request, response);
    }
}
