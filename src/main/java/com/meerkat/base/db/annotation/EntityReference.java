package com.meerkat.base.db.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Entity A里引用Entity B, 分两种情况:
 * 1. 正向(inverse = false)，A里referenceProperty对应B的主键
 * 2. 反向(inverse = true)，B里referenceProperty对应A的主键
 */
@Target({ METHOD, FIELD })
@Retention(RUNTIME)
public @interface EntityReference {

    /**
     * 主记录里的引用属性名.
     */
    String referenceProperty() default "";

    boolean inverse() default false;

}
