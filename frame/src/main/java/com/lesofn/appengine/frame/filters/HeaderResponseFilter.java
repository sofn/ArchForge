package com.lesofn.appengine.frame.filters;

import com.lesofn.appengine.frame.context.ThreadLocalContext;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author sofn
 * @version 1.0 Created at: 2015-04-30 18:46
 */
public class HeaderResponseFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) throws IOException, ServletException {
        ((HttpServletResponse) response).setHeader("X-Engine-IP", request.getLocalAddr());
        ((HttpServletResponse) response).setHeader("X-Engine-RequestID", ThreadLocalContext.getRequestContext().getRequestId());
        filterChain.doFilter(request, response);
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void destroy() {
    }
}
