package com.geek.common.core.storage.domain;

import java.io.InputStream;

import lombok.Data;

/** 存储实体 */
@Data
public class StorageEntity {
    private InputStream inputStream;
    private Long byteCount;
    private String filePath;
}
