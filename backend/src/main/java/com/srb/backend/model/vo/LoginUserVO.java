package com.srb.backend.model.vo;

import lombok.Data;
import java.io.Serializable;

@Data
public class LoginUserVO implements Serializable {
    private Long id;
    private String username;
    private String userAccount;
    private String avatarUrl;
    private Integer gender;
    private String email;
    private String phone;
    private Integer noticeEnabled;
    private Integer userRole;
}
