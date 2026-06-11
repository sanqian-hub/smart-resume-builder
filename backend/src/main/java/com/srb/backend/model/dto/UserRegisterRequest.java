package com.srb.backend.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserRegisterRequest {
    @NotBlank(message = "用户名不能为空")
    @Size(max = 64, message = "用户名长度不能超过 64 位")
    private String username;
    @NotBlank(message = "账号不能为空")
    @Size(min = 4, max = 64, message = "账号长度需在 4 到 64 位之间")
    private String userAccount;
    @NotBlank(message = "密码不能为空")
    @Size(min = 8, max = 128, message = "密码长度需在 8 到 128 位之间")
    private String userPassword;
    @Size(max = 20, message = "手机号长度不能超过 20 位")
    private String phone;
    private String email;
}
