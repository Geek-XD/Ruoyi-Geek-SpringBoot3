package com.ruoyi.file.service;

import static com.ruoyi.common.utils.file.FileUtils.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Paths;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.ruoyi.common.config.RuoYiConfig;
import com.ruoyi.common.constant.Constants;
import com.ruoyi.common.utils.ServletUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.sign.Md5Utils;
import com.ruoyi.file.domain.FileEntity;
import com.ruoyi.file.utils.FileOperateUtils;

import jakarta.servlet.http.HttpServletRequest;

/**
 * 磁盘文件操作实现类
 */
@Component("file:strategy:disk")
public class DiskFileService implements FileService {

    private static final long URL_EXPIRATION = 3600 * 1000; // URL有效期1小时

    @Override
    public String upload(String filePath, MultipartFile file) throws Exception {
        String absPath = getAbsoluteFile(RuoYiConfig.getProfile() + File.separator + filePath).getAbsolutePath();
        file.transferTo(Paths.get(absPath));
        return getURL(filePath);
    }

    @Override
    public InputStream downLoad(String filePath) throws Exception {
        String normalizedPath = normalizeFilePath(filePath); // 标准化路径
        String localPath = RuoYiConfig.getProfile(); // 获取本地存储根路径
        String fullPath = localPath + File.separator + normalizedPath; // 拼接完整路径，确保分隔符正确
        File file = new File(fullPath);
        if (!file.exists()) {
            throw new FileNotFoundException("文件不存在: " + fullPath);
        }
        if (!file.isFile()) {
            throw new FileNotFoundException("不是有效的文件: " + fullPath);
        }
        return new FileInputStream(file);
    }

    @Override
    public boolean deleteFile(String filePath) throws Exception {
        String normalizedPath = normalizeFilePath(filePath); // 标准化路径
        String localPath = RuoYiConfig.getProfile(); // 获取本地存储根路径
        String fullPath = localPath + File.separator + normalizedPath; // 拼接完整路径，确保分隔符正确
        boolean flag = false;
        File file = new File(fullPath);
        // 路径为文件且不为空则进行删除
        if (file.isFile() && file.exists()) {
            String md5 = Md5Utils.getMd5(file);
            System.gc();
            flag = file.delete();
            if (flag) {
                FileOperateUtils.deleteFileAndMd5ByMd5(md5);
            }
        }
        return flag;
    }

    @Override
    public FileEntity getFile(String filePath) throws Exception {
        String localPath = RuoYiConfig.getProfile();
        String downloadPath = localPath + StringUtils.substringAfter(filePath, Constants.RESOURCE_PREFIX);
        File file = new File(downloadPath);
        if (!file.exists()) {
            throw new FileNotFoundException("未找到文件");
        }
        FileInputStream fileInputStream = new FileInputStream(file);
        FileEntity fileEntity = new FileEntity();
        fileEntity.setFileInputSteam(fileInputStream);
        fileEntity.setByteCount(file.length());
        return fileEntity;
    }

    @Override
    public URL generatePresignedUrl(String filePath) throws Exception {
        try {
            // 生成临时访问凭证
            String normalizedPath = normalizeFilePath(filePath);
            long expireTime = System.currentTimeMillis() + URL_EXPIRATION;
            String toHex = Md5Utils.hash(normalizedPath + expireTime);

            // 构建访问URL
            String urlString = getUrl() +
                    "/common/download/resource?resource=" +
                    normalizedPath +
                    "&toHex=" + toHex +
                    "&expires=" + expireTime;
            return new URL(urlString);
        } catch (Exception e) {
            throw new RuntimeException("生成访问URL失败: " + e.getMessage(), e);
        }
    }

    /**
     * 标准化文件路径
     */
    private String normalizeFilePath(String filePath) {
        if (StringUtils.isEmpty(filePath)) {
            return "";
        }
        // 统一使用正斜杠并去除前缀
        String normalizedPath = filePath.replace('\\', '/')
                .replace("ruoyi/uploadPath/", "")
                .replace("/profile/", "");
        // 去除开头和结尾的斜杠
        normalizedPath = StringUtils.strip(normalizedPath, "/");
        // 处理文件存储重复的斜杠
        normalizedPath = normalizedPath.replaceAll("/+", "/");
        return normalizedPath;
    }

    /**
     * 获取完整的请求路径，包括：域名，端口，上下文访问路径
     *
     * @return 服务地址
     */
    public static String getUrl() {
        HttpServletRequest request = ServletUtils.getRequest();
        return getDomain(request);
    }

    public static String getDomain(HttpServletRequest request) {
        StringBuffer url = request.getRequestURL();
        String contextPath = request.getSession().getServletContext().getContextPath();
        return url.delete(url.length() - request.getRequestURI().length(), url.length()).append(contextPath).toString();
    }
}
