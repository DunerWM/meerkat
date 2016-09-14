package com.meerkat.base.db.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Entity A对应多个Entity B. 目前只考虑一対多, 不考虑多对多.
 *
 */
@Target({ METHOD, FIELD })
@Retention(RUNTIME)
public @interface EntityReferences {

    /**
     * Entity B里对应Entity A主键的属性.
     */
    String property();

    /**
     * Entity B的Class. 无法有泛型推倒出Class, 需要指明.
     */
    Class<?> referenceClass();

    String orderBy() default "";

}
