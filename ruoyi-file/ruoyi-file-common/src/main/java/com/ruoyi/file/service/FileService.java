package com.ruoyi.file.service;

import java.io.File;
import java.io.InputStream;
import java.net.URL;

import org.springframework.web.multipart.MultipartFile;

import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.file.FileUtils;
import com.ruoyi.file.domain.FileEntity;

/**
 * 文件操作接口
 */
public interface FileService {

    /**
     * 文件上传
     *
     * @param file 文件对象
     * @return 返回上传成功的文路径
     * @throws Exception 比如读写文件出错时
     *
     */
    default public String upload(MultipartFile file) throws Exception {
        return upload(FileUtils.fastFilePath(file), file);
    };

    /**
     * 文件上传
     *
     * @param file     上传的文件
     * @param fileName 上传文件的名称
     * @return 返回上传成功的文路径
     * @throws Exception 比如读写文件出错时
     *
     */
    default public String upload(MultipartFile file, String fileName) throws Exception {
        return upload(DateUtils.datePath() + File.separator + fileName, file);
    };

    /**
     * 文件上传
     *
     * @param filePath 上传的文件路径
     * @param file     文件对象
     * @return 返回上传成功的文路径
     * @throws Exception 比如读写文件出错时
     *
     */
    public String upload(String filePath, MultipartFile file) throws Exception;

    /**
     * 文件上传
     *
     * @param baseDir  上传文件的根坐标
     * @param filePath 文件路径
     * @param file     文件对象
     * @return 返回上传成功的文路径
     * @throws Exception 比如读写文件出错时
     *
     */
    public String upload(String baseDir, String filePath, MultipartFile file) throws Exception;

    /**
     * 文件下载
     *
     * @param filePath 下载的文件路径
     * @return 返回上传成功的文路径
     * @throws Exception 比如读写文件出错时
     *
     */
    public InputStream downLoad(String filePath) throws Exception;

    /**
     * 文件下载
     *
     * @param filePath 删除的文件路径
     * @return 返回上传成功的文路径
     * @throws Exception 比如读写文件出错时
     *
     */
    public boolean deleteFile(String filePath) throws Exception;

    /**
     * 获取文件
     * 
     * @param filePath 文件路径
     * @return 文件对象
     * @throws Exception
     */
    public FileEntity getFile(String filePath) throws Exception;

    /**
     * 生成预签名URL
     *
     * @param filePath 文件路径
     * @return 预签名URL
     * @throws Exception 如果生成过程中出现错误
     */
    public URL generatePresignedUrl(String filePath) throws Exception;
}
