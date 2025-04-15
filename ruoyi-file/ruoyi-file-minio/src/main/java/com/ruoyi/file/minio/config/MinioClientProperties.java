package com.ruoyi.file.minio.config;
import io.minio.MinioClient;

public class MinioClientProperties {

    private String url;
    private String accessKey;
    private String secretKey;
    private String bucketName;

    private MinioClient client;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getAccessKey() {
        return accessKey;
    }

    public void setAccessKey(String accessKey) {
        this.accessKey = accessKey;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public MinioClient getClient() {
        return client;
    }

    public void setClient(MinioClient client) {
        this.client = client;
    }

    public MinioClientProperties(String url, String accessKey, String secretKey, String bucketName) {
        this.url = url;
        this.accessKey = accessKey;
        this.secretKey = secretKey;
        this.bucketName = bucketName;
    }

    public MinioClientProperties() {
    }

    public String getBucketName() {
        return bucketName;
    }

    public void setBucketName(String bucketName) {
        this.bucketName = bucketName;
    }
}
