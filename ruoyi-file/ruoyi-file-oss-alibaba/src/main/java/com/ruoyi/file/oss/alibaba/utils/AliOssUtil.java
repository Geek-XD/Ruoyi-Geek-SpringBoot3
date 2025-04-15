package com.ruoyi.file.oss.alibaba.utils;

import java.io.IOException;
import java.net.URL;

import org.springframework.web.multipart.MultipartFile;

import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.file.FileUtils;
import com.ruoyi.common.utils.spring.SpringUtils;
import com.ruoyi.common.utils.uuid.UUID;
import com.ruoyi.file.oss.alibaba.config.AliOssConfig;
import com.ruoyi.file.oss.alibaba.domain.AliOssBucket;
import com.ruoyi.file.oss.alibaba.domain.AliOssFileVO;
import com.ruoyi.file.oss.alibaba.exception.AliOssClientErrorException;

/**
 * oss工具
 */
public class AliOssUtil {

    private static AliOssConfig aliOssConfig;

    private static AliOssConfig getAliOssConfig() {
        if (aliOssConfig == null) {
            synchronized (AliOssUtil.class) {
                if (aliOssConfig == null) {
                    aliOssConfig = SpringUtils.getBean(AliOssConfig.class);
                }
            }
        }
        return aliOssConfig;
    }

    /**
     * 文件上传
     *
     * @param client 连接名
     * @param file   上传的文件
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
     * @param client   连接名
     * @param fileName 上传文件名
     * @param file     上传的文件
     * @return 返回上传成功的文件名
     * @throws IOException 比如读写文件出错时
     */
    public static String uploadFile(String client, String fileName, MultipartFile file) throws Exception {
        getAliOssConfig().getBucket(client).put(fileName, file);
        return getURL(client, fileName);
    }

    /**
     * 获取文件URL
     *
     * @param client   连接名
     * @param filePath 文件路径
     * @return 返回上传成功的文件路径
     */
    public static String getURL(String client, String filePath) throws Exception {
        try {
            StringBuilder url = new StringBuilder();
            url.append(getAliOssConfig().getPrefix()).append("/").append(client)
                    .append("?").append("fileName=").append(filePath);
            return url.toString();
        } catch (Exception e) {
            throw new AliOssClientErrorException(e.getMessage());
        }
    }

    /**
     * 文件删除
     *
     * @param filePath 上传文件路径
     * @throws IOException 比如读写文件出错时
     */
    public static void removeFile(String filePath) throws Exception {
        getAliOssConfig().getMasterBucket().remove(filePath);
    }

    /**
     * 文件删除
     *
     * @param client   连接名
     * @param filePath 上传文件路径
     * @throws IOException 比如读写文件出错时
     */
    public static void removeFile(String client, String filePath) throws Exception {
        getAliOssConfig().getBucket(client).remove(filePath);
    }

    /**
     * 文件下载
     *
     * @param filePath 文件路径
     * @return 返回封装的Oss下载文件对象
     * @throws IOException 比如读写文件出错时
     */
    public static AliOssFileVO getFile(String filePath) throws Exception {
        return getAliOssConfig().getMasterBucket().get(filePath);
    }

    /**
     * 文件下载
     *
     * @param client   连接名
     * @param filePath 文件路径
     * @return 返回封装的Oss下载文件对象
     * @throws IOException 比如读写文件出错时
     */
    public static AliOssFileVO getFile(String client, String filePath) throws Exception {
        AliOssBucket ossBucket = getAliOssConfig().getBucket(client);
        String bucketName = getAliOssConfig().getBucketName(client);

        if (bucketName == null) {
            throw new AliOssClientErrorException("参数 \"bucketName\" 为空指针");
        }
        return ossBucket.get(filePath);
    }

    public static URL generatePresignedUrl(String filePath) {
        AliOssBucket ossBucket = getAliOssConfig().getMasterBucket();
        return ossBucket.generatePresignedUrl(filePath);
    }

    public static URL generatePresignedUrl(String client, String filePath) {
        AliOssBucket ossBucket = getAliOssConfig().getBucket(client);
        return ossBucket.generatePresignedUrl(filePath);
    }
}