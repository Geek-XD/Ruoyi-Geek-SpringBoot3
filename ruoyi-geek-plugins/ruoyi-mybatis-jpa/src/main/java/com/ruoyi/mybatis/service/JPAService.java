package com.ruoyi.mybatis.service;

import java.util.List;

import com.ruoyi.common.core.domain.BaseEntity;

public interface JPAService<T extends BaseEntity> {

    public T get(T entity);

    public List<T> list(T entity);

    public int add(T entity);

    public int update(T entity);

    public int del(T entity);
}
