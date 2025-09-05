package com.ruoyi.mybatisinterceptor.dialect;

import net.sf.jsqlparser.statement.select.PlainSelect;

/**
 * SQL Server 2012+ 支持 OFFSET ... FETCH NEXT，需要 SQL 存在稳定的 ORDER BY。
 * 由于 JSqlParser 对 OFFSET/FETCH 支持有限，这里采用包裹方案：
 * select * from (select row_number() over(order by 1) rn, t.* from (orig) t) x where rn between offset+1 and offset+limit
 * 但该方式对没有稳定 order by 的结果不确定性较大，生产中建议在 SQL 层显式 order by。
 */
public class SqlServerDialect implements Dialect {
    // supportsLimit 已移除

    @Override
    public void applyPagination(PlainSelect select, long offset, long limit) {
        // 在 PagePreHandler 内采用字符串包裹法来处理，避免在此直接改 PlainSelect
        // 这里不做操作。
    }

    // 使用 Dialect 默认的 buildCountSql

    /**
     * 生成 SQL Server 的分页包装 SQL。
     */
    public String wrapPaginationSql(String originalSql, long offset, long limit) {
        String body = originalSql;
        if (body.endsWith(";")) body = body.substring(0, body.length() - 1);
        long start = offset + 1;
        long end = offset + limit;
        // 使用常量 order by 1 的行号；对生产建议强制在业务 SQL 中提供 order by，提高确定性
        return "SELECT * FROM (SELECT ROW_NUMBER() OVER(ORDER BY 1) AS RN, T.* FROM (" + body + ") T) X WHERE X.RN BETWEEN "
                + start + " AND " + end;
    }

    @Override
    public boolean preferWrap() { return true; }
}
