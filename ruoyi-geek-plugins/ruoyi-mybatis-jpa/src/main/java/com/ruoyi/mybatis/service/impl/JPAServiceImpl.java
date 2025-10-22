package com.ruoyi.mybatis.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.ruoyi.common.core.domain.BaseEntity;
import com.ruoyi.mybatis.mapper.JPAMapper;
import com.ruoyi.mybatis.service.JPAService;

public class JPAServiceImpl<T extends BaseEntity> implements JPAService<T> {

    @Autowired
    private JPAMapper<T> mapper;

    @Override
    public T get(T entity) {
        return mapper.selectById(entity);
    };

    @Override
    public List<T> list(T entity) {
        return mapper.selectList(entity);
    }

    @Override
    public int add(T entity) {
        return mapper.insert(entity);
    }

    @Override
    public int update(T entity) {
        return mapper.update(entity);
    }

    @Override
    public int del(T entity) {
        return mapper.deleteById(entity);
    }
}
