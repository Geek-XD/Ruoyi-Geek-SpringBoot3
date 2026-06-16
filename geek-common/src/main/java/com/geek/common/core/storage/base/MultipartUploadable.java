package com.geek.common.core.storage.base;

import java.io.InputStream;
import java.util.List;

import com.geek.common.core.storage.domain.SysFilePartETag;

public interface MultipartUploadable {
    /**
     * 初始化分片上传
     * 
     * @param filePath 文件路径
     * @return 返回uploadId
     */
    public String initMultipartUpload(String filePath) throws Exception;

    /**
     * 上传分片
     * 
     * @param filePath    文件路径
     * @param uploadId    上传ID
     * @param partNumber  分片序号
     * @param partSize    分片大小
     * @param inputStream 分片数据流
     * @return 分片的ETag
     */
    public SysFilePartETag uploadPart(String filePath, String uploadId, int partNumber, long partSize,
            InputStream inputStream)
            throws Exception;

    /**
     * 完成分片上传
     * 
     * @param filePath  文件路径
     * @param uploadId  上传ID
     * @param partETags 分片的ETag列表
     * @return 文件的最终路径
     */
    public String completeMultipartUpload(String filePath, String uploadId, List<SysFilePartETag> partETags)
            throws Exception;
}
