package com.ruoyi.mybatisinterceptor.handler.page;

import org.springframework.stereotype.Component;

import com.ruoyi.mybatisinterceptor.annotation.MybatisHandlerOrder;
import com.ruoyi.mybatisinterceptor.context.page.PageContextHolder;
import com.ruoyi.mybatisinterceptor.handler.MybatisFinishHandler;

@Component
@MybatisHandlerOrder(10)
public class PageFinishHandler implements MybatisFinishHandler {
    @Override
    public void finish() {
        PageContextHolder.clear();
    }

}
