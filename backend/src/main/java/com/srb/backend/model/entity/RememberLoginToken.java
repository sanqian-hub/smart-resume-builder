package com.srb.backend.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("remember_login_token")
public class RememberLoginToken implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String selector;

    private String validatorHash;

    private LocalDateTime expiresAt;

    private LocalDateTime lastUsedAt;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private Integer revoked;

    private String userAgent;

    private String clientIp;
}
