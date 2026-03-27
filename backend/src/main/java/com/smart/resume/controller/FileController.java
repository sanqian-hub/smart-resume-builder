package com.smart.resume.controller;

import com.smart.resume.common.BaseResponse;
import com.smart.resume.common.ErrorCode;
import com.smart.resume.exception.BusinessException;
import com.smart.resume.model.domain.User;
import com.smart.resume.service.UserService;
import com.smart.resume.utils.CosUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.InputStream;

@RestController
@RequestMapping("/file")
public class FileController {

    @Autowired
    private UserService userService;

    @Resource
    private CosUtils cosUtils;

    @PostMapping("/upload-avatar")
    public BaseResponse<String> uploadAvatar(@RequestParam("file") MultipartFile file,
                                             HttpServletRequest request) {

        // 1. 前置校验
        if (file.isEmpty()) {
            throw new BusinessException(ErrorCode.FILE_UPLOAD_ERROR,
                    "上传的头像文件不能为空");
        }

        // 2. 上传
        try {
            InputStream inputStream = file.getInputStream();
            String url = cosUtils.uploadFile(inputStream, file.getOriginalFilename());

            // 3. 更新数据库 ⭐⭐⭐（关键）
            User loginUser = userService.getLoginUser(request);
            loginUser.setAvatarUrl(url);
            userService.updateById(loginUser);

            return BaseResponse.success(url);
        } catch (Exception e) {
            throw  new BusinessException(ErrorCode.FILE_UPLOAD_ERROR, e.getMessage());
        }
    }
}
