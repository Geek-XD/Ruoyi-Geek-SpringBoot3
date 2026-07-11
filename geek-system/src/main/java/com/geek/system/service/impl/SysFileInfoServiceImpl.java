package com.geek.system.service.impl;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.geek.common.exception.ServiceException;
import com.geek.common.utils.StringUtils;
import com.geek.common.utils.poi.ExcelUtil;
import com.geek.system.domain.SysFileInfo;
import com.geek.system.mapper.SysFileInfoMapper;
import com.geek.system.service.ISysFileInfoService;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryChain;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SysFileInfoServiceImpl extends ServiceImpl<SysFileInfoMapper, SysFileInfo> implements ISysFileInfoService {

    private final SysFileInfoMapper fileInfoMapper;

    private QueryChain<SysFileInfo> selectSysFileInfoList(SysFileInfo sysFileInfo) {
        QueryChain<SysFileInfo> queryChain = this.queryChain()
                .from(SysFileInfo.class)
                .eq(SysFileInfo::getFileId, sysFileInfo.getFileId())
                .like(SysFileInfo::getFileName, sysFileInfo.getFileName())
                .eq(SysFileInfo::getFilePath, sysFileInfo.getFilePath())
                .eq(SysFileInfo::getStorageType, sysFileInfo.getStorageType())
                .eq(SysFileInfo::getBucketName, sysFileInfo.getBucketName())
                .eq(SysFileInfo::getFileType, sysFileInfo.getFileType())
                .eq(SysFileInfo::getFileSize, sysFileInfo.getFileSize())
                .eq(SysFileInfo::getMd5, sysFileInfo.getMd5())
                .eq(SysFileInfo::getReferenceFileId, sysFileInfo.getReferenceFileId())
                .eq(SysFileInfo::getUseType, sysFileInfo.getUseType());
        if (Boolean.TRUE.equals(sysFileInfo.getReferenceFlag())) {
            queryChain.isNotNull(SysFileInfo::getReferenceFileId);
        } else if (Boolean.FALSE.equals(sysFileInfo.getReferenceFlag())) {
            queryChain.isNull(SysFileInfo::getReferenceFileId);
        }
        return queryChain.orderBy(SysFileInfo::getFileId, false);
    }

    @Override
    public SysFileInfo prepareReferenceFileInfo(SysFileInfo sysFileInfo) {
        if (sysFileInfo == null || sysFileInfo.getReferenceFileId() == null) {
            return sysFileInfo;
        }
        SysFileInfo referenceSource = this.getById(sysFileInfo.getReferenceFileId());
        if (referenceSource == null) {
            throw new ServiceException("引用的文件信息不存在");
        }
        Long rootFileId = referenceSource.getReferenceFileId() != null
                ? referenceSource.getReferenceFileId()
                : referenceSource.getFileId();
        SysFileInfo rootFileInfo = this.getById(rootFileId);
        if (rootFileInfo == null) {
            throw new ServiceException("引用的根文件信息不存在");
        }
        sysFileInfo.setReferenceFileId(rootFileInfo.getFileId());
        sysFileInfo.setBucketName(rootFileInfo.getBucketName());
        sysFileInfo.setStorageType(rootFileInfo.getStorageType());
        sysFileInfo.setFilePath(rootFileInfo.getFilePath());
        sysFileInfo.setFileType(rootFileInfo.getFileType());
        sysFileInfo.setFileSize(rootFileInfo.getFileSize());
        sysFileInfo.setMd5(rootFileInfo.getMd5());
        if (StringUtils.isEmpty(sysFileInfo.getFileName())) {
            sysFileInfo.setFileName(rootFileInfo.getFileName());
        }
        return sysFileInfo;
    }

    @Override
    public SysFileInfo findReusableFileInfo(String bucketName, String storageType, String md5) {
        if (StringUtils.isEmpty(bucketName) || StringUtils.isEmpty(storageType) || StringUtils.isEmpty(md5)) {
            return null;
        }
        return fileInfoMapper.selectReusableFileInfo(bucketName, storageType, md5);
    }

    @Override
    public boolean shouldDeletePhysicalFile(SysFileInfo fileInfo) {
        if (fileInfo == null) {
            return false;
        }
        Long count = fileInfoMapper.countActiveFileInfoByPhysicalIdentity(
                fileInfo.getBucketName(),
                fileInfo.getStorageType(),
                fileInfo.getMd5(),
                fileInfo.getFilePath());
        return count == null || count == 0L;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean removeFileInfos(List<Long> fileIds) {
        List<SysFileInfo> deletingRows = this.list(QueryWrapper.create().in(SysFileInfo::getFileId, fileIds));
        if (deletingRows.isEmpty()) {
            return true;
        }
        for (SysFileInfo row : deletingRows) {
            if (this.queryChain()
                    .from(SysFileInfo.class)
                    .eq(SysFileInfo::getReferenceFileId, row.getFileId())
                    .exists()) {
                throw new ServiceException("当前文件仍存在引用，无法删除");
            }
        }
        return this.removeByIds(fileIds);
    }

    @Override
    public Page<SysFileInfo> page(SysFileInfo sysFileInfo, int pageNum, int pageSize) {
        Page<SysFileInfo> page = this.selectSysFileInfoList(sysFileInfo).page(Page.of(pageNum, pageSize));
        enrichRecords(page.getRecords());
        return page;
    }

    @Override
    public void export(SysFileInfo sysFileInfo, HttpServletResponse response) {
        List<SysFileInfo> list = this.selectSysFileInfoList(sysFileInfo).list();
        enrichRecords(list);
        ExcelUtil<SysFileInfo> util = new ExcelUtil<>(SysFileInfo.class);
        util.exportExcel(response, list, "文件数据");
    }

    private void enrichRecords(List<SysFileInfo> records) {
        if (records == null || records.isEmpty()) {
            return;
        }
        Set<Long> referenceIds = records.stream()
                .map(SysFileInfo::getReferenceFileId)
                .filter(id -> id != null)
                .collect(Collectors.toSet());
        Map<Long, SysFileInfo> referenceTargets = referenceIds.isEmpty()
                ? Map.of()
                : this.listByIds(referenceIds).stream().collect(Collectors.toMap(SysFileInfo::getFileId, item -> item));
        for (SysFileInfo record : records) {
            record.setReferenceFlag(record.getReferenceFileId() != null);
            Long referenceCount = this.queryChain()
                    .from(SysFileInfo.class)
                    .eq(SysFileInfo::getReferenceFileId, record.getFileId())
                    .count();
            record.setReferenceCount(referenceCount == null ? 0L : referenceCount);
            if (record.getReferenceFileId() == null) {
                continue;
            }
            SysFileInfo referenceTarget = referenceTargets.get(record.getReferenceFileId());
            if (referenceTarget == null) {
                continue;
            }
            record.setReferenceTargetFileName(referenceTarget.getFileName());
            record.setReferenceTargetFilePath(referenceTarget.getFilePath());
        }
    }
}
