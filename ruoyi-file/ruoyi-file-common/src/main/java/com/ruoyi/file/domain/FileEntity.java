package com.ruoyi.file.domain;

import java.io.InputStream;

public class FileEntity {
    private InputStream fileInputSteam;
    private Long byteCount;
    private String filePath;

    public FileEntity() {
    }

    public InputStream getFileInputSteam() {
        return fileInputSteam;
    }

    public void setFileInputSteam(InputStream fileInputSteam) {
        this.fileInputSteam = fileInputSteam;
    }

    public Long getByteCount() {
        return byteCount;
    }

    public void setByteCount(Long byteCount) {
        this.byteCount = byteCount;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

}
