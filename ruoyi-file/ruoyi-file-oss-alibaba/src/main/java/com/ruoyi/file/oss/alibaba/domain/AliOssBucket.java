package com.ruoyi.file.oss.alibaba.domain;

import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.web.multipart.MultipartFile;

import com.aliyun.oss.OSS;
import com.aliyun.oss.ServiceException;
import com.aliyun.oss.model.AbortMultipartUploadRequest;
import com.aliyun.oss.model.CompleteMultipartUploadRequest;
import com.aliyun.oss.model.GeneratePresignedUrlRequest;
import com.aliyun.oss.model.GetObjectRequest;
import com.aliyun.oss.model.InitiateMultipartUploadRequest;
import com.aliyun.oss.model.OSSObject;
import com.aliyun.oss.model.ObjectMetadata;
import com.aliyun.oss.model.PartETag;
import com.aliyun.oss.model.PutObjectRequest;
import com.aliyun.oss.model.UploadPartRequest;
import com.ruoyi.file.domain.SysFilePartETag;
import com.ruoyi.file.oss.alibaba.exception.AliOssClientErrorException;
import com.ruoyi.file.storage.StorageBucket;

import lombok.Builder;
import lombok.extern.slf4j.Slf4j;

@Builder
@Slf4j
public class AliOssBucket implements StorageBucket {

    private String bucketName;
    private OSS ossClient;
    private String permission;
    private String endpoint;

    @Override
    public void put(String filePath, MultipartFile file) throws Exception {
        try {
            InputStream inputStream = file.getInputStream();
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentType(file.getContentType());
            metadata.setContentLength(inputStream.available());
            PutObjectRequest putRequest = new PutObjectRequest(bucketName, filePath, inputStream, metadata);
            this.ossClient.putObject(putRequest);
        } catch (Exception e) {
            log.error("Error uploading file to OSS: {}", e.getMessage(), e);
            throw new AliOssClientErrorException("Error uploading file to OSS: " + e.getMessage(), e);
        }
    }

    @Override
    public void remove(String filePath) throws Exception {
        ossClient.deleteObject(bucketName, filePath);
    }

    @Override
    public AliOssEntityVO get(String filePath) throws Exception {
        GetObjectRequest request = new GetObjectRequest(this.bucketName, filePath);
        OSSObject ossObject = this.ossClient.getObject(request);
        if (ossObject == null) {
            throw new Exception("Failed to retrieve object from OSS.");
        }
        AliOssEntityVO fileVO = new AliOssEntityVO();
        fileVO.setInputStream(ossObject.getObjectContent());
        fileVO.setKey(ossObject.getKey());
        fileVO.setBucketName(ossObject.getBucketName());
        fileVO.setByteCount(ossObject.getObjectMetadata().getContentLength());
        fileVO.setFilePath(filePath);
        return fileVO;
    }

    @Override
    public URL generatePresignedUrl(String filePath, int expireTime) {
        GeneratePresignedUrlRequest request = new GeneratePresignedUrlRequest(bucketName, filePath);
        Date expiration = new Date(System.currentTimeMillis() + expireTime * 1000); // 设置过期时间为1小时
        request.setExpiration(expiration);
        return ossClient.generatePresignedUrl(request);
    }

    @Override
    public URL generatePublicURL(String filePath) throws Exception {
        StringBuilder sb = new StringBuilder();
        sb.append("https://").append(getBucketName())
                .append(".").append(getEndpoint())
                .append("/").append(filePath.replace("\\", "/"));
        return URI.create(sb.toString()).toURL();
    }

    /**
     * 初始化分片上传
     */
    public String initMultipartUpload(String filePath) throws Exception {
        InitiateMultipartUploadRequest initRequest = new InitiateMultipartUploadRequest(bucketName, filePath);
        String uploadId = ossClient.initiateMultipartUpload(initRequest).getUploadId();
        return uploadId;
    }

    /**
     * 上传单个分片
     */
    public SysFilePartETag uploadPart(String filePath, String uploadId, int partNumber, long partSize,
            InputStream inputStream)
            throws Exception {
        UploadPartRequest uploadPartRequest = new UploadPartRequest();
        uploadPartRequest.setBucketName(bucketName);
        uploadPartRequest.setKey(filePath);
        uploadPartRequest.setUploadId(uploadId);
        uploadPartRequest.setInputStream(inputStream);
        uploadPartRequest.setPartSize(partSize);
        uploadPartRequest.setPartNumber(partNumber);
        PartETag partETag = ossClient.uploadPart(uploadPartRequest).getPartETag();
        return new SysFilePartETag(partETag.getPartNumber(), partETag.getETag(), partETag.getPartSize(),
                partETag.getPartCRC());
    }

    /**
     * 完成分片上传
     */
    public String completeMultipartUpload(String filePath, String uploadId, List<SysFilePartETag> sysFilePartETags)
            throws Exception {
        if (sysFilePartETags == null || sysFilePartETags.isEmpty()) {
            throw new ServiceException("分片ETag列表不能为空");
        }
        List<PartETag> partETags = sysFilePartETags.stream()
                .map(part -> new PartETag(part.getPartNumber(), part.getETag().trim()))
                .sorted(Comparator.comparingInt(PartETag::getPartNumber))
                .collect(Collectors.toList());

        try {
            CompleteMultipartUploadRequest completeRequest = new CompleteMultipartUploadRequest(bucketName, filePath,
                    uploadId, partETags);
            ossClient.completeMultipartUpload(completeRequest);
            log.info("分片上传已完成并合并: 文件={}, uploadId={}, 分片数={}",
                    filePath, uploadId, partETags.size());
            return filePath;
        } catch (Exception e) {
            log.error("合并分片失败: 文件={}, uploadId={}, 错误={}",
                    filePath, uploadId, e.getMessage());
            try {
                ossClient.abortMultipartUpload(new AbortMultipartUploadRequest(bucketName, filePath, uploadId));
            } catch (Exception abortEx) {
                log.error("取消分片上传失败: {}", abortEx.getMessage());
            }
            throw new AliOssClientErrorException("合并分片失败: " + e.getMessage(), e);
        }
    }

    public OSS getOssClient() {
        return ossClient;
    }

    public void setOssClient(OSS ossClient) {
        this.ossClient = ossClient;
    }

    public String getBucketName() {
        return bucketName;
    }

    public void setBucketName(String bucketName) {
        this.bucketName = bucketName;
    }

    public String getPermission() {
        return permission;
    }

    public String getEndpoint() {
        return endpoint;
    }

}