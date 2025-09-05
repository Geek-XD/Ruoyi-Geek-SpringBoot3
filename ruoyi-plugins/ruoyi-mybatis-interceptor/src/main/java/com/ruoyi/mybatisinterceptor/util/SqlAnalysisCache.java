package com.ruoyi.mybatisinterceptor.util;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import com.ruoyi.common.utils.sql.SqlUtil;

import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.Limit;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;

/**
 * 轻量 SQL 分析缓存：缓存无 OrderBy 的 SQL 与特征位，减少重复解析负担。
 */
public final class SqlAnalysisCache {
    private SqlAnalysisCache() {}

    public static final class Analysis {
        public final boolean isSelect;
        public final boolean complex;
        public final boolean hasOrderBy;
        public final boolean hasLimit;
        public final String noOrderSql; // 去除 order by 后的 SQL（仅 PlainSelect 有值）

        Analysis(boolean isSelect, boolean complex, boolean hasOrderBy, boolean hasLimit, String noOrderSql) {
            this.isSelect = isSelect;
            this.complex = complex;
            this.hasOrderBy = hasOrderBy;
            this.hasLimit = hasLimit;
            this.noOrderSql = noOrderSql;
        }
    }

    private static final int MAX = 1024;
    private static final Map<String, Analysis> CACHE = Collections.synchronizedMap(new LinkedHashMap<String, Analysis>(128, 0.75f, true) {
        private static final long serialVersionUID = 1L;
        @Override
        protected boolean removeEldestEntry(Map.Entry<String, Analysis> eldest) {
            return size() > MAX;
        }
    });

    public static Analysis analyze(String originSql) {
        if (originSql == null) return new Analysis(false, false, false, false, null);
        Analysis a = CACHE.get(originSql);
        if (a != null) return a;
        boolean isSelect = false;
        boolean complex = false;
        boolean hasOrderBy = false;
        boolean hasLimit = false;
        String noOrderSql = null;
        try {
            Statement st = SqlUtil.parseSql(originSql);
            if (st instanceof Select) {
                isSelect = true;
                Select sel = (Select) st;
                PlainSelect plain = sel.getPlainSelect();
                if (plain != null) {
                    hasOrderBy = plain.getOrderByElements() != null && !plain.getOrderByElements().isEmpty();
                    Limit limit = plain.getLimit();
                    hasLimit = limit != null;
                    complex = plain.getDistinct() != null || plain.getGroupBy() != null || plain.getIntoTables() != null
                            || plain.getHaving() != null || (plain.getJoins() != null && !plain.getJoins().isEmpty());
                    if (hasOrderBy) {
                        // 暂存后移除 order by 生成字符串，再不复用该 AST
                        plain.setOrderByElements(null);
                        noOrderSql = sel.toString();
                    }
                } else {
                    // 非 PlainSelect（如 UNION）按复杂处理
                    complex = true;
                }
            }
        } catch (Throwable ignore) {
            // 解析失败，视为非 select
        }
        Analysis res = new Analysis(isSelect, complex, hasOrderBy, hasLimit, noOrderSql);
        CACHE.put(originSql, res);
        return res;
    }
}
