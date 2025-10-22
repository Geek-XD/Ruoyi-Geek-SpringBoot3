package com.ruoyi.mybatis.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 开启数据库关联
 *
 * @author Dftre
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface EnableTableMap {
    String name() default "t";

    String dept() default "";

    String user() default "";

    String userOn() default "user_id";

    String userOnLeft() default "";

    String userOnRight() default "";

    String deptOn() default "dept_id";

    String deptOnLeft() default "";

    String deptOnRight() default "";

    String deptFrom() default "";
}
