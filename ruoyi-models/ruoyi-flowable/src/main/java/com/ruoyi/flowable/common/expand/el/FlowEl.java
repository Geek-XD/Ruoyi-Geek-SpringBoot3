package com.ruoyi.flowable.common.expand.el;

import org.springframework.stereotype.Component;

import com.ruoyi.system.service.ISysDeptService;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;


/**
 * 扩展表达式
 *
 * @author Tony
 * @date 2023-03-04 12:10
 */
@Component
@Slf4j
public class FlowEl implements BaseEl {

    @Resource
    private ISysDeptService sysDeptService;

    public String findDeptLeader(String name){
        log.info("开始查询表达式变量值,getName");
        return name;
    }

    public String getName(String name){
        log.info("开始查询表达式变量值,getName");
        return name;
    }
}

