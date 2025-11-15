package com.ruoyi.atomikos.datasource;

import java.util.Properties;

import javax.sql.CommonDataSource;
import javax.sql.DataSource;
import javax.sql.XADataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import com.atomikos.spring.AtomikosDataSourceBean;
import com.ruoyi.atomikos.config.AtomikosConfig;
import com.ruoyi.common.core.datasource.AfterCreateDataSource;

@Component
@ConditionalOnProperty(name = "atomikos.enabled", havingValue = "true")
@DependsOn({ "transactionManager" })
public class AtomikosDataSourceCreate implements AfterCreateDataSource {

    @Autowired
    private AtomikosConfig atomikosConfig;

    public DataSource afterCreateDataSource(String name, Properties prop, CommonDataSource dataSource) {
        AtomikosDataSourceBean ds = new AtomikosDataSourceBean();
        atomikosConfig.getAtomikosDataSourceBeans().add(ds);
        ds.setXaDataSourceClassName("com.alibaba.druid.pool.xa.DruidXADataSource");
        ds.setUniqueResourceName(name);
        ds.setXaDataSource((XADataSource) dataSource);
        ds.setXaProperties(prop);
        if (prop.getProperty("minIdle") != null) {
            ds.setMinPoolSize(Integer.parseInt(prop.getProperty("minIdle")));
        }
        if (prop.getProperty("maxActive") != null) {
            ds.setMaxPoolSize(Integer.parseInt(prop.getProperty("maxActive")));
        }
        return ds;
    }
}
