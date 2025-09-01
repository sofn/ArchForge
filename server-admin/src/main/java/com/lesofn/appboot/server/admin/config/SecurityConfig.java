package com.lesofn.appboot.server.admin.config;

import com.lesofn.appboot.server.admin.filter.JwtAuthenticationFilter;
import com.lesofn.appboot.server.admin.service.login.AdminUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security配置类
 *
 * @author lesofn
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    /**
     * 配置密码加密器
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 配置认证管理器
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    /**
     * 配置DaoAuthenticationProvider
     */
    @Bean
    public DaoAuthenticationProvider authenticationProvider(AdminUserDetailsService userDetailsService) {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    /**
     * 配置安全过滤链
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, JwtAuthenticationFilter jwtAuthenticationFilter) throws Exception {
        http
            // 禁用CSRF保护
            .csrf(AbstractHttpConfigurer::disable)
            // 禁用CORS
            .cors(AbstractHttpConfigurer::disable)
            // 禁用HTTP基本认证
            .httpBasic(AbstractHttpConfigurer::disable)
            // 禁用表单登录
            .formLogin(AbstractHttpConfigurer::disable)
            // 禁用注销功能
            .logout(AbstractHttpConfigurer::disable)
            // 设置会话管理策略为无状态
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            // 添加JWT认证过滤器
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            // 配置请求授权规则
            .authorizeHttpRequests(auth -> auth
                // 允许访问Swagger相关路径
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                // 允许访问Jolokia端点
                .requestMatchers("/jolokia/**").permitAll()
                // 允许访问健康检查端点
                .requestMatchers("/actuator/**").permitAll()
                // 允许访问登录端点
                .requestMatchers("/login").permitAll()
                // 其他所有请求都需要认证
                .anyRequest().authenticated()
            );

        return http.build();
    }
}