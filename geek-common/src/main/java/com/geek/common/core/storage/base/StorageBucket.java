package com.geek.common.core.storage.base;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.stream.Collectors;

import org.springframework.web.multipart.MultipartFile;

import com.geek.common.core.storage.domain.StorageEntity;

/** 存储桶 */
public interface StorageBucket extends StoragePermission {

    /**
     * 获取文件实体
     * 
     * @param filePath
     * @return
     * @throws Exception
     */
    StorageEntity get(String filepath) throws Exception;

    /**
     * 上传文件
     * 
     * @param filePath 文件路径
     * @param file     文件
     * @throws Exception
     */
    void put(String filePath, MultipartFile file) throws Exception;

    /**
     * 删除文件
     * 
     * @param filePath 文件路径
     * @throws Exception
     */
    void remove(String filePath) throws Exception;

    default String encodeUrlPath(String filePath) {
        if (filePath == null || filePath.isEmpty()) {
            return "";
        }
        return Arrays.stream(filePath.replace("\\", "/").split("/"))
                .map(segment -> URLEncoder.encode(segment, StandardCharsets.UTF_8).replace("+", "%20"))
                .collect(Collectors.joining("/"));
    }

}
