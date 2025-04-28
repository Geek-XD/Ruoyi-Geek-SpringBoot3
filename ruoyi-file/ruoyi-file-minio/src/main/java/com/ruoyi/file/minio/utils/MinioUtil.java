package com.ruoyi.file.minio.utils;

import java.io.IOException;
import java.net.URL;

import org.springframework.web.multipart.MultipartFile;

import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.file.FileUtils;
import com.ruoyi.common.utils.spring.SpringUtils;
import com.ruoyi.common.utils.uuid.UUID;
import com.ruoyi.file.minio.config.MinioConfig;
import com.ruoyi.file.minio.domain.MinioBucket;
import com.ruoyi.file.minio.domain.MinioFileVO;

/**
 * Minio工具
 * 
 */
public class MinioUtil {

    private static MinioConfig minioConfig;

    private static MinioConfig getMinioConfig() {
        if (minioConfig == null) {
            synchronized (MinioUtil.class) {
                if (minioConfig == null) {
                    minioConfig = SpringUtils.getBean(MinioConfig.class);
                }
            }
        }
        return minioConfig;
    }

    /**
     * 文件上传
     *
     * @param buketName Minio的桶名
     * @param file      上传的文件
     * @return 返回上传成功的文件名
     * @throws IOException 比如读写文件出错时
     */
    public static String uploadFile(String client, MultipartFile file) throws Exception {
        String fileName = DateUtils.dateTimeNow() + UUID.fastUUID().toString().substring(0, 5) + "."
                + FileUtils.getExtension(file);
        return uploadFile(client, fileName, file);
    }

    /**
     * 文件上传
     *
     * @param buketName Minio的桶名
     * @param file      上传的文件
     * @param fileName  上传文件名
     * @return 返回上传成功的文件名
     * @throws IOException 比如读写文件出错时
     */
    public static String uploadFile(String client, String fileName, MultipartFile file) throws Exception {
        getMinioConfig().getBucket(client).put(fileName, file);
        return getURL(client, fileName);
    }

    /**
     * 文件上传
     *
     * @param filePath 文件路径
     * @return 返回上传成功的文件路径
     */
    public static String getURL(String client, String filePath) {
        StringBuilder url = new StringBuilder();
        url.append(getMinioConfig().getPrefix()).append("/").append(client)
                .append("?").append("fileName=").append(filePath);
        return url.toString();
    }

    /**
     * 文件删除
     *
     * @param filePath 上传文件路径
     * @throws IOException 比如读写文件出错时
     */
    public static void removeFile(String filePath) throws Exception {
        getMinioConfig().getPrimaryBucket().remove(filePath);
    }

    /**
     * 文件删除
     *
     * @param client   连接名
     * @param filePath 上传文件路径
     * @throws IOException 比如读写文件出错时
     */
    public static void removeFile(String client, String filePath) throws Exception {
        getMinioConfig().getBucket(client).remove(filePath);
    }

    /**
     * 文件下载
     *
     * @param filePath 文件路径
     * @return 返回封装的Minio下载文件对象
     * @throws IOException 比如读写文件出错时
     */
    public static MinioFileVO getFile(String filePath) throws Exception {
        return getMinioConfig().getPrimaryBucket().get(filePath);
    }

    /**
     * 文件下载
     *
     * @param client   连接名
     * @param filePath 文件路径
     * @return 返回封装的Minio下载文件对象
     * @throws IOException 比如读写文件出错时
     */
    public static MinioFileVO getFile(String client, String filePath) throws Exception {
        return getMinioConfig().getBucket(client).get(filePath);
    }

    public static URL generatePresignedUrl(String filePath) throws Exception {
        MinioBucket minioBucket = getMinioConfig().getPrimaryBucket();
        return minioBucket.generatePresignedUrl(filePath);
    }

    public static URL generatePresignedUrl(String client, String filePath) throws Exception {
        MinioBucket minioBucket = getMinioConfig().getBucket(client);
        return minioBucket.generatePresignedUrl(filePath);
    }

}
