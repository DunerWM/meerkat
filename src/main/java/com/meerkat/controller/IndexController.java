package com.meerkat.controller;

import com.meerkat.service.IndexService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

/**
 * Created by wm on 16/9/12.
 */
@Controller
@RequestMapping(value = "/")
public class IndexController {

    @Inject
    IndexService indexService;

    @RequestMapping(value = "index")
    public String showIndex(HttpServletRequest request) {
        int count = indexService.count();
        request.setAttribute("count", count);
        return "kat/index";
    }

}

