package com.ruoyi.mybatisinterceptor.dialect;

import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.statement.select.Limit;
import net.sf.jsqlparser.statement.select.PlainSelect;

public class H2Dialect implements Dialect {
    @Override
    public boolean supportsLimit() {
        return true;
    }

    @Override
    public void applyPagination(PlainSelect select, long offset, long limit) {
        Limit l = new Limit();
        l.setOffset(new LongValue(offset));
        l.setRowCount(new LongValue(limit));
        select.setLimit(l);
    }

    @Override
    public String buildCountSql(String originalSql, boolean hasDistinctOrGroupOrUnion) {
        String body = originalSql;
        if (body.endsWith(";")) body = body.substring(0, body.length() - 1);
        return "SELECT COUNT(1) FROM (" + body + ") TMP_COUNT";
    }
}
