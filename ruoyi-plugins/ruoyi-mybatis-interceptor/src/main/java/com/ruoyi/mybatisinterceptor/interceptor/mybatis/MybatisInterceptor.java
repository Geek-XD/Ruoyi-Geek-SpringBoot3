package com.ruoyi.mybatisinterceptor.interceptor.mybatis;

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

   private static List<MybatisPreHandler> preHandlersChain;

   private static List<MybatisAfterHandler> afterHandlersChain;

   @PostConstruct
   public void init() {
      // 对处理器按注解排序
      preHandlersChain = sortHandlers(preHandlerBeans);
      afterHandlersChain = sortHandlers(afterHandlerBeans);
   }

   /**
    * 通用的处理器排序方法
    */
   private <T> List<T> sortHandlers(List<T> handlers) {
      return handlers.stream()
            .sorted((item1, item2) -> {
               MybatisHandlerOrder ann1 = item1.getClass().getAnnotation(MybatisHandlerOrder.class);
               MybatisHandlerOrder ann2 = item2.getClass().getAnnotation(MybatisHandlerOrder.class);
               int a = ann1 == null ? 0 : ann1.value();
               int b = ann2 == null ? 0 : ann2.value();
               return a - b;
            }).collect(Collectors.toList());
   }

   @Override
   public Object intercept(Invocation invocation) throws Throwable {
      Executor targetExecutor = (Executor) invocation.getTarget();
      Object[] args = invocation.getArgs();
      if (args.length < 6) {
         if (preHandlersChain != null && preHandlersChain.size() > 0) {
            MappedStatement ms = (MappedStatement) args[0];
            Object parameterObject = args[1];
            RowBounds rowBounds = (RowBounds) args[2];
            Executor executor = (Executor) invocation.getTarget();
            BoundSql boundSql = ms.getBoundSql(parameterObject);
            // 可以对参数做各种处理
            CacheKey cacheKey = executor.createCacheKey(ms, parameterObject, rowBounds, boundSql);
            for (MybatisPreHandler item : preHandlersChain) {
               item.preHandle(targetExecutor, ms, args[1], (RowBounds) args[2],
                     (ResultHandler<?>) args[3], cacheKey, boundSql);
            }
         }
         Object result = invocation.proceed();
         if (afterHandlersChain != null && afterHandlersChain.size() > 0) {
            for (MybatisAfterHandler item : afterHandlersChain) {
               item.handleObject(result);
            }
         }
         return result;
      }
      if (preHandlersChain != null && preHandlersChain.size() > 0) {
         for (MybatisPreHandler item : preHandlersChain) {
            item.preHandle(targetExecutor, (MappedStatement) args[0], args[1], (RowBounds) args[2],
                  (ResultHandler<?>) args[3], (CacheKey) args[4], (BoundSql) args[5]);
         }
      }
      Object result = invocation.proceed();
      if (afterHandlersChain != null && afterHandlersChain.size() > 0) {
         for (MybatisAfterHandler item : afterHandlersChain) {
            result = item.handleObject(result);
         }
      }
      return result;
   }

}
