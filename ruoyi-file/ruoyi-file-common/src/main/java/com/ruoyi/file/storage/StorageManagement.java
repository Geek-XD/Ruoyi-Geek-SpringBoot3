package com.ruoyi.file.storage;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class StorageManagement {
    @Autowired
    private Map<String, StorageConfig> storageConfigs;

    public StorageConfig getStorageConfig(String clientName) {
        return storageConfigs.get(clientName);
    }

    public StorageBucket getStorageBucket(String storage, String clientName) {
        StorageConfig config = getStorageConfig(storage);
        if (config != null) {
            return config.getBucket(clientName);
        }
        return null;
    }

    public Map<String, StorageConfig> getStorageTypes() {
        return storageConfigs;
    }

    public void setStorageTypes(Map<String, StorageConfig> storageConfigs) {
        this.storageConfigs = storageConfigs;
    }
}
