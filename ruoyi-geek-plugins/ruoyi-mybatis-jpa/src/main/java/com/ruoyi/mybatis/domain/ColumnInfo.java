package com.ruoyi.mybatis.domain;

import java.lang.reflect.Field;

import org.springframework.core.annotation.AnnotationUtils;

import com.ruoyi.mybatis.annotation.Column;
import com.ruoyi.mybatis.annotation.Query;
import com.ruoyi.mybatis.utils.QueryUtil;

public class ColumnInfo extends BaseColumnInfo {
    private Column column;

    public ColumnInfo(Field field) {
        this.field = field;
        this.column = AnnotationUtils.findAnnotation(this.field, Column.class);
        this.query = AnnotationUtils.findAnnotation(this.field, Query.class);
        this.columnName = this.column.name();
        this.fieldName = this.field.getName();
        this.field.setAccessible(true);
        this.querySql = this.getQuerySql(this.query);
    }

    public boolean isPrimaryKey() {
        return this.column.primaryKey();
    }

    public String getQuerySql(Query query) {
        return QueryUtil.getQuerySql(this.getColumnName(), getTemplate(), query);
    }

    public String getQuerySql() {
        return this.querySql;
    }

    public String getFullyQualifiedColumnName(String tableName) {
        return tableName + "." + this.getColumnName();
    }
}
