package com.srb.backend.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class ResumeShareVO implements Serializable {
    private Long id;
    private Long resumeId;
    private String shareKey;
    private String sourceType;
    private Long sourceVersionId;
    private Integer sourceVersionNum;
    private Integer viewCount;
    private Integer status;
    private String password;
    private Boolean hasPassword;
    private Boolean expired;
    private LocalDateTime expireTime;
    private Integer expireDays;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
