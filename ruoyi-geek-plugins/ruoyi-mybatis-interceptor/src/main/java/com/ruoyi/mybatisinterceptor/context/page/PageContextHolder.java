package com.ruoyi.mybatisinterceptor.context.page;

import com.alibaba.fastjson2.JSONObject;
import com.ruoyi.mybatisinterceptor.context.page.model.PageInfo;

public class PageContextHolder {
   private static final ThreadLocal<JSONObject> PAGE_CONTEXT_HOLDER = new ThreadLocal<>();

   private static final String PAGE_FLAG = "isPage";

   private static final String PAGE_INFO = "pageInfo";

   private static final String TOTAL = "total";
   private static final String SKIP_QUERY = "skipQuery";

   public static void startPage() {
      JSONObject jsonObject = new JSONObject();
      jsonObject.put(PAGE_FLAG, Boolean.TRUE);
      PAGE_CONTEXT_HOLDER.set(jsonObject);
   }

   public static void setPageInfo() {
      PAGE_CONTEXT_HOLDER.get().put(PAGE_INFO, PageInfo.defaultPageInfo());
   }

   public static PageInfo getPageInfo() {
      return (PageInfo) PAGE_CONTEXT_HOLDER.get().get(PAGE_INFO);
   }

   public static void clear() {
      PAGE_CONTEXT_HOLDER.remove();
   }

   public static boolean isPage() {
      return PAGE_CONTEXT_HOLDER.get() != null && PAGE_CONTEXT_HOLDER.get().getBooleanValue(PAGE_FLAG);
   }

   public static void setTotal(Long total) {
      PAGE_CONTEXT_HOLDER.get().put(TOTAL, total);
   }

   public static Long getTotal() {
      return PAGE_CONTEXT_HOLDER.get().getLong(TOTAL);
   }

   public static void setSkipQuery(boolean skip) {
      if (!isPage()) {
         startPage();
         setPageInfo();
      }
      PAGE_CONTEXT_HOLDER.get().put(SKIP_QUERY, skip);
   }

   public static boolean shouldSkipQuery() {
      return PAGE_CONTEXT_HOLDER.get() != null && PAGE_CONTEXT_HOLDER.get().getBooleanValue(SKIP_QUERY);
   }

   // === Facade methods for compatibility with PageHelper-like API ===

   /**
    * 兼容：startPage(pageNum,pageSize)
    */
   public static void startPage(Integer pageNum, Integer pageSize) {
      // 兼容旧签名，委托到原生 int 版本
      startPage(pageNum == null ? 1 : pageNum.intValue(), pageSize == null ? 10 : pageSize.intValue());
   }

   /**
    * 与 PageHelper 对齐的签名：startPage(int pageNum, int pageSize)
    */
   public static void startPage(int pageNum, int pageSize) {
      startPage();
      setPageInfo();
      PageInfo info = getPageInfo();
      if (info != null) {
         info.setPageNumber((long) pageNum);
         info.setPageSize((long) pageSize);
      }
   }

   /**
    * 兼容：startPage(pageNum,pageSize,orderBy)
    */
   public static void startPage(Integer pageNum, Integer pageSize, String orderBy) {
      // 兼容旧签名，委托到原生 int 版本
      startPage(pageNum == null ? 1 : pageNum.intValue(), pageSize == null ? 10 : pageSize.intValue(), orderBy);
   }

   /**
    * 与 PageHelper 对齐的签名：startPage(int pageNum, int pageSize, String orderBy)
    */
   public static void startPage(int pageNum, int pageSize, String orderBy) {
      startPage(pageNum, pageSize);
      PageInfo info = getPageInfo();
      if (info != null) {
         // 直接存入原始 orderBy 字符串；后续由拦截器中的 OrderByUtil 统一校验/构建
         info.setOrderByColumn(orderBy);
      }
   }

   /**
    * 兼容：orderBy("col asc,...")
    */
   public static void orderBy(String orderBy) {
      if (!isPage()) {
         startPage();
         setPageInfo();
      }
      PageInfo info = getPageInfo();
      if (info != null) {
         info.setOrderByColumn(orderBy);
      }
   }

   /**
    * 设置合理化参数
    */
   public static void setReasonable(Boolean reasonable) {
      if (!isPage()) {
         startPage();
         setPageInfo();
      }
      PageInfo info = getPageInfo();
      if (info != null) {
         info.setReasonable(reasonable);
      }
   }

   /**
    * 兼容：clearPage()
    */
   public static void clearPage() {
      clear();
   }
}
