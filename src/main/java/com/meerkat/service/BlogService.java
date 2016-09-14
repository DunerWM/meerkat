package com.meerkat.service;

import com.meerkat.base.db.DB;
import com.meerkat.entity.Blog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

/**
 * Created by wm on 16/9/14.
 */
@Service
public class BlogService {

    private final Logger log = LoggerFactory.getLogger(getClass());

    @Inject
    DB db;

    public Blog getById(Long id) {
        return db.from(Blog.class).where("id", id).first(Blog.class);
    }

}
