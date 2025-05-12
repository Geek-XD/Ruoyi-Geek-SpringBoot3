package com.ruoyi.file.storage;

import java.io.File;
import java.io.InputStream;

import org.springframework.web.multipart.MultipartFile;

import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.file.FileUtils;

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
     * 分片上传文件方法
     * 
     * @param file     待上传的文件，类型为 MultipartFile。
     * @param filePath 文件在服务器上的存储路径。
     * @param partSize 每个分片的大小，单位为字节。
     * @throws Exception 如果在上传过程中出现错误，如读写文件出错时，则抛出异常。
     */
    public String uploadFileByMultipart(MultipartFile file, String filePath, double partSize) throws Exception;
}
