package com.srb.backend.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("resume_notice")
public class ResumeNotice implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long resumeId;

    private Integer resumeVersionNum;

    private String type;

    private String title;

    private String content;

    private Integer isRead;

    private LocalDateTime createTime;
}
