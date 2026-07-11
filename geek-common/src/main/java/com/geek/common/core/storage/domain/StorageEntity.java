package com.geek.common.core.storage.domain;

import java.io.InputStream;

import org.springframework.core.io.InputStreamSource;

import lombok.Data;

/** 存储实体 */
@Data
public class StorageEntity implements InputStreamSource {
    private InputStream inputStream;
    private Long byteCount;
    private String filePath;
}
