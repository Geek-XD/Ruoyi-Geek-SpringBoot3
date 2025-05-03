package com.ruoyi.file.storage;

import java.net.URL;

import org.springframework.web.multipart.MultipartFile;

public interface StorageBucket {

    /**
     * 获取文件实例
     * 
     * @param filePath
     * @return
     * @throws Exception
     */
    StorageEntity get(String filepath) throws Exception;

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
    URL generatePresignedUrl(String filePath, int expireTime) throws Exception;

    /**
     * 获取文件的公开访问方式的URL
     *
     * @param filePath 文件路径
     * @return 公开访问URL
     */
    URL generatePublicURL(String filePath) throws Exception;
}
