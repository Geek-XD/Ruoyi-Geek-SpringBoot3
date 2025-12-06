package com.geek.common.processor.serializer;

import java.io.IOException;
import java.util.Objects;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;
import com.geek.common.annotation.FilePath;
import com.geek.common.config.GeekConfig;
import com.geek.common.core.file.GeekStorageBucket;
import com.geek.common.core.file.service.StorageService;
import com.geek.common.utils.StringUtils;

public class FilePathJsonSerializer extends JsonSerializer<String> implements ContextualSerializer {

    StorageService storageService;

    @Override
    public JsonSerializer<?> createContextual(SerializerProvider prov, BeanProperty property)
            throws JsonMappingException {
        FilePath annotation = property.getAnnotation(FilePath.class);
        if (Objects.nonNull(annotation) && Objects.equals(String.class, property.getType().getRawClass())) {
            String storageName = annotation.value();
            GeekStorageBucket geekStorageBucket = GeekConfig.getGeekStorageBucket();
            if (StringUtils.isNotEmpty(storageName)) {
                this.storageService = new StorageService(geekStorageBucket.getStorageBucket(storageName));
            } else {
                this.storageService = new StorageService(geekStorageBucket);
            }
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
