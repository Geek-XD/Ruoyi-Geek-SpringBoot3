package com.geek.common.utils;

import java.util.HashMap;
import java.util.Map;

import com.geek.common.utils.spring.SpringUtils;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ObjectNode;
import tools.jackson.databind.ser.FilterProvider;

public class JSON {

    private static JsonMapper JSON_MAPPER = null;

    private JSON() {
    }

    public static JsonMapper getJsonMapper() {
        if (JSON_MAPPER == null) {
            JSON_MAPPER = SpringUtils.getBean(JsonMapper.class);
        }
        return JSON_MAPPER;
    }

    public static String toJSONString(Object object) {
        return toJSONString(object, null);
    }

    public static String toJsonString(Object object) {
        if (object == null) {
            return null;
        }
        return toJSONString(object);
    }

    public static String toJSONString(Object object, FilterProvider filterProvider) {
        try {
            if (filterProvider != null) {
                return getJsonMapper().writer(filterProvider).writeValueAsString(object);
            } else {
                return getJsonMapper().writeValueAsString(object);
            }
        } catch (Exception e) {
            throw new RuntimeException("对象转JSON字符串异常", e);
        }
    }

    public static JsonNode parseObject(String text) {
        if (StringUtils.isEmpty(text)) {
            return null;
        }
        try {
            return getJsonMapper().readTree(text);
        } catch (Exception e) {
            throw new RuntimeException("JSON字符串转JsonNode异常", e);
        }
    }

    public static <T> T parseObject(String text, Class<T> clazz) {
        if (StringUtils.isEmpty(text)) {
            return null;
        }
        try {
            return getJsonMapper().readValue(text, clazz);
        } catch (Exception e) {
            throw new RuntimeException("JSON字符串转对象异常", e);
        }
    }

    public static <T> T fromJson(String text, Class<T> clazz) {
        return parseObject(text, clazz);
    }

    public static JsonNode parseJson(String text) {
        return parseObject(text);
    }

    public static boolean isValidJson(String text) {
        if (StringUtils.isEmpty(text)) {
            return false;
        }
        try {
            getJsonMapper().readTree(text);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static String getString(String text, String fieldName) {
        JsonNode node = parseObject(text);
        if (node != null && node.has(fieldName)) {
            return node.get(fieldName).asText();
        }
        return null;
    }

    public static Integer getInt(String text, String fieldName) {
        JsonNode node = parseObject(text);
        if (node != null && node.has(fieldName)) {
            return node.get(fieldName).asInt();
        }
        return null;
    }

    public static Long getLong(String text, String fieldName) {
        JsonNode node = parseObject(text);
        if (node != null && node.has(fieldName)) {
            return node.get(fieldName).asLong();
        }
        return null;
    }

    public static Boolean getBoolean(String text, String fieldName) {
        JsonNode node = parseObject(text);
        if (node != null && node.has(fieldName)) {
            return node.get(fieldName).asBoolean();
        }
        return null;
    }

    public static String emptyObject() {
        return "{}";
    }

    public static String emptyArray() {
        return "[]";
    }

    public static String fromMap(Map<String, Object> map) {
        return toJsonString(map);
    }

    public static Map<String, Object> toMap(String text) {
        if (StringUtils.isEmpty(text)) {
            return new HashMap<>();
        }
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> result = getJsonMapper().readValue(text, Map.class);
            return result;
        } catch (Exception e) {
            throw new RuntimeException("JSON字符串转Map异常", e);
        }
    }

    public static ObjectNode createObjectNode() {
        return getJsonMapper().createObjectNode();
    }

    public static ObjectNode createObjectNode(Map<String, ?> map) {
        return getJsonMapper().convertValue(map, ObjectNode.class);
    }
}
