package com.geek.web.controller.file;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.geek.common.core.controller.BaseController;
import com.geek.common.core.domain.AjaxResult;
import com.geek.common.core.storage.GeekStorageBucket;
import com.geek.common.core.storage.StorageBucketKey;
import com.geek.common.core.storage.domain.StorageEntity;
import com.geek.common.core.storage.domain.SysFilePartETag;
import com.geek.common.core.storage.service.StorageService;
import com.geek.common.core.text.CharsetKit;
import com.geek.common.exception.ServiceException;
import com.geek.common.utils.Sb;
import com.geek.common.utils.StringUtils;
import com.geek.common.utils.file.FileUtils;
import com.geek.common.utils.sign.Md5Utils;
import com.geek.system.domain.SysFileInfo;
import com.geek.system.service.ISysFileInfoService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Tag(name = "默认文件存储")
@RestController
@RequestMapping("/file")
public class FileController extends BaseController {

    @Autowired
    private ISysFileInfoService sysFileInfoService;

    @Autowired
    private GeekStorageBucket geekStorageBucket;

    /**
     * 获取所有可用存储渠道及其client列表
     */
    @GetMapping("/client-list")
    public AjaxResult getClientList() {
        return success(geekStorageBucket.getStorageBucketMap().keySet());
    }

