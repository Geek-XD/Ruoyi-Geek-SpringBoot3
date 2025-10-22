package com.ruoyi.mybatisinterceptor.dialect;

import net.sf.jsqlparser.statement.select.PlainSelect;

/**
 * Oracle 分页通过 ROWNUM 包裹。
 */
public class OracleDialect implements Dialect {
    // supportsLimit 已移除

    @Override
    public void applyPagination(PlainSelect select, long offset, long limit) {
        // 在 PagePreHandler 中以字符串方式包裹处理
    }

    // 使用 Dialect 默认的 buildCountSql

    public String wrapPaginationSql(String originalSql, long offset, long limit) {
        String body = originalSql;
        if (body.endsWith(";")) body = body.substring(0, body.length() - 1);
        long end = offset + limit;
        return "SELECT * FROM (SELECT T1.*, ROWNUM RN FROM (" + body + ") T1 WHERE ROWNUM <= " + end + ") WHERE RN > " + offset;
    }

    @Override
    public boolean preferWrap() { return true; }
}
