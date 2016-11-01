package com.meerkat.controller;

import com.meerkat.base.db.Pagination;
import com.meerkat.base.util.JsonResponse;
import com.meerkat.entity.Blog;
import com.meerkat.entity.User;
import com.meerkat.interceptor.Login;
import com.meerkat.service.BlogService;
import com.meerkat.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

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

    /**
     * 博客列表
     *
     * @param request
     * @param pageNum
     * @param pageSize
     * @return
     */
    @RequestMapping(value = "list", method = RequestMethod.GET)
    public String showBlogList(HttpServletRequest request, @RequestParam(defaultValue = "1") Integer pageNum, @RequestParam(defaultValue = "20") Integer pageSize) {
        Pagination<Blog> blogPagination = blogService.getBlogPagination(null, pageNum, pageSize);
        request.setAttribute("pagination", blogPagination);
        return "blog/list";
    }

    /**
     * 博客列表json
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "list/json", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public JsonResponse getBlogList(HttpServletRequest request, @RequestParam(defaultValue = "1") Integer pageNum, @RequestParam(defaultValue = "20") Integer pageSize) {
        JsonResponse jsonResponse = new JsonResponse();
        Pagination<Blog> blogPagination = blogService.getBlogPagination(null, pageNum, pageSize);
        jsonResponse.setSuccess(true);
        jsonResponse.set("list", blogPagination.getData());
        return jsonResponse;
    }

    /**
     * blog详情
     *
     * @param request
     * @param id
     * @return
     */
    @RequestMapping(value = "{id:^\\d+$}", method = RequestMethod.GET)
    public String showBlog(HttpServletRequest request, @PathVariable Long id) {
        Blog blog = blogService.getById(id);
        if (blog != null) {
            request.setAttribute("blog", blog);
        }
        return "blog/blog";
    }

    /**
     * 创建blog
     *
     * @param request
     * @return
     */
    @Login
    @RequestMapping(value = "new", method = RequestMethod.GET)
    public String newBlog(HttpServletRequest request) {
        return "blog/new";
    }

    /**
     * 创建blog
     *
     * @param request
     * @param response
     * @param blog
     * @return
     */
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
