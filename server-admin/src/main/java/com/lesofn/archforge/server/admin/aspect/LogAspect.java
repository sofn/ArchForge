package com.lesofn.archforge.server.admin.aspect;

import com.lesofn.archforge.common.utils.ip.IpRegionUtil;
import com.lesofn.archforge.common.utils.ip.IpUtil;
import com.lesofn.archforge.infrastructure.annotation.Log;
import com.lesofn.archforge.infrastructure.frame.context.ScopedValueContext;
import com.lesofn.archforge.user.domain.SysOperLog;
import com.lesofn.archforge.user.service.SysOperLogService;
import eu.bitwalker.useragentutils.UserAgent;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

/**
 * 操作日志切面。
 *
 * <p>
 * 拦截 {@link Log} 注解的方法，方法执行完成后写入 sys_oper_log。
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class LogAspect {

    private final SysOperLogService operLogService;

    @Around("@annotation(logAnnotation)")
    public Object around(ProceedingJoinPoint point, Log logAnnotation) throws Throwable {
        MethodSignature signature = (MethodSignature) point.getSignature();

        HttpServletRequest request = ScopedValueContext.getServletRequest();
        String userAgentHeader = request == null ? "" : request.getHeader("User-Agent");
        UserAgent userAgent = UserAgent.parseUserAgentString(userAgentHeader);
        String ip = request == null ? "" : IpUtil.getRealIpAddr(request);

        SysOperLog operLog = new SysOperLog();
        operLog.setUsername(getCurrentUsername());
        operLog.setModule(resolveModule(logAnnotation, signature));
        operLog.setSummary(resolveSummary(logAnnotation, signature));
        operLog.setIp(ip);
        operLog.setAddress(IpRegionUtil.getBriefLocationByIp(ip));
        operLog.setSystemName(userAgent.getOperatingSystem().getName());
        operLog.setBrowser(userAgent.getBrowser().getName());
        operLog.setOperatingTime(LocalDateTime.now());

        Integer status = 1;
        try {
            return point.proceed();
        } catch (Throwable e) {
            status = 0;
            throw e;
        } finally {
            operLog.setStatus(status);
            try {
                operLogService.create(operLog);
            } catch (Exception ex) {
                log.warn("Failed to save operation log", ex);
            }
        }
    }

    private String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return "";
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof UserDetails userDetails) {
            return userDetails.getUsername();
        }
        return authentication.getName() == null ? "" : authentication.getName();
    }

    private String resolveModule(Log annotation, MethodSignature signature) {
        if (annotation != null && !annotation.module().isBlank()) {
            return annotation.module();
        }
        Class<?> declaringType = signature.getDeclaringType();
        Tag tag = declaringType.getAnnotation(Tag.class);
        if (tag != null) {
            return tag.name();
        }
        return "";
    }

    private String resolveSummary(Log annotation, MethodSignature signature) {
        if (annotation != null && !annotation.summary().isBlank()) {
            return annotation.summary();
        }
        Operation operation = signature.getMethod().getAnnotation(Operation.class);
        if (operation != null) {
            return operation.summary();
        }
        return "";
    }
}
