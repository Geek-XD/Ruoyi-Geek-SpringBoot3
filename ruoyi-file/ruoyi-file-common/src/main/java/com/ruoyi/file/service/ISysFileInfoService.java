package com.ruoyi.file.service;

import java.util.List;
import com.ruoyi.file.domain.SysFileInfo;

/**
 * 文件信息Service接口
 * 
 * @author ruoyi
 * @date 2025-04-25
 */
public interface ISysFileInfoService 
{
    /**
     * 查询文件信息
     * 
     * @param fileId 文件信息主键
     * @return 文件信息
     */
    public SysFileInfo selectSysFileInfoByFileId(Long fileId);

    /**
     * 查询文件信息列表
     * 
     * @param sysFileInfo 文件信息
     * @return 文件信息集合
     */
    public List<SysFileInfo> selectSysFileInfoList(SysFileInfo sysFileInfo);

    /**
     * 新增文件信息
     * 
     * @param sysFileInfo 文件信息
     * @return 结果
     */
    public int insertSysFileInfo(SysFileInfo sysFileInfo);

    /**
     * 修改文件信息
     * 
     * @param sysFileInfo 文件信息
     * @return 结果
     */
    public int updateSysFileInfo(SysFileInfo sysFileInfo);

    /**
     * 批量删除文件信息
     * 
     * @param fileIds 需要删除的文件信息主键集合
     * @return 结果
     */
    public int deleteSysFileInfoByFileIds(Long[] fileIds);

    /**
     * 删除文件信息信息
     * 
     * @param fileId 文件信息主键
     * @return 结果
     */
    public int deleteSysFileInfoByFileId(Long fileId);
}
