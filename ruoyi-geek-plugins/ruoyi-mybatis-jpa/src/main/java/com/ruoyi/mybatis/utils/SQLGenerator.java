package com.ruoyi.mybatis.utils;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;

import org.apache.ibatis.jdbc.SQL;

import com.ruoyi.common.core.domain.BaseEntity;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.mybatis.domain.ColumnInfo;
import com.ruoyi.mybatis.domain.MapColumnInfo;
import com.ruoyi.mybatis.domain.TableInfo;

public class SQLGenerator {

	public static class Method {
		public final static String LIST = "list";
		public final static String INSERT = "insert";
		public final static String UPDATE = "update";
		public final static String SELECT_BY_ID = "selectById";
		public final static String DELETE_BY_ID = "deleteById";
		public final static String SELECT_BY_IDS = "selectByIds";
		public final static String DELETE_BY_IDS = "deleteByIds";
		public final static String SELECT_BY_ID_WITH_ENTITY = "selectByIdWithEntity";
		public final static String DELETE_BY_ID_WITH_ENTITY = "deleteByIdWithEntity";
	}

	public static <T extends BaseEntity> String list(T entity) {
		SQL sql = new SQL();
		TableInfo tableInfo = TableContainer.getTableInfo(entity);
		sql.SELECT(String.join(",", tableInfo.getQueryColumnNames()))
				.FROM(tableInfo.getTableNameFrom());

		if (tableInfo.isEnbleMap()) {
			tableInfo.getJoinSql().stream()
					.filter(StringUtils::isNotEmpty)
					.forEach(sql::LEFT_OUTER_JOIN);
			tableInfo.getNotNullMapColumnsForQuery(entity).stream()
					.map(MapColumnInfo::getQuerySql)
					.forEach(sql::WHERE);
			tableInfo.getNotNullColumnsForQuery(entity).stream()
					.map(ColumnInfo::getQuerySql)
					.map(query -> tableInfo.getTableNameT() + "." + query)
					.forEach(sql::WHERE);
			if (tableInfo.hasDataScope()) {
				sql.WHERE("1=1 ${params.dataScope}");
			}

			Arrays.stream(tableInfo.getOrderBy())
					.filter(StringUtils::isNotEmpty)
					.map(order -> tableInfo.getTableNameT() + "." + order)
					.forEach(sql::ORDER_BY);
		} else {
			tableInfo.getNotNullColumnsForQuery(entity).stream()
					.map(ColumnInfo::getQuerySql)
					.filter(StringUtils::isNotEmpty)
					.forEach(sql::WHERE);
			Arrays.stream(tableInfo.getOrderBy())
					.filter(StringUtils::isNotEmpty)
					.forEach(sql::ORDER_BY);
		}
		return sql.toString();
	}

	public static <T extends BaseEntity> String insert(T entity) {
		SQL sql = new SQL();
		TableInfo tableInfo = TableContainer.getTableInfo(entity);
		sql.INSERT_INTO(tableInfo.getTableName());
		tableInfo.getNotNullColumns(entity).stream()
				.forEach(column -> sql.VALUES(column.getColumnName(), column.getTemplate()));
		return sql.toString();
	}

	public static <T extends BaseEntity> String update(T entity) {
		SQL sql = new SQL();
		TableInfo tableInfo = TableContainer.getTableInfo(entity);
		sql.UPDATE(tableInfo.getTableName());
		tableInfo.getPrimaryKeys().stream()
				.map(column -> column.getColumnName() + " = " + column.getTemplate())
				.forEach(sql::WHERE);
		if (tableInfo.hasDataScope()) {
			sql.WHERE("1=1 ${params.dataScope}");
		}
		tableInfo.getNotNullColumns(entity).stream()
				.filter(column -> !column.isPrimaryKey())
				.map(column -> column.getColumnName() + " = " + column.getTemplate())
				.forEach(sql::SET);
		return sql.toString();
	}

	public static <T extends BaseEntity> String deleteByIdWithEntity(T entity) {
		SQL sql = new SQL();
		TableInfo tableInfo = TableContainer.getTableInfo(entity);
		sql.DELETE_FROM(tableInfo.getTableName());
		tableInfo.getPrimaryKeys().stream()
				.map(column -> column.getColumnName() + " = " + column.getTemplate())
				.forEach(sql::WHERE);

		if (tableInfo.hasDataScope()) {
			sql.WHERE("1=1 ${params.dataScope}");
		}
		return sql.toString();
	}

