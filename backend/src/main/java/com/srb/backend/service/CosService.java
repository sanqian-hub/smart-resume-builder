package com.srb.backend.service;

import com.qcloud.cos.COSClient;
import com.qcloud.cos.model.ObjectMetadata;
import com.srb.backend.common.BusinessException;
import com.srb.backend.common.ErrorCode;
import com.srb.backend.config.CosConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CosService {

    private final COSClient cosClient;
    private final CosConfig cosConfig;

    public String upload(MultipartFile file, String folder) {
        String originalFilename = file.getOriginalFilename();
        String ext = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            ext = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String key = folder + "/" + UUID.randomUUID() + ext;

        try {
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(file.getSize());
            metadata.setContentType(file.getContentType());
            cosClient.putObject(cosConfig.getBucket(), key, file.getInputStream(), metadata);
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "文件上传失败");
        }

        String baseUrl = "https://" + cosConfig.getBucket() + ".cos." + cosConfig.getRegion() + ".myqcloud.com/" + key;
        if ("avatar".equals(folder)) {
            baseUrl += "?imageMogr2/thumbnail/200x200/format/webp";
        }
        return baseUrl;
    }
}
