package com.meerkat.base.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by wm on 16/9/14.
 */
public abstract class BaseFilter implements Filter {
    private Logger logger = LoggerFactory.getLogger(BaseFilter.class);
    protected FilterConfig filterConfig;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        this.filterConfig = filterConfig;
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        try {
            doFilter(request, response, chain);
        } catch (Exception e) {
            logger.error("过滤器出现问题在处理请求时: " + request.getRequestURI(), e);
            throw e;
        }
    }

    protected abstract void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException;

    @Override
    public void destroy() {
    }
}
