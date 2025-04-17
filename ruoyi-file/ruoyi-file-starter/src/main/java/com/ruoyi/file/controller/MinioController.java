package com.ruoyi.file.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.ruoyi.common.annotation.Anonymous;
import com.ruoyi.common.utils.file.FileUtils;
import com.ruoyi.file.minio.domain.MinioFileVO;
import com.ruoyi.file.minio.utils.MinioUtil;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;

@Tag(name = "Minio文件存储")
@RestController
@RequestMapping("/minio")
public class MinioController {

    @Anonymous
    @Operation(summary = "上传接口minio")
    @GetMapping("/{client}")
    public void downLoadFile(HttpServletResponse response,
            @PathVariable("client") String client,
            @RequestParam("fileName") String fileName) throws Exception {
        MinioFileVO file = MinioUtil.getFile(client, fileName);
        FileUtils.setAttachmentResponseHeader(response, FileUtils.getName(fileName));
        response.setContentLengthLong(file.getByteCount());
        FileUtils.writeBytes(file.getFileInputSteam(), response.getOutputStream());
    }

    @Operation(summary = "上传接口minio")
    @PutMapping("/{client}")
    public String uploadFile(
            @PathVariable("client") String client,
            @RequestBody MultipartFile file)
            throws Exception {
        return MinioUtil.uploadFile(client, file);
    }
}
