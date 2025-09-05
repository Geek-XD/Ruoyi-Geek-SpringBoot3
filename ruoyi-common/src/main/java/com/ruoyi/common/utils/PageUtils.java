package com.ruoyi.common.utils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.ruoyi.common.core.page.PageDomain;
import com.ruoyi.common.core.page.TableSupport;
import com.ruoyi.common.utils.sql.SqlUtil;

import jakarta.annotation.PostConstruct;

/**
 * 分页工具类
 * 
 * @author ruoyi
 */
@Component
public class PageUtils {
    // 开关：通过系统属性 ruoyi.paging 控制，custom=自研，helper=PageHelper。默认 custom
    private static boolean USE_CUSTOM = false;

    // Provider 选择：根据 USE_CUSTOM 决定使用 PageContextHolder 或 PageHelper
    private static Method START_2;
    private static Method START_3;
    private static Method ORDER_BY;
    private static Method CLEAR;
    private static Method SET_REASONABLE;
    private static Method PI_GET_TOTAL;
    private static Constructor<?> PI_CTOR;

    @Value("${pageutils.type:default}")
    private String pageUtilsType;

    @PostConstruct
    private void init() throws Exception {
        Class<?> PROVIDER_CLS = null;
        if ("custom".equals(pageUtilsType)) {
            PROVIDER_CLS = Class.forName("com.ruoyi.mybatisinterceptor.context.page.PageContextHolder");
            SET_REASONABLE = PROVIDER_CLS.getMethod("setReasonable", Boolean.class);
            Class<?> PageInfoClz = Class.forName("com.ruoyi.mybatisinterceptor.context.page.model.TableInfo");
            PI_GET_TOTAL = PageInfoClz.getMethod("getTotal");
            USE_CUSTOM = true;
        } else {
            PROVIDER_CLS = Class.forName("com.github.pagehelper.PageHelper");
            Class<?> PageClz = Class.forName("com.github.pagehelper.Page");
            SET_REASONABLE = PageClz.getMethod("setReasonable", Boolean.class);
            Class<?> PageInfoClz = Class.forName("com.github.pagehelper.PageInfo");
            PI_GET_TOTAL = PageInfoClz.getMethod("getTotal");
            PI_CTOR = PageInfoClz.getConstructor(java.util.List.class);
            USE_CUSTOM = false;
        }
        START_2 = PROVIDER_CLS.getMethod("startPage", int.class, int.class);
        START_3 = PROVIDER_CLS.getMethod("startPage", int.class, int.class, String.class);
        ORDER_BY = PROVIDER_CLS.getMethod("orderBy", String.class);
        CLEAR = PROVIDER_CLS.getMethod("clearPage");
    }

    /**
     * 设置请求分页数据
     */
    public static void startPage() {
        PageDomain pageDomain = TableSupport.buildPageRequest();
        Integer pageNum = pageDomain.getPageNum();
        Integer pageSize = pageDomain.getPageSize();
        String orderBy = SqlUtil.escapeOrderBySql(pageDomain.getOrderBy());
        Boolean reasonable = pageDomain.getReasonable();
        Object ret = invoke(START_3, null, pageNum, pageSize, orderBy);
        invoke(SET_REASONABLE, USE_CUSTOM ? null : ret, reasonable);
    }

    /**
     * 清理分页的线程变量
     */
    public static void clearPage() {
        invoke(CLEAR, null);
    }

    /**
     * 显式页码与页大小
     */
    public static void startPage(Integer pageNum, Integer pageSize) {
        invoke(START_2, null, pageNum, pageSize);
    }

    /**
     * 设置排序
     */
    public static void orderBy(String orderBy) {
        String safeOrderBy = SqlUtil.escapeOrderBySql(orderBy);
        invoke(ORDER_BY, null, safeOrderBy);
    }

    public static long getTotal(java.util.List<?> list) {
        if (list == null)
            return 0L;
        try {
            if (USE_CUSTOM) {
                Object total = PI_GET_TOTAL.invoke(list);
                return ((Number) total).longValue();
            } else {
                Object pageInfo = PI_CTOR.newInstance(list);
                Object total = PI_GET_TOTAL.invoke(pageInfo);
                return ((Number) total).longValue();
            }
        } catch (Throwable e) {
            throw new IllegalStateException("Failed to obtain total count from PageInfo", e);
        }
    }

    private static Object invoke(Method m, Object target, Object... args) {
        try {
            return m == null ? null : m.invoke(target, args);
        } catch (Throwable e) {
            return null;
        }
    }

}
