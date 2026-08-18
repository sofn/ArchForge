package com.lesofn.archforge.server.admin.advice;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotPermissionException;
import cn.dev33.satoken.exception.NotRoleException;
import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class AdminAuthExceptionHandler {

    @ExceptionHandler(NotLoginException.class)
    public ProblemDetail handleNotLogin(HttpServletRequest request, NotLoginException e) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, "未登录或登录已过期");
        problem.setTitle("Unauthorized");
        problem.setProperty("code", 401);
        problem.setInstance(URI.create(request.getRequestURI()));
        return problem;
    }

    @ExceptionHandler({
            NotRoleException.class, NotPermissionException.class
    })
    public ProblemDetail handleForbidden(HttpServletRequest request, RuntimeException e) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, "没有访问权限");
        problem.setTitle("Forbidden");
        problem.setProperty("code", 403);
        problem.setInstance(URI.create(request.getRequestURI()));
        return problem;
    }
}
