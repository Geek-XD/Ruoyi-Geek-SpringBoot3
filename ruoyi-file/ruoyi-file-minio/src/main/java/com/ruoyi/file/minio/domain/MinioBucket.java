package com.ruoyi.file.minio.domain;

import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;

import com.ruoyi.common.core.text.Convert;
import com.ruoyi.file.minio.exception.MinioClientErrorException;
import com.ruoyi.file.storage.StorageBucket;

import io.minio.GetObjectArgs;
import io.minio.GetObjectResponse;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.http.Method;
import lombok.Builder;

@Builder
public class MinioBucket implements StorageBucket {

    private static final Logger log = LoggerFactory.getLogger(StorageBucket.class);
    private String url;
    private String permission;
    private MinioClient client;
    private String bucketName;

    @Override
    public void put(String filePath, MultipartFile file) {
        try {
            InputStream inputStream = file.getInputStream();
            PutObjectArgs putObjectArgs = PutObjectArgs.builder()
                    .contentType(file.getContentType())
                    .stream(inputStream, inputStream.available(), -1)
                    .bucket(bucketName)
                    .object(filePath)
                    .build();
            this.client.putObject(putObjectArgs);
        } catch (Exception e) {
            throw new MinioClientErrorException(e.getMessage());
        }
    }

    @Override
    public void remove(String filePath) throws Exception {
        RemoveObjectArgs removeObjectArgs = RemoveObjectArgs.builder()
                .object(filePath)
                .bucket(bucketName)
                .build();
        this.client.removeObject(removeObjectArgs);
    }

    @Override
    public MinioEntityVO get(String filePath) throws Exception {
        GetObjectArgs getObjectArgs = GetObjectArgs.builder()
                .object(filePath)
                .bucket(bucketName)
                .build();
        GetObjectResponse inputStream = this.client.getObject(getObjectArgs);
        MinioEntityVO minioFileVO = new MinioEntityVO();
        minioFileVO.setInputStream(inputStream);
        minioFileVO.setByteCount(Convert.toLong(inputStream.headers().get("Content-Length"), null));
        minioFileVO.setFilePath(filePath);
        minioFileVO.setObject(inputStream.object());
        minioFileVO.setRegion(inputStream.region());
        minioFileVO.setBuket(inputStream.bucket());
        minioFileVO.setHeaders(inputStream.headers());
        return minioFileVO;
    }

    @Override
    public URL generatePresignedUrl(String filePath, int expireTime) throws Exception {
        GetPresignedObjectUrlArgs request = GetPresignedObjectUrlArgs.builder()
                .method(Method.GET)
                .bucket(bucketName)
                .object(filePath)
                .expiry(expireTime, TimeUnit.SECONDS) // 设置过期时间为1小时
                .build();
        String urlString = client.getPresignedObjectUrl(request);
        return URI.create(urlString).toURL();
    }

    @Override
    public URL generatePublicURL(String filePath) throws Exception {
        StringBuilder sb = new StringBuilder();
        sb.append(getUrl())
                .append("/").append(getBucketName())
                .append("/").append(filePath.replace("\\", "/"));
        return URI.create(sb.toString()).toURL();
    }

    public String uploadByMultipart(MultipartFile file, String filePath, double partSizeInMB) throws Exception {
        // 验证分片大小（直接比较 MB 值）
        if (partSizeInMB < 15.0) {
            log.warn("分片大小过小，调整为15MB: 传入值={}MB", partSizeInMB);
            partSizeInMB = 15.0; // 修正：使用 MB 值
        }

        // 将 MB 转换为字节（仅在需要时转换）
        long partSizeInBytes = (long) (partSizeInMB * 1024 * 1024);

        log.info("实际分片大小: {}MB ({}字节)", partSizeInMB, partSizeInBytes);

        try (InputStream inputStream = file.getInputStream()) {
            PutObjectArgs putArgs = PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(filePath)
                    .stream(inputStream, file.getSize(), partSizeInBytes) // 使用字节值
                    .contentType(file.getContentType())
                    .build();

            client.putObject(putArgs);
            return filePath;
        } catch (Exception e) {
            throw new MinioClientErrorException("上传失败: " + e.getMessage());
        }
    }

    public String getName() {
        return bucketName;
    }

    public MinioClient getClient() {
        return client;
    }

    public String getBucketName() {
        return bucketName;
    }

    public void setBucketName(String bucketName) {
        this.bucketName = bucketName;
    }

    public void setClient(MinioClient client) {
        this.client = client;
    }

    public String getPermission() {
        return permission;
    }

    public String getUrl() {
        return url;
    }

}
