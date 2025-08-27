package com.ruoyi.mybatisinterceptor.aspectj;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.ruoyi.common.annotation.DataScope;
import com.ruoyi.common.constant.UserConstants;
import com.ruoyi.common.core.domain.entity.SysRole;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.core.domain.model.LoginUser;
import com.ruoyi.common.core.text.Convert;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.framework.security.context.PermissionContextHolder;
import com.ruoyi.mybatisinterceptor.context.sqlContext.SqlContextHolder;
import com.ruoyi.mybatisinterceptor.enums.ContextKey;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.conditional.OrExpression;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.expression.operators.relational.InExpression;
import net.sf.jsqlparser.expression.operators.relational.NotEqualsTo;
import net.sf.jsqlparser.expression.operators.relational.ParenthesedExpressionList;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.SelectItem;

/**
 * 数据过滤处理
 *
 * @author ruoyi
 */
@Aspect
@Component
@ConditionalOnProperty(prefix = "datascope", name = "type", havingValue = "plus", matchIfMissing = false)
public class DataScopeAspect {
    /**
     * 全部数据权限
     */
    public static final String DATA_SCOPE_ALL = "1";

    /**
     * 自定数据权限
     */
    public static final String DATA_SCOPE_CUSTOM = "2";

    /**
     * 部门数据权限
     */
    public static final String DATA_SCOPE_DEPT = "3";

    /**
     * 部门及以下数据权限
     */
    public static final String DATA_SCOPE_DEPT_AND_CHILD = "4";

    /**
     * 仅本人数据权限
     */
    public static final String DATA_SCOPE_SELF = "5";

    /**
     * 数据权限过滤关键字
     */
    public static final String DATA_SCOPE = "dataScope";

    @Before("@annotation(controllerDataScope)")
    public void doBefore(JoinPoint point, DataScope controllerDataScope) throws Throwable {
        SqlContextHolder.startContext();
        handleDataScope(point, controllerDataScope);

    }

    protected void handleDataScope(final JoinPoint joinPoint, DataScope controllerDataScope) {
        // 获取当前的用户
        LoginUser loginUser = SecurityUtils.getLoginUser();
        if (StringUtils.isNotNull(loginUser)) {
            SysUser currentUser = loginUser.getUser();
            // 如果是超级管理员，则不过滤数据
            if (StringUtils.isNotNull(currentUser) && !currentUser.isAdmin()) {
                String permission = StringUtils.defaultIfEmpty(controllerDataScope.permission(),
                        PermissionContextHolder.getContext());
                dataScopeFilter(joinPoint, currentUser, controllerDataScope.deptAlias(),
                        controllerDataScope.userAlias(), permission);
            }
        }
    }

