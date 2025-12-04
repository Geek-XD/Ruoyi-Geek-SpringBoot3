package com.ruoyi.system.domain;

import com.mybatisflex.annotation.Table;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 用户和角色关联 sys_user_role
 * 
 * @author ruoyi
 */
@Schema(title = "用户和角色关联")
@Data
@Table("sys_user_role")
public class SysUserRole {
    /** 用户ID */
    @Schema(title = "用户ID")
    private Long userId;

    /** 角色ID */
    @Schema(title = "角色ID")
    private Long roleId;
}
