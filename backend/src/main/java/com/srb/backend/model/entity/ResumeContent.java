package com.srb.backend.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("resume_content")
public class ResumeContent implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long resumeId;

    private String moduleType;

    private String contentJson;

    private Integer sortOrder;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
