package com.ruoyi.system.mapper;

import java.util.List;

import com.ruoyi.system.domain.SysFileInfo;

/**
 * 文件Mapper接口
 * 
 * @author ruoyi
 * @date 2025-04-25
 */
public interface SysFileInfoMapper 
{
    /**
     * 查询文件
     * 
     * @param fileId 文件主键
     * @return 文件
     */
    public SysFileInfo selectSysFileInfoByFileId(Long fileId);

    /**
     * 查询文件列表
     * 
     * @param sysFileInfo 文件
     * @return 文件集合
     */
    public List<SysFileInfo> selectSysFileInfoList(SysFileInfo sysFileInfo);

    /**
     * 新增文件
     * 
     * @param sysFileInfo 文件
     * @return 结果
     */
    public int insertSysFileInfo(SysFileInfo sysFileInfo);

    /**
     * 修改文件
     * 
     * @param sysFileInfo 文件
     * @return 结果
     */
    public int updateSysFileInfo(SysFileInfo sysFileInfo);

    /**
     * 删除文件
     * 
     * @param fileId 文件主键
     * @return 结果
     */
    public int deleteSysFileInfoByFileId(Long fileId);

    /**
     * 批量删除文件
     * 
     * @param fileIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteSysFileInfoByFileIds(Long[] fileIds);
}
