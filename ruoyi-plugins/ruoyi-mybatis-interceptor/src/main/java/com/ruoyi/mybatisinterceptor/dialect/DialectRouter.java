package com.ruoyi.mybatisinterceptor.dialect;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ruoyi.framework.manager.DataSourceManager;

@Component
public class DialectRouter {

    @Autowired
    DataSourceManager dataSourceManager;

    private final Dialect mysql = new MySqlLikeDialect();
    private final Dialect h2 = new H2Dialect();
    private final Dialect oracle = new OracleDialect();
    private final Dialect sqlserver = new SqlServerDialect();

    public Dialect routeByCurrent() {
        String id = dataSourceManager.getCurrentDatabaseId();
        return route(id);
    }

    public Dialect route(String databaseId) {
        String id = databaseId == null ? null : databaseId.toLowerCase();
        if (id == null)
            return mysql; // 默认按 MySQL/PG 类处理
        if (id.contains("mysql") || id.contains("mariadb") || id.contains("postgres") || id.contains("opengauss")) {
            return mysql;
        }
        if (id.contains("h2"))
            return h2;
        if (id.contains("oracle"))
            return oracle;
        if (id.contains("sqlserver") || id.contains("microsoft"))
            return sqlserver;
        return mysql;
    }
}
