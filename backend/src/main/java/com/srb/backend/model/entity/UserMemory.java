package com.srb.backend.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("user_memory")
public class UserMemory implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String category;

    private String content;

    private String source;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
