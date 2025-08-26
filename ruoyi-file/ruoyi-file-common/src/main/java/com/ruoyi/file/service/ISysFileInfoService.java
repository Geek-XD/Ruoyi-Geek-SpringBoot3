package com.ruoyi.file.service;

import java.util.Date;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.ruoyi.common.utils.sign.Md5Utils;
import com.ruoyi.file.domain.SysFileInfo;

/**
 * 文件Service接口
 * 
 * @author ruoyi
 * @date 2025-04-25
 */
public interface ISysFileInfoService {
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
     * 新增文件
     * 
     * @param file
     * @return 结果
     */
    default public SysFileInfo buildSysFileInfo(MultipartFile file) {
        String fileType = null;
        if (file.getOriginalFilename() != null && file.getOriginalFilename().contains(".")) {
            fileType = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf('.') + 1);
        }
        SysFileInfo fileInfo = new SysFileInfo();
        String md5 = Md5Utils.getMd5(file);
        fileInfo.setFileName(file.getOriginalFilename());
        fileInfo.setFileType(fileType);
        fileInfo.setFileSize(file.getSize());
        fileInfo.setMd5(md5);
        fileInfo.setCreateTime(new Date());
        fileInfo.setUpdateTime(new Date());
        fileInfo.setDelFlag("0");
        return fileInfo;
    }

    /**
     * 修改文件
     * 
     * @param sysFileInfo 文件
     * @return 结果
     */
    public int updateSysFileInfo(SysFileInfo sysFileInfo);

    /**
     * 批量删除文件
     * 
     * @param fileIds 需要删除的文件主键集合
     * @return 结果
     */
    public int deleteSysFileInfoByFileIds(Long[] fileIds);

    /**
     * 删除文件信息
     * 
     * @param fileId 文件主键
     * @return 结果
     */
    public int deleteSysFileInfoByFileId(Long fileId);
}
