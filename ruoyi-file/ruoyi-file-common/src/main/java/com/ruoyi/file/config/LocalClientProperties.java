package com.ruoyi.file.config;

public class LocalClientProperties {
    /** 本地存储路径 */
    private String path;
    private String permission;
    private String api;

    public String getPath() {
        return path;
    }

    public String getPermission() {
        return permission;
    }

    public void setPermission(String permission) {
        this.permission = permission;
    }

    public String getApi() {
        return api;
    }

    public void setApi(String api) {
        this.api = api;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public LocalClientProperties() {
    }

    public LocalClientProperties(String path) {
        this.path = path;
    }

}
