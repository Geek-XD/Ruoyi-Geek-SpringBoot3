package com.geek.common.processor.serializer;

import java.util.Objects;

import com.geek.common.annotation.FilePath;
import com.geek.common.core.storage.StorageBucketKey;
import com.geek.common.utils.Sb;

import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.BeanProperty;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueSerializer;

public class FilePathJsonSerializer extends ValueSerializer<String> {

    String storageName;

    @Override
    public ValueSerializer<?> createContextual(SerializationContext prov, BeanProperty property) {
        FilePath annotation = property.getAnnotation(FilePath.class);
        if (Objects.nonNull(annotation) && Objects.equals(String.class, property.getType().getRawClass())) {
            this.storageName = annotation.value();
            return this;
        }
        return prov.findValueSerializer(property.getType());
    }

    @Override
    public void serialize(String arg0, JsonGenerator arg1, SerializationContext arg2)
            throws JacksonException {
        if (storageName != null) {
            StorageBucketKey.use(storageName, () -> {
                arg1.writeString(Sb.getURL(arg0));
            });
        } else {
            arg1.writeString(arg0);
        }
    }

}
