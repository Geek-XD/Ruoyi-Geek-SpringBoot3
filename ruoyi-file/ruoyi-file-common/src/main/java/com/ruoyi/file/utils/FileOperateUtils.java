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
import com.ruoyi.file.domain.FileEntity;
import com.ruoyi.file.service.FileService;

import jakarta.servlet.http.HttpServletResponse;

/**
 * 文件上传工具类
 *
 * @author ruoyi
 */
public class FileOperateUtils {

    private static FileService fileService = SpringUtils.getBean("file:strategy:" + RuoYiConfig.getFileServer());
    /**
     * 默认大小 50M
     */
    public static final long DEFAULT_MAX_SIZE = Long.valueOf(SpringUtils.getRequiredProperty("ruoyi.fileMaxSize")) * 1024 * 1024;

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
     * @param file 上传的文件
     * @return 文件路径
     * @throws Exception
     */
    public static final String upload(MultipartFile file, String[] allowedExtension) throws IOException {
        try {
            String md5 = Md5Utils.getMd5(file);
            String pathForMd5 = FileOperateUtils.getFilePathForMd5(md5);
            if (StringUtils.isNotEmpty(pathForMd5)) {
                return pathForMd5;
            }
            FileUtils.assertAllowed(file, allowedExtension);
            String pathFileName = fileService.upload(file);
            FileOperateUtils.saveFileAndMd5(pathFileName, md5);
            return pathFileName;
        } catch (Exception e) {
            throw new IOException(e.getMessage(), e);
        }
    }

    /**
     * 根据文件路径上传
     *
     * @param filePath         上传文件的路径
     * @param file             上传的文件
     * @param allowedExtension 允许的扩展名
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
     * @return 文件名称
     * @throws IOException
     */
    public static final String upload(String filePath, MultipartFile file, String[] allowedExtension)
            throws IOException {
        try {
            String md5 = Md5Utils.getMd5(file);
            String pathForMd5 = FileOperateUtils.getFilePathForMd5(md5);
            if (StringUtils.isNotEmpty(pathForMd5)) {
                return pathForMd5;
            }
            FileUtils.assertAllowed(file, allowedExtension);
            fileService.upload(filePath, file);
            FileOperateUtils.saveFileAndMd5(filePath, md5);
            return filePath;
        } catch (Exception e) {
            throw new IOException(e.getMessage(), e);
        }
    }

    /**
     * 根据文件路径上传
     *
     * @param baseDir          上传文件的根坐标
     * @param file             上传的文件
     * @param filePath         上传文件路径
     * @param allowedExtension 允许的扩展名
     * @return 文件名称
     * @throws IOException
     */
    public static final String upload(String baseDir, String filePath, MultipartFile file)
            throws IOException {
        return upload(baseDir, filePath, file, MimeTypeUtils.DEFAULT_ALLOWED_EXTENSION);
    }

    /**
     * 根据文件路径上传
     *
     * @param baseDir          上传文件的根坐标
     * @param file             上传的文件
     * @param fileName         上传文件名
     * @param allowedExtension 允许的扩展名
     * @return 文件名称
     * @throws IOException
     */
    public static final String upload(String baseDir, String filePath, MultipartFile file,
            String[] allowedExtension)
            throws IOException {
        try {
            String md5 = Md5Utils.getMd5(file);
            String pathForMd5 = FileOperateUtils.getFilePathForMd5(md5);
            if (StringUtils.isNotEmpty(pathForMd5)) {
                return pathForMd5;
            }
            FileUtils.assertAllowed(file, allowedExtension);
            fileService.upload(baseDir, filePath, file);
            FileOperateUtils.saveFileAndMd5(filePath, md5);
            return filePath;
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
    public static final void downLoad(String fileUrl, OutputStream outputStream) throws Exception {
        InputStream inputStream = fileService.downLoad(fileUrl);
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
    public static final void downLoad(String filepath, HttpServletResponse response) throws Exception {
        FileEntity fileEntity = fileService.getFile(filepath);
        InputStream inputStream = fileEntity.getFileInputSteam();
        OutputStream outputStream = response.getOutputStream();
        FileUtils.setAttachmentResponseHeader(response, FileUtils.getName(fileEntity.getFilePath()));
        response.setContentLengthLong(fileEntity.getByteCount());
        response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
        FileUtils.writeBytes(inputStream, outputStream);
    }

    /**
     * 根据文件路径删除
     *
     * @param fileUrl 下载文件路径
     * @return 是否成功
     * @throws IOException
     */
    public static final boolean deleteFile(String fileUrl) throws Exception {
        deleteFileAndMd5ByFilePath(fileUrl);
        return fileService.deleteFile(fileUrl);
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
     * 保存文件的md5
     *
     * @param path 文件的路径
     * @param md5  文件的md5
     */
    public static void saveFileAndMd5(String path, String md5) {
        CacheUtils.put(CacheConstants.FILE_MD5_PATH_KEY, md5, path);
        CacheUtils.put(CacheConstants.FILE_PATH_MD5_KEY, path, md5);
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
        String md5ByFilePath = CacheUtils.get(CacheConstants.FILE_PATH_MD5_KEY, filePath, String.class);
        if (StringUtils.isNotEmpty(md5ByFilePath)) {
            CacheUtils.remove(CacheConstants.FILE_PATH_MD5_KEY, filePath);
            CacheUtils.remove(CacheConstants.FILE_MD5_PATH_KEY, md5ByFilePath);
        }
    }
}
