package com.ruoyi.file.storage;

import java.util.Map;

import org.springframework.beans.factory.InitializingBean;

public interface StorageManagement extends InitializingBean {

    StorageBucket getBucket(String clientName);

    public Map<String, ?> getClient();

}
