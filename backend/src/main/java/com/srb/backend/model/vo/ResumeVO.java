package com.srb.backend.model.vo;

import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ResumeVO implements Serializable {
    private Long id;
    private String title;
    private Integer status;
    private String currentTemplate;
    private String styleConfig;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private List<ResumeContentVO> contents;

    @Data
    public static class ResumeContentVO implements Serializable {
        private Long id;
        private String moduleType;
        private String contentJson;
        private Integer sortOrder;
    }
}
