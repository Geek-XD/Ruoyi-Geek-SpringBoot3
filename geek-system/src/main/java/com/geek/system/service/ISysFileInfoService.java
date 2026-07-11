package com.geek.system.service;

import java.util.List;
import java.util.Date;

import org.springframework.web.multipart.MultipartFile;

import com.geek.common.utils.sign.Md5Utils;
import com.geek.system.domain.SysFileInfo;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.service.IService;

import jakarta.servlet.http.HttpServletResponse;

/**
 * 文件Service接口
 * 
 * @author geek
 * @date 2025-04-25
 */
public interface ISysFileInfoService extends IService<SysFileInfo> {

    /**
     * 新增文件
     * 
     * @param file
     * @return 结果
     */
    default public SysFileInfo buildSysFileInfo(MultipartFile file) {
        String md5 = Md5Utils.getMd5(file);
        return buildSysFileInfo(file.getOriginalFilename(), file.getSize(), md5);
    }

    default public SysFileInfo buildSysFileInfo(String fileName, Long fileSize, String md5) {
        String fileType = null;
        if (fileName != null && fileName.contains(".")) {
            fileType = fileName.substring(fileName.lastIndexOf('.') + 1);
        }
        SysFileInfo fileInfo = new SysFileInfo();
        fileInfo.setFileName(fileName);
        fileInfo.setFileType(fileType);
        fileInfo.setFileSize(fileSize);
        fileInfo.setMd5(md5);
        fileInfo.setCreateTime(new Date());
        fileInfo.setUpdateTime(new Date());
        fileInfo.setDelFlag(0);
        return fileInfo;
    }

    SysFileInfo prepareReferenceFileInfo(SysFileInfo sysFileInfo);

    SysFileInfo findReusableFileInfo(String bucketName, String storageType, String md5);

    boolean shouldDeletePhysicalFile(SysFileInfo fileInfo);

    boolean removeFileInfos(List<Long> fileIds);

    Page<SysFileInfo> page(SysFileInfo sysFileInfo, int pageNum, int pageSize);

    void export(SysFileInfo sysFileInfo, HttpServletResponse response);
}
