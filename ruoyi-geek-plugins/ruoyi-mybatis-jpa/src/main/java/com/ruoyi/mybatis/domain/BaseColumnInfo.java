package com.ruoyi.mybatis.domain;

import java.lang.reflect.Field;

import com.ruoyi.mybatis.annotation.Query;
import com.ruoyi.mybatis.utils.QueryUtil;

public class BaseColumnInfo {
    protected String columnName;
    protected String fieldName;
    protected Field field;
    protected Query query;
    protected String querySql;

    public Field getField() {
        return field;
    }

    public Query getQuery() {
        return query;
    }

    public String getTemplate() {
        return getTemplate(false);
    }

    public String getColumnName() {
        return columnName;
    }

    public String getUnqualifiedColumnName() {
        return columnName;
    }

    public String getTemplate(boolean params) {
        if (params) {
            return "#{params." + fieldName + "}";
        } else {
            return "#{" + fieldName + "}";
        }
    }

    public boolean fieldQueryIsNotNull(Object entity) {
        return QueryUtil.fieldQueryIsNotNull(entity, field, query);
    }

    public boolean fieldIsNotNull(Object entity) {
        try {
            return this.field.get(entity) != null;
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Failed to access field for building query conditions.", e);
        }
    }
}
