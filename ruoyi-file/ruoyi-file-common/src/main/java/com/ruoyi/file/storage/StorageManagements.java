package com.ruoyi.file.storage;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class StorageManagements {
    @Autowired
    private Map<String, StorageManagement> storageConfigs;

    public StorageManagement getStorageConfig(String clientName) {
        return storageConfigs.get(clientName);
    }

    public StorageBucket getStorageBucket(String storage, String clientName) {
        StorageManagement config = getStorageConfig(storage);
        if (config != null) {
            return config.getBucket(clientName);
        }
        return null;
    }

    public Map<String, StorageManagement> getStorageTypes() {
        return storageConfigs;
    }

    public void setStorageTypes(Map<String, StorageManagement> storageConfigs) {
        this.storageConfigs = storageConfigs;
    }
}
