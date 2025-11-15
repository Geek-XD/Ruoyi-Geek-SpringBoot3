package com.ruoyi.framework.file.domain;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.channels.FileChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.web.multipart.MultipartFile;

import com.ruoyi.common.core.file.domain.SysFilePartETag;
import com.ruoyi.common.core.file.storage.StorageBucket;
import com.ruoyi.common.core.file.storage.StorageEntity;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.ServletUtils;
import com.ruoyi.common.utils.sign.Md5Utils;
import com.ruoyi.common.utils.uuid.UUID;

import jakarta.servlet.http.HttpServletRequest;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;

@Builder
@Slf4j
public class LocalBucket implements StorageBucket {

    private String bucketName;
    private String basePath;
    private String permission;
    private String api;

    @Override
    public void put(String filePath, MultipartFile file) {
        Path dest = Paths.get(getBasePath(), filePath);
        try (InputStream inputStream = file.getInputStream()) {
            Files.createDirectories(dest.getParent());
            Files.copy(inputStream, dest);
        } catch (Exception e) {
            throw new ServiceException("Failed to upload file: " + e.getMessage());
        }
    }

    @Override
    public StorageEntity get(String filePath) throws IOException {
        Path file = Paths.get(getBasePath(), filePath);
        StorageEntity fileEntity = new StorageEntity();
        fileEntity.setFilePath(filePath);
        fileEntity.setInputStream(new FileInputStream(file.toFile()));
        fileEntity.setByteCount(file.toFile().length());
        return fileEntity;
    }

    @Override
    public void remove(String filePath) throws IOException {
        Path file = Paths.get(getBasePath(), filePath);
        Files.deleteIfExists(file);
    }

    @Override
    public URL generatePresignedUrl(String filePath, int expireTime) throws Exception {
        HttpServletRequest request = ServletUtils.getRequest();
        StringBuffer url = request.getRequestURL();
        String contextPath = request.getSession().getServletContext().getContextPath();
        String toHex = Md5Utils.hash(filePath + expireTime);
        StringBuilder sb = new StringBuilder();
        sb.append(url.delete(url.length() - request.getRequestURI().length(), url.length())
                .append(contextPath).toString())
                .append(getApi()).append("?")
                .append("filePath=").append(URLEncoder.encode(filePath, "UTF-8"))
                .append("&toHex=").append(toHex);
        return URI.create(sb.toString()).toURL();
    }

    @Override
    public URL generatePublicUrl(String filePath) throws Exception {
        HttpServletRequest request = ServletUtils.getRequest();
        StringBuffer url = request.getRequestURL();
        String contextPath = request.getSession().getServletContext().getContextPath();
        StringBuilder sb = new StringBuilder();
        sb.append(url.delete(url.length() - request.getRequestURI().length(), url.length())
                .append(contextPath).toString()).append(getApi())
                .append("/").append(filePath.replace("\\", "/"));
        return new URI(sb.toString()).toURL();
    }

    // 存储分片上传的元数据
    private final ConcurrentHashMap<String, List<Map<String, Object>>> uploadMetadata = new ConcurrentHashMap<>();

    public String initMultipartUpload(String filePath) throws Exception {
        try {
            String uploadId = UUID.randomUUID().toString();
            uploadMetadata.put(uploadId, new ArrayList<>());

            // 创建临时上传目录
            Path tempDir = Paths.get(getBasePath(), "temp_uploads", uploadId);
            Files.createDirectories(tempDir);
            return uploadId;
        } catch (Exception e) {
            log.error("初始化失败: 文件={}, 错误={}", filePath, e.getMessage());
            throw new ServiceException("初始化分片上传失败: " + e.getMessage());
        }
    }

    public SysFilePartETag uploadPart(String filePath, String uploadId, int partNumber, long partSize,
            InputStream inputStream)
            throws Exception {
        if (!uploadMetadata.containsKey(uploadId)) {
            throw new ServiceException("无效的 uploadId: " + uploadId);
        }
        Path tempDir = Paths.get(getBasePath(), "temp_uploads", uploadId);
        Path partPath = tempDir.resolve("part_" + partNumber);
        try (OutputStream fos = Files.newOutputStream(partPath)) {
            byte[] buffer = new byte[8192];
            int bytesRead;
            long totalBytesWritten = 0;
            while ((bytesRead = inputStream.read(buffer)) != -1 && totalBytesWritten < partSize) {
                int writeSize = (int) Math.min(bytesRead, partSize - totalBytesWritten);
                fos.write(buffer, 0, writeSize);
                totalBytesWritten += writeSize;
            }
            if (totalBytesWritten != partSize) {
                throw new ServiceException("分片大小不匹配: 预期=" + partSize + ", 实际=" + totalBytesWritten);
            }
        }
        String etag = Md5Utils.getMd5(partPath.toFile());
        if (etag == null) {
            throw new ServiceException("计算分片 MD5 失败");
        }
        etag = etag.toUpperCase();
        Map<String, Object> partInfo = Map.of(
                "partNumber", partNumber,
                "etag", etag,
                "size", partSize,
                "path", partPath.toString());
        List<Map<String, Object>> parts = uploadMetadata.get(uploadId);
        int insertPos = 0;
        while (insertPos < parts.size()
                && ((Number) parts.get(insertPos).get("partNumber")).intValue() < partNumber) {
            insertPos++;
        }
        parts.add(insertPos, partInfo);
        return new SysFilePartETag(partNumber, etag, partSize, null);
    }

    public String completeMultipartUpload(String filePath, String uploadId, List<SysFilePartETag> partETags)
            throws Exception {
        List<Map<String, Object>> storedParts = uploadMetadata.get(uploadId);
        if (storedParts == null) {
            throw new ServiceException("无效的 uploadId: " + uploadId);
        }
        if (partETags.size() != storedParts.size()) {
            throw new ServiceException("分片数量不匹配: 预期=" + storedParts.size() + ", 实际=" + partETags.size());
        }
        // 验证每个分片的 ETag
        for (int i = 0; i < partETags.size(); i++) {
            Map<String, Object> expected = storedParts.get(i);
            SysFilePartETag actual = partETags.get(i);

            if (!expected.get("etag").equals(actual.getETag()) ||
                    !expected.get("partNumber").equals(actual.getPartNumber())) {
                throw new ServiceException("分片验证失败: 序号=" + actual.getPartNumber());
            }
        }
        Path destPath = Paths.get(getBasePath(), filePath);
        Files.createDirectories(destPath.getParent());
        try (WritableByteChannel outChannel = Files.newByteChannel(
                destPath,
                StandardOpenOption.CREATE,
                StandardOpenOption.WRITE,
                StandardOpenOption.TRUNCATE_EXISTING)) {
            for (Map<String, Object> part : storedParts) {
                Path partPath = Paths.get((String) part.get("path"));
                try (FileChannel inChannel = FileChannel.open(partPath, StandardOpenOption.READ)) {
                    inChannel.transferTo(0, inChannel.size(), outChannel);
                }
            }
        }
        // 清理临时文件和元数据
        Path tempDir = Paths.get(getBasePath(), "temp_uploads", uploadId);
        Files.walk(tempDir).sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
        uploadMetadata.remove(uploadId);
        log.info("分片合并完成: 文件={}, uploadId={}, 分片数={}", filePath, uploadId, storedParts.size());
        return filePath;
    }

    public String getBucketName() {
        return bucketName;
    }

    public String getBasePath() {
        if (basePath != null && !basePath.endsWith("/")) {
            basePath = basePath + "/";
        }
        return basePath;
    }

    public String getPermission() {
        return permission;
    }

    public String getApi() {
        return api;
    }
}
