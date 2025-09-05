package com.ruoyi.mybatisinterceptor.dialect;

import net.sf.jsqlparser.statement.select.PlainSelect;

/**
 * Oracle 分页通过 ROWNUM 包裹。
 */
public class OracleDialect implements Dialect {
    @Override
    public boolean supportsLimit() {
        return true;
    }

    @Override
    public void applyPagination(PlainSelect select, long offset, long limit) {
        // 在 PagePreHandler 中以字符串方式包裹处理
    }

    @Override
    public String buildCountSql(String originalSql, boolean hasDistinctOrGroupOrUnion) {
        String body = originalSql;
        if (body.endsWith(";")) body = body.substring(0, body.length() - 1);
        return "SELECT COUNT(1) FROM (" + body + ") TMP_COUNT";
    }

    public String wrapPaginationSql(String originalSql, long offset, long limit) {
        String body = originalSql;
        if (body.endsWith(";")) body = body.substring(0, body.length() - 1);
        long end = offset + limit;
        return "SELECT * FROM (SELECT T1.*, ROWNUM RN FROM (" + body + ") T1 WHERE ROWNUM <= " + end + ") WHERE RN > " + offset;
    }
}
