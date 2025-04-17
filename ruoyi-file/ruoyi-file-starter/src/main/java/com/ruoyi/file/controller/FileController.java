package com.ruoyi.file.controller;

import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
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
import com.ruoyi.file.service.DiskFileService;
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
     * 通用上传请求（单个）
     */
    @Operation(summary = "通用上传请求（单个）")
    @PostMapping("/upload")
    @Anonymous
    public AjaxResult uploadFile(@RequestBody MultipartFile file) throws Exception {
        try {
            // 上传文件路径
            // String filePath = RuoYiConfig.getUploadPath();
            // 上传并返回新文件名称
            String uri = FileOperateUtils.upload(file);
            String url = getUrl() + uri;
            AjaxResult ajax = AjaxResult.success();
            ajax.put("url", url);
            ajax.put("fileName", uri);
            ajax.put("newFileName", FileUtils.getName(uri));
            ajax.put("originalFilename", file.getOriginalFilename());
            return ajax;
        } catch (Exception e) {
            return AjaxResult.error(e.getMessage());
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
     * 本地资源通用下载
     */
    @Operation(summary = "本地资源通用下载")
    @GetMapping("/download/resource")
    @Anonymous
    public void resourceDownload(@Parameter(name = "resource", description = "资源名称") String resource,
            HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        OutputStream outputStream = response.getOutputStream();
        try {
            if (!FileUtils.checkAllowDownload(resource)) {
                throw new Exception(StringUtils.format("资源文件({})非法，不允许下载。 ", resource));
            }
            response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
            FileUtils.setAttachmentResponseHeader(response, resource);
            FileOperateUtils.downLoad(resource, outputStream);
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

    @Autowired
    private DiskFileService diskFileService;

    /**
     * 获取文件访问URL
     */
    @Operation(summary = "获取本地文件访问URL")
    @GetMapping("/getUrl")
    @Anonymous
    public AjaxResult getFileUrl(@RequestParam("filePath") String filePath) throws Exception {
        try {
            // 检查文件路径是否合法
            if (!FileUtils.checkAllowDownload(filePath)) {
                return AjaxResult.error("非法的文件路径");
            }
            // 生成访问URL
            URL url = diskFileService.generatePresignedUrl(filePath);
            return AjaxResult.success("本地磁盘URL获取成功", url.toString());
        } catch (Exception e) {
            return AjaxResult.error("获取URL失败: " + e.getMessage());
        }
    }
}
