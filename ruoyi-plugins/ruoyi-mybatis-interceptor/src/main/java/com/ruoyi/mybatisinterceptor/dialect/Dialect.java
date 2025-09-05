package com.ruoyi.mybatisinterceptor.dialect;

import net.sf.jsqlparser.statement.select.PlainSelect;

/**
 * 方言接口：负责在不同数据库下生成 limit/offset、count SQL。
 */
public interface Dialect {

    /**
     * 对 PlainSelect 注入分页（limit/offset 或 top/row_number）。
     */
    void applyPagination(PlainSelect select, long offset, long limit);

    /**
     * 生成 count SQL；对于复杂 SQL，可能需要包裹子查询。
     */
    default String buildCountSql(String originalSql, boolean hasDistinctOrGroupOrUnion) {
        String body = originalSql;
        if (body != null && body.endsWith(";")) {
            body = body.substring(0, body.length() - 1);
        }
        return "SELECT COUNT(1) FROM (" + body + ") TMP_COUNT";
    }

    /**
     * 为原始 SQL 包裹/追加分页（字符串方式）。
     * 默认实现适用于支持 "LIMIT n OFFSET m" 的方言（MySQL/H2/PostgreSQL/openGauss）。
     * 不要求实现检查原 SQL 是否已有 LIMIT，调用方可结合分析结果决定是否使用。
     */
    default String wrapPaginationSql(String originalSql, long offset, long limit) {
        String body = originalSql;
        if (body.endsWith(";"))
            body = body.substring(0, body.length() - 1);
        return body + " LIMIT " + limit + " OFFSET " + offset;
    }

    /**
     * 是否优先使用 wrap 方式分页（而非 AST 改写）。
     * 对于不支持 LIMIT 语法的方言（如 Oracle/SQL Server）应返回 true。
     */
    default boolean preferWrap() {
        return false;
    }
}
