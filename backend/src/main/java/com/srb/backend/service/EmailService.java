package com.srb.backend.service;

public interface EmailService {
    boolean send(String to, String subject, String content, Long resumeId, String resumeTitle);
}
