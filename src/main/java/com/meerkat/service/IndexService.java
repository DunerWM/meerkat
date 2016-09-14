package com.meerkat.service;

import com.meerkat.base.db.DB;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

/**
 * Created by wm on 16/9/14.
 */
@Service
public class IndexService {

    @Inject
    DB db;

    public int count() {
        return db.from("user").count();
    }

}
