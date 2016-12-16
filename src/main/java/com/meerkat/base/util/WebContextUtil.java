package com.meerkat.base.util;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by wm on 16/11/2.
 */
public class WebContextUtil {

    public static Boolean isMobile(HttpServletRequest request) {
        String ua = request.getHeader("user-agent").toLowerCase();
        if (ua.contains("mobile")) {
            return true;
        }
        return false;
    }

    public static String getRemoteIp(HttpServletRequest request) {
        String ipAddress = request.getHeader("X-FORWARDED-FOR");
        if (ipAddress == null) {
            ipAddress = request.getRemoteAddr();
        }
        return ipAddress;
    }

}
