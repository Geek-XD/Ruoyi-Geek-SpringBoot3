package com.geek.system.mapper;

import com.geek.common.core.domain.entity.SysDept;
import com.geek.common.core.domain.entity.SysRole;
import com.geek.common.core.domain.entity.SysUser;
import com.geek.system.domain.SysUserRole;
import com.mybatisflex.core.BaseMapper;
import com.mybatisflex.core.query.QueryChain;

/**
 * 角色表 数据层
 * 
 * @author geek
 */
public interface SysRoleMapper extends BaseMapper<SysRole> {

    default public QueryChain<SysRole> baseQueryChain() {
        return QueryChain.of(SysRole.class)
                .leftJoin(SysUserRole.class)
                .on(SysUserRole::getRoleId, SysRole::getRoleId)
                .leftJoin(SysUser.class)
                .on(SysUser::getUserId, SysUserRole::getUserId)
                .leftJoin(SysDept.class)
                .on(SysUser::getDeptId, SysDept::getDeptId);
    }
}
