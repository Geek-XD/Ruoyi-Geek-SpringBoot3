package com.ruoyi.file.minio.domain;

import com.ruoyi.common.core.text.Convert;
import com.ruoyi.file.storage.StorageEntity;

import io.minio.GetObjectResponse;
import okhttp3.Headers;

public class MinioEntityVO extends StorageEntity {
    private String object;
    private Headers headers;
    private String bucket;
    private String region;

    public MinioEntityVO(GetObjectResponse response, String filePath) {
        this.setFilePath(filePath);
        this.setInputStream(response);
        this.setByteCount(Convert.toLong(response.headers().get("Content-Length"), null));
        this.setObject(response.object());
        this.setRegion(response.region());
        this.setBucket(response.bucket());
        this.setHeaders(response.headers());
    }

    public String getObject() {
        return object;
    }

    public void setObject(String object) {
        this.object = object;
    }

    public Headers getHeaders() {
        return headers;
    }

    public void setHeaders(Headers headers) {
        this.headers = headers;
    }

    public String getBucket() {
        return bucket;
    }

    public void setBucket(String bucket) {
        this.bucket = bucket;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

}
