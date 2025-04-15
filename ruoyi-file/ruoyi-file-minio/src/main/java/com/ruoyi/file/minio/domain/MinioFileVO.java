package com.ruoyi.file.minio.domain;

import com.ruoyi.file.domain.FileEntity;

import okhttp3.Headers;

public class MinioFileVO extends FileEntity {
    private String object;
    private Headers headers;
    private String buket;
    private String region;

    public MinioFileVO() {
        super();
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

    public String getBuket() {
        return buket;
    }

    public void setBuket(String buket) {
        this.buket = buket;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

}
