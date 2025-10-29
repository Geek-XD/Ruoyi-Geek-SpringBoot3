package com.ruoyi.file.utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;

import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.file.FileUtils;
import com.ruoyi.common.utils.file.MimeTypeUtils;
import com.ruoyi.file.service.StorageService;
import com.ruoyi.file.storage.StorageEntity;

import jakarta.servlet.http.HttpServletResponse;

/**
 * 文件操作工具类
 *
 * @author ruoyi
 */
public class FileOperateUtils {

    /**
     * 以默认配置进行文件上传
     *
     * @param file 上传的文件
     * @return 文件路径
     * @throws Exception
     */
    public static String upload(MultipartFile file) throws IOException {
        return upload(file, MimeTypeUtils.DEFAULT_ALLOWED_EXTENSION);
    }

    /**
     * 以默认配置进行文件上传
     *
     * @param file 上传的文件
     * @return 文件路径
     * @throws Exception
     */
    public static String upload(MultipartFile file, String fileName) throws Exception {
        return upload(DateUtils.datePath() + File.separator + fileName, file);
    }

    /**
     * 以默认配置进行文件上传
     *
     * @param file             上传的文件
     * @param allowedExtension 允许的扩展名
     * @return 文件路径
     * @throws Exception
     */
    public static String upload(MultipartFile file, String[] allowedExtension)
            throws IOException {
        return upload(FileUtils.fastFilePath(file), file, allowedExtension);
    }

    /**
     * 根据文件路径上传
     *
     * @param filePath 上传文件的路径
     * @param file     上传的文件
     * @return 文件名称
     * @throws IOException
     */
    public static String upload(String filePath, MultipartFile file) throws Exception {
        return upload(filePath, file, MimeTypeUtils.DEFAULT_ALLOWED_EXTENSION);
    }

    /**
     * 根据文件路径上传
     *
     * @param filePath         上传文件的路径
     * @param file             上传的文件
     * @param allowedExtension 允许的扩展名
     * @return 访问链接
     * @throws IOException
     */
    public static String upload(String filePath, MultipartFile file, String[] allowedExtension)
            throws IOException {
        try {
            StorageService fileService = new StorageService(StorageUtils.getPrimaryStorageBucket());
            fileService.setAllowedExtension(allowedExtension);
            return fileService.upload(filePath, file);
        } catch (Exception e) {
            throw new IOException(e.getMessage(), e);
        }
    }

    /**
     * 根据文件路径下载
     *
     * @param fileUrl      下载文件路径
     * @param outputStream 需要输出到的输出流
     * @return 文件名称
     * @throws IOException
     */
    public static void downLoad(String filePath, OutputStream outputStream) throws Exception {
        StorageService fileService = new StorageService(StorageUtils.getPrimaryStorageBucket());
        InputStream inputStream = fileService.downLoad(filePath);
        FileUtils.writeBytes(inputStream, outputStream);
    }

    /**
     * 根据文件路径下载
     *
     * @param filepath 下载文件路径
     * @param response 响应
     * @return 文件名称
     * @throws IOException
     */
    public static void downLoad(String filePath, HttpServletResponse response) throws Exception {
        StorageService fileService = new StorageService(StorageUtils.getPrimaryStorageBucket());
        StorageEntity fileEntity = fileService.getFile(filePath);
        InputStream inputStream = fileEntity.getInputStream();
        OutputStream outputStream = response.getOutputStream();
        FileUtils.setAttachmentResponseHeader(response, FileUtils.getName(fileEntity.getFilePath()));
        response.setContentLengthLong(fileEntity.getByteCount());
        response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
        FileUtils.writeBytes(inputStream, outputStream);
    }

    /**
     * 根据文件路径删除
     *
     * @param filePath 下载文件路径
     * @throws IOException
     */
    public static void deleteFile(String filePath) throws Exception {
        StorageService fileService = new StorageService(StorageUtils.getPrimaryStorageBucket());
        fileService.deleteFile(filePath);
    }

    /**
     * 获取文件的访问链接
     *
     * @param filePath 文件路径
     * @return 访问链接
     * @throws Exception
     */
    public static String getURL(String filePath) throws Exception {
        StorageService fileService = new StorageService(StorageUtils.getPrimaryStorageBucket());
        return fileService.generateUrl(filePath);
    }
}
