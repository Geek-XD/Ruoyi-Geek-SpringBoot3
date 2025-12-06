package com.geek.framework.datasource.config;
// package com.geek.framework.datasource.config;

// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.context.annotation.Bean;
// import org.springframework.context.annotation.Configuration;
// import org.springframework.jdbc.datasource.DriverManagerDataSource;

// import com.alibaba.druid.pool.DruidDataSource;
// import com.geek.framework.datasource.manager.DataSourceManager;

// import liquibase.integration.spring.SpringLiquibase;

// @Configuration
// public class LiquibaseConfig {

//     @Autowired
//     private DataSourceManager dataSourceManager;

//     @Bean
//     public SpringLiquibase liquibase() {
//         DriverManagerDataSource nativeDs = new DriverManagerDataSource();
//         DruidDataSource druidDataSource = (DruidDataSource) dataSourceManager.getPrimaryDataSource();
//         nativeDs.setUrl(druidDataSource.getUrl());
//         nativeDs.setUsername(druidDataSource.getUsername());
//         nativeDs.setPassword(druidDataSource.getPassword());
//         nativeDs.setDriverClassName(druidDataSource.getDriverClassName());
//         SpringLiquibase liquibase = new SpringLiquibase();
//         liquibase.setDataSource(nativeDs);
//         liquibase.setChangeLog("classpath:db/changelog-master.xml");
//         liquibase.setShouldRun(true);
//         return liquibase;
//     }
// }
