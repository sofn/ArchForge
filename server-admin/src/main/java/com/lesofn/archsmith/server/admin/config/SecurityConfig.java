package com.lesofn.archsmith.server.admin.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lesofn.archsmith.infrastructure.auth.errors.AdminAuthErrorCode;
import com.lesofn.archsmith.infrastructure.config.ArchSmithConfig;
import com.lesofn.archsmith.infrastructure.frame.response.model.ResponseResult;
import com.lesofn.archsmith.server.admin.filter.JwtAuthenticationFilter;
import com.lesofn.archsmith.server.admin.service.login.AdminUserDetailsService;
import com.lesofn.archsmith.server.admin.service.login.TokenService;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/**
 * Spring Security配置类 主要配置登录流程逻辑涉及以下几个类
 *
 * @see AdminUserDetailsService#loadUserByUsername 用于登录流程通过用户名加载用户
 * @see this#unauthorizedHandler() 用于用户未授权或登录失败处理
 * @see this#logoutSuccessHandler() 用于退出登录成功后的逻辑
 * @see JwtAuthenticationFilter token的校验和刷新
 * @author lesofn
 */
@Slf4j
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final UserDetailsService userDetailsService;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final TokenService tokenService;
    private final ObjectMapper objectMapper;
    private final ArchSmithConfig archSmithConfig;
    private final Environment environment;

    /** 配置密码加密器 */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 鉴权管理类
     *
     * @see AdminUserDetailsService#loadUserByUsername
     */
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    /** 登录异常处理类 用户未登陆的话 在这个Bean中处理 */
    @Bean
    public AuthenticationEntryPoint unauthorizedHandler() {
        return (request, response, exception) -> {
            log.warn("Unauthorized access to: {}", request.getRequestURI());
            renderJsonResponse(
                    response,
                    HttpServletResponse.SC_UNAUTHORIZED,
                    ResponseResult.error(AdminAuthErrorCode.USER_AUTHFAIL));
        };
    }

    /** 退出成功处理类 返回成功 在SecurityConfig类当中 定义了/logout 路径对应处理逻辑 */
    @Bean
    public LogoutSuccessHandler logoutSuccessHandler() {
        return (request, response, authentication) -> {
            String authHeader = request.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                // 清除token相关缓存，并将 JTI 加入黑名单
                tokenService.removeToken(token);
                log.info("User logged out successfully");
            }
            renderJsonResponse(response, HttpServletResponse.SC_OK, ResponseResult.success("ok"));
        };
    }

    /** 配置CORS跨域（从 arch-smith.cors 配置读取白名单） */
    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        ArchSmithConfig.Cors corsConfig = archSmithConfig.getCors();

        config.setAllowCredentials(corsConfig.isAllowCredentials());

        // 白名单来源：如果配置了 "*" 且 allowCredentials=true，改用 AllowedOriginPattern
        if (corsConfig.getAllowedOrigins().size() == 1
                && "*".equals(corsConfig.getAllowedOrigins().getFirst())) {
            config.addAllowedOriginPattern("*");
        } else {
            corsConfig.getAllowedOrigins().forEach(config::addAllowedOrigin);
        }
        corsConfig.getAllowedMethods().forEach(config::addAllowedMethod);
        corsConfig.getAllowedHeaders().forEach(config::addAllowedHeader);
        config.setExposedHeaders(corsConfig.getExposedHeaders());
        config.setMaxAge(corsConfig.getMaxAge());

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }

    /** 配置安全过滤链 */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, CorsFilter corsFilter)
            throws Exception {
        boolean isProdProfile = environment.matchesProfiles("prod");

        http
                // 禁用CSRF保护，因为使用JWT不需要session
                .csrf(AbstractHttpConfigurer::disable)
                // 认证失败处理类
                .exceptionHandling(
                        exception -> exception.authenticationEntryPoint(unauthorizedHandler()))
                // 基于token，所以不需要session
                .sessionManagement(
                        session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // 配置logout
                .logout(
                        logout ->
                                logout.logoutUrl("/logout")
                                        .logoutSuccessHandler(logoutSuccessHandler()))
                // 配置请求授权规则
                .authorizeHttpRequests(
                        auth ->
                                auth
                                        // 对于登录login 注册register 验证码captchaImage 以及公共Api的请求允许匿名访问
                                        .requestMatchers(
                                                "/login",
                                                "/register",
                                                "/getConfig",
                                                "/captchaImage",
                                                "/refresh-token")
                                        .permitAll()
                                        .requestMatchers("/api/**")
                                        .permitAll()
                                        // 静态资源允许匿名访问
                                        .requestMatchers(
                                                HttpMethod.GET,
                                                "/",
                                                "/*.html",
                                                "/**",
                                                "/profile/**")
                                        .permitAll()
                                        .requestMatchers(
                                                HttpMethod.GET,
                                                "/**/*.html",
                                                "/**/*.css",
                                                "/**/*.js")
                                        .permitAll()
                                        // Swagger相关路径允许匿名访问
                                        .requestMatchers(
                                                "/swagger-ui/**",
                                                "/swagger-ui.html",
                                                "/v3/api-docs/**",
                                                "/swagger-resources/**",
                                                "/webjars/**")
                                        .permitAll()
                                        // H2 Console允许匿名访问（仅开发环境）
                                        .requestMatchers("/h2-console/**")
                                        .permitAll()
                                        // Actuator端点允许匿名访问
                                        .requestMatchers("/actuator/**")
                                        .permitAll()
                                        // Jolokia端点允许匿名访问
                                        .requestMatchers("/jolokia/**")
                                        .permitAll()
                                        // 其他所有请求都需要认证
                                        .anyRequest()
                                        .authenticated())
                // 安全响应头
                .headers(
                        headers -> {
                            headers
                                    // X-Content-Type-Options: nosniff (防止 MIME 嗅探)
                                    .contentTypeOptions(cto -> {})
                                    // X-Frame-Options: 禁用（H2 控制台等需要 iframe）
                                    .frameOptions(HeadersConfigurer.FrameOptionsConfig::disable)
                                    // Referrer-Policy: strict-origin-when-cross-origin
                                    .referrerPolicy(
                                            rp ->
                                                    rp.policy(
                                                            ReferrerPolicyHeaderWriter
                                                                    .ReferrerPolicy
                                                                    .STRICT_ORIGIN_WHEN_CROSS_ORIGIN));
                            // Content-Security-Policy: 基础策略
                            headers.contentSecurityPolicy(
                                    csp ->
                                            csp.policyDirectives(
                                                    "default-src 'self'; "
                                                            + "script-src 'self' 'unsafe-inline' 'unsafe-eval'; "
                                                            + "style-src 'self' 'unsafe-inline'; "
                                                            + "img-src 'self' data: blob:; "
                                                            + "connect-src 'self'"));
                            // HSTS 仅在 prod profile 启用
                            if (isProdProfile) {
                                headers.httpStrictTransportSecurity(
                                        hsts ->
                                                hsts.includeSubDomains(true)
                                                        .maxAgeInSeconds(31536000L));
                            }
                        });

        // 添加CORS filter
        http.addFilterBefore(corsFilter, LogoutFilter.class);
        // 添加JWT filter 需要一开始就通过token识别出登录用户 并放到上下文中
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /** 渲染JSON响应 */
    private void renderJsonResponse(HttpServletResponse response, int status, Object data)
            throws IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        try (PrintWriter writer = response.getWriter()) {
            writer.write(objectMapper.writeValueAsString(data));
            writer.flush();
        }
    }
}