    /**
     * 统一上传接口：/file/{bucketName}/upload
     */
    @PostMapping({ "/upload", "/{bucketName}/upload" })
    public AjaxResult uploadUnified(
            @PathVariable(name = "bucketName", required = false) String bucketName,
            @RequestParam(value = "useType", required = false) String useType,
            @RequestParam("file") MultipartFile file) {
        try {
            String resolvedBucketName = resolveBucketName(bucketName);
            String storageType = resolveStorageType(resolvedBucketName);
            StorageService storageService = new StorageService(geekStorageBucket);
            SysFileInfo sysFileInfo = sysFileInfoService.buildSysFileInfo(file);
            sysFileInfo.setBucketName(resolvedBucketName);
            sysFileInfo.setStorageType(storageType);
            sysFileInfo.setUseType(useType);
            String uploadPath = "upload/" + System.currentTimeMillis() + "_" + file.getOriginalFilename();
            StorageBucketKey.use(resolvedBucketName);
            try {
                sysFileInfo.setFilePath(storageService.upload(uploadPath, file));
            } finally {
                StorageBucketKey.clear();
            }
            sysFileInfoService.save(sysFileInfo);
            AjaxResult ajax = AjaxResult.success();
            ajax.put("url", StorageBucketKey.use(sysFileInfo.getBucketName(), () -> {
                try {
                    return storageService.generateUrl(sysFileInfo.getFilePath());
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }));
            ajax.put("info", sysFileInfo);
            ajax.put("fileName", sysFileInfo.getFileName());
            return ajax;
        } catch (Exception e) {
            return AjaxResult.error(e.getMessage());
        }
    }

    /**
     * 统一下载接口：/file/{storageType}/{bucketName}/download?filePath=xxx
     */
    @GetMapping({ "/download", "/{bucketName}/download" })
    public void downloadUnified(
            @PathVariable(name = "bucketName", required = false) String bucketName,
            @RequestParam("filePath") String filePath,
            HttpServletResponse response) throws IOException {
        try {
            response.setContentType("application/octet-stream");
            if (StringUtils.isEmpty(bucketName)) {
                Sb.downLoad(filePath, response);
            } else {
                StorageBucketKey.use(bucketName, () -> Sb.downLoad(filePath, response));
            }
        } catch (Exception e) {
            response.setContentType("text/plain;charset=UTF-8");
            response.getWriter().write("下载失败: " + e.getMessage());
        }
    }

    /**
     * 统一预览接口：/file/{storageType}/{bucketName}/preview?filePath=xxx
     */
    @GetMapping({ "/preview", "/{bucketName}/preview" })
    public void preview(
            @PathVariable(name = "storageType", required = false) String storageType,
            @PathVariable(name = "bucketName", required = false) String bucketName,
            @RequestParam("filePath") String filePath,
            HttpServletResponse response) throws Exception {
        try {
            if (StringUtils.isNotEmpty(bucketName)) {
                StorageBucketKey.use(bucketName);
            }
            StorageService storageService = new StorageService(geekStorageBucket);
            filePath = URLDecoder.decode(filePath, CharsetKit.UTF_8);
            try (InputStream inputStream = storageService.downLoad(filePath)) {
                String contentType = URLConnection.guessContentTypeFromName(FileUtils.getName(filePath));
                if (contentType == null) {
                    contentType = "application/octet-stream";
                }
                response.setContentType(contentType);
                IOUtils.copy(inputStream, response.getOutputStream());
                response.flushBuffer();
            }
        } catch (Exception e) {
            response.setContentType("text/plain;charset=UTF-8");
            response.getWriter().write("预览失败: " + e.getMessage());
        } finally {
            if (StorageBucketKey.get() != null) {
                StorageBucketKey.clear();
            }
        }
    }

    /**
     * 本地资源通用下载
     */
    @Operation(summary = "本地资源通用下载")
    @GetMapping("/resource")
    public void resourceDownload(
            @RequestParam String filePath,
            HttpServletRequest request,
            HttpServletResponse response) throws IOException {
        OutputStream outputStream = response.getOutputStream();
        try {
            if (!FileUtils.checkAllowDownload(filePath)) {
                throw new IllegalArgumentException(StringUtils.format("资源文件({})非法，不允许下载。 ", filePath));
            }
            response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
            FileUtils.setAttachmentResponseHeader(response, filePath);
            Sb.downLoad(filePath, outputStream);
        } catch (Exception e) {
            response.reset();
            response.setContentType(MediaType.TEXT_HTML_VALUE);
            response.setCharacterEncoding(CharsetKit.UTF_8);
            String errorMessage = "下载文件失败: " + e.getMessage();
            outputStream.write(errorMessage.getBytes(StandardCharsets.UTF_8));
            outputStream.flush();
        } finally {
            outputStream.close();
        }
    }

    /**
     * 初始化分片上传
     */
    @PostMapping("/initUpload")
    public AjaxResult initMultipartUpload(
            @RequestParam("fileName") String fileName,
            @RequestParam("fileSize") Long fileSize,
            @RequestParam(value = "bucketName", required = false) String bucketName,
            @RequestParam(value = "useType", required = false) String useType) {
        try {
            if (fileName == null || fileName.isEmpty() || fileSize == null || fileSize <= 0) {
                throw new ServiceException("文件名或文件大小不能为空");
            }
            String resolvedBucketName = StringUtils.isEmpty(bucketName)
                    ? geekStorageBucket.getDefaultStorageBucketKey()
                    : bucketName;
            String currentDate = new SimpleDateFormat("yyyy/MM/dd").format(new Date());
            String timestamp = String.valueOf(System.currentTimeMillis());
            String objectName = String.format("%s/%s/%s_%s", "/upload", currentDate, timestamp, fileName);
            String uploadId = StorageBucketKey.use(resolvedBucketName,
                    () -> Sb.initMultipartUpload(objectName, fileSize));
            return AjaxResult.success(Map.of(
                    "uploadId", uploadId,
                    "filePath", objectName,
                    "fileName", fileName,
                    "bucketName", resolvedBucketName));
        } catch (Exception e) {
            return AjaxResult.error(e.getMessage());
        }
    }

    /**
     * 上传文件分片
     */
    @PostMapping("/uploadChunk")
    public AjaxResult uploadFileChunk(
            @RequestParam("uploadId") String uploadId,
            @RequestParam("filePath") String filePath,
            @RequestParam("partNumber") int partNumber,
            @RequestParam(value = "bucketName", required = false) String bucketName,
            @RequestParam("chunk") MultipartFile chunk) {
        if (chunk == null || chunk.isEmpty())
            throw new ServiceException("分片数据不能为空");
        String resolvedBucketName = StringUtils.isEmpty(bucketName)
                ? geekStorageBucket.getDefaultStorageBucketKey()
                : bucketName;
        String etag = StorageBucketKey.use(resolvedBucketName,
                () -> Sb.uploadPart(filePath, uploadId, partNumber, chunk));
        if (etag == null || etag.isEmpty())
            throw new ServiceException("上传分片失败：未获取到ETag");
        return AjaxResult.success(Map.of(
                "etag", etag,
                "partNumber", partNumber));
    }

    /**
     * 完成分片上传并合并文件
     */
    @PostMapping("/completeUpload")
    public AjaxResult completeMultipartUpload(
            @RequestParam("uploadId") String uploadId,
            @RequestParam("filePath") String filePath,
            @RequestParam("fileSize") Long fileSize,
            @RequestParam("fileName") String fileName,
            @RequestParam(value = "bucketName", required = false) String bucketName,
            @RequestParam(value = "useType", required = false) String useType,
            @RequestBody List<SysFilePartETag> partETags) {
        try {
            String resolvedBucketName = StringUtils.isEmpty(bucketName)
                    ? geekStorageBucket.getDefaultStorageBucketKey()
                    : bucketName;
            String finalPath = StorageBucketKey.use(resolvedBucketName,
                    () -> Sb.completeMultipartUpload(filePath, uploadId, partETags));
            if (finalPath == null || finalPath.isEmpty()) {
                throw new ServiceException("合并分片失败：未获取到最终文件路径");
            }
            String storageType = resolveStorageType(resolvedBucketName);
            SysFileInfo fileInfo = sysFileInfoService.buildSysFileInfo(
                    fileName,
                    fileSize,
                    readFileMd5(resolvedBucketName, finalPath));
            fileInfo.setFilePath(finalPath);
            fileInfo.setStorageType(storageType);
            fileInfo.setBucketName(resolvedBucketName);
            fileInfo.setUseType(useType);
            sysFileInfoService.save(fileInfo);
            return AjaxResult.success(fileInfo);
        } catch (Exception e) {
            return AjaxResult.error(e.getMessage());
        }
    }

    private String resolveBucketName(String bucketName) {
        return StringUtils.isEmpty(bucketName) ? geekStorageBucket.getDefaultStorageBucketKey() : bucketName;
    }

    private String resolveStorageType(String bucketName) {
        return geekStorageBucket.getSbType(bucketName);
    }

    private String readFileMd5(String bucketName, String filePath) throws Exception {
        StorageService storageService = new StorageService(geekStorageBucket);
        StorageEntity storageEntity;
        StorageBucketKey.use(bucketName);
        try {
            storageEntity = storageService.getFile(filePath);
        } finally {
            StorageBucketKey.clear();
        }
        return Md5Utils.getMd5(storageEntity);
    }
}
