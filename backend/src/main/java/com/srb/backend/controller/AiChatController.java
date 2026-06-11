package com.srb.backend.controller;

import com.srb.backend.common.BaseResponse;
import com.srb.backend.model.dto.ChatRequest;
import com.srb.backend.model.entity.ResumeChat;
import com.srb.backend.model.entity.UserMemory;
import com.srb.backend.service.AiChatService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiChatController {

    private final AiChatService aiChatService;

    /**
     * SSE 流式对话
     */
    @PostMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter chatStream(@RequestBody @Valid ChatRequest chatRequest,
                                 HttpServletRequest request) {
        SseEmitter emitter = new SseEmitter(120000L); // 2 分钟超时
        aiChatService.chatStream(
                chatRequest.getResumeId(),
                chatRequest.getMessage(),
                chatRequest.getMode(),
                chatRequest.getModuleData(),
                emitter,
                request
        );
        return emitter;
    }

    /**
     * 获取对话历史
     */
    @GetMapping("/chat/history")
    public BaseResponse<List<ResumeChat>> getChatHistory(@RequestParam Long resumeId,
                                                          HttpServletRequest request) {
        return BaseResponse.success(aiChatService.getChatHistory(resumeId, request));
    }

    /**
     * 清空对话历史
     */
    @PostMapping("/chat/clear")
    public BaseResponse<Void> clearChatHistory(@RequestParam Long resumeId,
                                                  HttpServletRequest request) {
        aiChatService.clearChatHistory(resumeId, request);
        return BaseResponse.success(null);
    }

    @GetMapping("/memory/list")
    public BaseResponse<List<UserMemory>> getMemoryList(HttpServletRequest request) {
        return BaseResponse.success(aiChatService.getMemoryList(request));
    }

    @DeleteMapping("/memory/{id}")
    public BaseResponse<Void> deleteMemory(@PathVariable Long id, HttpServletRequest request) {
        aiChatService.deleteMemory(id, request);
        return BaseResponse.success(null);
    }

    @PostMapping("/memory/clear")
    public BaseResponse<Void> clearMemory(HttpServletRequest request) {
        aiChatService.clearMemory(request);
        return BaseResponse.success(null);
    }
}
