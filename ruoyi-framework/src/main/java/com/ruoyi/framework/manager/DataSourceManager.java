package com.ruoyi.framework.manager;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import javax.sql.CommonDataSource;
import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.pool.xa.DruidXADataSource;
import com.ruoyi.common.service.datasource.AfterCreateDataSource;
import com.ruoyi.framework.config.DruidConfig;
import com.ruoyi.framework.config.properties.DynamicDataSourceProperties;
import com.ruoyi.framework.datasource.DynamicDataSource;
import com.ruoyi.framework.datasource.DynamicDataSourceContextHolder;

@Configuration
public class DataSourceManager implements InitializingBean {
    protected final Logger logger = LoggerFactory.getLogger(DataSourceManager.class);
    private Map<String, DataSource> targetDataSources = new HashMap<>();
    private final Map<String, String> dsDatabaseId = new ConcurrentHashMap<>();
    private final Map<DataSource, String> dataSourceKeyIndex = new ConcurrentHashMap<>();

    @Value("${spring.datasource.dynamic.xa}")
    private boolean xa;

    @Autowired
    private DynamicDataSourceProperties dataSourceProperties;

    @Autowired
    private DruidConfig druidConfig;

    @Autowired(required = false)
    private AfterCreateDataSource afterCreateDataSource;

    @Bean(name = "dynamicDataSource")
    @Primary
    DynamicDataSource dataSource() {
        Map<Object, Object> objectMap = new HashMap<>();
        Map<String, DataSource> targetDataSources = this.getDataSourcesMap();
        for (Map.Entry<String, DataSource> entry : targetDataSources.entrySet()) {
            objectMap.put(entry.getKey(), entry.getValue());
        }
        return new DynamicDataSource(targetDataSources.get(dataSourceProperties.getPrimaryStorageBucket()), objectMap);
    }

    // 仅启动期调用：基于 JDBC 元数据判定 mybatis databaseId（含 openGauss 区分），与 MyBatis 机制兼容
    private String detectDatabaseId(DataSource dataSource) {
        try (Connection conn = dataSource.getConnection()) {
            DatabaseMetaData md = conn.getMetaData();
            String productName = safeLower(md.getDatabaseProductName());
            String driverName = safeLower(md.getDriverName());
            String url = safeLower(null);
            try {
                url = safeLower(md.getURL());
            } catch (Exception ignore) {
            }

            // 优先尝试：通过 select version() 识别并顺带验证数据源
            try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery("select version()")) {
                if (rs.next()) {
                    String v = safeLower(rs.getString(1));
                    if (v != null) {
                        if (v.contains("openGauss") || v.contains("gauss")) {
                            return "openGauss";
                        }
                        if (v.contains("postgresql") || v.contains("postgres")) {
                            return "postgresql";
                        }
                        if (v.contains("mysql") || v.contains("mariadb")) {
                            return "mysql";
                        }
                        if (v.contains("oracle")) {
                            return "oracle";
                        }
                        if (v.contains("sql server") || v.contains("microsoft")) {
                            return "sqlserver";
                        }
                        if (v.contains("h2")) {
                            return "h2";
                        }
                    }
                }
            } catch (Exception ignore) {
            }
            // 先看 URL/驱动是否明确标识 openGauss/Gauss
            if ((url != null && url.startsWith("jdbc:openGauss:"))
                    || (driverName != null && (driverName.contains("openGauss") || driverName.contains("gauss")))
                    || (productName != null && (productName.contains("openGauss") || productName.contains("gauss")))) {
                return "openGauss";
            }

            if (productName != null && productName.contains("postgres")) {
                // 区分 openGauss 与 PostgreSQL
                String ver = safeLower(md.getDatabaseProductVersion());
                if ((ver != null && (ver.contains("openGauss") || ver.contains("gauss")))) {
                    return "openGauss";
                }
                return "postgresql";
            }
            if (productName != null) {
                if (productName.contains("mysql"))
                    return "mysql";
                if (productName.contains("oracle"))
                    return "oracle";
                if (productName.contains("sql server") || productName.contains("microsoft sql"))
                    return "sqlserver";
                if (productName.contains("h2"))
                    return "h2";
            }
            return productName != null ? productName : null;
        } catch (SQLException e) {
            logger.warn("databaseId 判定失败，返回 null", e);
            return null;
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        dataSourceProperties.getDatasource().forEach((name, props) -> {
            Properties properties = dataSourceProperties.build(props);
            CommonDataSource commonDataSource = createDataSource(name, properties);
            if (afterCreateDataSource != null) {
                afterCreateDataSource.afterCreateDataSource(name, properties, commonDataSource);
            }
            DataSource dataSource = (DataSource) commonDataSource;
            logger.info("数据源：{} 校验中.......", name);
            long start = System.currentTimeMillis();
            String databaseId = detectDatabaseId(dataSource);
            if (databaseId != null) {
                dsDatabaseId.put(name, databaseId);
            }
            logger.info("数据源：{} 链接成功，耗时：{}ms，databaseId：{}", name, System.currentTimeMillis() - start, databaseId);

            putDataSource(name, dataSource);
        });
    }

    public DataSource createDataSource(String name, Properties prop) {
        DruidDataSource dataSource = null;
        if (xa) {
            dataSource = new DruidXADataSource();
        } else {
            dataSource = new DruidDataSource();
        }
        druidConfig.getDruidDataSources().add(dataSource);
        dataSource.setConnectProperties(prop);
        dataSourceProperties.setProperties(dataSource, prop);
        return dataSource;
    }

    public DataSource getPrimaryDataSource() {
        return targetDataSources.get(dataSourceProperties.getPrimaryStorageBucket());
    }

    public DataSource getDataSource(String name) {
        return targetDataSources.get(name);
    }

    public Collection<DataSource> getDataSources() {
        return targetDataSources.values();
    }

    public Map<String, DataSource> getDataSourcesMap() {
        return targetDataSources;
    }

    public void putDataSource(String name, DataSource dataSource) {
        targetDataSources.put(name, dataSource);
        dataSourceKeyIndex.put(dataSource, name);
        dsDatabaseId.computeIfAbsent(name, k -> detectDatabaseId(dataSource));
    }

    // 提供给 Provider/业务：根据 key 或当前线程获取已缓存的 databaseId
    public String getDatabaseId(String dsKey) {
        String key = (dsKey == null || dsKey.isEmpty()) ? dataSourceProperties.getPrimaryStorageBucket() : dsKey;
        return dsDatabaseId.get(key);
    }

    public String getCurrentDatabaseId() {
        return getDatabaseId(DynamicDataSourceContextHolder.getDataSourceType());
    }

    // 提供 DataSource -> key 的反查，供 Provider 复用缓存
    public String findKeyByDataSource(DataSource dataSource) {
        return dataSourceKeyIndex.get(dataSource);
    }

    // 提供复用：按 DataSource 计算并（在可确定 key 时）写入缓存
    public String computeDatabaseIdForDataSource(DataSource dataSource) {
        String key = findKeyByDataSource(dataSource);
        String id = detectDatabaseId(dataSource);
        if (key != null && id != null) {
            dsDatabaseId.putIfAbsent(key, id);
        }
        return id;
    }

    // 提供复用：按指定 key 计算并写入缓存
    public String computeAndCacheDatabaseId(String dsKey, DataSource dataSource) {
        String id = detectDatabaseId(dataSource);
        if (dsKey != null && id != null) {
            dsDatabaseId.putIfAbsent(dsKey, id);
        }
        return id;
    }

    private static String safeLower(String s) {
        return s == null ? null : s.toLowerCase(Locale.ROOT);
    }

}
