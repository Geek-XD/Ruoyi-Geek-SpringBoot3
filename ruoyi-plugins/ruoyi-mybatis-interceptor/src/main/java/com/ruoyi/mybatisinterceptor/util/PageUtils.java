package com.ruoyi.mybatisinterceptor.util;

import java.util.List;

import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.mybatisinterceptor.context.page.PageContextHolder;
import com.ruoyi.mybatisinterceptor.context.page.model.TableInfo;

public class PageUtils {
    public static <E> TableDataInfo toTableInfo(List<E> list) {
        if (list instanceof TableInfo) {
            TableInfo<E> tableInfo = (TableInfo<E>) list;
            return new TableDataInfo(list, tableInfo.getTotal().intValue());
        }
        return new TableDataInfo(list, -1);

    }

    public static void ruoyiStartPage() {
        PageContextHolder.startPage();
        PageContextHolder.setPageInfo();
    }

}
