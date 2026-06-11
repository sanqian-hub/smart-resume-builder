package com.srb.backend.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.Map;

@Data
public class MatchRequest {

    private Long resumeId;

    @NotBlank(message = "岗位描述不能为空")
    private String jobDescription;

    /** 前端当前各模块数据，key=模块类型，value=JSON字符串（resumeId 为空时使用） */
    private Map<String, String> moduleData;
}
