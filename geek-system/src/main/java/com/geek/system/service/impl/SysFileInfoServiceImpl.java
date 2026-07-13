package com.geek.system.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.geek.common.constant.CacheConstants;
import com.geek.common.core.storage.StorageBucketKey;
import com.geek.common.utils.CacheUtils;
import com.geek.common.utils.poi.ExcelUtil;
import com.geek.common.utils.sign.Md5Utils;
import com.geek.system.domain.SysFileInfo;
import com.geek.system.mapper.SysFileInfoMapper;
import com.geek.system.service.ISysFileInfoService;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryChain;
import com.mybatisflex.spring.service.impl.ServiceImpl;

import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

/**
 * 文件Service业务层处理
 * 
 * @author geek
 * @date 2025-04-25
 */
@Slf4j
@Service
public class SysFileInfoServiceImpl extends ServiceImpl<SysFileInfoMapper, SysFileInfo> implements ISysFileInfoService {

    /**
     * 查询文件列表
     * 
     * @param sysFileInfo 文件
     * @return 文件
     */
    private QueryChain<SysFileInfo> selectSysFileInfoList(SysFileInfo sysFileInfo) {
        return this.queryChain()
                .from(SysFileInfo.class)
                .like(SysFileInfo::getFileName, sysFileInfo.getFileName())
                .eq(SysFileInfo::getFilePath, sysFileInfo.getFilePath())
                .eq(SysFileInfo::getStorageName, sysFileInfo.getStorageName())
                .eq(SysFileInfo::getFileType, sysFileInfo.getFileType())
                .eq(SysFileInfo::getFileSize, sysFileInfo.getFileSize())
                .eq(SysFileInfo::getMd5, sysFileInfo.getMd5());
    }

    @Override
    public Page<SysFileInfo> page(SysFileInfo sysFileInfo, int pageNum, int pageSize) {
        return this.selectSysFileInfoList(sysFileInfo).page(Page.of(pageNum, pageSize));
    }

    @Override
    public void export(SysFileInfo sysFileInfo, HttpServletResponse response) {
        List<SysFileInfo> list = this.selectSysFileInfoList(sysFileInfo).list();
        ExcelUtil<SysFileInfo> util = new ExcelUtil<>(SysFileInfo.class);
        util.exportExcel(response, list, "文件数据");
    }

    /**
     * 新增文件
     * 
     * @param file
     * @return 结果
     */
    @Override
    public SysFileInfo buildSysFileInfo(MultipartFile file, String bucketName) {
        if (bucketName == null) {
            bucketName = StorageBucketKey.get();
        }
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
        fileInfo.setStorageName(bucketName);
        return fileInfo;
    }

    @Override
    public void enableFastUpload(SysFileInfo file, String bucketName) {
        if (file.getFilePath() == null) {
            throw new IllegalArgumentException("文件路径不能为空");
        }
        if (file.getMd5() == null) {
            throw new IllegalArgumentException("文件MD5不能为空");
        }
        file.setFileId(null);
        CacheUtils.put(CacheConstants.FILE_INFO, bucketName + file.getMd5(), file);
    }

    @Override
    public SysFileInfo canFastUpload(MultipartFile file, String bucketName) {
        String md5 = Md5Utils.getMd5(file);
        if (bucketName == null) {
            bucketName = StorageBucketKey.get();
        }
        SysFileInfo sysFileInfo = CacheUtils.get(CacheConstants.FILE_INFO, bucketName + md5, SysFileInfo.class);
        if (sysFileInfo == null) {
            sysFileInfo = this.queryChain()
                    .from(SysFileInfo.class)
                    .eq(SysFileInfo::getMd5, md5)
                    .eq(SysFileInfo::getStorageName, bucketName)
                    .one();
            if (sysFileInfo != null) {
                sysFileInfo.setFileId(null);
                CacheUtils.put(CacheConstants.FILE_INFO, bucketName + md5, sysFileInfo);
            }
        }
        return sysFileInfo;
    }

    @Override
    public void clearFastUploadCache(String md5, String bucketName) {
        CacheUtils.remove(CacheConstants.FILE_INFO, bucketName + md5);
    }
}
