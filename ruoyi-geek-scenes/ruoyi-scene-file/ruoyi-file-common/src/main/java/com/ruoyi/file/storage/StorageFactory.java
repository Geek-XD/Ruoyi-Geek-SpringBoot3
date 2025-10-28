package com.ruoyi.file.storage;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.InitializingBean;

/** 存储管理器 */
public abstract class StorageFactory<P, S extends StorageBucket> implements InitializingBean, BeanNameAware {
    protected Map<String, S> targetBucket = new HashMap<>();
    protected String primary;
    private Map<String, P> buckets;
    private String beanName;

    public abstract S createBucket(String name, P props);

    public abstract void validateBucket(S bucket);

    /**
     * 获取主存储桶
     * 
     * @return
     */
    public S getPrimaryBucket() {
        return this.targetBucket.get(this.primary);
    }

    /**
     * 获取存储桶
     *
     * @param bucketName 存储桶名称
     * @return 存储桶
     */
    public S getBucket(String bucketName) {
        return targetBucket.get(bucketName);
    }

    /**
     * 获取所有储存桶的名称
     *
     * @return 存储桶名称集合
     */
    public Set<String> getBuckets() {
        return buckets.keySet();
    }

    /**
     * 获取存储桶属性集合
     * 
     * @return
     */
    public Map<String, P> getProperties() {
        return buckets;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if (this.buckets == null || this.buckets.isEmpty()) {
            throw new RuntimeException(String.format("StorageFactory %s properties cannot be null or empty",
                    beanName));
        }
        this.buckets.forEach((name, props) -> {
            S bucket = createBucket(name, props);
            validateBucket(bucket);
            targetBucket.put(name, bucket);
        });

        if (targetBucket.get(primary) == null) {
            throw new RuntimeException(String.format("StorageFactory %s primary bucket %s does not exist",
                    beanName, primary));
        }
    }

    @Override
    public void setBeanName(String name) {
        this.beanName = name;
    }

    public void setPrimary(String primary) {
        this.primary = primary;
    }

    public void setBuckets(Map<String, P> buckets) {
        this.buckets = buckets;
    }
}
