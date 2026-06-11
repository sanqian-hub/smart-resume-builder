package com.srb.backend.service;

import com.srb.backend.model.entity.ResumeNotice;
import com.srb.backend.model.vo.NoticeVO;

import java.util.List;

public interface NoticeService {
    List<NoticeVO> listNotices(Long userId);
    long getUnreadCount(Long userId);
    void markRead(Long noticeId, Long userId);
    void markAllRead(Long userId);
    void analyzeAndNotify(Long resumeId, Long userId, Integer resumeVersionNum);
    void scanAndSendRecallEmails();
}
