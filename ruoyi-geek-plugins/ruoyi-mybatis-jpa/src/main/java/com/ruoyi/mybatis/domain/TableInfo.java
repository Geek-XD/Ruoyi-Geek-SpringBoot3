package com.ruoyi.mybatis.domain;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.core.annotation.AnnotationUtils;

import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.mybatis.annotation.Column;
import com.ruoyi.mybatis.annotation.ColumnMap;
import com.ruoyi.mybatis.annotation.EnableTableMap;
import com.ruoyi.mybatis.annotation.Table;
import com.ruoyi.mybatis.utils.QueryUtil;

/**
 * 数据库表信息
 *
 * @author Dftre
 */
public class TableInfo {
    private String tableName;
    private EnableTableMap enableTableMap;
    private Table table;
    private List<ColumnInfo> columns = new ArrayList<>(); // 所有标记column注解的字段
    private List<ColumnInfo> primaryKeys = new ArrayList<>();
    private List<MapColumnInfo> mapColumns = new ArrayList<>();
    private Set<String> joinSql = new HashSet<>();
    boolean hasDataScopeValue = false;

    public TableInfo(Class<?> cls) {
        this.table = AnnotationUtils.findAnnotation(cls, Table.class);
        if (this.table == null) {
            throw new RuntimeException("error , not find tableName");
        }
        this.tableName = this.table.name();
        this.enableTableMap = AnnotationUtils.findAnnotation(cls, EnableTableMap.class);

        Arrays.stream(cls.getDeclaredFields())
                .filter(field -> AnnotationUtils.findAnnotation(field, Column.class) != null)
                .map(ColumnInfo::new)
                .forEach(this.columns::add);

        Arrays.stream(cls.getDeclaredFields())
                .filter(field -> AnnotationUtils.findAnnotation(field, ColumnMap.class) != null)
                .map(MapColumnInfo::new)
                .forEach(this.mapColumns::add);

        this.getColumns().stream()
                .filter(ColumnInfo::isPrimaryKey)
                .forEach(this.primaryKeys::add);

        this.getMapColumns().stream()
                .map(MapColumnInfo::getJoin)
                .map(join -> {
                    String lf = join.onLeft();
                    String rf = join.onRight();
                    String lt = this.getTableNameT();
                    String rt = join.target();
                    if (StringUtils.isEmpty(lf) || StringUtils.isEmpty(rf)) {
                        lf = join.on();
                        rf = join.on();
                    }
                    return QueryUtil.getJoinSql(lt, rt, lf, rf);
                })
                .forEach(joinSql::add);

        if (this.enableTableMap != null) {
            if (StringUtils.isNotEmpty(this.enableTableMap.user())) {
                String lf = this.enableTableMap.userOnLeft();
                String rf = this.enableTableMap.userOnRight();
                String lt = this.getTableNameT();
                String rt = this.enableTableMap.user();
                if (StringUtils.isEmpty(lf) || StringUtils.isEmpty(rf)) {
                    lf = this.enableTableMap.userOn();
                    rf = this.enableTableMap.userOn();
                }
                this.joinSql.add("sys_user " + QueryUtil.getJoinSql(lt, rt, lf, rf));
                this.hasDataScopeValue = true;
            }

            if (StringUtils.isNotEmpty(this.enableTableMap.dept())) {
                String lf = this.enableTableMap.deptOnLeft();
                String rf = this.enableTableMap.deptOnRight();
                String lt = StringUtils.isNotEmpty(this.enableTableMap.deptFrom()) ? this.enableTableMap.deptFrom()
                        : this.getTableNameT();
                String rt = this.enableTableMap.dept();
                if (StringUtils.isEmpty(lf) || StringUtils.isEmpty(rf)) {
                    lf = this.enableTableMap.deptOn();
                    rf = this.enableTableMap.deptOn();
                }
                this.joinSql.add("sys_dept " + QueryUtil.getJoinSql(lt, rt, lf, rf));
                this.hasDataScopeValue = true;
            }
        }
    }

    public String[] getOrderBy() {
        return this.table.orderBy();
    }

    public boolean hasDataScope() {
        return this.hasDataScopeValue;
    }

    public Set<String> getJoinSql() {
        return this.joinSql;
    }

    public Boolean isEnbleMap() {
        return this.enableTableMap != null;
    }

    public String getTableNameT() {
        return this.enableTableMap.name();
    }

    public String getTableNameFrom() {
        if (this.isEnbleMap()){
            return this.tableName + " " + this.enableTableMap.name();
        }else{
            return this.tableName;
        }
    }

    public List<String> getQueryColumnNames() {
        List<String> columns = Arrays.asList(this.table.columns());
        if (columns.size() <= 0) {
            columns = this.columns.stream()
                    .map(ColumnInfo::getColumnName)
                    .collect(Collectors.toList());
        }
        if (this.isEnbleMap()) {
            columns = columns.stream()
                    .map(column -> this.getTableNameT() + "." + column)
                    .collect(Collectors.toList());
            this.mapColumns.stream()
                    .map(MapColumnInfo::getFullyQualifiedColumnName)
                    .forEach(columns::add);
        }

        return columns;

    }

    public List<String> getColumnNames() {
        List<String> columns = this.columns.stream()
                .map(ColumnInfo::getColumnName)
                .collect(Collectors.toList());

        if (this.isEnbleMap()) {
            columns = columns.stream()
                    .map(column -> this.getTableNameT() + "." + column)
                    .collect(Collectors.toList());
            this.mapColumns.stream()
                    .map(MapColumnInfo::getFullyQualifiedColumnName)
                    .forEach(columns::add);
        }

        return columns;
    }

    public List<ColumnInfo> getColumns() {
        return columns;
    }

    public List<MapColumnInfo> getMapColumns() {
        return mapColumns;
    }

    public <T> List<ColumnInfo> getNotNullColumnsForQuery(T entity) {
        return this.columns.stream()
                .filter(column -> column.fieldQueryIsNotNull(entity))
                .collect(Collectors.toList());
    }

    public <T> List<ColumnInfo> getNotNullColumns(T entity) {
        return this.columns.stream()
                .filter(column -> column.fieldIsNotNull(entity))
                .collect(Collectors.toList());
    }

    public <T> List<MapColumnInfo> getNotNullMapColumnsForQuery(T entity) {
        return this.mapColumns.stream()
                .filter(column -> column.fieldQueryIsNotNull(entity))
                .collect(Collectors.toList());
    }

    public <T> List<MapColumnInfo> getNotNullMapColumns(T entity) {
        return this.mapColumns.stream()
                .filter(column -> column.fieldIsNotNull(entity))
                .collect(Collectors.toList());
    }

    public List<ColumnInfo> getPrimaryKeys() {
        return primaryKeys;
    }

    public String getTableName() {
        return tableName;
    }

}
