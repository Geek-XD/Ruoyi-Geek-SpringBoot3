package com.ruoyi.file.minio.domain;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.concurrent.TimeUnit;

import org.springframework.web.multipart.MultipartFile;

import com.ruoyi.common.core.text.Convert;
import com.ruoyi.file.minio.exception.MinioClientErrorException;
import com.ruoyi.file.storage.StorageBucket;

import io.minio.GetObjectArgs;
import io.minio.GetObjectResponse;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.http.Method;

public class MinioBucket implements StorageBucket {

    private MinioClient client;
    private String bucketName;

    public MinioBucket() {
    }

    public MinioBucket(MinioClient client, String bucketName) {
        this.client = client;
        this.bucketName = bucketName;
    }

    public String getName() {
        return bucketName;
    }

    public MinioClient getClient() {
        return client;
    }

    public String getBucketName() {
        return bucketName;
    }

    public void setBucketName(String bucketName) {
        this.bucketName = bucketName;
    }

    public void setClient(MinioClient client) {
        this.client = client;
    }

    public void put(String fileName, MultipartFile file) throws Exception {
        put(fileName, file.getContentType(), file.getInputStream());
    }

    public void put(String filePath, String contentType, InputStream inputStream) throws Exception {
        PutObjectArgs build = PutObjectArgs.builder().contentType(contentType)
                .stream(inputStream, inputStream.available(), -1)
                .bucket(bucketName).object(filePath).build();
        put(build);
    }

    public void put(PutObjectArgs putObjectArgs) throws Exception {
        try {
            this.client.putObject(putObjectArgs);
        } catch (Exception e) {
            throw new MinioClientErrorException(e.getMessage());
        }
    }

    public void remove(String filePath) throws Exception {
        RemoveObjectArgs build = RemoveObjectArgs.builder().object(filePath).bucket(bucketName).build();
        remove(build);
    }

    public void remove(RemoveObjectArgs removeObjectArgs) throws Exception {
        this.client.removeObject(removeObjectArgs);
    }

    /**
     * 文件下载
     *
     * @param buketName Minio的桶名
     * @param filePath  文件路径
     * @return 返回封装的Minio下载文件对象
     * @throws IOException 比如读写文件出错时
     */
    public MinioFileVO get(String filePath) throws Exception {
        GetObjectArgs getObjectArgs = GetObjectArgs.builder().object(filePath).bucket(bucketName).build();
        GetObjectResponse inputStream = this.client.getObject(getObjectArgs);
        MinioFileVO minioFileVO = new MinioFileVO();

        minioFileVO.setFileInputSteam(inputStream);
        minioFileVO.setByteCount(Convert.toLong(inputStream.headers().get("Content-Length"), null));
        minioFileVO.setFilePath(filePath);
        minioFileVO.setObject(inputStream.object());
        minioFileVO.setRegion(inputStream.region());
        minioFileVO.setBuket(inputStream.bucket());
        minioFileVO.setHeaders(inputStream.headers());
        return minioFileVO;
    }

    public URL generatePresignedUrl(String filePath) throws Exception {
        GetPresignedObjectUrlArgs request = GetPresignedObjectUrlArgs.builder()
                .method(Method.GET)
                .bucket(bucketName)
                .object(filePath)
                .expiry(1, TimeUnit.HOURS) // 设置过期时间为1小时
                .build();
        String urlString = client.getPresignedObjectUrl(request);
        return URI.create(urlString).toURL();
    }
}
