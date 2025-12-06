package com.geek.system.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.geek.common.core.domain.entity.SysDept;
import com.mybatisflex.core.BaseMapper;

/**
 * 部门管理 数据层
 * 
 * @author geek
 */
public interface SysDeptMapper extends BaseMapper<SysDept> {

    /**
     * 根据ID查询所有子部门
     * 
     * @param deptId 部门ID
     * @return 部门列表
     */
    public List<SysDept> selectChildrenDeptById(Long deptId);

    /**
     * 根据ID查询所有子部门（正常状态）
     * 
     * @param deptId 部门ID
     * @return 子部门数
     */
    public int selectNormalChildrenDeptById(Long deptId);

    /**
     * 修改所在部门正常状态
     * 
     * @param deptIds 部门ID组
     */
    public void updateDeptStatusNormal(Long[] deptIds);

    /**
     * 修改子元素关系
     * 
     * @param depts 子元素
     * @return 结果
     */
    public int updateDeptChildren(@Param("depts") List<SysDept> depts);
}
