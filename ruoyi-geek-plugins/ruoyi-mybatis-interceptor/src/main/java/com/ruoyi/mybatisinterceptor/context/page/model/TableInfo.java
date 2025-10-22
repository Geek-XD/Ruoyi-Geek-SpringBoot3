package com.ruoyi.mybatisinterceptor.context.page.model;

import java.util.ArrayList;
import java.util.List;

public class TableInfo<E> extends ArrayList<E> {

   private Long total;

   public TableInfo() {
      super();
   }

   public TableInfo(List<? extends E> list) {
      super(list);
   }

   public Long getTotal() {
      return total;
   }

   public void setTotal(Long total) {
      this.total = total;
   }

}
