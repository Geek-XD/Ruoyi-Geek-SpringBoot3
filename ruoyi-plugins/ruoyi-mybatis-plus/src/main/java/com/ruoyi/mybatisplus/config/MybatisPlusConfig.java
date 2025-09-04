package com.ruoyi.mybatisplus.config;

import javax.sql.DataSource;

import org.apache.ibatis.io.VFS;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.io.DefaultResourceLoader;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.autoconfigure.SpringBootVFS;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.BlockAttackInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.OptimisticLockerInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.baomidou.mybatisplus.extension.spring.MybatisSqlSessionFactoryBean;
import com.github.pagehelper.PageInterceptor;
import com.github.pagehelper.autoconfigure.PageHelperStandardProperties;
import com.ruoyi.common.service.mybatis.CreateSqlSessionFactory;
import com.ruoyi.common.utils.MybatisUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.mybatisinterceptor.interceptor.mybatis.DataScopeInterceptor;
import com.ruoyi.framework.mybatis.CustomDatabaseIdProvider;
/**
 * Mybatis Plus 配置
 *
 * @author ruoyi
 */
@Configuration
public class MybatisPlusConfig {
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        // 分页插件
        interceptor.addInnerInterceptor(paginationInnerInterceptor());
        // 乐观锁插件
        interceptor.addInnerInterceptor(optimisticLockerInnerInterceptor());
        // 阻断插件
        interceptor.addInnerInterceptor(blockAttackInnerInterceptor());
        return interceptor;
    }

    /**
     * 分页插件，自动识别数据库类型 https://baomidou.com/guide/interceptor-pagination.html
     */
    public PaginationInnerInterceptor paginationInnerInterceptor() {
        PaginationInnerInterceptor paginationInnerInterceptor = new PaginationInnerInterceptor();
        // 设置数据库类型为mysql
        paginationInnerInterceptor.setDbType(DbType.MYSQL);
        // 设置最大单页限制数量，默认 500 条，-1 不受限制
        paginationInnerInterceptor.setMaxLimit(-1L);
        return paginationInnerInterceptor;
    }

    /**
     * 乐观锁插件 https://baomidou.com/guide/interceptor-optimistic-locker.html
     */
    public OptimisticLockerInnerInterceptor optimisticLockerInnerInterceptor() {
        return new OptimisticLockerInnerInterceptor();
    }

    /**
     * 如果是对全表的删除或更新操作，就会终止该操作
     * https://baomidou.com/guide/interceptor-block-attack.html
     */
    public BlockAttackInnerInterceptor blockAttackInnerInterceptor() {
        return new BlockAttackInnerInterceptor();
    }

    @Bean
    @ConditionalOnProperty(prefix = "createSqlSessionFactory", name = "use", havingValue = "mybatis-plus")
    public CreateSqlSessionFactory createSqlSessionFactory(PageHelperStandardProperties packageHelperStandardProperties) {
        return new CreateSqlSessionFactory() {
            public SqlSessionFactory createSqlSessionFactory(Environment env, DataSource dataSource) throws Exception {
                String typeAliasesPackage = env.getProperty("mybatis-plus.typeAliasesPackage");
                String mapperLocations = env.getProperty("mybatis-plus.mapperLocations");
                String configLocation = env.getProperty("mybatis-plus.configLocation");
                typeAliasesPackage = MybatisUtils.setTypeAliasesPackage(typeAliasesPackage);
                VFS.addImplClass(SpringBootVFS.class);

                final MybatisSqlSessionFactoryBean sessionFactory = new MybatisSqlSessionFactoryBean();
                sessionFactory.setPlugins(new Interceptor[] { mybatisPlusInterceptor() });
                sessionFactory.setDataSource(dataSource);
                // 设置自定义 DatabaseIdProvider：区分 openGauss 与 PostgreSQL
                sessionFactory.setDatabaseIdProvider(new CustomDatabaseIdProvider());
                sessionFactory.setTypeAliasesPackage(typeAliasesPackage);
                sessionFactory.setMapperLocations(
                        MybatisUtils.resolveMapperLocations(StringUtils.split(mapperLocations, ",")));
                sessionFactory.setConfigLocation(new DefaultResourceLoader().getResource(configLocation));
                PageInterceptor interceptor = new PageInterceptor();
                interceptor.setProperties(packageHelperStandardProperties.getProperties());
                sessionFactory.addPlugins(interceptor,new DataScopeInterceptor());
                return sessionFactory.getObject();
            }
        };
    }

}
