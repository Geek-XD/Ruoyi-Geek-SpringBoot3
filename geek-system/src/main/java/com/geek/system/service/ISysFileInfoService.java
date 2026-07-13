package com.geek.system.service;

import org.springframework.web.multipart.MultipartFile;

import com.geek.common.core.storage.StorageBucketKey;
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

    Page<SysFileInfo> page(SysFileInfo sysFileInfo, int pageNum, int pageSize);

    void export(SysFileInfo sysFileInfo, HttpServletResponse response);

    SysFileInfo buildSysFileInfo(MultipartFile file, String bucketName);

    default SysFileInfo buildSysFileInfo(MultipartFile file) {
        return buildSysFileInfo(file, StorageBucketKey.get());
    }

    void enableFastUpload(SysFileInfo file, String bucketName);

    default void enableFastUpload(SysFileInfo file) {
        enableFastUpload(file, StorageBucketKey.get());
    }

    SysFileInfo canFastUpload(MultipartFile file, String bucketName);

    default SysFileInfo canFastUpload(MultipartFile file) {
        return canFastUpload(file, StorageBucketKey.get());
    }

    void clearFastUploadCache(String md5, String bucketName);

    default void clearFastUploadCache(String md5) {
        clearFastUploadCache(md5, StorageBucketKey.get());
    }
}
