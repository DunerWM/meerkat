package com.meerkat.interceptor;

import java.lang.annotation.*;

/**
 * Created by wm on 16/9/26.
 */
@Target(value = {ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Login {

    /**
     * 登录校验失败后的动作方式
     *
     * @return
     */
    ResultTypeEnum value() default ResultTypeEnum.redirect;   //默认整页跳转
}
