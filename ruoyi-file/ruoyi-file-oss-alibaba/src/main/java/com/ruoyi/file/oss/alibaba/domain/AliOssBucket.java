package com.ruoyi.file.oss.alibaba.domain;

import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;

import com.aliyun.oss.OSS;
import com.aliyun.oss.model.DeleteObjectsRequest;
import com.aliyun.oss.model.GeneratePresignedUrlRequest;
import com.aliyun.oss.model.GetObjectRequest;
import com.aliyun.oss.model.OSSObject;
import com.aliyun.oss.model.ObjectMetadata;
import com.aliyun.oss.model.PutObjectRequest;
import com.ruoyi.file.oss.alibaba.exception.AliOssClientErrorException;
import com.ruoyi.file.storage.StorageBucket;

public class AliOssBucket implements StorageBucket {

    private static final Logger logger = LoggerFactory.getLogger(AliOssBucket.class);
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
            logger.error("Error uploading file to OSS: {}", e.getMessage(), e);
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
                .append("/").append(filePath);
        return URI.create(sb.toString()).toURL();
    }

    public void removeMultiple(List<String> filePaths) throws Exception {
        try {
            DeleteObjectsRequest deleteRequest = new DeleteObjectsRequest(bucketName).withKeys(filePaths);
            ossClient.deleteObjects(deleteRequest);
        } catch (Exception e) {
            logger.error("Error deleting files: {}", e.getMessage(), e);
            throw new AliOssClientErrorException("Error deleting files: " + e.getMessage(), e);
        }
    }

    public AliOssBucket() {
    }

    // 构造函数
    public AliOssBucket(OSS ossClient, String bucketName) {
        this.ossClient = ossClient;
        this.bucketName = bucketName;
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