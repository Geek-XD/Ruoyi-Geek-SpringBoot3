package com.ruoyi.framework.mybatis;

import static com.mybatisflex.core.constant.SqlConsts.*;

import com.mybatisflex.core.dialect.OperateType;
import com.mybatisflex.core.dialect.impl.CommonsDialectImpl;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.table.TableInfo;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.framework.processor.context.DataScopeContextHolder;

public class DataScopeDialectImpl extends CommonsDialectImpl {

    @Override
    public void prepareAuth(QueryWrapper queryWrapper, OperateType operateType) {
        String dataScopeSql = DataScopeContextHolder.get();
        if (StringUtils.isNotEmpty(dataScopeSql)) {
            queryWrapper.and(dataScopeSql);
        }
        super.prepareAuth(queryWrapper, operateType);
    }

    @Override
    public void prepareAuth(String schema, String tableName, StringBuilder sql, OperateType operateType) {
        String dataScopeSql = DataScopeContextHolder.get();
        if (StringUtils.isNotEmpty(dataScopeSql)) {
            sql.append(AND).append(dataScopeSql);
        }
        super.prepareAuth(schema, tableName, sql, operateType);
    }

    @Override
    public void prepareAuth(TableInfo tableInfo, StringBuilder sql, OperateType operateType) {
        String dataScopeSql = DataScopeContextHolder.get();
        if (StringUtils.isNotEmpty(dataScopeSql)) {
            sql.append(AND).append(dataScopeSql);
        }
        super.prepareAuth(tableInfo, sql, operateType);
    }
}