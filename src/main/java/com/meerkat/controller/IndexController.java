package com.meerkat.controller;

import com.meerkat.base.db.DB;
import com.meerkat.base.util.JsonResponse;
import com.meerkat.base.util.PasswordEncoder;
import com.meerkat.entity.User;
import com.meerkat.interceptor.Login;
import com.meerkat.service.IndexService;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * Created by wm on 16/9/12.
 */
@Controller
@RequestMapping(value = "/")
public class IndexController {

    @Inject
    IndexService indexService;
    @Inject
    DB db;

    @Login
    @RequestMapping(value = "index")
    public String showIndex(HttpServletRequest request) {
        int count = indexService.count();
        request.setAttribute("count", count);
        return "kat/index";
    }

    @RequestMapping(value = "login")
    public String showLogin(HttpServletRequest request) {
        return "kat/login";
    }

    /**
     * @param request
     * @param response
     * @param loginName
     * @param password
     * @return
     * @throws IOException
     */
    @RequestMapping(value = {"login"}, method = RequestMethod.POST)
    @ResponseBody
    public JsonResponse login(HttpServletRequest request,
                              HttpServletResponse response,
                              @RequestParam(defaultValue = "") String loginName,
                              @RequestParam(defaultValue = "") String password) throws IOException {
        JsonResponse jsonResponse = new JsonResponse(false);
        User user = db.from(User.class).where("nick", loginName).where("status", 1).select("id,salt,encrypted").first(User.class);
        if (user != null) {
            if (StringUtils.equals(PasswordEncoder.encodePassword(password, user.getSalt()), user.getEncrypted())) {
                HttpSession session = request.getSession();
                session.setAttribute("user", user);
                String url = request.getParameter("url");
                if (StringUtils.isNotBlank(url)) {
                    jsonResponse.set("url", url);
                }
                jsonResponse.setSuccess(true);
            } else {
                jsonResponse.setMessage("用户名或者密码错误");
            }
        } else {
            jsonResponse.setMessage("用户不保存在");
        }
        //用户名或密码错误
        return jsonResponse;
    }

    @RequestMapping(value = "register", method = RequestMethod.GET)
    public String showRegister(HttpServletRequest request) {
        return "kat/register";
    }

//    @RequestMapping(value = "register")

    public static void createCookie(HttpServletResponse response, String cookieName, String cookieValue, int maxAge) {
        Cookie cookie = new Cookie(cookieName, cookieValue);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setDomain("meerkat.wiki");
        cookie.setMaxAge(maxAge);
        response.addCookie(cookie);
    }

    public static String getRefer(HttpServletRequest request) {
        String refer = request.getHeader("Referer");
        String patternStr = "";
        return "";
    }


}

