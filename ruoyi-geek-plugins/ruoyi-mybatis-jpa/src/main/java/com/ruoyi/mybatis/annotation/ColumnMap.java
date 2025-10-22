package com.ruoyi.mybatis.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标注数据库映射字段
 *
 * @author Dftre
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ColumnMap {
    String name(); // 对应数据库字段

    String target(); // 映射表来源

    String on() default ""; // 映射表字段

    String onLeft() default ""; // 映射表左字段

    String onRight() default ""; // 映射表右字段
}
