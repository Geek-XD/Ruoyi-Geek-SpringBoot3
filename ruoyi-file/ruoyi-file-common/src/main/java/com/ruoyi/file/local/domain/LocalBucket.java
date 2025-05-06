package com.ruoyi.file.local.domain;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.web.multipart.MultipartFile;

import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.ServletUtils;
import com.ruoyi.common.utils.sign.Md5Utils;
import com.ruoyi.file.storage.StorageBucket;
import com.ruoyi.file.storage.StorageEntity;

import jakarta.servlet.http.HttpServletRequest;

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

    public LocalBucket(String clientName, String basePath, String permission, String api) {
        this.clientName = clientName;
        this.basePath = basePath;
        this.permission = permission;
        this.api = api;
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
