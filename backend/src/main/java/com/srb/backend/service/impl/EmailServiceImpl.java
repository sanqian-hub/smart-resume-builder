package com.srb.backend.service.impl;

import com.srb.backend.service.EmailService;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;
    private final String from;
    private final String fromName;
    private final String frontendUrl;

    public EmailServiceImpl(JavaMailSender mailSender,
                            @Value("${spring.mail.username}") String from,
                            @Value("${app.mail.from-name:SmartResume}") String fromName,
                            @Value("${app.frontend-url}") String frontendUrl) {
        this.mailSender = mailSender;
        this.from = from;
        this.fromName = fromName;
        this.frontendUrl = frontendUrl;
    }

    @Override
    public boolean send(String to, String subject, String content, Long resumeId, String resumeTitle) {
        try {
            String html = buildEmailHtml(subject, content, resumeId, resumeTitle);
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(new InternetAddress(from, fromName, "UTF-8"));
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(html, true);
            mailSender.send(message);
            log.info("邮件发送成功: {} -> {}", subject, to);
            return true;
        } catch (Exception e) {
            log.error("邮件发送失败: {} -> {}", subject, to, e);
            return false;
        }
    }

    private String buildEmailHtml(String title, String content, Long resumeId, String resumeTitle) {
        String resumeUrl = frontendUrl + "/edit/" + resumeId;
        return """
                <!DOCTYPE html>
                <html lang="zh-CN">
                <head>
                  <meta charset="UTF-8">
                  <style>
                    .notice-content ol { padding-left: 20px; margin: 8px 0; }
                    .notice-content li { margin-bottom: 6px; }
                    .notice-content p { margin: 0 0 8px; }
                  </style>
                </head>
                <body style="margin:0;padding:0;font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,sans-serif;">
                  <table width="100%%" cellpadding="0" cellspacing="0">
                    <tr><td height="100"></td></tr>
                    <tr>
                      <td align="center">
                        <table width="600" cellpadding="0" cellspacing="0" style="background:#fff;border-radius:12px;overflow:hidden;box-shadow:0 2px 12px rgba(0,0,0,0.08);">
                          <tr>
                            <td style="padding:24px 32px;border-bottom:1px solid #eee;">
                              <span style="font-size:20px;font-weight:700;color:#1a1a1a;">SmartResume</span>
                              <span style="font-size:13px;color:#999;margin-left:12px;">智能简历</span>
                            </td>
                          </tr>
                          <tr>
                            <td style="padding:28px 32px;">
                              <h2 style="font-size:18px;color:#4f46e5;margin:0 0 16px;font-weight:600;">%s</h2>
                              <div class="notice-content" style="font-size:14px;color:#333;line-height:1.8;">
                                %s
                              </div>
                              <a href="%s" target="_blank" style="display:inline-block;margin-top:24px;padding:12px 28px;background:#4f46e5;color:#fff;text-decoration:none;border-radius:8px;font-size:14px;font-weight:500;">查看简历详情 &rarr;</a>
                            </td>
                          </tr>
                          <tr>
                            <td style="padding:16px 32px;background:#fafafa;border-top:1px solid #eee;text-align:center;">
                              <p style="font-size:12px;color:#999;margin:0;">由智能简历自动生成</p>
                            </td>
                          </tr>
                        </table>
                      </td>
                    </tr>
                    <tr><td height="24"></td></tr>
                  </table>
                </body>
                </html>
                """.formatted(title, content, resumeUrl);
    }
}
