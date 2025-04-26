package com.ruoyi.file.domain;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.web.multipart.MultipartFile;

import com.ruoyi.file.storage.StorageBucket;

public class LocalBucket implements StorageBucket {
    private String clientName;
    private String basePath;

    public LocalBucket(String clientName, String basePath) {
        this.clientName = clientName;
        this.basePath = basePath;
    }

    public String getClientName() {
        return clientName;
    }

    public String getBasePath() {
        return basePath;
    }

    /**
     * 上传文件
     */
    public void put(String filePath, MultipartFile file) throws IOException {
        Path dest = Paths.get(basePath, filePath);
        Files.createDirectories(dest.getParent());
        try (InputStream in = file.getInputStream(); OutputStream out = Files.newOutputStream(dest)) {
            byte[] buffer = new byte[8192];
            int len;
            while ((len = in.read(buffer)) != -1) {
                out.write(buffer, 0, len);
            }
        }
    }

    /**
     * 获取文件输入流
     */
    public FileEntity get(String filePath) throws IOException {
        Path file = Paths.get(basePath, filePath);
        FileEntity fileEntity = new FileEntity();
        fileEntity.setFilePath(filePath);
        fileEntity.setFileInputSteam(new FileInputStream(file.toFile()));
        fileEntity.setByteCount(file.toFile().length());
        return fileEntity;
    }

    /**
     * 删除文件
     */
    public boolean remove(String filePath) throws IOException {
        Path file = Paths.get(basePath, filePath);
        return Files.deleteIfExists(file);
    }

    public URL generatePresignedUrl(String filePath) throws Exception {
        // 这里仅示例，实际应结合你的静态资源映射规则

        // 如 http://localhost:8080/file/download/resource?resource=xxx
        URI uri = new URI("http://localhost:8080/file/download/resource?resource=" +
                java.net.URLEncoder.encode(filePath, "UTF-8"));
        return uri.toURL();
    }

}
