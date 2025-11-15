package com.ruoyi.common.core.file.service;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.ruoyi.common.constant.CacheConstants;
import com.ruoyi.common.core.file.domain.SysFilePartETag;
import com.ruoyi.common.core.file.storage.StorageBucket;
import com.ruoyi.common.core.file.storage.StorageEntity;
import com.ruoyi.common.utils.CacheUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.file.FileUtils;
import com.ruoyi.common.utils.file.MimeTypeUtils;
import com.ruoyi.common.utils.sign.Md5Utils;

import jakarta.servlet.http.HttpServletResponse;
import lombok.Getter;
import lombok.Setter;

/**
 * 存储操作业务
 */
@Getter
@Setter
public class StorageService {

    private StorageBucket storageBucket;
    private String[] allowedExtension = MimeTypeUtils.DEFAULT_ALLOWED_EXTENSION;
    private Boolean fastUpload = true;
    private Integer expireTime = 3600;
    private Long MAX_FILE_SIZE = 500 * 1024 * 1024L;

    public StorageService(StorageBucket storageBucket) {
        this.storageBucket = storageBucket;
    }

    /**
     * 上传文件（指定文件路径）
     *
     * @param filePath 指定上传文件的路径
     * @param file     上传的文件
     * @return 上传后的访问链接
     * @throws Exception 比如读写文件出错时
     *
     */
    public String upload(String filePath, MultipartFile file) throws Exception {
        FileUtils.assertAllowed(file, allowedExtension);
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("文件过大");
        }
        if (this.fastUpload) {
            String md5 = Md5Utils.getMd5(file);
            String pathForMd5 = CacheUtils.get(CacheConstants.FILE_MD5_PATH_KEY, md5, String.class);
            if (StringUtils.isNotEmpty(pathForMd5)) {
                filePath = pathForMd5;
            } else {
                this.storageBucket.put(filePath, file);
                CacheUtils.put(CacheConstants.FILE_MD5_PATH_KEY, md5, filePath);
                CacheUtils.put(CacheConstants.FILE_PATH_MD5_KEY, filePath, md5);
            }
        } else {
            this.storageBucket.put(filePath, file);
        }
        return filePath;
    }

    /**
     * 下载文件
     *
     * @param filePath 文件路径
     * @return 返回文件输入流
     * @throws Exception 比如读写文件出错时
     *
     */
    public InputStream downLoad(String filePath) throws Exception {
        return this.storageBucket.get(filePath).getInputStream();
    }

    /**
     * 根据文件路径下载
     *
     * @param fileUrl      下载文件路径
     * @param outputStream 需要输出到的输出流
     * @return 文件名称
     * @throws IOException
     */
    public void downLoad(String filePath, OutputStream outputStream) throws Exception {
        InputStream inputStream = downLoad(filePath);
        FileUtils.writeBytes(inputStream, outputStream);
    }

    /**
     * 下载文件
     *
     * @param filePath 文件路径
     * @return 返回文件输入流
     * @throws Exception 比如读写文件出错时
     *
     */
    public void downLoad(String filePath, HttpServletResponse response) throws Exception {
        StorageEntity fileEntity = this.storageBucket.get(filePath);
        InputStream inputStream = fileEntity.getInputStream();
        OutputStream outputStream = response.getOutputStream();
        FileUtils.setAttachmentResponseHeader(response, FileUtils.getName(fileEntity.getFilePath()));
        response.setContentLengthLong(fileEntity.getByteCount());
        FileUtils.writeBytes(inputStream, outputStream);
    }

    /**
     * 获取文件实体对象
     * 
     * @param filePath 文件路径
     * @return 文件对象
     * @throws Exception
     */
    public StorageEntity getFile(String filePath) throws Exception {
        return this.storageBucket.get(filePath);
    }

    /**
     * 删除文件
     *
     * @param filePath 文件路径
     * @return 返回是否删除成功
     * @throws Exception 比如读写文件出错时
     *
     */
    public void deleteFile(String filePath) throws Exception {
        this.storageBucket.remove(filePath);
        if (this.fastUpload) {
            String md5 = CacheUtils.get(CacheConstants.FILE_PATH_MD5_KEY, filePath, String.class);
            if (StringUtils.isNotEmpty(md5)) {
                CacheUtils.remove(CacheConstants.FILE_PATH_MD5_KEY, filePath);
                CacheUtils.remove(CacheConstants.FILE_MD5_PATH_KEY, md5);
            }
        }
    }

    /**
     * 生成文件访问链接
     *
     * @param filePath 文件路径
     * @return 返回文件访问链接
     * @throws Exception 比如读写文件出错时
     *
     */
    public String generateUrl(String filePath) throws Exception {
        if ("public".equals(this.storageBucket.getPermission())) {
            return this.storageBucket.generatePublicUrl(filePath).toString();
        } else {
            return this.storageBucket.generatePresignedUrl(filePath, expireTime).toString();
        }
    }

    /**
     * 初始化分片上传
     * 
     * @param filePath 文件路径
     * @return 返回uploadId
     */
    public String initMultipartUpload(String filePath, Long fileSize) throws Exception {
        if (fileSize > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("文件过大");
        }
        return this.storageBucket.initMultipartUpload(filePath);
    }

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
    public String uploadPart(String filePath, String uploadId, int partNumber, long partSize, InputStream inputStream)
            throws Exception {
        return this.storageBucket.uploadPart(filePath, uploadId, partNumber, partSize, inputStream).getETag();
    }

    /**
     * 完成分片上传
     * 
     * @param filePath  文件路径
     * @param uploadId  上传ID
     * @param partETags 分片的ETag列表
     * @return 文件的最终路径
     */
    public String completeMultipartUpload(String filePath, String uploadId, List<SysFilePartETag> partETags)
            throws Exception {
        if (partETags == null || partETags.isEmpty()) {
            throw new IllegalArgumentException("分片标识列表不能为空");
        }
        return this.storageBucket.completeMultipartUpload(filePath, uploadId, partETags);
    }

}
