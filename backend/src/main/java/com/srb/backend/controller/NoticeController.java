package com.srb.backend.controller;

import com.srb.backend.common.BaseResponse;
import com.srb.backend.common.SessionUtils;
import com.srb.backend.model.vo.NoticeVO;
import com.srb.backend.service.NoticeService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notice")
@RequiredArgsConstructor
public class NoticeController {

    private final NoticeService noticeService;

    @GetMapping("/list")
    public BaseResponse<List<NoticeVO>> list(HttpServletRequest request) {
        Long userId = SessionUtils.getLoginUserId(request);
        return BaseResponse.success(noticeService.listNotices(userId));
    }

    @GetMapping("/unread-count")
    public BaseResponse<Long> unreadCount(HttpServletRequest request) {
        Long userId = SessionUtils.getLoginUserId(request);
        return BaseResponse.success(noticeService.getUnreadCount(userId));
    }

    @PostMapping("/read/{id}")
    public BaseResponse<Void> markRead(@PathVariable Long id, HttpServletRequest request) {
        Long userId = SessionUtils.getLoginUserId(request);
        noticeService.markRead(id, userId);
        return BaseResponse.success(null);
    }

    @PostMapping("/read-all")
    public BaseResponse<Void> markAllRead(HttpServletRequest request) {
        Long userId = SessionUtils.getLoginUserId(request);
        noticeService.markAllRead(userId);
        return BaseResponse.success(null);
    }

    @PostMapping("/analyze/{resumeId}")
    public BaseResponse<Void> analyzeAndNotify(@PathVariable Long resumeId,
                                               @RequestParam(required = false) Integer resumeVersionNum,
                                               HttpServletRequest request) {
        Long userId = SessionUtils.getLoginUserId(request);
        noticeService.analyzeAndNotify(resumeId, userId, resumeVersionNum);
        return BaseResponse.success(null);
    }
}
