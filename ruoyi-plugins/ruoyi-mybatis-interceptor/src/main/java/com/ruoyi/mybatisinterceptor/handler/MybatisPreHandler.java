package com.ruoyi.mybatisinterceptor.handler;

import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;

public interface MybatisPreHandler {

      Object preHandle(Executor executor, MappedStatement mappedStatement, Object params,
                  RowBounds rowBounds, ResultHandler<?> resultHandler, CacheKey cacheKey, BoundSql boundSql)
                  throws Throwable;
}
