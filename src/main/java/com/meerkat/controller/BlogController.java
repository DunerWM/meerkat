package com.meerkat.controller;

import com.meerkat.base.util.DateUtil;
import com.meerkat.base.util.JsonResponse;
import com.meerkat.entity.Blog;
import com.meerkat.entity.User;
import com.meerkat.interceptor.Login;
import com.meerkat.service.BlogService;
import com.meerkat.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by wm on 16/9/14.
 */
@Controller
@RequestMapping(value = "blog/")
public class BlogController {

    @Inject
    BlogService blogService;
    @Inject
    UserService userService;

    private static Logger log = LoggerFactory.getLogger(BlogController.class);

    @RequestMapping(value = "{id:^\\d+$}", method = RequestMethod.GET)
    public String showBlog(HttpServletRequest request, @PathVariable Long id) {
        Blog blog = blogService.getById(id);
        blog.setPostAt(DateUtil.dataToString(blog.getCreatedAt(), "yyyy-MM-dd"));
        if (blog != null) {
            request.setAttribute("blog", blog);
        }
        return "blog/blog";
    }


    @Login
    @RequestMapping(value = "new", method = RequestMethod.GET)
    public String newBlog(HttpServletRequest request) {
        return "blog/new";
    }

    @Login
    @RequestMapping(value = "new", method = RequestMethod.POST)
    @ResponseBody
    public JsonResponse newBlog(HttpServletRequest request, HttpServletResponse response, Blog blog) {
        JsonResponse jsonResponse = new JsonResponse(false);
        User user = (User) request.getSession().getAttribute("user");
        if (user == null) {
            jsonResponse.setMessage("登录过期，请重新登录");
            return jsonResponse;
        } else {
            try {
                blog.setAuthor(user.getId());
                blog = blogService.create(blog);
                jsonResponse.set("blogId", blog.getId());
                jsonResponse.setSuccess(true);
            } catch (Exception e) {
                log.error("创建文章出错", e);
            }
        }
        return jsonResponse;
    }

}
