package com.ruoyi.file.local.domain;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Comparator;

import org.springframework.web.multipart.MultipartFile;

import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.ServletUtils;
import com.ruoyi.common.utils.sign.Md5Utils;
import com.ruoyi.common.utils.uuid.UUID;
import com.ruoyi.file.storage.StorageBucket;
import com.ruoyi.file.storage.StorageEntity;

import jakarta.servlet.http.HttpServletRequest;
import lombok.Builder;

@Builder
public class LocalBucket implements StorageBucket {
    private String clientName;
    private String basePath;
    private String permission;
    private String api;

    @Override
    public void put(String filePath, MultipartFile file) {
        Path dest = Paths.get(basePath, filePath);
        try (InputStream inputStream = file.getInputStream()) {
            Files.createDirectories(dest.getParent());
            Files.copy(inputStream, dest);
        } catch (Exception e) {
            throw new ServiceException("Failed to upload file: " + e.getMessage());
        }
    }

    @Override
    public StorageEntity get(String filePath) throws IOException {
        Path file = Paths.get(basePath, filePath);
        StorageEntity fileEntity = new StorageEntity();
        fileEntity.setFilePath(filePath);
        fileEntity.setInputStream(new FileInputStream(file.toFile()));
        fileEntity.setByteCount(file.toFile().length());
        return fileEntity;
    }

    @Override
    public void remove(String filePath) throws IOException {
        Path file = Paths.get(basePath, filePath);
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
    public URL generatePublicURL(String filePath) throws Exception {
        HttpServletRequest request = ServletUtils.getRequest();
        StringBuffer url = request.getRequestURL();
        String contextPath = request.getSession().getServletContext().getContextPath();
        StringBuilder sb = new StringBuilder();
        sb.append(url.delete(url.length() - request.getRequestURI().length(), url.length())
                .append(contextPath).toString())
                .append(getApi()).append("/")
                .append(filePath);
        return new URI(sb.toString()).toURL();
    }

    public String uploadByMultipart(MultipartFile file, String filePath, double partSizeInBytes) throws Exception {
        final long PART_SIZE_IN_BYTES = partSizeInBytes > 0 ? (long) partSizeInBytes : 15L * 1024 * 1024;
        int totalParts = (int) Math.ceil((double) file.getSize() / PART_SIZE_IN_BYTES);

        String uploadId = UUID.randomUUID().toString();
        Path tempDir = Paths.get(basePath, "temp_uploads", uploadId);
        Files.createDirectories(tempDir);

        try (InputStream inputStream = file.getInputStream()) {
            for (int i = 0; i < totalParts; i++) {
                Path partPath = tempDir.resolve("part_" + i);
                long partSize = Math.min(PART_SIZE_IN_BYTES, file.getSize() - i * PART_SIZE_IN_BYTES);
                try (WritableByteChannel channel = Files.newByteChannel(
                        partPath,
                        StandardOpenOption.WRITE,
                        StandardOpenOption.CREATE,
                        StandardOpenOption.TRUNCATE_EXISTING)) {
                    ByteBuffer buffer = ByteBuffer.allocate(8192);
                    int bytesRead;
                    while (partSize > 0 && (bytesRead = inputStream.read(buffer.array())) != -1) {
                        buffer.limit(bytesRead);
                        channel.write(buffer);
                        buffer.clear();
                        partSize -= bytesRead;
                    }
                }
            }
            Path destPath = Paths.get(basePath, filePath);
            Files.createDirectories(destPath.getParent());
            try (WritableByteChannel outChannel = Files.newByteChannel(
                    destPath,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.WRITE,
                    StandardOpenOption.TRUNCATE_EXISTING)) {
                for (int i = 0; i < totalParts; i++) {
                    try (FileChannel inChannel = FileChannel.open(tempDir.resolve("part_" + i),
                            StandardOpenOption.READ)) {
                        inChannel.transferTo(0, inChannel.size(), outChannel);
                    }
                }
            }
            return filePath;
        } finally {
            Files.walk(tempDir).sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
        }
    }

    public String getClientName() {
        return clientName;
    }

    public String getBasePath() {
        return basePath;
    }

    public String getPermission() {
        return permission;
    }

    public String getApi() {
        return api;
    }
}
