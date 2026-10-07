package com.lesofn.archforge.infrastructure.frame.response;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.lesofn.archforge.common.error.SystemErrorCode;
import com.lesofn.archforge.common.error.system.SystemException;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.util.Objects;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpMethod;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.mock.http.MockHttpInputMessage;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * The catch-all handler must not turn client errors into 500s: server-web (which does not enable Spring Boot's
 * problem-details handler) answered every unmapped path with "500 NoResourceFoundException" and an ERROR stack trace.
 */
class ErrorExceptionHandleTest {

    private final SimpleMeterRegistry meters = new SimpleMeterRegistry();
    /** production default: details are not exposed */
    private final ErrorExceptionHandle handler = new ErrorExceptionHandle(false, meters);
    private final MockHttpServletRequest request = new MockHttpServletRequest("GET", "/nope");

    @Test
    void unmappedPathIs404() {
        ProblemDetail problem = handler.processException(request,
                new NoResourceFoundException(HttpMethod.GET, "/nope", "nope"));

        assertEquals(404, problem.getStatus());
        assertEquals("/nope", String.valueOf(problem.getInstance()));
    }

    @Test
    void wrongMethodIs405() {
        assertEquals(405, handler.processException(request, new HttpRequestMethodNotSupportedException("DELETE"))
                .getStatus());
    }

    @Test
    void missingParameterIs400() {
        assertEquals(400, handler.processException(request, new MissingServletRequestParameterException("page", "int"))
                .getStatus());
    }

    @Test
    void unreadableBodyIs400WithoutParserInternals() throws NoSuchMethodException {
        ProblemDetail problem = handler.processException(request,
                new HttpMessageNotReadableException("JSON parse error: Unexpected character ('}' (code 125)) at [Source: REDACTED]", new MockHttpInputMessage(new byte[0])));

        assertEquals(400, problem.getStatus());
        assertFalse(Objects.toString(problem.getDetail()).contains("JSON parse error"));
    }

    @Test
    void argumentTypeMismatchIs400() throws NoSuchMethodException {
        MethodParameter parameter = new MethodParameter(ErrorExceptionHandleTest.class.getDeclaredMethod("sample",
                Long.class), 0);

        ProblemDetail problem = handler.processException(request,
                new MethodArgumentTypeMismatchException("abc", Long.class, "id", parameter, null));

        assertEquals(400, problem.getStatus());
    }

    @Test
    void unexpectedFailureStays500() {
        assertEquals(500, handler.processException(request, new IllegalStateException("boom")).getStatus());
    }

    /** SEC-M5: class names and root-cause messages (SQL, table names, ...) never reach the client by default. */
    @Test
    void serverFailureDetailIsGenericUnlessDetailsAreExposed() {
        ProblemDetail problem = handler.processException(request,
                new IllegalStateException("wrap", new IllegalArgumentException("relation \"sys_user\" does not exist")));

        String detail = Objects.toString(problem.getDetail());
        assertFalse(detail.contains("sys_user") || detail.contains("IllegalArgumentException"), detail);

        ProblemDetail dev = new ErrorExceptionHandle(true, meters).processException(request,
                new IllegalStateException("wrap", new IllegalArgumentException("relation \"sys_user\" does not exist")));
        assertTrue(Objects.toString(dev.getDetail()).contains("sys_user"), "dev/test keep the details");
    }

    /** Business errors stay HTTP 200 (contract) — so they are counted by code for monitoring instead. */
    @Test
    void businessErrorsAreCountedByCode() {
        ProblemDetail problem = handler.processException(request, new SystemException(SystemErrorCode.E_RATE_LIMIT_EXCEEDED));

        assertEquals(200, problem.getStatus());
        double count = meters.counter("archforge.business.errors", "code",
                String.valueOf(SystemErrorCode.E_RATE_LIMIT_EXCEEDED.getCode())).count();
        assertEquals(1.0, count);
    }

    @SuppressWarnings("unused")
    private void sample(Long id) {
    }
}
