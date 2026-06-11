package com.srb.backend.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("resume_version")
public class ResumeVersion implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long resumeId;

    private Long userId;

    private Integer versionNum;

    private String snapshotJson;

    private String remark;

    private LocalDateTime createTime;
}
