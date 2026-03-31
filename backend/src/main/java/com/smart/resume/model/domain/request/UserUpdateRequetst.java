package com.smart.resume.model.domain.request;

import lombok.Data;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Data
public class UserUpdateRequetst {

    @NotBlank(message = "用户名不能为空")
    private String username;

    @Email(message = "邮箱格式错误")
    private String email;
}
