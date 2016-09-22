package com.meerkat.service;

import com.meerkat.base.db.DB;
import com.meerkat.entity.User;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

/**
 * Created by wm on 16/9/21.
 */
@Service
public class UserService {

    @Inject
    DB db;

    public User getById(Long userId) {
        return db.from(User.class).where("id", userId).first(User.class);
    }

    public User getByUserName(String userName) {
        return db.from(User.class).where("user_name", userName).first(User.class);
    }

}
