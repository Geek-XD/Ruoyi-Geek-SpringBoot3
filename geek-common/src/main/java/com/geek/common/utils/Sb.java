package com.geek.common.utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.poi.EmptyFileException;
import org.springframework.web.multipart.MultipartFile;

import com.geek.common.config.GeekConfig;
import com.geek.common.core.storage.base.MultipartUploadable;
import com.geek.common.core.storage.domain.StorageEntity;
import com.geek.common.core.storage.domain.SysFilePartETag;
import com.geek.common.exception.ServiceException;
import com.geek.common.utils.file.FileUtils;
import com.geek.common.utils.file.MimeTypeUtils;

import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

/**
 * 文件操作工具类
 *
 * @author geek
 */
@Slf4j
public class Sb {

    private static Long MAX_FILE_SIZE = 500 * 1024 * 1024L;

    /**
     * 以默认配置进行文件上传
     *
     * @param file 上传的文件
     * @return 文件路径
     * @throws Exception
     */
    public static String upload(MultipartFile file) {
        return upload(file, MimeTypeUtils.DEFAULT_ALLOWED_EXTENSION);
    }

    /**
     * 以默认配置进行文件上传
     *
     * @param file 上传的文件
     * @return 文件路径
     * @throws Exception
     */
    public static String upload(MultipartFile file, String fileName) {
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
    public static String upload(MultipartFile file, String[] allowedExtension) {
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
    public static String upload(String filePath, MultipartFile file) {
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
    public static String upload(String filePath, MultipartFile file, String[] allowedExtension) {
        try {
            FileUtils.assertAllowed(file, allowedExtension);
            if (file.getSize() > MAX_FILE_SIZE) {
                throw new IllegalArgumentException("文件过大");
            }
            GeekConfig.getGeekStorageBucket().put(filePath, file);
            return filePath;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
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
    public static InputStream downLoad(String filePath) {
        try {
            return GeekConfig.getGeekStorageBucket().get(filePath).getInputStream();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
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
    public static void downLoad(String filePath, OutputStream outputStream) {
        try {
            InputStream inputStream = downLoad(filePath);
            FileUtils.writeBytes(inputStream, outputStream);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
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
    public static void downLoad(String filePath, HttpServletResponse response) {
        try {
            StorageEntity fileEntity = GeekConfig.getGeekStorageBucket().get(filePath);
            InputStream inputStream = fileEntity.getInputStream();
            OutputStream outputStream = response.getOutputStream();
            FileUtils.setAttachmentResponseHeader(response, FileUtils.getName(fileEntity.getFilePath()));
            response.setContentLengthLong(fileEntity.getByteCount());
            FileUtils.writeBytes(inputStream, outputStream);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    /**
     * 根据文件路径删除
     *
     * @param filePath 下载文件路径
     * @throws IOException
     */
    public static void deleteFile(String filePath) {
        try {
            GeekConfig.getGeekStorageBucket().remove(filePath);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }

    }

    /**
     * 获取文件的访问链接
     *
     * @param filePath 文件路径
     * @return 访问链接
     * @throws Exception
     */
    public static String getURL(String filePath) {
        try {
            if (filePath == null || filePath.startsWith("http")) {
                return filePath;
            }
            if ("public".equals(GeekConfig.getGeekStorageBucket().getPermission())) {
                return GeekConfig.getGeekStorageBucket().generatePublicUrl(filePath).toString();
            } else {
                return GeekConfig.getGeekStorageBucket().generatePresignedUrl(filePath, 3600).toString();
            }
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    /**
     * 初始化文件分片上传任务
     *
     * @param filePath 文件路径
     * @param fileSize 文件大小
     * @return 任务ID
     * @throws Exception
     */
    public static String initMultipartUpload(String filePath, Long fileSize) {
        try {
            if (fileSize > MAX_FILE_SIZE) {
                throw new IllegalArgumentException("文件过大");
            }
            if (GeekConfig.getGeekStorageBucket() instanceof MultipartUploadable msb) {
                return msb.initMultipartUpload(filePath);
            } else {
                throw new UnsupportedOperationException("当前存储桶不支持分片上传");
            }
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    /**
     * 上传文件分片
     *
     * @param filePath   文件路径
     * @param uploadId   上传任务ID
     * @param partNumber 分片序号
     * @param chunk      分片文件
     * @return 分片的ETag
     * @throws Exception
     */
    public static String uploadPart(String filePath, String uploadId, int partNumber, MultipartFile chunk) {
        try {
            if (chunk == null || chunk.isEmpty())
                throw new EmptyFileException();
            SysFilePartETag partETag = SysFilePartETag.builder()
                    .partNumber(partNumber)
                    .partSize(chunk.getSize())
                    .taskId(uploadId)
                    .filePath(filePath)
                    .build();
            if (GeekConfig.getGeekStorageBucket() instanceof MultipartUploadable msb) {
                try {
                    return msb.uploadPart(
                            partETag.getFilePath(),
                            partETag.getTaskId(),
                            partETag.getPartNumber(),
                            partETag.getPartSize(),
                            chunk.getInputStream())
                            .getETag();
                } catch (Exception e) {
                    log.error("分片上传失败: 文件={}, 分片={}, 错误={}", partETag.getFilePath(), partETag.getPartNumber(), e);
                    throw new ServiceException("上传分片失败");
                }
            } else {
                throw new UnsupportedOperationException("当前存储桶不支持分片上传");
            }
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    /**
     * 合并文件分片
     *
     * @param filePath  文件路径
     * @param uploadId  上传任务ID
     * @param partETags 分片信息
     * @return 文件地址
     * @throws Exception
     */
    public static String completeMultipartUpload(String filePath, String uploadId, List<SysFilePartETag> partETags) {
        try {
            if (GeekConfig.getGeekStorageBucket() instanceof MultipartUploadable msb) {
                if (partETags == null || partETags.isEmpty()) {
                    throw new IllegalArgumentException("分片标识列表不能为空");
                }
                List<SysFilePartETag> validParts = partETags.stream()
                        .filter(part -> part != null && part.getPartNumber() != null && part.getETag() != null)
                        .peek(part -> {
                            if (part.getPartNumber() <= 0 || StringUtils.isEmpty(part.getETag())) {
                                throw new ServiceException("分片序号或ETag无效");
                            }
                        })
                        .collect(Collectors.toList());
                if (validParts.size() != partETags.size()) {
                    throw new ServiceException("分片信息格式不正确");
                }
                partETags.sort(Comparator.comparingInt(p -> p.getPartNumber()));
                String resultFilePath = msb.completeMultipartUpload(filePath, uploadId, partETags);
                log.info("分片合并完成: 文件={}, uploadId={}, 分片数={}", resultFilePath, uploadId, partETags.size());
                return resultFilePath;
            } else {
                throw new UnsupportedOperationException("当前存储桶不支持分片上传");
            }
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    private Sb() {
    }
}
