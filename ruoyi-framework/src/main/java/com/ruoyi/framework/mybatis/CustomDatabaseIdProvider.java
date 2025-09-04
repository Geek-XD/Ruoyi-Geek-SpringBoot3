package com.ruoyi.framework.mybatis;

import java.sql.SQLException;
import java.util.Properties;

import javax.sql.DataSource;

import org.apache.ibatis.mapping.DatabaseIdProvider;

import com.ruoyi.common.utils.spring.SpringUtils;
import com.ruoyi.framework.manager.DataSourceManager;

/**
 * 自定义 DatabaseIdProvider：
 * - 与 MyBatis 兼容的扩展点，用于为不同数据库返回 databaseId。
 * - 解决 openGauss 的 DatabaseProductName 返回 "PostgreSQL" 无法区分的问题。
 *
 * 规则：
 * 1) 先取 DatabaseProductName（与 MyBatis 一致）。
 * 2) 若为 PostgreSQL，再用 ProductVersion / DriverName / 可选的 select version() 轻量判断一次 openGauss。
 * 3) 仅在构建 SqlSessionFactory 时调用一次，不引入运行期开销。
 */
public class CustomDatabaseIdProvider implements DatabaseIdProvider {

    @Override
    public void setProperties(Properties p) {
        // 无需额外属性
    }

    @Override
    public String getDatabaseId(DataSource dataSource) throws SQLException {
        // 先尝试从缓存复用：避免重复探测，兼容动态数据源
        try {
            DataSourceManager dsm = SpringUtils.getBean(DataSourceManager.class);
            String key = dsm.findKeyByDataSource(dataSource);
            if (key != null) {
                String cached = dsm.getDatabaseId(key);
                if (cached != null) return cached;
                // 缓存缺失：计算并写回缓存，再返回
                String computed = dsm.computeAndCacheDatabaseId(key, dataSource);
                if (computed != null) return computed;
            } else {
                // 缓存缺失且 key 未知：计算但不写回（无法索引），直接返回
                return dsm.computeDatabaseIdForDataSource(dataSource);
            }
        } catch (Exception ignore) { /* Spring 尚未就绪或非容器内调用时，忽略 */ }
        // 容器未就绪时的兜底：直接打开连接由 DataSourceManager 再次计算（会在 SqlSessionFactory 创建期间运行）
        try {
            DataSourceManager dsm = SpringUtils.getBean(DataSourceManager.class);
            return dsm.computeDatabaseIdForDataSource(dataSource);
        } catch (Exception e) {
            throw new SQLException("Unable to resolve databaseId", e);
        }
    }
}
