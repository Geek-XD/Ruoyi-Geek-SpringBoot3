package com.ruoyi.mybatisinterceptor.interceptor;

import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;

public abstract class MybatisInterceptor implements Interceptor {

   @Override
   public Object intercept(Invocation invocation) throws Throwable {
      Executor targetExecutor = (Executor) invocation.getTarget();
      Object[] args = invocation.getArgs();
      MappedStatement ms = (MappedStatement) args[0];
      Object parameterObject = args[1];
      RowBounds rowBounds = (RowBounds) args[2];
      ResultHandler<?> resultHandler = (ResultHandler<?>) args[3];
      Object ret = null;
      QueryFetcher fetcher = null;
      if (args.length < 6) {
         BoundSql boundSql = ms.getBoundSql(parameterObject);
         CacheKey cacheKey = targetExecutor.createCacheKey(ms, parameterObject, rowBounds, boundSql);
         ret = runPreHandlers(targetExecutor, ms, parameterObject, rowBounds, resultHandler, cacheKey, boundSql);
         fetcher = () -> targetExecutor.query(ms, parameterObject, rowBounds, resultHandler, cacheKey, boundSql);
      } else {
         CacheKey cacheKey = (CacheKey) args[4];
         BoundSql boundSql = (BoundSql) args[5];
         ret = runPreHandlers(targetExecutor, ms, parameterObject, rowBounds, resultHandler, cacheKey, boundSql);
         fetcher = () -> invocation.proceed();
      }
      try {
         if (ret != null) {
            return applyAfterHandlers(ret);
         } else {
            return applyAfterHandlers(fetcher.get());
         }
      } finally {
         finish();
      }
   }

   public abstract Object runPreHandlers(Executor executor, MappedStatement ms, Object parameterObject,
         RowBounds rowBounds, ResultHandler<?> resultHandler, CacheKey cacheKey, BoundSql boundSql) throws Throwable;

   public abstract Object applyAfterHandlers(Object result) throws Throwable;

   public abstract void finish();

   @FunctionalInterface
   private interface QueryFetcher {
      Object get() throws Throwable;
   }

}
