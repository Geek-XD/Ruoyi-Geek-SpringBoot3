package com.geek.framework.datasource.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;

/**
 * druid 配置属性
 * 
 * @author ruoyi
 */
@Data
@ConfigurationProperties(prefix = "spring.datasource.druid")
public class DruidProperties {
    private int initialSize;
    private int minIdle;
    private int maxActive;
    private int maxWait;
    private int connectTimeout;
    private int socketTimeout;
    private int timeBetweenEvictionRunsMillis;
    private int minEvictableIdleTimeMillis;
    private int maxEvictableIdleTimeMillis;
    private String validationQuery;
    private boolean testWhileIdle;
    private boolean testOnBorrow;
    private boolean testOnReturn;
    private String connectionProperties;
    private String filters;
}
