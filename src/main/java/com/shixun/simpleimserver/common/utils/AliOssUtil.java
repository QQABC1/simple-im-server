package com.shixun.simpleimserver.common.utils;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.InputStream;

@Component
public class AliOssUtil {

    @Value("${aliyun.oss.endpoint}")
    private String endpoint;

    @Value("${aliyun.oss.access-key-id}")
    private String accessKeyId;

    @Value("${aliyun.oss.access-key-secret}")
    private String accessKeySecret;

    @Value("${aliyun.oss.bucket-name}")
    private String bucketName;

    /**
     * 上传文件
     * @param inputStream 文件流
     * @param objectName  OSS中的完整路径 (例如: 20231010/abc.png)
     * @return 文件的完整访问 URL
     */
    public String upload(InputStream inputStream, String objectName) {
        // 创建 OSSClient 实例
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);

        try {
            // 上传文件流
            ossClient.putObject(bucketName, objectName, inputStream);

            // 拼接返回 URL (默认是 HTTPS)
            // 格式: https://bucket-name.endpoint/object-name
            return "https://" + bucketName + "." + endpoint + "/" + objectName;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("OSS上传失败");
        } finally {
            // 关闭 OSSClient
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
    }
}