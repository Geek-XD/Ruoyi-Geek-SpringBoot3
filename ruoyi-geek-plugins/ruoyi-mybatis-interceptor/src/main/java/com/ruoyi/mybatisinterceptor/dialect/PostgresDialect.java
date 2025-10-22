package com.ruoyi.mybatisinterceptor.dialect;

import net.sf.jsqlparser.statement.select.PlainSelect;

/**
 * PostgreSQL/openGauss 方言：
 * - 支持 LIMIT n OFFSET m 语法。
 * - 计数查询统一包裹子查询，避免复杂 SQL 场景下的统计误差。
 */
public class PostgresDialect implements Dialect {
    // supportsLimit 已移除

    @Override
    public void applyPagination(PlainSelect select, long offset, long limit) {
        // 为避免 JSqlParser 在某些场景下输出 MySQL 风格的 "LIMIT offset, rowCount"，
        // PostgreSQL 的分页将优先通过字符串包裹方式处理，见 wrapPaginationSql。
        // 这里不直接改 AST。
    }

        // 使用 Dialect 默认的 buildCountSql

    /**
     * 生成 PostgreSQL 的分页 SQL（LIMIT n OFFSET m）。
     */
    public String wrapPaginationSql(String originalSql, long offset, long limit) {
        String body = originalSql;
        if (body.endsWith(";")) body = body.substring(0, body.length() - 1);
        return body + " LIMIT " + limit + " OFFSET " + offset;
    }

    @Override
    public boolean preferWrap() { return true; }
}
