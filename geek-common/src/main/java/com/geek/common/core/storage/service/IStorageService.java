package com.geek.common.core.storage.service;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.geek.common.core.storage.base.StorageBucket;
import com.geek.common.core.storage.domain.StorageEntity;
import com.geek.common.core.storage.domain.SysFilePartETag;

import jakarta.servlet.http.HttpServletResponse;

/**
 * 存储操作业务
 */
@Service
public interface IStorageService {

    public StorageBucket getStorageBucket();

    /**
     * 上传文件（指定文件路径）
     *
     * @param filePath 指定上传文件的路径
     * @param file     上传的文件
     * @return 上传后的访问链接
     * @throws Exception 比如读写文件出错时
     *
     */
    public String upload(String filePath, MultipartFile file) throws Exception;

    /**
     * 下载文件
     *
     * @param filePath 文件路径
     * @return 返回文件输入流
     * @throws Exception 比如读写文件出错时
     *
     */
    public InputStream downLoad(String filePath) throws Exception;

    /**
     * 根据文件路径下载
     *
     * @param fileUrl      下载文件路径
     * @param outputStream 需要输出到的输出流
     * @return 文件名称
     * @throws IOException
     */
    public void downLoad(String filePath, OutputStream outputStream) throws Exception;

    /**
     * 下载文件
     *
     * @param filePath 文件路径
     * @return 返回文件输入流
     * @throws Exception 比如读写文件出错时
     *
     */
    public void downLoad(String filePath, HttpServletResponse response) throws Exception;

    /**
     * 获取文件实体对象
     * 
     * @param filePath 文件路径
     * @return 文件对象
     * @throws Exception
     */
    public StorageEntity getFile(String filePath) throws Exception;

    /**
     * 删除文件
     *
     * @param filePath 文件路径
     * @return 返回是否删除成功
     * @throws Exception 比如读写文件出错时
     *
     */
    public void deleteFile(String filePath) throws Exception;

    public void clearFileCache(String filePath);

    /**
     * 生成文件访问链接
     *
     * @param filePath 文件路径
     * @return 返回文件访问链接
     * @throws Exception 比如读写文件出错时
     *
     */
    public String generateUrl(String filePath) throws Exception;

    /**
     * 初始化分片上传
     * 
     * @param filePath 文件路径
     * @return 返回uploadId
     */
    public String initMultipartUpload(String filePath, Long fileSize) throws Exception;

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
    public String uploadPart(SysFilePartETag partETag, InputStream inputStream)
            throws Exception;

    /**
     * 完成分片上传
     * 
     * @param filePath  文件路径
     * @param uploadId  上传ID
     * @param partETags 分片的ETag列表
     * @return 文件的最终路径
     */
    public String completeMultipartUpload(String filePath, String uploadId, List<SysFilePartETag> partETags)
            throws Exception;
}
