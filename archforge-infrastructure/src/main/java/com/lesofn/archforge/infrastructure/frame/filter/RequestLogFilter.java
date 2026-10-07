package com.lesofn.archforge.infrastructure.frame.filter;

import com.lesofn.archforge.common.error.exception.IErrorCodeException;
import com.lesofn.archforge.common.utils.GlobalConstants;
import com.lesofn.archforge.common.utils.ip.IpUtil;
import com.lesofn.archforge.infrastructure.frame.context.RequestContext;
import com.lesofn.archforge.infrastructure.frame.context.RequestIDGenerator;
import com.lesofn.archforge.infrastructure.frame.context.ScopedValueContext;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.Strings;
import org.jspecify.annotations.Nullable;
import org.slf4j.MDC;
import jakarta.servlet.ServletException;

@Slf4j
public class RequestLogFilter implements Filter {

    public RequestLogFilter(ObservationRegistry observationRegistry, @Nullable Tracer tracer) {
        this.observationRegistry = observationRegistry;
        this.tracer = tracer;
    }

    private static final RequestIDGenerator REQUEST_ID_GENERATOR = RequestIDGenerator.getInstance();
    private final ObservationRegistry observationRegistry;
    private final @Nullable Tracer tracer;

    @Override
    public void doFilter(
            ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        String path = request.getRequestURI();

        // Always bind RequestContext so downstream filters/loggers can access requestId.
        RequestContext context = new RequestContext(REQUEST_ID_GENERATOR.nextId());
        context.setOriginRequest(request);
        context.setIp(IpUtil.getRealIpAddr(request));
        MDC.put("requestId", context.getRequestId());

        // ScopedValue: entire filter chain runs within context scope, auto-cleanup on exit
        try {
            ScopedValueContext.runInScope(
                    context, () -> doFilterInScope(request, response, filterChain, context, path));
        } catch (IOException | ServletException e) {
            throw e;
        } catch (Exception e) {
            throw new ServletException(e);
        } finally {
            MDC.remove("requestId");
            MDC.remove("traceId");
            MDC.remove("spanId");
        }
    }

    private void doFilterInScope(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain,
            RequestContext context,
            String path)
            throws IOException, ServletException {
        if (isStaticOrSwagger(path)) {
            filterChain.doFilter(request, response);
            return;
        }

        // 不能叫 http.server.requests：那是 Spring Boot 自己的 HTTP 指标名，重名会让每个请求在 sum()/告警比值里算两遍。
        // 原始路径（含 id）只作 trace 属性（高基数），绝不能成为指标标签。
        Observation observation = Observation.start("archforge.request", observationRegistry);
        // scope 打开后本请求的 span 才是 current：下游日志/子 span 才能关联到它
        try (Observation.Scope ignored = observation.openScope()) {
            observation.lowCardinalityKeyValue("http.method", request.getMethod());
            observation.highCardinalityKeyValue("http.path", path);
            bindTraceIds();
            filterChain.doFilter(request, response);
        } catch (Exception e) {
            observation.error(e);
            // 此处拦截也必须抛出，否则不执行ErrorHandlerResource
            if (e instanceof IErrorCodeException errorCodeEx) {
                log.error(
                        "EngineException error",
                        e.getMessage() + " " + errorCodeEx.getErrorInfo().getMsg());
            } else if (e.getCause()instanceof IErrorCodeException causeErrorCodeEx) {
                log.error(
                        "EngineException error",
                        e.getCause().getMessage() + " " + causeErrorCodeEx.getErrorInfo().getMsg());
            } else {
                log.error("filterChain.doFilter error", e);
            }
            throw e;
        } finally {
            observation.lowCardinalityKeyValue(
                    "http.status", String.valueOf(response.getStatus()));
            observation.stop();
        }
    }

    /** 把当前 span 的 traceId/spanId 放进 MDC，log4j2 pattern 里的 %X{traceId} 才有值。 */
    private void bindTraceIds() {
        Span span = tracer == null ? null : tracer.currentSpan();
        if (span != null) {
            MDC.put("traceId", span.context().traceId());
            MDC.put("spanId", span.context().spanId());
        }
    }

    private static boolean isStaticOrSwagger(String path) {
        return Strings.CS.startsWithAny(path, "/webjars", "/static", "/js", "/css", "/libs", "/WEB-INF") || Strings.CS
                .startsWithAny(path, "/swagger-", "/v3/api-docs") || Strings.CS.startsWithAny(path,
                        GlobalConstants.STATIC_RESOURCE_EXTENSIONS.toArray(String[]::new));
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void destroy() {
    }
}
