package com.geek.common.core.storage.base;

import java.net.URL;

public interface StoragePermission {

    /**
     * 生成预签名URL
     * 
     * @param filePath   文件路径
     * @param expireTime 过期时间（秒）
     * @return
     * @throws Exception
     */
    URL generatePresignedUrl(String filePath, int expireTime) throws Exception;

    /**
     * 生成公开访问URL
     *
     * @param filePath 文件路径
     * @return 公开访问URL
     */
    URL generatePublicUrl(String filePath) throws Exception;

    /**
     * 获取存储桶权限
     * 
     * @return public/private
     */
    String getPermission();

    /**
     * 获取基于权限生成的URL
     * 
     * @param filePath 文件路径
     * @return
     * @throws Exception
     */
    default URL getUrl(String filePath) throws Exception {
        if ("public".equals(getPermission())) {
            return generatePublicUrl(filePath);
        } else {
            return generatePresignedUrl(filePath, 3600);
        }
    };

}
