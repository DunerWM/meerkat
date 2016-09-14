package com.meerkat.controller;

import com.meerkat.entity.Blog;
import com.meerkat.service.BlogService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

/**
 * Created by wm on 16/9/14.
 */
@Controller
@RequestMapping(value = "blog/")
public class BlogController {

    @Inject
    BlogService blogService;

    @RequestMapping(value = "{id:^\\d+$}", method = RequestMethod.GET)
    public String showBlog(HttpServletRequest request, @PathVariable Long id) {
        Blog blog = blogService.getById(id);
        if (blog != null) {
            request.setAttribute("blog", blog);
        }
        return "blog/blog";
    }

}