	public static <T extends BaseEntity> String selectByIdWithEntity(T entity) {
		SQL sql = new SQL();
		TableInfo tableInfo = TableContainer.getTableInfo(entity);
		sql.SELECT(String.join(",", tableInfo.getColumnNames()))
				.FROM(tableInfo.getTableNameFrom());
		if (tableInfo.isEnbleMap()) {
			tableInfo.getJoinSql().stream()
					.forEach(sql::LEFT_OUTER_JOIN);

			tableInfo.getPrimaryKeys().stream()
					.map(column -> tableInfo.getTableNameT() + "." + column.getColumnName() + " = "
							+ column.getTemplate())
					.forEach(sql::WHERE);
		} else {
			tableInfo.getPrimaryKeys().stream()
					.map(column -> column.getColumnName() + " = " + column.getTemplate())
					.forEach(sql::WHERE);
		}
		if (tableInfo.hasDataScope()) {
			sql.WHERE("1=1 ${params.dataScope}");
		}
		return sql.toString();
	}

	public static <T extends BaseEntity> String selectById(Serializable id, Class<T> clz) {
		SQL sql = new SQL();
		TableInfo tableInfo = TableContainer.getTableInfo(clz);
		sql.SELECT(String.join(",", tableInfo.getColumnNames()))
				.FROM(tableInfo.getTableNameFrom());
		if (tableInfo.isEnbleMap()) {
			tableInfo.getJoinSql().stream().forEach(sql::LEFT_OUTER_JOIN);
			ColumnInfo columnInfo = tableInfo.getPrimaryKeys().get(0);
			String columnName = tableInfo.getTableNameT() + "." + columnInfo.getColumnName();
			sql.WHERE(columnName + " = " + id);
		} else {
			ColumnInfo columnInfo = tableInfo.getPrimaryKeys().get(0);
			sql.WHERE(columnInfo.getColumnName() + " = " + id);
		}
		if (tableInfo.hasDataScope()) {
			sql.WHERE("1=1 ${params.dataScope}");
		}
		return sql.toString();
	}

	public static <T extends BaseEntity> String selectByIds(Collection<Serializable> ids, Class<T> clz) {
		SQL sql = new SQL();
		TableInfo tableInfo = TableContainer.getTableInfo(clz);
		sql.SELECT(String.join(",", tableInfo.getColumnNames()))
				.FROM(tableInfo.getTableNameFrom());
		if (tableInfo.isEnbleMap()) {
			tableInfo.getJoinSql().stream()
					.forEach(sql::LEFT_OUTER_JOIN);

			ColumnInfo columnInfo = tableInfo.getPrimaryKeys().get(0);
			String columnName = tableInfo.getTableNameT() + "." + columnInfo.getColumnName();
			sql.WHERE(columnName + " in " + QueryUtil.listToInSQL(ids));
		} else {
			ColumnInfo columnInfo = tableInfo.getPrimaryKeys().get(0);
			sql.WHERE(columnInfo.getColumnName() + " in " + QueryUtil.listToInSQL(ids));
		}
		if (tableInfo.hasDataScope()) {
			sql.WHERE("1=1 ${params.dataScope}");
		}
		return sql.toString();
	}

	public static <T extends BaseEntity> String deleteById(Serializable id, Class<T> clz) {
		SQL sql = new SQL();
		TableInfo tableInfo = TableContainer.getTableInfo(clz);
		sql.DELETE_FROM(tableInfo.getTableName());
		ColumnInfo columnInfo = tableInfo.getPrimaryKeys().get(0);
		sql.WHERE(columnInfo.getColumnName() + " = " + id);
		if (tableInfo.hasDataScope()) {
			sql.WHERE("1=1 ${params.dataScope}");
		}
		return sql.toString();
	}

	public static <T extends BaseEntity> String deleteByIds(Collection<Serializable> ids, Class<T> clz) {
		SQL sql = new SQL();
		TableInfo tableInfo = TableContainer.getTableInfo(clz);
		sql.DELETE_FROM(tableInfo.getTableName());
		ColumnInfo columnInfo = tableInfo.getPrimaryKeys().get(0);
		sql.WHERE(columnInfo.getColumnName() + " in " + QueryUtil.listToInSQL(ids));
		if (tableInfo.hasDataScope()) {
			sql.WHERE("1=1 ${params.dataScope}");
		}
		return sql.toString();
	}

}
