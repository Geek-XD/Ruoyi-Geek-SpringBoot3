package com.ruoyi.framework.config;

import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.ibatis.io.VFS;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.boot.autoconfigure.SpringBootVFS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.io.DefaultResourceLoader;

import com.ruoyi.common.enums.DataSourceType;
import com.ruoyi.common.interceptor.mybatis.CreateSqlSessionFactory;
import com.ruoyi.common.utils.MybatisUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.spring.SpringUtils;
import com.ruoyi.framework.datasource.DynamicSqlSessionTemplate;

/**
 * Mybatis支持*匹配扫描包
 *
 * @author ruoyi
 */
@Configuration
public class MyBatisConfig {

    Logger logger = LoggerFactory.getLogger(MyBatisConfig.class);

    @Bean
    @ConditionalOnProperty(prefix = "createSqlSessionFactory", name = "use", havingValue = "mybatis")
    public CreateSqlSessionFactory createSqlSessionFactory() {
        return new CreateSqlSessionFactory() {
            public SqlSessionFactory createSqlSessionFactory(Environment env, DataSource dataSource) throws Exception {
                String typeAliasesPackage = env.getProperty("mybatis.typeAliasesPackage");
                String mapperLocations = env.getProperty("mybatis.mapperLocations");
                String configLocation = env.getProperty("mybatis.configLocation");
                typeAliasesPackage = MybatisUtils.setTypeAliasesPackage(typeAliasesPackage);
                VFS.addImplClass(SpringBootVFS.class);

                final SqlSessionFactoryBean sessionFactory = new SqlSessionFactoryBean();
                sessionFactory.setDataSource(dataSource);
                sessionFactory.setTypeAliasesPackage(typeAliasesPackage);
                sessionFactory.setMapperLocations(
                        MybatisUtils.resolveMapperLocations(StringUtils.split(mapperLocations, ",")));
                sessionFactory.setConfigLocation(new DefaultResourceLoader().getResource(configLocation));
                return sessionFactory.getObject();
            }
        };
    }

    @Bean(name = "sqlSessionTemplate")
    public DynamicSqlSessionTemplate sqlSessionTemplate(
            @Qualifier("sqlSessionFactoryMaster") SqlSessionFactory factoryMaster) throws Exception {
        Map<Object, SqlSessionFactory> sqlSessionFactoryMap = new HashMap<>();
        sqlSessionFactoryMap.put(DruidConfig.MASTER, factoryMaster);
        putSqlSessionFactory("sqlSessionFactorySlave", DataSourceType.SLAVE, sqlSessionFactoryMap);
        DynamicSqlSessionTemplate customSqlSessionTemplate = new DynamicSqlSessionTemplate(factoryMaster);
        customSqlSessionTemplate.setTargetSqlSessionFactorys(sqlSessionFactoryMap);
        return customSqlSessionTemplate;
    }

    private void putSqlSessionFactory(String sqlSessionFactoryName, DataSourceType dataSourceType,
            Map<Object, SqlSessionFactory> sqlSessionFactoryMap) {
        try {
            SqlSessionFactory factorySlave = SpringUtils.getBean(sqlSessionFactoryName);
            sqlSessionFactoryMap.put(dataSourceType.name(), factorySlave);
        } catch (Exception e) {
            logger.error("Failed to register a SqlSessionFactory:{}", sqlSessionFactoryName);
        }
    }

}
