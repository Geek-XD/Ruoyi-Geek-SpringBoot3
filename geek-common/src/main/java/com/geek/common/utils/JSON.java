package com.geek.common.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.geek.common.utils.spring.SpringUtils;

public class JSON {

    private static ObjectMapper OBJECT_MAPPER = null;

    private JSON() {
    }

    private static ObjectMapper getObjectMapper() {
        if (OBJECT_MAPPER == null) {
            OBJECT_MAPPER = SpringUtils.getBean(ObjectMapper.class);
        }
        return OBJECT_MAPPER;
    }

    public static String toJSONString(Object object) {
        return toJSONString(object, null);
    }

    public static String toJSONString(Object object, FilterProvider filterProvider) {
        try {
            if (filterProvider != null) {
                return getObjectMapper().writer(filterProvider).writeValueAsString(object);
            } else {
                return getObjectMapper().writeValueAsString(object);
            }
        } catch (Exception e) {
            throw new RuntimeException("对象转JSON字符串异常", e);
        }
    }

    public static JsonNode parseObject(String text) {
        try {
            return getObjectMapper().readTree(text);
        } catch (Exception e) {
            throw new RuntimeException("JSON字符串转JsonNode异常", e);
        }
    }

    public static <T> T parseObject(String text, Class<T> clazz) {
        try {
            return getObjectMapper().readValue(text, clazz);
        } catch (Exception e) {
            throw new RuntimeException("JSON字符串转对象异常", e);
        }
    }
}
