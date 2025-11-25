package com.ruoyi.framework.orm;

import javax.sql.DataSource;

import org.apache.ibatis.io.VFS;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.boot.autoconfigure.SpringBootVFS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.io.DefaultResourceLoader;

import com.github.pagehelper.PageInterceptor;
import com.github.pagehelper.autoconfigure.PageHelperStandardProperties;
import com.ruoyi.common.core.orm.CreateSqlSessionFactory;
import com.ruoyi.common.utils.MybatisUtils;
import com.ruoyi.common.utils.StringUtils;

/**
 * Mybatis支持*匹配扫描包
 *
 * @author ruoyi
 */
@Configuration
public class MyBatisConfig {

    Logger logger = LoggerFactory.getLogger(MyBatisConfig.class);

    @Bean
    @ConditionalOnProperty(prefix = "enhance", name = "mybatis", havingValue = "mybatis")
    public CreateSqlSessionFactory createSqlSessionFactory(PageHelperStandardProperties packageHelperStandardProperties) {
        return new CreateSqlSessionFactory() {
            public SqlSessionFactory createSqlSessionFactory(Environment env, DataSource dataSource) throws Exception {
                String typeAliasesPackage = env.getProperty("mybatis.typeAliasesPackage");
                String mapperLocations = env.getProperty("mybatis.mapperLocations");
                String configLocation = env.getProperty("mybatis.configLocation");
                typeAliasesPackage = MybatisUtils.setTypeAliasesPackage(typeAliasesPackage);
                VFS.addImplClass(SpringBootVFS.class);
                
                final SqlSessionFactoryBean sessionFactory = new SqlSessionFactoryBean();
                sessionFactory.setDataSource(dataSource);
                sessionFactory.setDatabaseIdProvider(new CustomDatabaseIdProvider());
                sessionFactory.setTypeAliasesPackage(typeAliasesPackage);
                sessionFactory.setMapperLocations(
                        MybatisUtils.resolveMapperLocations(StringUtils.split(mapperLocations, ",")));
                sessionFactory.setConfigLocation(new DefaultResourceLoader().getResource(configLocation));
                PageInterceptor interceptor = new PageInterceptor();
                interceptor.setProperties(packageHelperStandardProperties.getProperties());
                sessionFactory.addPlugins(interceptor);
                return sessionFactory.getObject();
            }
        };
    }

}
