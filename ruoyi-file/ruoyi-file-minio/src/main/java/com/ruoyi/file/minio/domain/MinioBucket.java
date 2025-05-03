package com.ruoyi.file.minio.domain;

import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.concurrent.TimeUnit;

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

public class MinioBucket implements StorageBucket {

    private String url;
    private String permission;
    private MinioClient client;
    private String bucketName;

    @Override
    public void put(String fileName, MultipartFile file) throws Exception {
        put(fileName, file.getContentType(), file.getInputStream());
    }

    @Override
    public void remove(String filePath) throws Exception {
        RemoveObjectArgs build = RemoveObjectArgs.builder().object(filePath).bucket(bucketName).build();
        remove(build);
    }

    @Override
    public MinioEntityVO get(String filePath) throws Exception {
        GetObjectArgs getObjectArgs = GetObjectArgs.builder().object(filePath).bucket(bucketName).build();
        GetObjectResponse inputStream = this.client.getObject(getObjectArgs);
        MinioEntityVO minioFileVO = new MinioEntityVO();

        minioFileVO.setInputSteam(inputStream);
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
                .append("/").append(filePath);
        return URI.create(sb.toString()).toURL();
    }

    public void put(String filePath, String contentType, InputStream inputStream) throws Exception {
        PutObjectArgs build = PutObjectArgs.builder().contentType(contentType)
                .stream(inputStream, inputStream.available(), -1)
                .bucket(bucketName).object(filePath).build();
        put(build);
    }

    public void put(PutObjectArgs putObjectArgs) throws Exception {
        try {
            this.client.putObject(putObjectArgs);
        } catch (Exception e) {
            throw new MinioClientErrorException(e.getMessage());
        }
    }

    public void remove(RemoveObjectArgs removeObjectArgs) throws Exception {
        this.client.removeObject(removeObjectArgs);
    }

    public MinioBucket() {
    }

    public MinioBucket(MinioClient client, String bucketName, String permission, String url) {
        this.client = client;
        this.bucketName = bucketName;
        this.permission = permission;
        this.url = url;
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
