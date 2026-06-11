package com.srb.backend.service;

import com.srb.backend.model.entity.ResumeChat;
import com.srb.backend.model.entity.UserMemory;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.Map;

public interface AiChatService {

    void chatStream(Long resumeId, String userMessage, String mode, Map<String, String> moduleData, SseEmitter emitter, HttpServletRequest request);

    List<ResumeChat> getChatHistory(Long resumeId, HttpServletRequest request);

    void clearChatHistory(Long resumeId, HttpServletRequest request);

    List<UserMemory> getMemoryList(HttpServletRequest request);

    void deleteMemory(Long memoryId, HttpServletRequest request);

    void clearMemory(HttpServletRequest request);
}
