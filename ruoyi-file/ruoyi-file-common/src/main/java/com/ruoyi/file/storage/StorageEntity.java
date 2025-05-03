package com.ruoyi.file.storage;

import java.io.InputStream;

public class StorageEntity {
    private InputStream inputStream;
    private Long byteCount;
    private String filePath;

    public StorageEntity() {
    }

    public InputStream getInputSteam() {
        return inputStream;
    }

    public void setInputSteam(InputStream inputStream) {
        this.inputStream = inputStream;
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
