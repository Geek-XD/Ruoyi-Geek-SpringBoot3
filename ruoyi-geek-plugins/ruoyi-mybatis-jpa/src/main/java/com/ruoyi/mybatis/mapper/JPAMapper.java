package com.ruoyi.mybatis.mapper;

import java.util.List;

import org.apache.ibatis.annotations.DeleteProvider;
import org.apache.ibatis.annotations.InsertProvider;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.SelectProvider;
import org.apache.ibatis.annotations.UpdateProvider;

import com.ruoyi.common.core.domain.BaseEntity;
import com.ruoyi.mybatis.utils.SQLGenerator;

public interface JPAMapper<T extends BaseEntity> {
    @SelectProvider(value = SQLGenerator.class, method = SQLGenerator.Method.SELECT_BY_ID)
    public T selectById(T entity);

    @SelectProvider(value = SQLGenerator.class, method = SQLGenerator.Method.LIST)
    public List<T> selectList(T entity);

    @InsertProvider(value = SQLGenerator.class, method = SQLGenerator.Method.INSERT)
    @Options(useGeneratedKeys = true)
    public int insert(T entity);

    @UpdateProvider(value = SQLGenerator.class, method = SQLGenerator.Method.UPDATE)
    public int update(T entity);

    @DeleteProvider(value = SQLGenerator.class, method = SQLGenerator.Method.DELETE_BY_ID)
    public int deleteById(T entity);
}
