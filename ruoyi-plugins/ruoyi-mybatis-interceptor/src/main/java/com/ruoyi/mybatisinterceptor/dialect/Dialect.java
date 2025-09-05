package com.ruoyi.mybatisinterceptor.dialect;

import net.sf.jsqlparser.statement.select.PlainSelect;

/**
 * 方言接口：负责在不同数据库下生成 limit/offset、count SQL。
 */
public interface Dialect {
    /**
     * 是否支持 limit/offset。
     */
    boolean supportsLimit();

    /**
     * 对 PlainSelect 注入分页（limit/offset 或 top/row_number）。
     */
    void applyPagination(PlainSelect select, long offset, long limit);

    /**
     * 生成 count SQL；对于复杂 SQL，可能需要包裹子查询。
     */
    String buildCountSql(String originalSql, boolean hasDistinctOrGroupOrUnion);

    /**
     * 规范化 databaseId 名称，便于路由。
     */
    default String normalizeId(String id) {
        return id == null ? null : id.toLowerCase();
    }
}
