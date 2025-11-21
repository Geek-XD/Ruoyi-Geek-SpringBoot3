package com.ruoyi.common.serializer;

import java.io.IOException;
import java.util.Objects;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;
import com.ruoyi.common.annotation.FilePath;
import com.ruoyi.common.core.file.service.StorageService;
import com.ruoyi.common.core.file.storage.StorageBucket;
import com.ruoyi.common.core.file.storage.StorageFactory;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.file.StorageUtils;

public class FilePathJsonSerializer extends JsonSerializer<String> implements ContextualSerializer {

    StorageService storageService;

    @Override
    public JsonSerializer<?> createContextual(SerializerProvider prov, BeanProperty property)
            throws JsonMappingException {
        FilePath annotation = property.getAnnotation(FilePath.class);
        if (Objects.nonNull(annotation) && Objects.equals(String.class, property.getType().getRawClass())) {
            String storageType = annotation.storageType();
            String storageName = annotation.storageName();
            if (StringUtils.isEmpty(storageType)) {
                storageType = StorageUtils.getPrimaryStorageType();
            }
            StorageFactory<?, ?> factory = StorageUtils.getStorageFactory(storageType);
            StorageBucket bucket;
            if (StringUtils.isEmpty(storageName)) {
                bucket = factory.getPrimaryBucket();
            } else {
                bucket = factory.getBucket(storageName);
            }
            this.storageService = new StorageService(bucket);
            return this;
        }
        return prov.findValueSerializer(property.getType(), property);
    }

    @Override
    public void serialize(String arg0, JsonGenerator arg1, SerializerProvider arg2) throws IOException {
        if (storageService != null) {
            String url;
            try {
                url = storageService.generateUrl(arg0);
                arg1.writeString(url);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            arg1.writeString(arg0);
        }
    }

}
