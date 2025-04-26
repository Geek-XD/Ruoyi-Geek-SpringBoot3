package com.ruoyi.file.config;

public class LocalClientProperties {
    /** 本地存储路径 */
    private String path;

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public LocalClientProperties() {}

    public LocalClientProperties(String path) {
        this.path = path;
    }
}
