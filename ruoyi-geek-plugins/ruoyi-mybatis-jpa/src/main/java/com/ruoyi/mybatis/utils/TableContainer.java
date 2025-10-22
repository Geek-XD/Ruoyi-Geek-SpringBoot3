package com.ruoyi.mybatis.utils;

import java.util.HashMap;
import java.util.Map;

import com.ruoyi.common.core.domain.BaseEntity;
import com.ruoyi.mybatis.domain.TableInfo;

/**
 * sql构建工具
 *
 * @author Dftre
 */
public class TableContainer {

    private static final Map<Class<?>, TableInfo> tableInfoMap = new HashMap<>();

    public static <T extends BaseEntity> TableInfo getTableInfo(T entity) {
        Class<?> clazz = entity.getClass();
        return getTableInfo(clazz);
    }

    public static <T extends BaseEntity> TableInfo getTableInfo(Class<?> clazz) {
        TableInfo tableInfo = tableInfoMap.get(clazz);
        if (tableInfo == null) {
            tableInfo = new TableInfo(clazz);
        }
        return tableInfo;
    }

}
