package com.ruoyi.file.storage;

import java.util.Set;

import org.springframework.beans.factory.InitializingBean;

/** 存储管理器 */
public interface StorageFactory extends InitializingBean {

    /**
     * 获取主存储桶
     * 
     * @return
     */
    StorageBucket getPrimaryBucket();

    /**
     * 获取存储桶
     *
     * @param clientName 客户端名称
     * @return 存储桶
     */
    StorageBucket getBucket(String clientName);

    /**
     * 获取所有储存桶的名称
     *
     * @return 存储桶名称集合
     */
    public Set<String> getBuckets();

}
