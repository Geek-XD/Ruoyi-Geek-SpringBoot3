package com.ruoyi.mybatisinterceptor.interceptor;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;

import com.ruoyi.mybatisinterceptor.context.sqlContext.SqlContextHolder;
import com.ruoyi.mybatisinterceptor.enums.ContextKey;

import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.relational.ParenthesedExpressionList;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.Join;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectItem;

@Slf4j
@Intercepts({
        @Signature(type = Executor.class, method = "update", args = { MappedStatement.class, Object.class }),
        @Signature(type = Executor.class, method = "query", args = { MappedStatement.class, Object.class,
                RowBounds.class, ResultHandler.class }),
        @Signature(type = Executor.class, method = "query", args = { MappedStatement.class, Object.class,
                RowBounds.class, ResultHandler.class, CacheKey.class, BoundSql.class })
})
public class DataScopeInterceptor implements Interceptor {
    private static final String USER_ID_COLUMN = "user_id";
    private static final String DEPT_ID_COLUMN = "dept_id";
    private static final String USER_TABLE = "sys_user";
    private static final String DEPT_TABLE = "sys_dept";

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        String userAlias = SqlContextHolder.getData(ContextKey.DATA_SCOPE, "userAlias", String.class);
        String deptAlias = SqlContextHolder.getData(ContextKey.DATA_SCOPE, "deptAlias", String.class);
        ParenthesedExpressionList<?> expressionList = SqlContextHolder.getData(
                ContextKey.DATA_SCOPE,
                "expression",
                ParenthesedExpressionList.class);

        if (expressionList == null || expressionList.isEmpty() || expressionList.get(0) == null) {
            return invocation.proceed();
        }
        Expression scopeExpression = expressionList.get(0);

        Executor targetExecutor = (Executor) invocation.getTarget();
        Object[] args = invocation.getArgs();
        MappedStatement ms = (MappedStatement) args[0];
        Object parameterObject = args[1];
        RowBounds rowBounds = (RowBounds) args[2];
        ResultHandler<?> resultHandler = (ResultHandler<?>) args[3];

        BoundSql boundSql;
        CacheKey cacheKey;
        if (args.length == 4) {
            boundSql = ms.getBoundSql(parameterObject);
            cacheKey = targetExecutor.createCacheKey(ms, parameterObject, rowBounds, boundSql);
        } else {
            cacheKey = (CacheKey) args[4];
            boundSql = (BoundSql) args[5];
        }

        try {
            String originalSql = boundSql.getSql();
            if (!isMySQLDatabase(invocation)) {
                log.warn("数据权限拦截器目前仅支持MySQL数据库，当前数据库类型不支持");
                return invocation.proceed();
            }
            Statement statement = CCJSqlParserUtil.parse(originalSql);
            if (ms.getSqlCommandType() == SqlCommandType.SELECT) {
                if (userAlias != null || deptAlias != null) {
                    String newSql = parseSelect(statement, scopeExpression, deptAlias, userAlias);
                    if (!newSql.equals(originalSql)) {
                        BoundSql newBoundSql = new BoundSql(ms.getConfiguration(), newSql,
                                boundSql.getParameterMappings(), boundSql.getParameterObject());
                        for (ParameterMapping mapping : boundSql.getParameterMappings()) {
                            String prop = mapping.getProperty();
                            if (boundSql.hasAdditionalParameter(prop)) {
                                newBoundSql.setAdditionalParameter(prop, boundSql.getAdditionalParameter(prop));
                            }
                        }
                        return targetExecutor.query(ms, parameterObject, rowBounds, resultHandler, cacheKey,
                                newBoundSql);
                    }

                }
            }
        } catch (Exception e) {
            log.error("数据权限拦截器处理异常", e);
        } finally {
            SqlContextHolder.clearContext();
        }