    /**
     * 数据范围过滤
     *
     * @param joinPoint  切点
     * @param user       用户
     * @param deptAlias  部门别名
     * @param userAlias  用户别名
     * @param permission 权限字符
     */
    public static void dataScopeFilter(JoinPoint joinPoint, SysUser user, String deptAlias, String userAlias,
            String permission) {
        List<Expression> orExpressions = new ArrayList<>();
        List<String> conditions = new ArrayList<>();
        List<String> scopeCustomIds = new ArrayList<>();
        user.getRoles().forEach(role -> {
            if (DATA_SCOPE_CUSTOM.equals(role.getDataScope())
                    && StringUtils.equals(role.getStatus(), UserConstants.ROLE_NORMAL)
                    && StringUtils.containsAny(role.getPermissions(), Convert.toStrArray(permission))) {
                scopeCustomIds.add(Convert.toStr(role.getRoleId()));
            }
        });

        for (SysRole role : user.getRoles()) {
            String dataScope = role.getDataScope();
            if (conditions.contains(dataScope) || StringUtils.equals(role.getStatus(), UserConstants.ROLE_DISABLE)) {
                continue;
            }
            if (StringUtils.isNotEmpty(permission)
                    && !StringUtils.containsAny(role.getPermissions(), Convert.toStrArray(permission))) {
                continue;
            }
            if (DATA_SCOPE_ALL.equals(dataScope)) {
                orExpressions.clear();
                break;
            } else if (DATA_SCOPE_CUSTOM.equals(dataScope) && !scopeCustomIds.isEmpty()) {
                // 自定义权限: d.dept_id IN (SELECT dept_id FROM sys_role_dept WHERE role_id IN\
                if (SqlContextHolder.getData(ContextKey.DATA_SCOPE, "deptAlias", String.class) == null) {
                    SqlContextHolder.addData(ContextKey.DATA_SCOPE, "deptAlias", deptAlias);
                }
                PlainSelect subPlainSelect = new PlainSelect();
                subPlainSelect.setSelectItems(List.of(new SelectItem<>(new Column("dept_id"))));
                subPlainSelect.setFromItem(new Table("sys_role_dept"));
                List<Expression> roleIdExpressions = scopeCustomIds.stream().map(Long::valueOf).map(LongValue::new)
                        .collect(Collectors.toList());
                subPlainSelect.setWhere(
                        new InExpression(new Column("role_id"), new ParenthesedExpressionList<>(roleIdExpressions)));
                InExpression inExpression = new InExpression(new Column(deptAlias + ".dept_id"),
                        new ParenthesedExpressionList<>(subPlainSelect));
                orExpressions.add(new ParenthesedExpressionList<>(inExpression));
            } else if (DATA_SCOPE_DEPT.equals(dataScope)) {
                // 部门权限: d.dept_id = ?
                if (SqlContextHolder.getData(ContextKey.DATA_SCOPE, "deptAlias", String.class) == null) {
                    SqlContextHolder.addData(ContextKey.DATA_SCOPE, "deptAlias", deptAlias);
                }
                orExpressions.add(new EqualsTo(new Column(deptAlias + ".dept_id"), new LongValue(user.getDeptId())));
            } else if (DATA_SCOPE_DEPT_AND_CHILD.equals(dataScope)) {
                // 部门及以下权限: d.dept_id IN (SELECT dept_id FROM sys_dept WHERE dept_id = ? OR
                // find_in_set(?, ancestors))
                if (SqlContextHolder.getData(ContextKey.DATA_SCOPE, "deptAlias", String.class) == null) {
                    SqlContextHolder.addData(ContextKey.DATA_SCOPE, "deptAlias", deptAlias);
                }
                PlainSelect subPlainSelect = new PlainSelect();
                subPlainSelect.setSelectItems(List.of(new SelectItem<>(new Column("dept_id"))));
                subPlainSelect.setFromItem(new Table("sys_dept"));
                Function findInSet = new Function();
                findInSet.setName("find_in_set");
                findInSet.setParameters(new ExpressionList<>(new LongValue(user.getDeptId()), new Column("ancestors")));
                subPlainSelect.setWhere(new OrExpression(
                        new EqualsTo(new Column("dept_id"), new LongValue(user.getDeptId())), findInSet));
                InExpression inExpression = new InExpression(new Column(deptAlias + ".dept_id"),
                        new ParenthesedExpressionList<>(subPlainSelect));
                orExpressions.add(new ParenthesedExpressionList<>(inExpression));
            } else if (DATA_SCOPE_SELF.equals(dataScope)) {
                if (StringUtils.isNotBlank(userAlias)) {
                    if (SqlContextHolder.getData(ContextKey.DATA_SCOPE, "userAlias", String.class) == null) {
                        SqlContextHolder.addData(ContextKey.DATA_SCOPE, "userAlias", userAlias);
                    }
                    orExpressions
                            .add(new EqualsTo(new Column(userAlias + ".user_id"), new LongValue(user.getUserId())));
                } else {
                    if (SqlContextHolder.getData(ContextKey.DATA_SCOPE, "deptAlias", String.class) == null) {
                        SqlContextHolder.addData(ContextKey.DATA_SCOPE, "deptAlias", deptAlias);
                    }
                    Expression expression = new AndExpression(
                            new EqualsTo(new Column(deptAlias + ".dept_id"), new LongValue(user.getDeptId())),
                            new NotEqualsTo(new Column(deptAlias + ".dept_id"), new LongValue(user.getDeptId())));
                    orExpressions.add(expression);
                }
            }
            conditions.add(dataScope);
        }

        if (!orExpressions.isEmpty()) {
            Expression finalExpression = orExpressions.stream().reduce(OrExpression::new).orElse(null);
            if (finalExpression != null) {
                SqlContextHolder.addData(ContextKey.DATA_SCOPE, "expression",
                        new ParenthesedExpressionList<>(finalExpression));
            }
        }
    }

    @After(value = "@annotation(controllerDataScope)")
    public void doAfter(final JoinPoint point, DataScope controllerDataScope) {
        SqlContextHolder.clearContext();
    }

}
