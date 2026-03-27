package com.smart.resume.utils;

import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.model.ObjectMetadata;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.region.Region;
import com.smart.resume.config.CosConfig;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.InputStream;
import java.util.UUID;

@Component
public class CosUtils {

    @Resource
    private CosConfig cosConfig;

    public String uploadFile(InputStream inputStream, String originalFilename) {
        // 1. 创建客户端
        BasicCOSCredentials cred = new BasicCOSCredentials(
                cosConfig.getSecretId(), cosConfig.getSecretKey());
        ClientConfig clientConfig = new ClientConfig(new Region(cosConfig.getRegion()));
        COSClient cosClient = new COSClient(cred, clientConfig);

        try {
            // 2. 生成唯一文件名
            String key = "avatar/" + UUID.randomUUID() + "-" + originalFilename;

            // 3. 上传
            PutObjectRequest putObjectRequest = new PutObjectRequest(
                    cosConfig.getBucket(), key, inputStream, new ObjectMetadata());
            cosClient.putObject(putObjectRequest);

            // 4. 拼接访问url
            return "https://" + cosConfig.getBucket() + ".cos." + cosConfig.getRegion()
                    + ".myqcloud.com/" + key;
        } finally {
            cosClient.shutdown();
        }
    }
}
