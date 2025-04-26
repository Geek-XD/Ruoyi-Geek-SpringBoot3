package com.ruoyi.file.oss.alibaba.domain;

import java.io.InputStream;
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

public class AliOssBucket implements StorageBucket{

    private static final Logger logger = LoggerFactory.getLogger(AliOssBucket.class);

    private String bucketName;
    private OSS ossClient;

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

    public void put(String fileName, MultipartFile file) throws Exception {
        put(fileName, file.getContentType(), file.getInputStream());
    }

    // 上传文件
    public void put(String filePath, String contentType, InputStream inputStream) throws Exception {
        try {
            // 创建 ObjectMetadata 对象，并设置内容类型
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentType(contentType);
            metadata.setContentLength(inputStream.available()); // 使用 InputStream 的 available 方法

            // 创建 PutObjectRequest 对象
            PutObjectRequest putRequest = new PutObjectRequest(bucketName, filePath, inputStream, metadata);

            // 上传对象
            ossClient.putObject(putRequest);
        } catch (Exception e) {
            logger.error("Error uploading file: {}", e.getMessage(), e);
            throw new AliOssClientErrorException("Error uploading file: " + e.getMessage(), e);
        }
    }

    public void put(PutObjectRequest putObjectRequest) throws Exception {
        try {
            this.ossClient.putObject(putObjectRequest);
        } catch (Exception e) {
            logger.error("Error uploading file: {}", e.getMessage(), e);
            throw new AliOssClientErrorException("Error uploading file: " + e.getMessage(), e);
        }
    }

    public void remove(String filePath) throws Exception {
        // 删除单个对象
        ossClient.deleteObject(bucketName, filePath);
    }

    public void removeMultiple(List<String> filePaths) throws Exception {
        // 删除多个对象
        try {
            DeleteObjectsRequest deleteRequest = new DeleteObjectsRequest(bucketName).withKeys(filePaths);
            ossClient.deleteObjects(deleteRequest);
        } catch (Exception e) {
            logger.error("Error deleting files: {}", e.getMessage(), e);
            throw new AliOssClientErrorException("Error deleting files: " + e.getMessage(), e);
        }
    }

    /**
     * 文件下载
     *
     * @param filePath 文件路径
     * @return 返回封装的OSS下载文件对象
     * @throws Exception 如果下载过程中出现问题
     */
    public AliOssFileVO get(String filePath) throws Exception {
        GetObjectRequest request = new GetObjectRequest(this.bucketName, filePath);
        OSSObject ossObject = this.ossClient.getObject(request);
        if (ossObject == null) {
            throw new Exception("Failed to retrieve object from OSS.");
        }
        // 设置 AliOssFileVO 对象的属性
        AliOssFileVO fileVO = new AliOssFileVO();
        fileVO.setFileInputSteam(ossObject.getObjectContent());
        fileVO.setKey(ossObject.getKey());
        fileVO.setBucketName(ossObject.getBucketName());
        fileVO.setByteCount(ossObject.getObjectMetadata().getContentLength());
        fileVO.setFilePath(filePath);
        return fileVO;
    }

    public URL generatePresignedUrl(String filePath){
        GeneratePresignedUrlRequest request = new GeneratePresignedUrlRequest(bucketName, filePath);
        Date expiration = new Date(System.currentTimeMillis() + 3600 * 1000); // 设置过期时间为1小时
        request.setExpiration(expiration);
        return ossClient.generatePresignedUrl(request);
    }
}