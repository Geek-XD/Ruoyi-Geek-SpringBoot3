package com.ruoyi.file.storage;

import java.util.Map;

import org.springframework.beans.factory.InitializingBean;

/** 存储管理器 */
public interface StorageManagement extends InitializingBean {
    /**
     * 获取存储桶
     *
     * @param clientName 客户端名称
     * @return 存储桶
     */
    StorageBucket getBucket(String clientName);

    /**
     * 获取存储桶
     *
     * @param clientName 客户端名称
     * @param bucketName 存储桶名称
     * @return 存储桶
     */
    public Map<String, ?> getClient();

}
