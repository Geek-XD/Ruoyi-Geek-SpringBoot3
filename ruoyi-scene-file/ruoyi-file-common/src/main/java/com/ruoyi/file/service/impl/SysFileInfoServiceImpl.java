package com.ruoyi.file.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.file.domain.SysFileInfo;
import com.ruoyi.file.mapper.SysFileInfoMapper;
import com.ruoyi.file.service.ISysFileInfoService;

/**
 * 文件Service业务层处理
 * 
 * @author ruoyi
 * @date 2025-04-25
 */
@Service
public class SysFileInfoServiceImpl implements ISysFileInfoService 
{
    @Autowired
    private SysFileInfoMapper sysFileInfoMapper;

    /**
     * 查询文件
     * 
     * @param fileId 文件主键
     * @return 文件
     */
    @Override
    public SysFileInfo selectSysFileInfoByFileId(Long fileId)
    {
        return sysFileInfoMapper.selectSysFileInfoByFileId(fileId);
    }

    /**
     * 查询文件列表
     * 
     * @param sysFileInfo 文件
     * @return 文件
     */
    @Override
    public List<SysFileInfo> selectSysFileInfoList(SysFileInfo sysFileInfo)
    {
        return sysFileInfoMapper.selectSysFileInfoList(sysFileInfo);
    }

    /**
     * 新增文件
     * 
     * @param sysFileInfo 文件
     * @return 结果
     */
    @Override
    public int insertSysFileInfo(SysFileInfo sysFileInfo)
    {
        sysFileInfo.setCreateTime(DateUtils.getNowDate());
        return sysFileInfoMapper.insertSysFileInfo(sysFileInfo);
    }

    /**
     * 修改文件
     * 
     * @param sysFileInfo 文件
     * @return 结果
     */
    @Override
    public int updateSysFileInfo(SysFileInfo sysFileInfo)
    {
        sysFileInfo.setUpdateTime(DateUtils.getNowDate());
        return sysFileInfoMapper.updateSysFileInfo(sysFileInfo);
    }

    /**
     * 批量删除文件
     * 
     * @param fileIds 需要删除的文件主键
     * @return 结果
     */
    @Override
    public int deleteSysFileInfoByFileIds(Long[] fileIds)
    {
        return sysFileInfoMapper.deleteSysFileInfoByFileIds(fileIds);
    }

    /**
     * 删除文件信息
     * 
     * @param fileId 文件主键
     * @return 结果
     */
    @Override
    public int deleteSysFileInfoByFileId(Long fileId)
    {
        return sysFileInfoMapper.deleteSysFileInfoByFileId(fileId);
    }
}
