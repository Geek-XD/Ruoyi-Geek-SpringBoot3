package com.ruoyi.file.minio.domain;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

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
import lombok.extern.slf4j.Slf4j;

@Builder
@Slf4j
public class MinioBucket implements StorageBucket {

    private String url;
    private String permission;
    private MinioClient client;
    private String bucketName;

    private static final ConcurrentHashMap<String, AtomicBoolean> uploadingParts = new ConcurrentHashMap<>();

    @Override
    public void put(String filePath, MultipartFile file) {
        try (InputStream inputStream = file.getInputStream()) {
            PutObjectArgs putObjectArgs = PutObjectArgs.builder()
                    .contentType(file.getContentType())
                    .stream(inputStream, file.getSize(), -1)
                    .bucket(bucketName)
                    .object(filePath)
                    .build();
            client.putObject(putObjectArgs);
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
        client.removeObject(removeObjectArgs);
    }

    @Override
    public MinioEntityVO get(String filePath) throws Exception {
        GetObjectArgs getObjectArgs = GetObjectArgs.builder()
                .object(filePath)
                .bucket(bucketName)
                .build();
        GetObjectResponse response = client.getObject(getObjectArgs);
        MinioEntityVO minioFileVO = new MinioEntityVO();
        minioFileVO.setInputStream(response);
        minioFileVO.setByteCount(Convert.toLong(response.headers().get("Content-Length"), null));
        minioFileVO.setFilePath(filePath);
        minioFileVO.setObject(response.object());
        minioFileVO.setRegion(response.region());
        minioFileVO.setBuket(response.bucket());
        minioFileVO.setHeaders(response.headers());
        return minioFileVO;
    }

    @Override
    public URL generatePresignedUrl(String filePath, int expireTime) throws Exception {
        GetPresignedObjectUrlArgs request = GetPresignedObjectUrlArgs.builder()
                .method(Method.GET)
                .bucket(bucketName)
                .object(filePath)
                .expiry(expireTime, TimeUnit.SECONDS)
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

    /**
     * 初始化分片上传
     */
    public String initMultipartUpload(String filePath) throws Exception {
        try {
            String uploadId = UUID.randomUUID().toString().replace("-", "").toUpperCase();
            return uploadId;
        } catch (Exception e) {
            log.error("初始化失败: 文件={}, 错误={}", filePath, e.getMessage());
            throw new MinioClientErrorException("初始化失败", e);
        }
    }

    /**
     * 上传单个分片
     */
    public String uploadPart(String filePath, String uploadId, int partNumber, long partSize, InputStream inputStream)
            throws Exception {
        // 生成唯一的上传键
        String uploadKey = String.format("%s-%s-%d", filePath, uploadId, partNumber);
        AtomicBoolean isUploading = uploadingParts.computeIfAbsent(uploadKey, k -> new AtomicBoolean(false));
        // 使用 CAS 检查是否已经在上传中
        if (!isUploading.compareAndSet(false, true)) {
            throw new MinioClientErrorException("分片正在上传中: " + uploadKey);
        }
        try {
            // 构建分片存储路径
            String partPath = String.format("%s.%s.part.%d", filePath, uploadId, partNumber);
            // 构造上传请求参数
            PutObjectArgs args = PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(partPath)
                    .stream(inputStream, partSize, -1)
                    .build();
            // 执行上传操作并获取 ETag
            String etag = client.putObject(args).etag().replace("\"", "").toUpperCase();
            return etag;
        } catch (Exception e) {
            log.error("分片上传失败: 文件={}, 分片={}, 错误={}", filePath, partNumber, e.getMessage());
            throw new MinioClientErrorException("上传分片失败", e);
        } finally {
            isUploading.set(false);// 标记为上传完成，并清理状态
            uploadingParts.remove(uploadKey);
        }
    }

    /**
     * 完成分片上传并合并文件
     */
    public String completeMultipartUpload(String filePath, String uploadId, List<Map<String, Object>> partInfos)
            throws Exception {
        if (partInfos == null || partInfos.isEmpty()) {
            throw new IllegalArgumentException("分片信息不能为空");
        }

        // 按分片序号排序
        List<Map<String, Object>> sortedParts = partInfos.stream()
                .sorted(Comparator.comparingInt(p -> ((Number) p.get("partNumber")).intValue()))
                .collect(Collectors.toList());

        // 创建临时文件并合并分片
        Path tempFilePath = Files.createTempFile("minio-merge-", ".tmp");
        try (OutputStream fos = Files.newOutputStream(tempFilePath)) {
            for (Map<String, Object> part : sortedParts) {
                int partNumber = ((Number) part.get("partNumber")).intValue();
                String partPath = String.format("%s.%s.part.%d", filePath, uploadId, partNumber);

                try (InputStream is = client.getObject(
                        GetObjectArgs.builder()
                                .bucket(bucketName)
                                .object(partPath)
                                .build())) {
                    byte[] buffer = new byte[8192];
                    int bytesRead;
                    while ((bytesRead = is.read(buffer)) != -1) {
                        fos.write(buffer, 0, bytesRead);
                    }
                }
            }
        }
        // 上传合并后的文件
        try (InputStream finalInputStream = Files.newInputStream(tempFilePath)) {
            long fileSize = Files.size(tempFilePath);
            PutObjectArgs putArgs = PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(filePath)
                    .stream(finalInputStream, fileSize, -1)
                    .build();

            client.putObject(putArgs);
        }
        // 清理临时文件和分片
        Files.deleteIfExists(tempFilePath);
        for (Map<String, Object> part : sortedParts) {
            int partNumber = ((Number) part.get("partNumber")).intValue();
            String partPath = String.format("%s.%s.part.%d", filePath, uploadId, partNumber);
            try {
                client.removeObject(
                        RemoveObjectArgs.builder()
                                .bucket(bucketName)
                                .object(partPath)
                                .build());
            } catch (Exception e) {
                log.warn("清理分片失败: {}", e.getMessage());
            }
        }

        log.info("分片合并完成: 文件={}, uploadId={}, 分片数={}", filePath, uploadId, sortedParts.size());
        return filePath;
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
