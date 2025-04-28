package com.ruoyi.file.storage;

import java.net.URL;

import org.springframework.web.multipart.MultipartFile;

import com.ruoyi.file.domain.FileEntity;

public interface StorageBucket {

    /**
     * 获取文件实例
     * 
     * @param filePath
     * @return
     * @throws Exception
     */
    FileEntity get(String filepath) throws Exception;

    /**
     * 上传文件
     * 
     * @param filePath
     * @param file
     * @throws Exception
     */
    void put(String filePath, MultipartFile file) throws Exception;

    /**
     * 删除文件
     * 
     * @param filePath
     * @throws Exception
     */
    void remove(String filePath) throws Exception;

    /**
     * 生成预签名URL
     * 
     * @param filePath
     * @return
     * @throws Exception
     */
    URL generatePresignedUrl(String filePath) throws Exception;
}
