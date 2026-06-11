package com.srb.backend.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("resume_share")
public class ResumeShare implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long resumeId;

    private Long userId;

    private String shareKey;

    private String password;

    private LocalDateTime expireTime;

    private Integer expireDays;

    private Integer viewCount;

    private Integer status;

    private String sourceType;

    private Long sourceVersionId;

    private Integer sourceVersionNum;

    private String snapshotJson;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    @TableLogic
    private Integer isDelete;
}
