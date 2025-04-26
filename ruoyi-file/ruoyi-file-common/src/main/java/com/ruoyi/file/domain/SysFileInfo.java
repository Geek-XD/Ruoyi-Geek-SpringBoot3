package com.ruoyi.file.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 文件信息对象 sys_file_info
 * 
 * @author ruoyi
 * @date 2025-04-25
 */
@Schema(description = "文件信息对象")
public class SysFileInfo extends BaseEntity
{
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
    public void setFileId(Long fileId) 
    {
        this.fileId = fileId;
    }

    public Long getFileId() 
    {
        return fileId;
    }


    public void setFileName(String fileName) 
    {
        this.fileName = fileName;
    }

    public String getFileName() 
    {
        return fileName;
    }


    public void setFilePath(String filePath) 
    {
        this.filePath = filePath;
    }

    public String getFilePath() 
    {
        return filePath;
    }


    public void setStorageType(String storageType) 
    {
        this.storageType = storageType;
    }

    public String getStorageType() 
    {
        return storageType;
    }


    public void setFileType(String fileType) 
    {
        this.fileType = fileType;
    }

    public String getFileType() 
    {
        return fileType;
    }


    public void setFileSize(Long fileSize) 
    {
        this.fileSize = fileSize;
    }

    public Long getFileSize() 
    {
        return fileSize;
    }


    public void setMd5(String md5) 
    {
        this.md5 = md5;
    }

    public String getMd5() 
    {
        return md5;
    }


    public void setDelFlag(String delFlag) 
    {
        this.delFlag = delFlag;
    }

    public String getDelFlag() 
    {
        return delFlag;
    }



    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("fileId", getFileId())
            .append("fileName", getFileName())
            .append("filePath", getFilePath())
            .append("storageType", getStorageType())
            .append("fileType", getFileType())
            .append("fileSize", getFileSize())
            .append("md5", getMd5())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .append("delFlag", getDelFlag())
            .toString();
    }
}
