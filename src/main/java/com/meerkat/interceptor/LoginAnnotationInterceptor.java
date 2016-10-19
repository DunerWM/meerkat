package com.meerkat.interceptor;

import com.meerkat.entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mobile.device.Device;
import org.springframework.mobile.device.DeviceUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;

/**
 * Created by wm on 16/9/26.
 */
public class LoginAnnotationInterceptor extends HandlerInterceptorAdapter {

    private String characterEncoding = "utf-8";                //http 头编码
    private String message = "您尚未登录，请登录后重试";            //返回错误信息
    private int retcode = 100;                                //返回错误代码
    private String loginPage = "/login.html";                    //登录页面
    private String mobileLoginPage = "/login.html";    //移动端的登录页面

    private static final Logger log = LoggerFactory.getLogger(LoginAnnotationInterceptor.class);

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        HandlerMethod hdlr;

        try {
            hdlr = (HandlerMethod) handler;    //类型转换失败
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return true;
        }

        Login login = hdlr.getMethodAnnotation(Login.class);

        if (login == null) {   //没有 login 注解
            login = hdlr.getBeanType().getAnnotation(Login.class);   //继续检查类上是否有注解
            if (login == null) {
                return true;
            }
        }

        User user = (User) request.getSession().getAttribute("user");

        if (user == null) {   //没有登录
            if (login.value() == ResultTypeEnum.redirect) {   //页面跳转
                sendRedirect(request, response);
            } else if (login.value() == ResultTypeEnum.json) {   //返回json类型数据
                sendJson(request, response);
            } else if (login.value() == ResultTypeEnum.xml) {
                sendXml(request, response);
            } else if (login.value() == ResultTypeEnum.smart) {
                //自动的
                if ((request.getHeader("Accept") != null
                        && request.getHeader("Accept").indexOf("application/json") != -1)
                        && (request.getHeader("X-Requested-With") != null
                        && request.getHeader("X-Requested-With").trim().equals("XMLHttpRequest"))) {
                    sendJson(request, response);
                } else {
                    sendRedirect(request, response);
                }
            }

            return false;
        }

        return true;
    }


    private void sendRedirect(HttpServletRequest request, HttpServletResponse response) throws Exception {
        Device device = DeviceUtils.getCurrentDevice(request);
        String url = null;

        if (device.isMobile() || device.isTablet()) {
            url = mobileLoginPage + "?url=" + getFullHref(request);
        } else {

            url = loginPage + "?url=" + getFullHref(request);
        }

        response.sendRedirect(url);
    }

    private void sendJson(HttpServletRequest request, HttpServletResponse response) throws Exception {
        response.setContentType("application/json;" + characterEncoding);

        response.setCharacterEncoding(characterEncoding);

        PrintWriter out = response.getWriter();
        //\u60a8\u5c1a\u672a\u767b\u5f55\uff0c\u8bf7\u767b\u5f55\u540e\u91cd\u8bd5
        //您尚未登录，请登录后重试
        String result = "{\"success\":false,\"retcode\":" + retcode + ",\"message\":\"" + message + "\"}";

        if (request.getParameter("login.callback") != null) {
            result = request.getParameter("login.callback") + "(" + result + ")";
        }

        out.append(result);
        out.flush();
        out.close();
    }

    private void sendXml(HttpServletRequest request, HttpServletResponse response) throws Exception {
        response.setContentType("text/xml");
        response.setCharacterEncoding(characterEncoding);

        //TODO:
        PrintWriter out = response.getWriter();
        out.append("<xml></xml>");
        out.flush();
        out.close();
    }

    /**
     * private void sendHTML(HttpServletRequest request,HttpServletResponse response) throws Exception{
     * <p/>
     * }*
     */


    private String getFullHref(HttpServletRequest request) {
        return request.getRequestURL() + (request.getQueryString() == null ? "" : "?" + request.getQueryString().replace("&", "%26"));
    }

    public String getCharacterEncoding() {
        return characterEncoding;
    }

    public void setCharacterEncoding(String characterEncoding) {
        this.characterEncoding = characterEncoding;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getRetcode() {
        return retcode;
    }

    public void setRetcode(int retcode) {
        this.retcode = retcode;
    }

    public String getLoginPage() {
        return loginPage;
    }

    public void setLoginPage(String loginPage) {
        this.loginPage = loginPage;
    }


}
