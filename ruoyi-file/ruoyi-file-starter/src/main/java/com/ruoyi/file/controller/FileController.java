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
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.file.FileUtils;
import com.ruoyi.common.utils.sign.Md5Utils;
import com.ruoyi.file.domain.SysFileInfo;
import com.ruoyi.file.service.ISysFileInfoService;
import com.ruoyi.file.storage.StorageBucket;
import com.ruoyi.file.storage.StorageEntity;
import com.ruoyi.file.storage.StorageManagement;
import com.ruoyi.file.storage.StorageManagements;
import com.ruoyi.file.utils.FileOperateUtils;

import io.swagger.v3.oas.annotations.Operation;
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
    StorageManagements storageManagement;

    /**
     * 获取所有可用存储渠道及其client列表
     */
    @GetMapping("/client-list")
    public AjaxResult getClientList() {
        Map<String, List<String>> result = new HashMap<>();
        Map<String, StorageManagement> configs = storageManagement.getStorageTypes();
        for (String storageType : configs.keySet()) {
            StorageManagement config = configs.get(storageType);
            result.put(storageType, new ArrayList<>(config.getClient().keySet()));
        }
        return AjaxResult.success(result);
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
            String url = storageBucket.getUrl(filePath).toString();
            SysFileInfo info = new SysFileInfo();
            info.setFileName(file.getOriginalFilename());
            info.setFilePath(filePath);
            info.setStorageType(storageType);
            info.setFileType(fileType);
            info.setFileSize(file.getSize());
            info.setMd5(md5);
            sysFileInfoService.insertSysFileInfo(info);
            AjaxResult ajax = AjaxResult.success();
            ajax.put("url", url);
            ajax.put("info", info);
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
            StorageEntity fileEntity = storageBucket.get(filePath);
            response.setContentType("application/octet-stream");
            response.setHeader("Content-Disposition",
                    "attachment; filename=" + URLEncoder.encode(filePath, "UTF-8"));
            IOUtils.copy(fileEntity.getInputStream(), response.getOutputStream());
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
            StorageEntity fileEntity = storageBucket.get(filePath);
            String contentType = URLConnection.guessContentTypeFromName(FileUtils.getName(filePath));
            if (contentType == null) {
                contentType = "application/octet-stream";
            }
            response.setContentType(contentType);
            IOUtils.copy(fileEntity.getInputStream(), response.getOutputStream());
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
