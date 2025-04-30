package com.ruoyi.file.controller;

import java.io.OutputStream;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.ruoyi.common.annotation.Anonymous;
import com.ruoyi.common.config.RuoYiConfig;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.utils.ServletUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.file.FileUtils;
import com.ruoyi.common.utils.file.MimeTypeUtils;
import com.ruoyi.common.utils.sign.Md5Utils;
import com.ruoyi.file.domain.FileEntity;
import com.ruoyi.file.domain.SysFileInfo;
import com.ruoyi.file.service.ISysFileInfoService;
import com.ruoyi.file.storage.StorageBucket;
import com.ruoyi.file.storage.StorageConfig;
import com.ruoyi.file.storage.StorageManagement;
import com.ruoyi.file.utils.FileOperateUtils;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Tag(name = "默认文件存储")
@RestController
@RequestMapping("/file")
public class FileController {

    @Autowired
    private ISysFileInfoService sysFileInfoService;

    @Autowired
    StorageManagement storageManagement;

    private static final String FILE_DELIMETER = ",";

    /**
     * 获取完整的请求路径，包括：域名，端口，上下文访问路径
     *
     * @return 服务地址
     */
    public String getUrl() {
        HttpServletRequest request = ServletUtils.getRequest();
        return getDomain(request);
    }

    public static String getDomain(HttpServletRequest request) {
        StringBuffer url = request.getRequestURL();
        String contextPath = request.getSession().getServletContext().getContextPath();
        return url.delete(url.length() - request.getRequestURI().length(), url.length()).append(contextPath).toString();
    }

    /**
     * 获取所有可用存储渠道及其client列表
     */
    @GetMapping("/client-list")
    public AjaxResult getClientList() {
        Map<String, List<String>> result = new HashMap<>();
        Map<String, StorageConfig> configs = storageManagement.getStorageTypes();
        for (String storageType : configs.keySet()) {
            StorageConfig config = configs.get(storageType);
            result.put(storageType, new ArrayList<>(config.getClient().keySet()));
        }
        return AjaxResult.success(result);
    }

    /**
     * 通用下载请求
     *
     * @param fileName 文件名称
     * @param delete   是否删除
     */
    @Operation(summary = "通用下载请求")
    @Parameters({
            @Parameter(name = "fileName", description = "文件名称"),
            @Parameter(name = "delete", description = "是否删除")
    })
    @GetMapping("/download")
    @Anonymous
    public void fileDownload(
            @RequestParam("fileName") String fileName,
            @RequestParam(name = "delete", defaultValue = "false") Boolean delete,
            HttpServletResponse response,
            HttpServletRequest request) throws Exception {
        OutputStream outputStream = response.getOutputStream();
        try {
            if (!FileUtils.checkAllowDownload(fileName)) {
                throw new Exception(StringUtils.format("文件名称({})非法，不允许下载。 ", fileName));
            }
            String realFileName = System.currentTimeMillis() + fileName.substring(fileName.indexOf("_") + 1);
            response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
            FileUtils.setAttachmentResponseHeader(response, realFileName);
            FileOperateUtils.downLoad(fileName, outputStream);
            if (delete) {
                FileOperateUtils.deleteFile(fileName);
            }
        } catch (Exception e) {
            response.reset();
            response.setContentType(MediaType.TEXT_HTML_VALUE);
            response.setCharacterEncoding("UTF-8");
            String errorMessage = "下载文件失败: " + e.getMessage();
            outputStream.write(errorMessage.getBytes("UTF-8"));
            outputStream.flush();
        } finally {
            outputStream.close();
        }
    }

    /**
     * 通用上传请求（多个）
     */
    @Operation(summary = "通用上传请求（多个）")
    @PostMapping("/uploads")
    @Anonymous
    public AjaxResult uploadFiles(@RequestBody List<MultipartFile> files)
            throws Exception {
        try {
            // 上传文件路径
            String filePath = RuoYiConfig.getUploadPath();
            List<String> urls = new ArrayList<String>();
            List<String> fileNames = new ArrayList<String>();
            List<String> newFileNames = new ArrayList<String>();
            List<String> originalFilenames = new ArrayList<String>();
            for (MultipartFile file : files) {
                // 上传并返回新文件名称
                String fileName = FileOperateUtils.upload(filePath, file, MimeTypeUtils.DEFAULT_ALLOWED_EXTENSION);
                String url = getUrl() + fileName;
                urls.add(url);
                fileNames.add(fileName);
                newFileNames.add(FileUtils.getName(fileName));
                originalFilenames.add(file.getOriginalFilename());
            }
            AjaxResult ajax = AjaxResult.success();
            ajax.put("urls", StringUtils.join(urls, FILE_DELIMETER));
            ajax.put("fileNames", StringUtils.join(fileNames, FILE_DELIMETER));
            ajax.put("newFileNames", StringUtils.join(newFileNames, FILE_DELIMETER));
            ajax.put("originalFilenames", StringUtils.join(originalFilenames, FILE_DELIMETER));
            return ajax;
        } catch (Exception e) {
            return AjaxResult.error(e.getMessage());
        }
    }

