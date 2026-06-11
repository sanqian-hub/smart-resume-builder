package com.srb.backend.model.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Map;

@Data
public class ResumeSelfIntroRequest {

    private Long resumeId;

    @NotNull(message = "时长不能为空")
    @Min(value = 30, message = "时长参数错误")
    @Max(value = 90, message = "时长参数错误")
    private Integer durationSeconds;

    @NotBlank(message = "风格不能为空")
    private String style;

    private String jobDescription;

    /** 前端当前各模块数据，key=模块类型，value=JSON字符串 */
    private Map<String, String> moduleData;
}
