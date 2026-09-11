package com.lesofn.archforge.infrastructure.frame.filter;

import com.lesofn.archforge.infrastructure.frame.context.RequestContext;
import com.lesofn.archforge.infrastructure.frame.context.ScopedValueContext;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import jakarta.servlet.FilterConfig;

/**
 * @author sofn
 * @version 1.0 Created at: 2015-04-30 18:46
 */
public class HeaderResponseFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain)
            throws IOException, ServletException {
        HttpServletResponse resp = (HttpServletResponse) response;
        resp.setHeader("X-Engine-IP", request.getLocalAddr());
        RequestContext ctx = ScopedValueContext.getRequestContext();
        resp.setHeader("X-Engine-RequestID", ctx == null ? "-" : ctx.getRequestId());
        filterChain.doFilter(request, response);
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void destroy() {
    }
}
