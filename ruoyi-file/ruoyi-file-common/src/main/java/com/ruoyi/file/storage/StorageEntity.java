package com.ruoyi.file.storage;

import java.io.InputStream;

import lombok.Data;

@Data
public class StorageEntity {
    private InputStream inputStream;
    private Long byteCount;
    private String filePath;
}
