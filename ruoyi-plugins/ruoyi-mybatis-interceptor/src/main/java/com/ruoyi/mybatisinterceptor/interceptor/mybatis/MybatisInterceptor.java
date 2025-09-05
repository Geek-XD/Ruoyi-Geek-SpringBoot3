package com.ruoyi.mybatisinterceptor.interceptor.mybatis;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ruoyi.mybatisinterceptor.annotation.MybatisHandlerOrder;
import com.ruoyi.mybatisinterceptor.handler.MybatisAfterHandler;
import com.ruoyi.mybatisinterceptor.handler.MybatisFinishHandler;
import com.ruoyi.mybatisinterceptor.handler.MybatisPreHandler;

import jakarta.annotation.PostConstruct;

@Component
@Intercepts({
      @Signature(type = Executor.class, method = "query", args = { MappedStatement.class, Object.class,
            RowBounds.class, ResultHandler.class, CacheKey.class, BoundSql.class }),
      @Signature(type = Executor.class, method = "query", args = {
            MappedStatement.class, Object.class, RowBounds.class,
            ResultHandler.class })

})
public class MybatisInterceptor implements Interceptor {

   @Autowired
   private List<MybatisPreHandler> preHandlerBeans;

   @Autowired
   private List<MybatisAfterHandler> afterHandlerBeans;

   @Autowired
   private List<MybatisFinishHandler> finishHandlerBeans;

   private static List<MybatisPreHandler> preHandlers;
   private static List<MybatisAfterHandler> afterHandlers;
   private static List<MybatisFinishHandler> finishHandlers;

   @PostConstruct
   public void init() {
      preHandlers = sortHandlers(preHandlerBeans);
      afterHandlers = sortHandlers(afterHandlerBeans);
      finishHandlers = sortHandlers(finishHandlerBeans);
   }

   /**
    * 通用的处理器排序方法
    */
   private <T> List<T> sortHandlers(List<T> handlers) {
      if (handlers == null || handlers.isEmpty()) {
         return Collections.emptyList();
      } else {
         return handlers.stream()
               .sorted((item1, item2) -> {
                  MybatisHandlerOrder ann1 = item1.getClass().getAnnotation(MybatisHandlerOrder.class);
                  MybatisHandlerOrder ann2 = item2.getClass().getAnnotation(MybatisHandlerOrder.class);
                  int a = ann1 == null ? 0 : ann1.value();
                  int b = ann2 == null ? 0 : ann2.value();
                  return a - b;
               }).collect(Collectors.toList());
      }
   }

   @Override
   public Object intercept(Invocation invocation) throws Throwable {
      Executor targetExecutor = (Executor) invocation.getTarget();
      Object[] args = invocation.getArgs();
      MappedStatement ms = (MappedStatement) args[0];
      Object parameterObject = args[1];
      RowBounds rowBounds = (RowBounds) args[2];
      ResultHandler<?> resultHandler = (ResultHandler<?>) args[3];
      if (args.length < 6) {
         BoundSql boundSql = ms.getBoundSql(parameterObject);
         CacheKey cacheKey = targetExecutor.createCacheKey(ms, parameterObject, rowBounds, boundSql);
         return executeWithCommonFlow(
               runPreHandlers(targetExecutor, ms, parameterObject, rowBounds, resultHandler, cacheKey, boundSql),
               () -> targetExecutor.query(ms, parameterObject, rowBounds, resultHandler, cacheKey, boundSql));
      } else {
         return executeWithCommonFlow(
               runPreHandlers(targetExecutor, ms, parameterObject, rowBounds, resultHandler,
                     (CacheKey) args[4], (BoundSql) args[5]),
               () -> invocation.proceed());
      }
   }

   private Object runPreHandlers(Executor executor, MappedStatement ms, Object parameterObject, RowBounds rowBounds,
         ResultHandler<?> resultHandler, CacheKey cacheKey, BoundSql boundSql) throws Throwable {
      Object ret = null;
      if (preHandlers == null || preHandlers.isEmpty())
         return ret;
      for (MybatisPreHandler item : preHandlers) {
         ret = item.preHandle(executor, ms, parameterObject, rowBounds, resultHandler, cacheKey, boundSql);
         if (ret != null) {
            break;
         }
      }
      return ret;
   }

   @FunctionalInterface
   private interface QueryFetcher {
      Object get() throws Throwable;
   }

   private Object executeWithCommonFlow(Object ret, QueryFetcher fetcher) throws Throwable {
      try {
         if (ret != null) {
            return applyAfterHandlers(ret);
         } else {
            Object result = fetcher.get();
            return applyAfterHandlers(result);
         }
      } finally {
         safeClearThreadLocal();
      }
   }

   private Object applyAfterHandlers(Object result) throws Throwable {
      if (afterHandlers == null || afterHandlers.isEmpty())
         return result;
      Object r = result;
      for (MybatisAfterHandler item : afterHandlers) {
         r = item.handleObject(r);
      }
      return r;
   }

   private void safeClearThreadLocal() {
      try {
         finishHandlers.forEach(MybatisFinishHandler::finish);
      } catch (Throwable ignore) {
      }
   }

}
