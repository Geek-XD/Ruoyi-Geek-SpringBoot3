package com.ruoyi.framework.manager.factory;

import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import com.ruoyi.common.service.mybatis.CreateSqlSessionFactory;
import com.ruoyi.framework.config.properties.DynamicDataSourceProperties;
import com.ruoyi.framework.datasource.DynamicSqlSessionTemplate;
import com.ruoyi.framework.manager.DataSourceManager;

@Component
public class DynamicSqlSessionFactory {

    @Autowired
    CreateSqlSessionFactory createSqlSessionFactory;

    @Autowired
    DynamicDataSourceProperties dataSourceProperties;

    @Autowired
    DataSourceManager dataSourceManager;

    @Bean(name = "sqlSessionTemplate")
    public DynamicSqlSessionTemplate sqlSessionTemplate(Environment env) throws Exception {
        Map<Object, SqlSessionFactory> sqlSessionFactoryMap = new HashMap<>();
        Map<String, DataSource> targetDataSources = dataSourceManager.getDataSourcesMap();
        for (Map.Entry<String, DataSource> entry : targetDataSources.entrySet()) {
            SqlSessionFactory sessionFactory = createSqlSessionFactory.createSqlSessionFactory(env, entry.getValue());
            sqlSessionFactoryMap.put(entry.getKey(), sessionFactory);
        }
        SqlSessionFactory factoryMaster = sqlSessionFactoryMap.get(dataSourceProperties.getPrimary());
        if (factoryMaster == null) {
            throw new RuntimeException("找不到主库配置" + dataSourceProperties.getPrimary());
        }
        DynamicSqlSessionTemplate customSqlSessionTemplate = new DynamicSqlSessionTemplate(factoryMaster);
        customSqlSessionTemplate.setTargetSqlSessionFactorys(sqlSessionFactoryMap);
        return customSqlSessionTemplate;
    }
}
