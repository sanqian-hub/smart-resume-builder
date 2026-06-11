package com.srb.backend.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Map;

@Data
public class ChatRequest {

    private Long resumeId;

    @NotBlank(message = "消息内容不能为空")
    private String message;

    /** 对话模式：chat=聊一聊，modify=直接修改 */
    private String mode;

    /** 前端当前各模块数据，key=模块类型，value=JSON字符串 */
    private Map<String, String> moduleData;
}
