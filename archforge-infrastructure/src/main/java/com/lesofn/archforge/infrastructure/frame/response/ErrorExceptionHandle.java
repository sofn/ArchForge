package com.lesofn.archforge.infrastructure.frame.response;

import com.google.common.base.Joiner;
import com.lesofn.archforge.common.error.exception.IErrorCodeException;
import com.lesofn.archforge.common.error.manager.ErrorInfo;
import com.lesofn.archforge.common.error.system.HttpCodes;
import com.lesofn.archforge.common.error.SystemErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import io.micrometer.core.instrument.MeterRegistry;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.TypeMismatchException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * RFC 9457 Problem Details error handler.
 *
 * @author sofn
 * @version 2019-07-11 16:56
 */
@Slf4j
@RestControllerAdvice
public class ErrorExceptionHandle {
    public static final Joiner.MapJoiner JOINER = Joiner.on(",").withKeyValueSeparator(": ");

    /** 业务错误按错误码计数：业务错误沿用 HTTP 200（契约），按状态码统计的监控看不到它们。 */
    static final String BUSINESS_ERRORS_METRIC = "archforge.business.errors";

    /** 500 是否回显异常类名与根因消息（SQL、表名等）。只有 dev/test 打开。 */
    private final boolean exposeDetails;
    private final @Nullable MeterRegistry meterRegistry;

    @Autowired
    public ErrorExceptionHandle(@Value("${arch-forge.error.expose-details:false}") boolean exposeDetails,
            ObjectProvider<MeterRegistry> meterRegistry) {
        this(exposeDetails, meterRegistry.getIfAvailable());
    }

    ErrorExceptionHandle(boolean exposeDetails, @Nullable MeterRegistry meterRegistry) {
        this.exposeDetails = exposeDetails;
        this.meterRegistry = meterRegistry;
    }

    @ExceptionHandler(value = Throwable.class)
    public ProblemDetail processException(HttpServletRequest request, Exception e) {
        ProblemDetail clientError = clientError(e);
        if (clientError != null) {
            // 客户端的错误（路径不存在、方法不对、参数缺失/类型不对、请求体读不了）不是服务端故障：
            // 用它自己的 4xx，不打 ERROR 堆栈（server-web 没开 Boot 的 problemdetails，之前全成了 500）
            log.info("{} {} -> {}: {}", request.getMethod(), request.getRequestURI(), clientError.getStatus(), e.getMessage());
            clientError.setProperty("code", clientError.getStatus());
            clientError.setInstance(URI.create(request.getRequestURI()));
            return clientError;
        }
        Pair<Throwable, String> pair = getExceptionMessage(e);
        if (e instanceof IErrorCodeException errorCodeEx) {
            if (e.getCause() != null) {
                log.error("error, request: {}", parseParam(request), e);
            } else {
                log.error("error: {}, request: {}", pair.getRight(), parseParam(request), e);
            }
            ErrorInfo errorInfo = errorCodeEx.getErrorInfo();
            if (errorInfo != null) {
                if (meterRegistry != null) {
                    meterRegistry.counter(BUSINESS_ERRORS_METRIC, "code", String.valueOf(errorInfo.getCode())).increment();
                }
                ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.OK, errorInfo.getMsg());
                problem.setTitle("Business Error");
                problem.setProperty("code", errorInfo.getCode());
                problem.setInstance(URI.create(request.getRequestURI()));
                return problem;
            }
            ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                    HttpStatus.INTERNAL_SERVER_ERROR, exposeDetails ? pair.getRight() : SystemErrorCode.SYSTEM_ERROR.getMsg());
            problem.setTitle("System Error");
            problem.setProperty("code", SystemErrorCode.SYSTEM_ERROR.getCode());
            return problem;
        }
        log.error("error, request: {}", parseParam(request), e);
        // 原始异常只进日志；客户端只拿到通用消息（除非 dev/test 显式打开 expose-details）
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.INTERNAL_SERVER_ERROR,
                exposeDetails ? pair.getLeft().getClass().getSimpleName() + ": " + pair.getRight()
                        : SystemErrorCode.SYSTEM_ERROR.getMsg());
        problem.setTitle("System Error");
        problem.setProperty("code", SystemErrorCode.SYSTEM_ERROR.getCode());
        problem.setInstance(URI.create(request.getRequestURI()));
        return problem;
    }

    /** Spring MVC 自身抛出的客户端错误；其余异常返回 null，按服务端错误处理。 */
    private static @Nullable ProblemDetail clientError(Throwable e) {
        if (e instanceof ErrorResponse errorResponse && errorResponse.getStatusCode().is4xxClientError()) {
            return errorResponse.getBody();
        }
        if (e instanceof HttpMessageNotReadableException) {
            // 不回显解析器细节（行列号、源片段）
            return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Failed to read request");
        }
        if (e instanceof TypeMismatchException mismatch) {
            String name = mismatch.getPropertyName() == null ? "parameter" : mismatch.getPropertyName();
            return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Invalid value for '" + name + "'");
        }
        return null;
    }

    /** 请求参数异常 */
    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public ProblemDetail badRequestException(
            HttpServletRequest request, MethodArgumentNotValidException e) {
        List<String> errors = e.getBindingResult().getFieldErrors().stream()
                .map(
                        (FieldError fieldError) -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
                .toList();

        log.error("BadRequestException, request: {}", parseParam(request), e);
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Validation failed");
        problem.setTitle("Invalid Request");
        problem.setProperty("code", HttpCodes.BAD_REQUEST.getStatus());
        problem.setProperty("errors", errors);
        problem.setInstance(URI.create(request.getRequestURI()));
        return problem;
    }

    public String parseParam(HttpServletRequest request) {
        Map<String, String[]> parameterMap = request.getParameterMap();
        HashMap<String, String> map = new HashMap<>(parameterMap.size());
        for (Map.Entry<String, String[]> entry : parameterMap.entrySet()) {
            map.put(
                    entry.getKey(),
                    ArrayUtils.isNotEmpty(entry.getValue()) ? entry.getValue()[0] : "");
        }
        return JOINER.join(map);
    }

    public Pair<Throwable, String> getExceptionMessage(Throwable e) {
        Throwable detail = e;
        while (detail.getCause() != null) {
            detail = detail.getCause();
        }
        return ImmutablePair.of(detail, detail.getMessage());
    }
}
