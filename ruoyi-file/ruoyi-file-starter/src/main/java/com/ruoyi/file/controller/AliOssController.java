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
import com.ruoyi.file.oss.alibaba.domain.AliOssFileVO;
import com.ruoyi.file.oss.alibaba.utils.AliOssUtil;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;

@Tag(name = "AliOss文件存储")
@RestController
@RequestMapping("/oss")
public class AliOssController {

    @Anonymous
    @Operation(summary = "下载接口oss")
    @GetMapping("/{client}")
    public void downLoadFile(HttpServletResponse response,
            @PathVariable("client") String client,
            @RequestParam("fileName") String fileName) throws Exception {
        AliOssFileVO file = AliOssUtil.getFile(client, fileName);
        FileUtils.setAttachmentResponseHeader(response, FileUtils.getName(fileName));
        response.setContentLengthLong(file.getByteCount());
        FileUtils.writeBytes(file.getFileInputSteam(), response.getOutputStream());
    }

    @Operation(summary = "上传接口oss")
    @PutMapping("/{client}")
    public String uploadFile(
            @PathVariable("client") String client,
            @RequestBody MultipartFile file)
            throws Exception {
        return AliOssUtil.uploadFile(client, file);
    }
}
