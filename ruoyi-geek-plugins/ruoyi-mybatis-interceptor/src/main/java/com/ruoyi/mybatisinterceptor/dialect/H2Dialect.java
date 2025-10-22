package com.ruoyi.mybatisinterceptor.dialect;

import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.statement.select.Limit;
import net.sf.jsqlparser.statement.select.PlainSelect;

public class H2Dialect implements Dialect {
    // supportsLimit 已移除

    @Override
    public void applyPagination(PlainSelect select, long offset, long limit) {
        Limit l = new Limit();
        l.setOffset(new LongValue(offset));
        l.setRowCount(new LongValue(limit));
        select.setLimit(l);
    }

    // 使用 Dialect 默认的 buildCountSql
}
