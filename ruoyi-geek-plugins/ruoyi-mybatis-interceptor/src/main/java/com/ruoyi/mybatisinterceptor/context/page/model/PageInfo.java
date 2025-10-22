package com.ruoyi.mybatisinterceptor.context.page.model;

import com.ruoyi.common.core.text.Convert;
import com.ruoyi.common.utils.ServletUtils;

public class PageInfo {

   private Long pageNumber;

   private Long pageSize;

   /**
    * 排序字段（原始请求值，进入 SQL 前需转义/校验）
    */
   private String orderByColumn;

   /**
    * 排序方向 asc/desc
    */
   private String isAsc;

   /**
    * 是否进行 count 查询
    */
   private Boolean searchCount;

   /**
    * 合理化分页（页码<1按1处理，pageSize<=0 按默认，超过最大按最大）
    */
   private Boolean reasonable;

   /**
    * pageSize 的最大上限（防止恶意大页），默认 1000
    */
   public static final long DEFAULT_MAX_PAGE_SIZE = 1000L;

   /**
    * 当前记录起始索引
    */
   public static final String PAGE_NUM = "pageNum";

   /**
    * 每页显示记录数
    */
   public static final String PAGE_SIZE = "pageSize";

   /**
    * 排序列
    */
   public static final String ORDER_BY_COLUMN = "orderByColumn";

   /**
    * 排序的方向 "desc" 或者 "asc".
    */
   public static final String IS_ASC = "isAsc";

   /**
    * 分页参数合理化
    */
   public static final String REASONABLE = "reasonable";

   /**
    * 是否进行 count 查询
    */
   public static final String SEARCH_COUNT = "searchCount";

   public Long getPageNumber() {
      return pageNumber;
   }

   public void setPageNumber(Long pageNumber) {
      this.pageNumber = pageNumber;
   }

   public Long getPageSize() {
      return pageSize;
   }

   public void setPageSize(Long pageSize) {
      this.pageSize = pageSize;
   }

   public static PageInfo defaultPageInfo() {
      PageInfo pageInfo = new PageInfo();
      long pn = Convert.toInt(ServletUtils.getParameter(PAGE_NUM), 1);
      long ps = Convert.toInt(ServletUtils.getParameter(PAGE_SIZE), 10);
      // 合理化
      boolean reasonable = Boolean.parseBoolean(ServletUtils.getParameter(REASONABLE));
      if (reasonable && pn < 1) pn = 1;
      if (ps <= 0) ps = 10;
      if (ps > DEFAULT_MAX_PAGE_SIZE) ps = DEFAULT_MAX_PAGE_SIZE;

      pageInfo.setPageNumber(pn);
      pageInfo.setPageSize(ps);
      pageInfo.setReasonable(reasonable);

      String ob = ServletUtils.getParameter(ORDER_BY_COLUMN);
      if (ob == null || ob.isEmpty()) {
         // 兼容 RuoYi 传统参数：orderBy（可含多列及各自方向）
         ob = ServletUtils.getParameter("orderBy");
      }
      pageInfo.setOrderByColumn(ob);
      String asc = ServletUtils.getParameter(IS_ASC);
      pageInfo.setIsAsc(asc);
      String sc = ServletUtils.getParameter(SEARCH_COUNT);
      pageInfo.setSearchCount(sc == null ? Boolean.TRUE : Boolean.parseBoolean(sc));
      return pageInfo;
   }

   public Long getOffset() {
      long pn = pageNumber == null ? 1L : pageNumber.longValue();
      long ps = pageSize == null ? 10L : pageSize.longValue();
      if (pn < 1L) pn = 1L;
      if (ps <= 0L) ps = 10L;
      return (pn - 1L) * ps;
   }

   /**
    * 兼容旧方法拼写
    */
   @Deprecated
   public Long getOffeset() { // 保持兼容
      return getOffset();
   }

   public String getOrderByColumn() {
      return orderByColumn;
   }

   public void setOrderByColumn(String orderByColumn) {
      this.orderByColumn = orderByColumn;
   }

   public String getIsAsc() {
      return isAsc;
   }

   public void setIsAsc(String isAsc) {
      this.isAsc = isAsc;
   }

   public Boolean getSearchCount() {
      return searchCount;
   }

   public void setSearchCount(Boolean searchCount) {
      this.searchCount = searchCount;
   }

   public Boolean getReasonable() {
      return reasonable;
   }

   public void setReasonable(Boolean reasonable) {
      this.reasonable = reasonable;
   }
}
