package com.meerkat.service;

import com.meerkat.base.db.DB;
import com.meerkat.base.db.Pagination;
import com.meerkat.entity.Blog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Created by wm on 16/9/14.
 */
@Service
public class BlogService {

    private final Logger log = LoggerFactory.getLogger(BlogService.class);

    @Inject
    DB db;

    public Pagination<Blog> getBlogPagination(Long categoryId, int page, int pageSize) {
        String segment = "";
        if (categoryId != null) {
            segment = " and category_id = " + categoryId;
        }
        return db.from(Blog.class).segment("deleted = 0" + segment).orderBy("created_at desc").eager(true).paginate(Blog.class, page, pageSize);
    }

    public Blog getById(Long id) {
        Blog blog = db.from("blog b").select("b.*, u.nick as authorName").join("INNER JOIN `user` u").where("b.id", id).segment("u.id = b.author").eager(true).first(Blog.class);
        update(blog);
        return blog;
    }

    public Blog create(Blog blog) {
        blog.setCreatedAt(new Date());
        blog.setUpdatedAt(new Date());
        db.insert(blog);
        return blog;
    }

    public void update(Blog blog) {
        blog.setUpdatedAt(new Date());
        db.update(blog);
    }

    public void updateBlogViews(HttpServletRequest request, Blog blog){

    }

}
