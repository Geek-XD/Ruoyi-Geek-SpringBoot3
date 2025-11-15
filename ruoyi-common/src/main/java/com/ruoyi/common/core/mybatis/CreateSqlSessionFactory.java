package com.ruoyi.common.core.mybatis;

import javax.sql.DataSource;

import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.core.env.Environment;

public interface CreateSqlSessionFactory {
    public SqlSessionFactory createSqlSessionFactory(Environment env, DataSource dataSource) throws Exception;
}
