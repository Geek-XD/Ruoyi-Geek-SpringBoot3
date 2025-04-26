package com.ruoyi.file.storage;

import java.util.Map;

public interface StorageConfig {

    StorageBucket getBucket(String clientName);

    public Map<String, ?> getClient();

}
