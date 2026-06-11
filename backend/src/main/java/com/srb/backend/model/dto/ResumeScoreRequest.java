package com.srb.backend.model.dto;

import lombok.Data;

import java.util.Map;

@Data
public class ResumeScoreRequest {

    private Long resumeId;

    /** 前端当前各模块数据，key=模块类型，value=JSON字符串（resumeId 为空时使用） */
    private Map<String, String> moduleData;
}
