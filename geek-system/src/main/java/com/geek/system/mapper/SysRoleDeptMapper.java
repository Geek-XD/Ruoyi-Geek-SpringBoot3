package com.geek.system.mapper;

import java.util.List;

import com.geek.system.domain.SysRoleDept;
import com.mybatisflex.core.BaseMapper;

/**
 * 角色与部门关联表 数据层
 * 
 * @author geek
 */
public interface SysRoleDeptMapper extends BaseMapper<SysRoleDept> {
    /**
     * 通过角色ID删除角色和部门关联
     * 
     * @param roleId 角色ID
     * @return 结果
     */
    public int deleteRoleDeptByRoleId(Long roleId);


    /**
     * 批量新增角色部门信息
     * 
     * @param roleDeptList 角色部门列表
     * @return 结果
     */
    public int batchRoleDept(List<SysRoleDept> roleDeptList);
}
