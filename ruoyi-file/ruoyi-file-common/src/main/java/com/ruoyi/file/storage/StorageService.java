package com.ruoyi.file.storage;

import java.io.File;
import java.io.InputStream;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.file.FileUtils;
import com.ruoyi.file.domain.SysFilePartETag;

/**
 * 文件操作接口
 */
public interface StorageService {

    /**
     * 上传文件
     *
     * @param file 上传的文件
     * @return
     * @throws Exception 比如读写文件出错时
     *
     */
    default public String upload(MultipartFile file) throws Exception {
        return upload(FileUtils.fastFilePath(file), file);
    };

    /**
     * 上传文件（指定文件名称）
     *
     * @param file     上传的文件
     * @param fileName 指定上传文件的名称
     * @return
     * @throws Exception 比如读写文件出错时
     *
     */
    default public String upload(MultipartFile file, String fileName) throws Exception {
        return upload(DateUtils.datePath() + File.separator + fileName, file);
    };

    /**
     * 上传文件（指定文件路径）
     *
     * @param filePath 指定上传文件的路径
     * @param file     上传的文件
     * @return
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
    public boolean deleteFile(String filePath) throws Exception;

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
    public String uploadPart(String filePath, String uploadId, int partNumber, long partSize, InputStream inputStream)
            throws Exception;

    /**
     * 完成分片上传
     * @param filePath 文件路径
     * @param uploadId 上传ID
     * @return 文件的最终路径
     */
    public String completeMultipartUpload(String filePath, String uploadId, List<SysFilePartETag> partETags)
            throws Exception;
}
