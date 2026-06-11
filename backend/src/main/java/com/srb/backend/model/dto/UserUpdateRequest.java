package com.srb.backend.model.dto;

import lombok.Data;

@Data
public class UserUpdateRequest {
    private String username;
    private String avatarUrl;
    private Integer gender;
    private String email;
    private String phone;
    private Integer noticeEnabled;
}
