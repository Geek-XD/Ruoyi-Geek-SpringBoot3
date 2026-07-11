package com.geek.system.mapper;

import com.geek.common.utils.StringUtils;
import com.geek.system.domain.SysFileInfo;
import com.mybatisflex.core.BaseMapper;
import com.mybatisflex.core.query.QueryWrapper;

/**
 * 文件Mapper接口
 * 
 * @author geek
 * @date 2025-04-25
 */
public interface SysFileInfoMapper extends BaseMapper<SysFileInfo> {

    default SysFileInfo selectReusableFileInfo(String bucketName,
            String storageType,
            String md5) {
        QueryWrapper queryWrapper = QueryWrapper.create()
                .where(SysFileInfo::getBucketName).eq(bucketName)
                .where(SysFileInfo::getStorageType).eq(storageType)
                .where(SysFileInfo::getMd5).eq(md5)
                .where(SysFileInfo::getDelFlag).eq(0)
                .orderBy(SysFileInfo::getFileId, false)
                .limit(1);

        return this.selectOneByQuery(queryWrapper);
    }

    default Long countActiveFileInfoByPhysicalIdentity(String bucketName,
            String storageType,
            String md5,
            String filePath) {
        QueryWrapper queryWrapper = QueryWrapper.create()
                .where(SysFileInfo::getBucketName).eq(bucketName)
                .where(SysFileInfo::getStorageType).eq(storageType)
                .where(SysFileInfo::getDelFlag).eq(0);

        if (StringUtils.isNotEmpty(md5)) {
            queryWrapper.and(SysFileInfo::getMd5).eq(md5)
                    .or(SysFileInfo::getFilePath).eq(filePath);
        } else {
            queryWrapper.where(SysFileInfo::getFilePath).eq(filePath);
        }

        return this.selectCountByQuery(queryWrapper);
    }

}
