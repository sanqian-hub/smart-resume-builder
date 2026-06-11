package com.srb.backend.service;

import com.srb.backend.model.dto.UserLoginRequest;
import com.srb.backend.model.dto.UserRegisterRequest;
import com.srb.backend.model.dto.UserUpdateRequest;
import com.srb.backend.model.vo.LoginUserVO;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.multipart.MultipartFile;

public interface UserService {
    long register(UserRegisterRequest request);
    LoginUserVO login(UserLoginRequest request);
    LoginUserVO getCurrentUser(HttpServletRequest request);
    void logout(HttpServletRequest request);
    LoginUserVO updateMyInfo(HttpServletRequest request, UserUpdateRequest updateRequest);
    String uploadAvatar(HttpServletRequest request, MultipartFile file);
    String uploadImage(HttpServletRequest request, MultipartFile file, String folder);
}
