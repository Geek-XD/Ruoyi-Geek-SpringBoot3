package com.ruoyi.file.storage;

import java.io.InputStream;

import lombok.Data;

@Data
/** 存储实体 */
public class StorageEntity {
    private InputStream inputStream;
    private Long byteCount;
    private String filePath;
}
