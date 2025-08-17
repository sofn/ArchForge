package com.lesofn.matrixboot.frame.filters;

import com.lesofn.matrixboot.common.exception.EngineException;
import com.lesofn.matrixboot.common.utils.GlobalConstants;
import com.lesofn.matrixboot.frame.context.RequestContext;
import com.lesofn.matrixboot.frame.context.ThreadLocalContext;
import com.lesofn.matrixboot.frame.utils.RequestLogRecord;
import com.lesofn.matrixboot.frame.utils.ResponseWrapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.MDC;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
public class RequestLogFilter implements Filter {

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        String path = request.getRequestURI();
        if (StringUtils.startsWithAny(path, "/webjars", "/static", "/js", "/css", "/libs", "/WEB-INF")
                || StringUtils.endsWithAny(path, GlobalConstants.staticResourceArray)) {
            filterChain.doFilter(request, response);
            return;
        }

        RequestContext context = ThreadLocalContext.getRequestContext();
        MDC.put("requestId", context.getRequestId());

        response = new ResponseWrapper(response);
        long startTime = System.currentTimeMillis();
        try {
            filterChain.doFilter(request, response);
        } catch (Exception e) {
            //此处拦截也必须抛出，否则不执行ErrorHandlerResource
            if (e instanceof EngineException) {
                log.error("EngineException error", e.getMessage() + " " + ((EngineException) e).getErrorMsgCn());
            } else if (e.getCause() instanceof EngineException) {
                log.error("EngineException error", e.getCause().getMessage() + " " + ((EngineException) e.getCause()).getErrorMsgCn());
            } else {
                log.error("filterChain.doFilter error", e);
            }
            throw e;
        } finally {
            //如果是错误页面 或 没有错误的第一次执行
            if (StringUtils.equals("/error", path) || request.getAttribute("org.springframework.boot.autoconfigure.web.DefaultErrorAttributes.ERROR") == null) {
                long endTime = System.currentTimeMillis();
                RequestLogRecord record = new RequestLogRecord();
                record.setRequestId(context.getRequestId());
                record.setIp(request.getRemoteHost());
                record.setUid(context.getCurrentUid());
                record.setSource(context.getAppId() + "");
                record.setUseTime(endTime - startTime);
                Object requestUri = request.getAttribute("jakarta.servlet.error.request_uri");
                record.setApi(requestUri != null ? (String) requestUri : path);
                record.setMethod(request.getMethod());
                record.setParameters(request.getParameterMap());
                record.setResponseStatus(response.getStatus());
                record.setClientVersion(context.getClientVersion());
                record.setResponse(new String(((ResponseWrapper) response).toByteArray(), response.getCharacterEncoding()));
                //text/html不打印body
                if (!StringUtils.contains(response.getContentType(), "application/json")) {
                    record.setWriteBody(false);
                }
                MDC.put("CUSTOM_LOG", "request");
                log.info(record.toString());
                MDC.remove("CUSTOM_LOG");
                ThreadLocalContext.clear();
            }
        }
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void destroy() {
    }
}