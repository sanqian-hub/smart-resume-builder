package com.srb.backend.controller;

import com.srb.backend.common.BaseResponse;
import com.srb.backend.common.SessionUtils;
import com.srb.backend.model.dto.UserLoginRequest;
import com.srb.backend.model.dto.UserRegisterRequest;
import com.srb.backend.model.dto.UserUpdateRequest;
import com.srb.backend.model.vo.LoginUserVO;
import com.srb.backend.service.RememberLoginTokenService;
import com.srb.backend.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;
    private final RememberLoginTokenService rememberLoginTokenService;

    public UserController(UserService userService, RememberLoginTokenService rememberLoginTokenService) {
        this.userService = userService;
        this.rememberLoginTokenService = rememberLoginTokenService;
    }

    @PostMapping("/register")
    public BaseResponse<Long> register(@Valid @RequestBody UserRegisterRequest request) {
        long userId = userService.register(request);
        return BaseResponse.success(userId);
    }

    @PostMapping("/login")
    public BaseResponse<LoginUserVO> login(@Valid @RequestBody UserLoginRequest request,
                                           HttpServletRequest httpRequest,
                                           HttpServletResponse httpResponse) {
        LoginUserVO loginUserVO = userService.login(request);
        SessionUtils.setLoginUserId(httpRequest.getSession(true), loginUserVO.getId());
        if (Boolean.TRUE.equals(request.getRememberMe())) {
            rememberLoginTokenService.issueTokenForCurrentDevice(loginUserVO.getId(), httpRequest, httpResponse);
        } else {
            rememberLoginTokenService.clearCurrentDeviceToken(httpRequest, httpResponse);
        }
        return BaseResponse.success(loginUserVO);
    }

    @PostMapping("/logout")
    public BaseResponse<Void> logout(HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        userService.logout(httpRequest);
        rememberLoginTokenService.clearCurrentDeviceToken(httpRequest, httpResponse);
        return BaseResponse.success(null);
    }

    @GetMapping("/current")
    public BaseResponse<LoginUserVO> getCurrentUser(HttpServletRequest httpRequest) {
        LoginUserVO user = userService.getCurrentUser(httpRequest);
        return BaseResponse.success(user);
    }

    @PostMapping("/update")
    public BaseResponse<LoginUserVO> updateMyInfo(@RequestBody UserUpdateRequest updateRequest, HttpServletRequest httpRequest) {
        LoginUserVO user = userService.updateMyInfo(httpRequest, updateRequest);
        return BaseResponse.success(user);
    }

    @PostMapping("/upload/avatar")
    public BaseResponse<String> uploadAvatar(@RequestParam("file") MultipartFile file, HttpServletRequest httpRequest) {
        String url = userService.uploadAvatar(httpRequest, file);
        return BaseResponse.success(url);
    }

    @PostMapping("/upload/image")
    public BaseResponse<String> uploadImage(@RequestParam("file") MultipartFile file,
                                             @RequestParam(defaultValue = "portfolio") String folder,
                                             HttpServletRequest httpRequest) {
        String url = userService.uploadImage(httpRequest, file, folder);
        return BaseResponse.success(url);
    }
}
