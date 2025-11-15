package com.ruoyi.common.core.datasource;

import java.util.Properties;

import javax.sql.CommonDataSource;
import javax.sql.DataSource;

public interface AfterCreateDataSource {
    DataSource afterCreateDataSource(String name,Properties prop, CommonDataSource dataSource);
}
