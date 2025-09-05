package com.ruoyi.mybatisinterceptor.handler;

public interface MybatisAfterHandler {

   Object handleObject(Object object) throws Throwable;

   default boolean stop() {
      return false;
   }
}
