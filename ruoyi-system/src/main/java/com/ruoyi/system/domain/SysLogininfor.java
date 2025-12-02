package com.ruoyi.system.domain;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.Table;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.annotation.Excel.ColumnType;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 系统访问记录表 sys_logininfor
 * 
 * @author ruoyi
 */
@Schema(title = "系统访问记录表")
@Table("sys_logininfor")
@Data
public class SysLogininfor {

    /** ID */
    @Schema(title = "序号")
    @Excel(name = "序号", cellType = ColumnType.NUMERIC)
    @Id
    private Long infoId;

    /** 用户账号 */
    @Schema(title = "用户账号")
    @Excel(name = "用户账号")
    private String userName;

    /** 登录状态 0成功 1失败 */
    @Schema(title = "登录状态")
    @Excel(name = "登录状态", readConverterExp = "0=成功,1=失败")
    private String status;

    /** 登录IP地址 */
    @Schema(title = "登录地址")
    @Excel(name = "登录地址")
    private String ipaddr;

    /** 登录地点 */
    @Schema(title = "登录地点")
    @Excel(name = "登录地点")
    private String loginLocation;

    /** 浏览器类型 */
    @Schema(title = "浏览器")
    @Excel(name = "浏览器")
    private String browser;

    /** 操作系统 */
    @Schema(title = "操作系统")
    @Excel(name = "操作系统")
    private String os;

    /** 提示消息 */
    @Schema(title = "提示消息")
    @Excel(name = "提示消息")
    private String msg;

    /** 访问时间 */
    @Schema(title = "访问时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "访问时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date loginTime;
}
