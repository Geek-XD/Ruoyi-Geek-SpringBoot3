package com.ruoyi.file.domain;

import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 文件信息对象 sys_file_info
 * 
 * @author ruoyi
 * @date 2025-04-25
 */
@Schema(description = "文件信息对象")
@Data
@EqualsAndHashCode(callSuper = true)
public class SysFileInfo extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 文件主键 */
    @Schema(title = "文件主键")
    private Long fileId;

    /** 原始文件名 */
    @Schema(title = "原始文件名")
    @Excel(name = "原始文件名")
    private String fileName;

    /** 统一逻辑路径（/开头） */
    @Schema(title = "统一逻辑路径（/开头）")
    @Excel(name = "统一逻辑路径", readConverterExp = "/=开头")
    private String filePath;

    /** 存储类型（local/minio/oss） */
    @Schema(title = "存储类型（local/minio/oss）")
    @Excel(name = "存储类型", readConverterExp = "l=ocal/minio/oss")
    private String storageType;

    /** 文件类型/后缀 */
    @Schema(title = "文件类型/后缀")
    @Excel(name = "文件类型/后缀")
    private String fileType;

    /** 文件大小（字节） */
    @Schema(title = "文件大小（字节）")
    @Excel(name = "文件大小", readConverterExp = "字=节")
    private Long fileSize;

    /** 文件MD5 */
    @Schema(title = "文件MD5")
    @Excel(name = "文件MD5")
    private String md5;

    /** 删除标志（0代表存在 2代表删除） */
    @Schema(title = "删除标志（0代表存在 2代表删除）")
    private String delFlag;

}
