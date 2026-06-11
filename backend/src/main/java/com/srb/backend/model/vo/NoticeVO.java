package com.srb.backend.model.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class NoticeVO {
    private Long id;
    private Long userId;
    private Long resumeId;
    private Integer resumeVersionNum;
    private String type;
    private String title;
    private String content;
    private Integer isRead;
    private LocalDateTime createTime;
    private String resumeTitle;
}