        return invocation.proceed();
    }

    private boolean isMySQLDatabase(Invocation invocation) {
        try {
            Executor executor = (Executor) invocation.getTarget();
            Connection connection = executor.getTransaction().getConnection();
            String databaseProductName = connection.getMetaData().getDatabaseProductName();
            return "MySQL".equalsIgnoreCase(databaseProductName);
        } catch (Exception e) {
            log.warn("当前数据权限仅支持MySQL数据库", e);
            return false;
        }
    }

    private String parseSelect(Statement statement, Expression expression, String deptAlias, String userAlias)
            throws Exception {
        PlainSelect plainSelect = ((Select) statement).getPlainSelect();

        Map<String, String> tableAliasMap = new HashMap<>();
        Map<String, String> aliasToTableMap = new HashMap<>();
        Map<String, Join> aliasToJoinMap = new HashMap<>();

        if (plainSelect.getFromItem() instanceof Table) {
            Table mainTable = (Table) plainSelect.getFromItem();
            String tableName = mainTable.getName();
            String alias = mainTable.getAlias() != null ? mainTable.getAlias().getName() : tableName;
            tableAliasMap.put(tableName, alias);
        }

        List<Join> joins = plainSelect.getJoins();
        if (joins != null) {
            for (Join join : joins) {
                if (join.getRightItem() instanceof Table) {
                    Table joinTable = (Table) join.getRightItem();
                    String tableName = joinTable.getName();
                    String alias = joinTable.getAlias() != null ? joinTable.getAlias().getName() : tableName;
                    tableAliasMap.put(tableName, alias);
                    aliasToTableMap.put(alias, tableName);
                    aliasToJoinMap.put(alias, join);
                }
            }
        }

        Set<String> selectColumns = parseSelectColumns(plainSelect);
        String targetAlias = null;
        String targetType = null;

        if (userAlias != null) {
            String userTableAlias = tableAliasMap.get(USER_TABLE);
            if (userTableAlias != null && userTableAlias.equals(userAlias)) {
                targetAlias = userAlias;
                targetType = "用户表";
                log.debug("数据权限拦截器：使用用户表别名 {}", userAlias);
            } else if (userTableAlias != null) {
                log.warn("数据权限拦截器：用户表别名不匹配，期望 {} 实际 {}", userAlias, userTableAlias);
            } else {
                if (tableAliasMap.containsValue(userAlias) &&
                        (selectColumns.contains(userAlias + "." + USER_ID_COLUMN) ||
                                selectColumns.contains(USER_ID_COLUMN))) {
                    targetAlias = userAlias;
                    String tableName = aliasToTableMap.get(targetAlias);
                    targetType = String.format("业务表(%s)", tableName);
                    log.debug("数据权限拦截器：列 {} 在表 {} 存在", DEPT_ID_COLUMN, tableName);
                }
            }
        }

        if (targetAlias == null && deptAlias != null) {
            String deptTableAlias = tableAliasMap.get(DEPT_TABLE);
            if (deptTableAlias != null && deptTableAlias.equals(deptAlias)) {
                targetAlias = deptAlias;
                targetType = "部门表";
                log.debug("数据权限拦截器：使用部门表别名 {}", deptAlias);
            } else if (deptTableAlias != null) {
                log.warn("数据权限拦截器：部门表别名不匹配，期望 {} 实际 {}", deptAlias, deptTableAlias);
            } else {
                if (tableAliasMap.containsValue(deptAlias) &&
                        (selectColumns.contains(deptAlias + "." + DEPT_ID_COLUMN) ||
                                selectColumns.contains(DEPT_ID_COLUMN))) {
                    targetAlias = deptAlias;
                    String tableName = aliasToTableMap.get(targetAlias);
                    targetType = String.format("业务表(%s)", tableName);
                    log.debug("数据权限拦截器：列 {} 在表 {} 存在", DEPT_ID_COLUMN, tableName);
                }
            }
        }

        if (userAlias != null || deptAlias != null) {
            if (targetAlias != null && aliasToJoinMap.containsKey(targetAlias)) {
                Join targetJoin = aliasToJoinMap.get(targetAlias);
                if (targetJoin.isInner()) {
                    Expression currentOn = null;
                    if (targetJoin.getOnExpressions() != null) {
                        currentOn = targetJoin.getOnExpressions().stream().reduce(AndExpression::new).orElse(null);
                    }
                    Expression newOn = (currentOn == null) ? expression : new AndExpression(currentOn, expression);
                    List<Expression> expressions = new ArrayList<>();
                    expressions.add(new ParenthesedExpressionList<>(newOn));
                    targetJoin.setOnExpressions(expressions);
                    log.debug("数据权限拦截器：数据权限条件已应用到 INNER JOIN ON 子句（表别名：{}）", targetAlias);
                }
            }
            Expression currentWhere = plainSelect.getWhere();
            Expression newWhere = currentWhere == null ? expression : new AndExpression(currentWhere, expression);
            plainSelect.setWhere(newWhere);
            log.debug("数据权限拦截器：数据权限条件已应用到主 WHERE 子句（表类型：{}）", targetType != null ? targetType : "未知");
        }

        return statement.toString();
    }

    private Set<String> parseSelectColumns(PlainSelect plainSelect) {
        Set<String> columns = new HashSet<>();

        if (plainSelect.getSelectItems() != null) {
            for (SelectItem<?> selectItem : plainSelect.getSelectItems()) {
                handleSingleColumn(selectItem.getExpression(), columns);
            }
        }

        if (plainSelect.getWhere() != null) {
            handleSingleColumn(plainSelect.getWhere(), columns);
        }

        if (plainSelect.getJoins() != null) {
            for (Join join : plainSelect.getJoins()) {
                Collection<Expression> onExpressions = join.getOnExpressions();
                if (onExpressions != null && !onExpressions.isEmpty()) {
                    for (Expression onExpression : onExpressions) {
                        handleSingleColumn(onExpression, columns);
                    }
                }
            }
        }

        return columns;
    }

    private void handleSingleColumn(Expression expression, Set<String> columns) {
        if (expression instanceof Column) {
            Column column = (Column) expression;
            String columnName = column.getColumnName();
            if (column.getTable() != null) {
                columns.add(column.getTable().getName() + "." + columnName);
            } else {
                columns.add(columnName);
            }
        }
    }
}