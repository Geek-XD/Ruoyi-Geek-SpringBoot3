package com.ruoyi.mybatisinterceptor.context.sqlContext;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.ruoyi.mybatisinterceptor.enums.ContextKey;

public class SqlContextHolder {


    private static final ThreadLocal<Map<ContextKey, Map<String, Map<Class<?>, Object>>>> SQL_CONTEXT_HOLDER = new ThreadLocal<>();

    public static void startContext() {
        if (SQL_CONTEXT_HOLDER.get() == null) {
            SQL_CONTEXT_HOLDER.set(new ConcurrentHashMap<>());
        }
    }


    public static <T> void addData(ContextKey key, String subKey, T value) {
        if (value == null) return;

        Map<ContextKey, Map<String, Map<Class<?>, Object>>> context = SQL_CONTEXT_HOLDER.get();
        if (context == null) {
            throw new IllegalStateException("SQL context 未初始化");
        }
        Map<String, Map<Class<?>, Object>> subContext = context.computeIfAbsent(key, k -> new ConcurrentHashMap<>());
        Map<Class<?>, Object> typeMap = subContext.computeIfAbsent(subKey, sk -> new ConcurrentHashMap<>());
        typeMap.put(value.getClass(), value);
    }
    public static <T> T getData(ContextKey key, String subKey, Class<T> clazz) {
        Map<ContextKey, Map<String, Map<Class<?>, Object>>> context = SQL_CONTEXT_HOLDER.get();
        if (context == null || !context.containsKey(key)) {
            return null;
        }
        Map<String, Map<Class<?>, Object>> subContext = context.get(key);
        if (!subContext.containsKey(subKey)) {
            return null;
        }
        Map<Class<?>, Object> typeMap = subContext.get(subKey);
        Object value = typeMap.get(clazz);
        return clazz.isInstance(value) ? clazz.cast(value) : null;
    }
    public static void clearContext() {
        SQL_CONTEXT_HOLDER.remove();
    }
}