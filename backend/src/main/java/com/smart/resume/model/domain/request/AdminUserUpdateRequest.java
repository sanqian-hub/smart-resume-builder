package com.smart.resume.model.domain.request;

import lombok.Data;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Data
public class AdminUserUpdateRequest {

    @NotNull(message = "id不能为空")
    @Positive(message = "id必须大于0")
    private Long id;

    private String username;

    @Email(message = "邮箱格式错误")
    private String email;

    private Integer userStatus;

    private Integer userRole;
}
