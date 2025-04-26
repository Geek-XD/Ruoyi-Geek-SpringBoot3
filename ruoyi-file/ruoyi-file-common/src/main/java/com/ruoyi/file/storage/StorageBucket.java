package com.ruoyi.file.storage;

import java.net.URL;

import org.springframework.web.multipart.MultipartFile;

import com.ruoyi.file.domain.FileEntity;

public interface StorageBucket {
    FileEntity get(String fileName) throws Exception;

    URL generatePresignedUrl(String fileName) throws Exception;

    void put(String fileName, MultipartFile file) throws Exception;

}
