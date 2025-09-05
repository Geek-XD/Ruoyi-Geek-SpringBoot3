package com.ruoyi.mybatisinterceptor.interceptor;

import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ResultMap;
import org.apache.ibatis.mapping.ResultMapping;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.springframework.util.ReflectionUtils;

import com.ruoyi.common.utils.spring.SpringUtils;
import com.ruoyi.common.utils.sql.SqlUtil;
import com.ruoyi.mybatisinterceptor.context.page.PageContextHolder;
import com.ruoyi.mybatisinterceptor.context.page.model.PageInfo;
import com.ruoyi.mybatisinterceptor.context.page.model.TableInfo;
import com.ruoyi.mybatisinterceptor.dialect.Dialect;
import com.ruoyi.mybatisinterceptor.dialect.DialectRouter;
// no dialect-specific imports needed; rely on Dialect API
import com.ruoyi.mybatisinterceptor.util.OrderByUtil;
import com.ruoyi.mybatisinterceptor.util.SqlAnalysisCache;

//
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.Statement;
//
import net.sf.jsqlparser.statement.select.OrderByElement;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;

@Intercepts({
        @Signature(type = Executor.class, method = "query", args = { MappedStatement.class, Object.class,
                RowBounds.class, ResultHandler.class, CacheKey.class, BoundSql.class }),
        @Signature(type = Executor.class, method = "query", args = {
                MappedStatement.class, Object.class, RowBounds.class,
                ResultHandler.class })

})
public class PageInercetor extends MybatisInterceptor {

    private static final List<ResultMapping> EMPTY_RESULTMAPPING = new ArrayList<ResultMapping>(0);

    private static final String SELECT_COUNT_SUFIX = "_SELECT_COUNT";
    private static final Field sqlFiled = ReflectionUtils.findField(BoundSql.class, "sql");
    static {
        sqlFiled.setAccessible(true);
    }

    private DialectRouter dialectRouter = SpringUtils.getBean(DialectRouter.class);

    @Override
    public Object runPreHandlers(Executor executor, MappedStatement mappedStatement, Object params, RowBounds rowBounds,
            ResultHandler<?> resultHandler, CacheKey cacheKey, BoundSql boundSql) throws Throwable {
        // 避免对内部 count 查询再次分页/递归
        if (mappedStatement.getId() != null && mappedStatement.getId().endsWith(SELECT_COUNT_SUFIX)) {
            return null;
        }
        if (PageContextHolder.isPage()) {
            String originSql = boundSql.getSql();
            SqlAnalysisCache.Analysis analysis = SqlAnalysisCache.analyze(originSql);
            // 惰性解析：仅在确需 AST（注入排序或复杂分页）时再 parse
            Statement sql = null;
            Select selectAst = null;
            {
                PageInfo pageInfo = PageContextHolder.getPageInfo();
                String orderExpr = OrderByUtil.build(pageInfo.getOrderByColumn(), pageInfo.getIsAsc());

                // 路由方言
                Dialect dialect = dialectRouter.routeByCurrent();

                // 生成 count（可关闭）
                Long total = null;
                boolean doCount = pageInfo.getSearchCount() == null || pageInfo.getSearchCount().booleanValue();
                if (doCount) {
                    String base = analysis.noOrderSql != null ? analysis.noOrderSql : originSql; // 优化：去掉 order by
                    String countSql = dialect.buildCountSql(base, analysis.complex);
                    total = getCount(executor, mappedStatement, params, boundSql, rowBounds, resultHandler,
                            countSql);
                    PageContextHolder.setTotal(total);
                    // 若 total=0，标记跳过主查询，由拦截器统一执行短路
                    if (total != null && total.longValue() == 0L) {
                        PageContextHolder.setSkipQuery(true);
                    }
                }

                // PageHelper 合理化语义对齐：在拿到 total 后修正页码
                if (Boolean.TRUE.equals(pageInfo.getReasonable())) {
                    // 仅在已执行 count 的情况下才能根据最大页修正
                    if (doCount) {
                        long ps = pageInfo.getPageSize() == null ? 10L
                                : Math.max(1L, pageInfo.getPageSize());
                        long pages = (total == null || total <= 0L) ? 0L : ((total + ps - 1L) / ps);
                        long pn = pageInfo.getPageNumber() == null ? 1L : pageInfo.getPageNumber();
                        if (pn < 1L)
                            pn = 1L;
                        if (pages == 0L) {
                            // 无数据时，PageHelper 将页码校正为 1
                            pn = 1L;
                        } else if (pn > pages) {
                            pn = pages;
                        }
                        pageInfo.setPageNumber(pn);
                    } else {
                        // 未进行 count 时，仅做下限校正
                        long pn = pageInfo.getPageNumber() == null ? 1L : pageInfo.getPageNumber();
                        if (pn < 1L)
                            pageInfo.setPageNumber(1L);
                    }
                }

                // 注入排序（仅当需要注入且原 SQL 无排序时，才解析 AST 注入）
                boolean needInjectOrder = (orderExpr != null && !orderExpr.isEmpty() && analysis.noOrderSql == null);
                if (needInjectOrder || analysis.hasLimit || (!dialect.preferWrap() && analysis.complex)) {
                    // 需要 AST：解析一次
                    if (sql == null)
                        sql = SqlUtil.parseSql(originSql);
                    if (sql instanceof Select) {
                        selectAst = (Select) sql;
                        if (needInjectOrder) {
                            applyOrderByIfPresent(selectAst, pageInfo);
                        }
                    }
                }

                // 注入分页
                String baseSqlForPage = (selectAst != null) ? selectAst.toString() : originSql;
                String pagedSqlStr;
                if (selectAst != null) {
                    // 用 AST 注入分页
                    pagedSqlStr = applyPagination(selectAst, baseSqlForPage, pageInfo, dialect, analysis);
                } else {
                    // 不解析，走包装
                    long limitSize = pageInfo.getPageSize();
                    long offset = pageInfo.getOffset();
                    pagedSqlStr = dialect.wrapPaginationSql(baseSqlForPage, offset, limitSize);
                }
                sqlFiled.set(boundSql, pagedSqlStr);
                cacheKey.update(pagedSqlStr);
            }
        }
        if (PageContextHolder.shouldSkipQuery()) {
            return Collections.emptyList();
        } else {
            return null;
        }
    }

