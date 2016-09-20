package com.meerkat.base.filter;

import com.google.common.base.Strings;
import com.google.common.io.Files;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by wm on 16/9/14.
 */
public class StaticFileFilter extends BaseFilter {
    private final static String DEFAULT_CONTENT_TYPE = "text/plain";
    private final static Map<String, String> mimeTypes = new HashMap<String, String>();
    private final static Pattern VERSIONING_FILE_PATTERN = Pattern
            .compile("^(.*)-([a-zA-Z0-9]{40})\\.(\\w+)$");

    static {
        mimeTypes.put("txt", "text/plain");
        mimeTypes.put("html", "text/html");
        mimeTypes.put("css", "text/css");
        mimeTypes.put("js", "application/x-javascript");
        mimeTypes.put("json", "application/json");
        mimeTypes.put("gif", "image/gif");
        mimeTypes.put("png", "image/png");
        mimeTypes.put("jpg", "image/jpeg");
        mimeTypes.put("jpeg", "image/jpeg");
        mimeTypes.put("xml", "text/xml");

        mimeTypes.put("ico", "image/x-icon");
        mimeTypes.put("svg", "image/svg+xml");

        mimeTypes.put("pdf", "application/pdf");
    }

    private Pattern excludePattern = Pattern.compile("\\.jsp$");

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        String exclude = filterConfig.getInitParameter("exclude");
        if (!Strings.isNullOrEmpty(exclude)) {
            excludePattern = Pattern.compile(exclude);
        }
    }

    @Override
    protected void doFilter(HttpServletRequest request, HttpServletResponse response,
                            FilterChain chain) throws IOException, ServletException {
        String path = request.getRequestURI();
        String realPath = request.getServletContext().getRealPath(path);
        if (realPath == null) {
            chain.doFilter(request, response);
            return;
        }

        File file = normalizeFile(new File(realPath));
        if (file.isFile() && file.exists() && !isExcluded(file)) {
            long length = file.length();
            response.setContentLength((int) length);
            String contentType = getContentType(file);
            response.setContentType(contentType);

            Files.copy(file, response.getOutputStream());
            response.getOutputStream().flush();
        } else {
            chain.doFilter(request, response);
        }
    }

    private boolean isExcluded(File file) {
        String path = file.getPath();
        if (path.indexOf("/WEB-INF/") != -1) {
            return true;
        }
        if (excludePattern != null) {
            Matcher m = excludePattern.matcher(path);
            if (m.find()) {
                return true;
            }
        }

        return false;
    }

    private File normalizeFile(File file) {
        if (file.isFile()) {
            return file;
        }
        String name = file.getName();
        Matcher m = VERSIONING_FILE_PATTERN.matcher(name);
        if (m.find()) {
            name = m.group(1) + "." + m.group(3);
            return new File(file.getParent(), name);
        }
        return file;
    }

    private String getContentType(File file) {
        String name = file.getName();
        int idx = name.lastIndexOf(".");
        String suffix = "";
        if (idx != -1) {
            suffix = name.substring(idx + 1);
        }
        String contentType = mimeTypes.get(suffix);
        if (contentType == null) {
            contentType = DEFAULT_CONTENT_TYPE;
        }
        return contentType + "; charset=UTF-8";
    }

}
