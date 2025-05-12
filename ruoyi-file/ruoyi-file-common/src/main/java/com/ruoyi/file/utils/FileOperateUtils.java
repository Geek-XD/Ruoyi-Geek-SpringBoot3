package com.ruoyi.file.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;

import com.ruoyi.common.config.RuoYiConfig;
import com.ruoyi.common.constant.CacheConstants;
import com.ruoyi.common.utils.CacheUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.file.FileUtils;
import com.ruoyi.common.utils.file.MimeTypeUtils;
import com.ruoyi.common.utils.sign.Md5Utils;
import com.ruoyi.common.utils.spring.SpringUtils;
import com.ruoyi.file.storage.StorageEntity;
import com.ruoyi.file.storage.StorageService;

import jakarta.servlet.http.HttpServletResponse;

/**
 * 文件上传工具类
 *
 * @author ruoyi
 */
public class FileOperateUtils {

    private static StorageService fileService = SpringUtils.getBean("file:strategy:" + RuoYiConfig.getFileServer());

    /**
     * 以默认配置进行文件上传
     *
     * @param file 上传的文件
     * @return 文件路径
     * @throws Exception
     */
    public static final String upload(MultipartFile file) throws IOException {
        return upload(file, MimeTypeUtils.DEFAULT_ALLOWED_EXTENSION);
    }

    /**
     * 以默认配置进行文件上传
     *
     * @param file             上传的文件
     * @param allowedExtension 允许的扩展名
     * @return 文件路径
     * @throws Exception
     */
    public static final String upload(MultipartFile file, String[] allowedExtension)
            throws IOException {
        try {
            String md5 = Md5Utils.getMd5(file);
            String pathForMd5 = getFilePathForMd5(md5);
            if (StringUtils.isNotEmpty(pathForMd5)) {
                return pathForMd5;
            }
            FileUtils.assertAllowed(file, allowedExtension);
            String filePath = fileService.upload(file);
            saveFilePathAndMd5(filePath, md5);
            return filePath;
        } catch (Exception e) {
            throw new IOException(e.getMessage(), e);
        }
    }

    /**
     * 根据文件路径上传
     *
     * @param filePath 上传文件的路径
     * @param file     上传的文件
     * @return 文件名称
     * @throws IOException
     */
    public static final String upload(String filePath, MultipartFile file) throws Exception {
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
    public static final String upload(String filePath, MultipartFile file, String[] allowedExtension)
            throws IOException {
        try {
            String md5 = Md5Utils.getMd5(file);
            String pathForMd5 = getFilePathForMd5(md5);
            if (StringUtils.isNotEmpty(pathForMd5)) {
                return pathForMd5;
            }
            FileUtils.assertAllowed(file, allowedExtension);
            String url = fileService.upload(filePath, file);
            saveFilePathAndMd5(url, md5);
            return url;
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
    public static final void downLoad(String filePath, OutputStream outputStream) throws Exception {
        InputStream inputStream = fileService.downLoad(filePath);
        FileUtils.writeBytes(inputStream, outputStream);
    }

    /**
     * 根据文件路径下载
     *
     * @param filepath 下载文件路径
     * @param response 相应
     * @return 文件名称
     * @throws IOException
     */
    public static final void downLoad(String filePath, HttpServletResponse response) throws Exception {
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
     * @return 是否成功
     * @throws IOException
     */
    public static final boolean deleteFile(String filePath) throws Exception {
        deleteFileAndMd5ByFilePath(filePath);
        return fileService.deleteFile(filePath);
    }

    /**
     * 根据md5获取文件的路径
     *
     * @param md5 文件的md5
     * @return 文件路径
     */
    public static String getFilePathForMd5(String md5) {
        return CacheUtils.get(CacheConstants.FILE_MD5_PATH_KEY, md5, String.class);
    }

    /**
     * 根据md5获取文件的路径
     *
     * @param md5 文件的md5
     * @return 文件路径
     */
    public static String getMd5ForFilePath(String filePath) {
        return CacheUtils.get(CacheConstants.FILE_PATH_MD5_KEY, filePath, String.class);
    }

    /**
     * 保存文件的md5
     *
     * @param filePath 文件的路径
     * @param md5      文件的md5
     */
    public static void saveFilePathAndMd5(String filePath, String md5) {
        CacheUtils.put(CacheConstants.FILE_MD5_PATH_KEY, md5, filePath);
        CacheUtils.put(CacheConstants.FILE_PATH_MD5_KEY, filePath, md5);
    }

    /**
     * 根据md5删除文件的缓存信息
     *
     * @param md5 文件的md5
     */
    public static void deleteFileAndMd5ByMd5(String md5) {
        String filePathByMd5 = getFilePathForMd5(md5);
        if (StringUtils.isNotEmpty(filePathByMd5)) {
            CacheUtils.remove(CacheConstants.FILE_MD5_PATH_KEY, md5);
            CacheUtils.remove(CacheConstants.FILE_PATH_MD5_KEY, filePathByMd5);
        }
    }

    /**
     * 根据文件路径删除文件的缓存信息
     *
     * @param filePath 文件的路径
     */
    public static void deleteFileAndMd5ByFilePath(String filePath) {
        String md5ByFilePath = getMd5ForFilePath(filePath);
        if (StringUtils.isNotEmpty(md5ByFilePath)) {
            CacheUtils.remove(CacheConstants.FILE_PATH_MD5_KEY, filePath);
            CacheUtils.remove(CacheConstants.FILE_MD5_PATH_KEY, md5ByFilePath);
        }
    }

    /**
     * 获取文件的访问链接
     *
     * @param filePath 文件路径
     * @return 访问链接
     * @throws Exception
     */
    public static String getURL(String filePath) throws Exception {
        return fileService.generateUrl(filePath);
    }

    /**
     * 分片上传文件方法
     * 
     * @param file     待上传的文件，类型为 MultipartFile。
     * @param filePath 文件在服务器上的存储路径。
     * @param partSize 每个分片的大小，单位为字节。
     * @throws Exception 如果在上传过程中出现错误，如读写文件出错时，则抛出异常。
     */
    public static String uploadFileByMultipart(MultipartFile file, String filePath, long partSize) throws Exception {
        return fileService.uploadFileByMultipart(file, filePath, partSize);
    }
}
