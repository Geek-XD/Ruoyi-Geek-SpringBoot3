package com.ruoyi.file.oss.alibaba.domain;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;

import com.aliyun.oss.OSS;
import com.aliyun.oss.model.AbortMultipartUploadRequest;
import com.aliyun.oss.model.CompleteMultipartUploadRequest;
import com.aliyun.oss.model.DeleteObjectsRequest;
import com.aliyun.oss.model.GeneratePresignedUrlRequest;
import com.aliyun.oss.model.GetObjectRequest;
import com.aliyun.oss.model.InitiateMultipartUploadRequest;
import com.aliyun.oss.model.OSSObject;
import com.aliyun.oss.model.ObjectMetadata;
import com.aliyun.oss.model.PartETag;
import com.aliyun.oss.model.PutObjectRequest;
import com.aliyun.oss.model.UploadPartRequest;
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

    public String uploadFileByMultipart(MultipartFile file, String filePath, double partSize) throws Exception {
        final long PART_SIZE_BYTES = 15 * 1024 * 1024; // 15MB
        // 初始化分片上传
        InitiateMultipartUploadRequest initRequest = new InitiateMultipartUploadRequest(bucketName, filePath);
        String uploadId = ossClient.initiateMultipartUpload(initRequest).getUploadId();

        List<PartETag> partETags = new ArrayList<>();

        try (InputStream inputStream = file.getInputStream()) {
            byte[] buffer = new byte[(int) PART_SIZE_BYTES];
            int bytesRead;
            int partNumber = 1;

            // 计算总分片数
            long totalParts = (file.getSize() + PART_SIZE_BYTES - 1) / PART_SIZE_BYTES;
            long totalBytesUploaded = 0;

            logger.info("开始分片上传: 文件={}, 总分片数={}, 分片大小={}MB",
                    filePath, totalParts, PART_SIZE_BYTES / (1024.0 * 1024.0));

            while ((bytesRead = inputStream.read(buffer)) != -1) {
                // 记录分片上传开始
                logger.info("开始上传分片: 文件={}, 分片={}/{}, 大小={}KB",
                        filePath, partNumber, totalParts, bytesRead / 1024);

                UploadPartRequest uploadPartRequest = new UploadPartRequest();
                uploadPartRequest.setBucketName(bucketName);
                uploadPartRequest.setKey(filePath);
                uploadPartRequest.setUploadId(uploadId);
                uploadPartRequest.setInputStream(new ByteArrayInputStream(buffer, 0, bytesRead));
                uploadPartRequest.setPartSize(bytesRead);
                uploadPartRequest.setPartNumber(partNumber);

                // 记录分片ETag
                PartETag partETag = ossClient.uploadPart(uploadPartRequest).getPartETag();
                partETags.add(partETag);

                // 更新已上传字节数并记录进度
                totalBytesUploaded += bytesRead;
                double progress = (double) totalBytesUploaded / file.getSize() * 100;

                logger.info("完成上传分片: 文件={}, 分片={}/{}, 大小={}KB, ETag={}, 进度={:.2f}%",
                        filePath,
                        partNumber,
                        totalParts,
                        bytesRead / 1024,
                        partETag.getETag(),
                        progress);

                partNumber++;
            }

            logger.info("上传完成: 文件={}, 总大小={}MB, 总分片数={}",
                    filePath,
                    file.getSize() / (1024.0 * 1024.0),
                    totalParts);
        } catch (Exception e) {
            // 上传失败，取消分片上传
            logger.error("分片上传失败: 文件={}, uploadId={}", filePath, uploadId, e);
            ossClient.abortMultipartUpload(new AbortMultipartUploadRequest(bucketName, filePath, uploadId));
            throw new AliOssClientErrorException("分片上传失败", e);
        }

        // 完成分片上传
        CompleteMultipartUploadRequest completeRequest = new CompleteMultipartUploadRequest(bucketName, filePath,
                uploadId, partETags);
        ossClient.completeMultipartUpload(completeRequest);

        logger.info("分片上传已完成并合并: 文件={}, uploadId={}", filePath, uploadId);
        return filePath;
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