    @Override
    public Object applyAfterHandlers(Object object) throws Throwable {
        if (PageContextHolder.isPage()) {
            if (object instanceof List) {
                TableInfo<Object> tableInfo = new TableInfo<Object>((List<?>) object);
                tableInfo.setTotal(PageContextHolder.getTotal());
                return tableInfo;
            }
            return object;
        }
        return object;
    }

    @Override
    public void finish() {
        PageContextHolder.clear();
    }

    private static MappedStatement createCountMappedStatement(MappedStatement ms, String newMsId) {
        MappedStatement.Builder builder = new MappedStatement.Builder(ms.getConfiguration(), newMsId,
                ms.getSqlSource(),
                ms.getSqlCommandType());
        builder.resource(ms.getResource());
        builder.fetchSize(ms.getFetchSize());
        builder.statementType(ms.getStatementType());
        builder.keyGenerator(ms.getKeyGenerator());
        if (ms.getKeyProperties() != null && ms.getKeyProperties().length != 0) {
            StringBuilder keyProperties = new StringBuilder();
            for (String keyProperty : ms.getKeyProperties()) {
                keyProperties.append(keyProperty).append(",");
            }
            keyProperties.delete(keyProperties.length() - 1, keyProperties.length());
            builder.keyProperty(keyProperties.toString());
        }
        builder.timeout(ms.getTimeout());
        builder.parameterMap(ms.getParameterMap());
        // count查询返回值int
        List<ResultMap> resultMaps = new ArrayList<ResultMap>();
        ResultMap resultMap = new ResultMap.Builder(ms.getConfiguration(), ms.getId(), Long.class,
                EMPTY_RESULTMAPPING)
                .build();
        resultMaps.add(resultMap);
        builder.resultMaps(resultMaps);
        builder.resultSetType(ms.getResultSetType());
        builder.cache(ms.getCache());
        builder.flushCacheRequired(ms.isFlushCacheRequired());
        builder.useCache(ms.isUseCache());
        return builder.build();
    }

    public static Long getCount(Executor executor, MappedStatement mappedStatement, Object parameter,
            BoundSql boundSql, RowBounds rowBounds, ResultHandler<?> resultHandler, String countSql)
            throws SQLException {

        Map<String, Object> additionalParameters = boundSql.getAdditionalParameters();

        BoundSql countBoundSql = new BoundSql(mappedStatement.getConfiguration(), countSql,
                boundSql.getParameterMappings(), parameter);
        for (String key : additionalParameters.keySet()) {
            countBoundSql.setAdditionalParameter(key, additionalParameters.get(key));
        }
        CacheKey countKey = executor.createCacheKey(mappedStatement, parameter, RowBounds.DEFAULT, countBoundSql);

        List<Object> query = executor.query(
                createCountMappedStatement(mappedStatement, getCountMSId(mappedStatement)),
                parameter, RowBounds.DEFAULT, resultHandler, countKey,
                countBoundSql);
        return (Long) query.get(0);
    }

    private static String getCountMSId(MappedStatement mappedStatement) {
        return mappedStatement.getId() + SELECT_COUNT_SUFIX;
    }

    // 已简化：count 直接通过 dialect.buildCountSql(originSql, analysis.complex)

    private String applyPagination(Select select, String originSql, PageInfo pageInfo, Dialect dialect,
            SqlAnalysisCache.Analysis analysis) {
        long limitSize = pageInfo.getPageSize();
        long offset = pageInfo.getOffset();
        if (dialect.preferWrap()) {
            return dialect.wrapPaginationSql(originSql, offset, limitSize);
        }
        // 支持 LIMIT 的方言：对于非复杂并且无现有 LIMIT 的 SQL，走字符串快路径（委托方言包装）
        if (!analysis.complex && !analysis.hasLimit) {
            return dialect.wrapPaginationSql(originSql, offset, limitSize);
        }
        // 其余情况使用 AST 添加 LIMIT（委托方言以保持一致性）
        dialect.applyPagination(select.getPlainSelect(), offset, limitSize);
        return select.toString();
    }

    private void applyOrderByIfPresent(Select select, PageInfo pageInfo) {
        String orderExpr = OrderByUtil.build(pageInfo.getOrderByColumn(), pageInfo.getIsAsc());
        if (orderExpr == null || orderExpr.isEmpty())
            return;
        PlainSelect plain = select.getPlainSelect();
        if (plain.getOrderByElements() != null && !plain.getOrderByElements().isEmpty()) {
            return; // 已有排序，按原 SQL 为准
        }
        String[] items = orderExpr.split(",");
        List<OrderByElement> list = new ArrayList<>();
        for (String item : items) {
            String[] parts = item.trim().split("\\s+");
            if (parts.length >= 1) {
                OrderByElement e = new OrderByElement();
                e.setExpression(new Column(parts[0]));
                if (parts.length >= 2) {
                    e.setAsc("asc".equalsIgnoreCase(parts[1]));
                } else {
                    e.setAsc(true);
                }
                list.add(e);
            }
        }
        if (!list.isEmpty())
            plain.setOrderByElements(list);
    }

}
