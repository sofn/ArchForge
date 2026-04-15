package com.lesofn.archsmith.infrastructure.frame.response;

import com.google.common.base.Joiner;
import com.lesofn.archsmith.common.error.exception.IErrorCodeException;
import com.lesofn.archsmith.common.error.manager.ErrorInfo;
import com.lesofn.archsmith.common.error.system.HttpCodes;
import com.lesofn.archsmith.common.errors.SystemErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.FieldError;
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

    @ExceptionHandler(value = Throwable.class)
    public ProblemDetail processException(HttpServletRequest request, Exception e) {
        Pair<Throwable, String> pair = getExceptionMessage(e);
        if (e instanceof IErrorCodeException errorCodeEx) {
            if (e.getCause() != null) {
                log.error("error, request: {}", parseParam(request), e);
            } else {
                log.error("error: {}, request: {}", pair.getRight(), parseParam(request), e);
            }
            ErrorInfo errorInfo = errorCodeEx.getErrorInfo();
            if (errorInfo != null) {
                ProblemDetail problem =
                        ProblemDetail.forStatusAndDetail(HttpStatus.OK, errorInfo.getMsg());
                problem.setTitle("Business Error");
                problem.setProperty("code", errorInfo.getCode());
                problem.setInstance(URI.create(request.getRequestURI()));
                return problem;
            }
            ProblemDetail problem =
                    ProblemDetail.forStatusAndDetail(
                            HttpStatus.INTERNAL_SERVER_ERROR, pair.getRight());
            problem.setTitle("System Error");
            problem.setProperty("code", SystemErrorCode.SYSTEM_ERROR.getCode());
            return problem;
        }
        log.error("error, request: {}", parseParam(request), e);
        ProblemDetail problem =
                ProblemDetail.forStatusAndDetail(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        pair.getLeft().getClass().getSimpleName() + ": " + pair.getRight());
        problem.setTitle("System Error");
        problem.setProperty("code", SystemErrorCode.SYSTEM_ERROR.getCode());
        problem.setInstance(URI.create(request.getRequestURI()));
        return problem;
    }

    /** 请求参数异常 */
    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public ProblemDetail badRequestException(
            HttpServletRequest request, MethodArgumentNotValidException e) {
        List<String> errors =
                e.getBindingResult().getFieldErrors().stream()
                        .map(
                                (FieldError fieldError) ->
                                        fieldError.getField()
                                                + ": "
                                                + fieldError.getDefaultMessage())
                        .toList();

        log.error("BadRequestException, request: {}", parseParam(request), e);
        ProblemDetail problem =
                ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Validation failed");
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
