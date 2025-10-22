package com.ruoyi.mybatis.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标注数据库字段
 *
 * @author Dftre
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Column {
    String name(); // 对应数据库字段名
    boolean primaryKey() default false; // 是否是主键
}
