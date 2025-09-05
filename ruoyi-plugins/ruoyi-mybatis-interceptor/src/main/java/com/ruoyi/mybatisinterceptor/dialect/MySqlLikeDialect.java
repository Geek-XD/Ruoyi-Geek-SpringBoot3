package com.ruoyi.mybatisinterceptor.dialect;

import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.statement.select.Limit;
import net.sf.jsqlparser.statement.select.PlainSelect;

/**
 * 适用于 MySQL/MariaDB/PostgreSQL/openGauss 的方言（均支持 limit offset）。
 */
public class MySqlLikeDialect implements Dialect {
    // supportsLimit 已移除

    @Override
    public void applyPagination(PlainSelect select, long offset, long limit) {
        Limit l = new Limit();
        l.setOffset(new LongValue(offset));
        l.setRowCount(new LongValue(limit));
        select.setLimit(l);
    }

    @Override
    public String buildCountSql(String originalSql, boolean hasDistinctOrGroupOrUnion) {
        // 简单场景可直接替换 select ... -> select count(1)
        // 兼容起见统一包裹子查询，省去诸多边界处理
        String wrapped = originalSql;
        // 移除末尾分号
        if (wrapped.endsWith(";")) {
            wrapped = wrapped.substring(0, wrapped.length() - 1);
        }
        return "SELECT COUNT(1) FROM (" + wrapped + ") TMP_COUNT";
    }
}
