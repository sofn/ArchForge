package com.lesofn.archforge.infrastructure.frame.filter;

import com.lesofn.archforge.common.error.exception.IErrorCodeException;
import com.lesofn.archforge.common.utils.GlobalConstants;
import com.lesofn.archforge.common.utils.ip.IpUtil;
import com.lesofn.archforge.infrastructure.frame.context.RequestContext;
import com.lesofn.archforge.infrastructure.frame.context.RequestIDGenerator;
import com.lesofn.archforge.infrastructure.frame.context.ScopedValueContext;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.Strings;
import org.slf4j.MDC;
import jakarta.servlet.ServletException;

@Slf4j
@RequiredArgsConstructor
public class RequestLogFilter implements Filter {

    private static final RequestIDGenerator REQUEST_ID_GENERATOR = RequestIDGenerator.getInstance();
    private final ObservationRegistry observationRegistry;

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

        Observation observation = Observation.start("http.server.requests", observationRegistry);
        try {
            observation.lowCardinalityKeyValue("http.method", request.getMethod());
            observation.lowCardinalityKeyValue("http.path", path);
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