    /**
     * 统一上传接口：/file/{storageType}/{clientName}/upload
     */
    @PostMapping("/{storageType}/{clientName}/upload")
    public AjaxResult uploadUnified(
            @PathVariable("storageType") String storageType,
            @PathVariable("clientName") String clientName,
            @RequestParam("file") MultipartFile file) {
        try {
            String md5 = Md5Utils.getMd5(file);
            String fileType = null;
            if (file.getOriginalFilename() != null && file.getOriginalFilename().contains(".")) {
                fileType = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf('.') + 1);
            }
            String filePath = "upload/" + System.currentTimeMillis() + "_" + file.getOriginalFilename();
            StorageBucket storageBucket = storageManagement.getStorageBucket(storageType, clientName);
            storageBucket.put(filePath, file);
            String url = storageBucket.generatePublicURL(filePath).toString();
            SysFileInfo info = new SysFileInfo();
            info.setFileName(file.getOriginalFilename());
            info.setFilePath(filePath);
            info.setStorageType(storageType);
            info.setFileType(fileType);
            info.setFileSize(file.getSize());
            info.setMd5(md5);
            info.setDelFlag("0");
            sysFileInfoService.insertSysFileInfo(info);
            AjaxResult ajax = AjaxResult.success();
            ajax.put("url", url);
            ajax.put("fileName", filePath);
            ajax.put("originalFilename", file.getOriginalFilename());
            ajax.put("fileId", info.getFileId());
            ajax.put("storageType", storageType);
            ajax.put("md5", md5);
            return ajax;
        } catch (Exception e) {
            return AjaxResult.error(e.getMessage());
        }
    }

    /**
     * 统一下载接口：/file/{storageType}/{clientName}/download?filePath=xxx
     */
    @GetMapping("/{storageType}/{clientName}/download")
    public void downloadUnified(
            @PathVariable("storageType") String storageType,
            @PathVariable("clientName") String clientName,
            @RequestParam("filePath") String filePath,
            HttpServletResponse response) throws Exception {
        try {
            StorageBucket storageBucket = storageManagement.getStorageBucket(storageType, clientName);
            FileEntity fileEntity = storageBucket.get(filePath);
            response.setContentType("application/octet-stream");
            response.setHeader("Content-Disposition",
                    "attachment; filename=" + URLEncoder.encode(filePath, "UTF-8"));
            IOUtils.copy(fileEntity.getFileInputSteam(), response.getOutputStream());
        } catch (Exception e) {
            response.setContentType("text/plain;charset=UTF-8");
            response.getWriter().write("下载失败: " + e.getMessage());
        }
    }

    /**
     * 统一预览接口：/file/{storageType}/{clientName}/preview?filePath=xxx
     */
    @Anonymous
    @GetMapping("/{storageType}/{clientName}/preview")
    public void preview(
            @PathVariable("storageType") String storageType,
            @PathVariable("clientName") String clientName,
            @RequestParam("filePath") String filePath,
            HttpServletResponse response) throws Exception {
        try {
            filePath = URLDecoder.decode(filePath, "UTF-8");
            StorageBucket storageBucket = storageManagement.getStorageBucket(storageType, clientName);
            FileEntity fileEntity = storageBucket.get(filePath);
            String contentType = URLConnection.guessContentTypeFromName(FileUtils.getName(filePath));
            if (contentType == null) {
                contentType = "application/octet-stream";
            }
            response.setContentType(contentType);
            IOUtils.copy(fileEntity.getFileInputSteam(), response.getOutputStream());
            response.flushBuffer();
        } catch (Exception e) {
            response.setContentType("text/plain;charset=UTF-8");
            response.getWriter().write("预览失败: " + e.getMessage());
        }
    }

    /**
     * 本地资源通用下载
     */
    @Operation(summary = "本地资源通用下载")
    @GetMapping("/resource")
    @Anonymous
    public void resourceDownload(@RequestParam String filePath,
            HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        OutputStream outputStream = response.getOutputStream();
        try {
            if (!FileUtils.checkAllowDownload(filePath)) {
                throw new Exception(StringUtils.format("资源文件({})非法，不允许下载。 ", filePath));
            }
            response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
            FileUtils.setAttachmentResponseHeader(response, filePath);
            FileOperateUtils.downLoad(filePath, outputStream);
        } catch (Exception e) {
            response.reset();
            response.setContentType(MediaType.TEXT_HTML_VALUE);
            response.setCharacterEncoding("UTF-8");
            String errorMessage = "下载文件失败: " + e.getMessage();
            outputStream.write(errorMessage.getBytes("UTF-8"));
            outputStream.flush();
        } finally {
            outputStream.close();
        }
    }
}
