package com.srb.backend.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("resume_chat")
public class ResumeChat implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long resumeId;

    private Long userId;

    private String role;

    private String content;

    private LocalDateTime createTime;
}
