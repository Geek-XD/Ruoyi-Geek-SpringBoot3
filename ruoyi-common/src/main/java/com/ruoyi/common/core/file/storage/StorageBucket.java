package com.ruoyi.common.core.file.storage;

import java.io.InputStream;
import java.net.URL;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.ruoyi.common.core.file.domain.SysFilePartETag;

/** 存储桶 */
public interface StorageBucket {

    /**
     * 获取文件实例
     * 
     * @param filePath
     * @return
     * @throws Exception
     */
    StorageEntity get(String filepath) throws Exception;

    /**
     * 上传文件
     * 
     * @param filePath
     * @param file
     * @throws Exception
     */
    void put(String filePath, MultipartFile file) throws Exception;

    /**
     * 删除文件
     * 
     * @param filePath
     * @throws Exception
     */
    void remove(String filePath) throws Exception;

    /**
     * 生成预签名URL
     * 
     * @param filePath
     * @param expireTime 过期时间（秒）
     * @return
     * @throws Exception
     */
    URL generatePresignedUrl(String filePath, int expireTime) throws Exception;

    /**
     * 获取文件的公开访问方式的URL
     *
     * @param filePath 文件路径
     * @return 公开访问URL
     */
    URL generatePublicUrl(String filePath) throws Exception;

    /**
     * 获取存储渠道权限
     * 
     * @return public/private
     */
    String getPermission();

    /**
     * 获取文件的默认访问方式的URL
     * 
     * @param filePath
     * @return
     * @throws Exception
     */
    default URL getUrl(String filePath) throws Exception {
        if ("public".equals(getPermission())) {
            return generatePublicUrl(filePath);
        } else {
            return generatePresignedUrl(filePath, 3600);
        }
    };

    /**
     * 初始化分片上传
     * 
     * @param filePath 文件路径
     * @return 返回uploadId
     */
    public String initMultipartUpload(String filePath) throws Exception;

    /**
     * 上传分片
     * 
     * @param filePath    文件路径
     * @param uploadId    上传ID
     * @param partNumber  分片序号
     * @param partSize    分片大小
     * @param inputStream 分片数据流
     * @return 分片的ETag
     */
    public SysFilePartETag uploadPart(String filePath, String uploadId, int partNumber, long partSize,
            InputStream inputStream)
            throws Exception;

    /**
     * 完成分片上传
     * 
     * @param filePath 文件路径
     * @param uploadId 上传ID
     * @param partETags 分片的ETag列表
     * @return 文件的最终路径
     */
    public String completeMultipartUpload(String filePath, String uploadId, List<SysFilePartETag> partETags)
            throws Exception;
}
