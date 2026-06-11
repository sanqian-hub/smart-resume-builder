package com.srb.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.srb.backend.ai.DeepSeekClient;
import com.srb.backend.ai.DeepSeekMessage;
import com.srb.backend.common.BusinessException;
import com.srb.backend.common.ErrorCode;
import com.srb.backend.config.ResumeRecallProperties;
import com.srb.backend.mapper.ResumeEmailLogMapper;
import com.srb.backend.mapper.ResumeContentMapper;
import com.srb.backend.mapper.ResumeMapper;
import com.srb.backend.mapper.ResumeNoticeMapper;
import com.srb.backend.mapper.UserMapper;
import com.srb.backend.model.entity.ResumeEmailLog;
import com.srb.backend.model.entity.Resume;
import com.srb.backend.model.entity.ResumeContent;
import com.srb.backend.model.entity.ResumeNotice;
import com.srb.backend.model.entity.User;
import com.srb.backend.model.vo.NoticeVO;
import com.srb.backend.service.EmailService;
import com.srb.backend.service.NoticeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RSetCache;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.BeanUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class NoticeServiceImpl extends ServiceImpl<ResumeNoticeMapper, ResumeNotice> implements NoticeService {

    private static final String RECALL_EMAIL_TYPE = "resume_recall";
    private static final Duration ANALYSIS_HASH_TTL = Duration.ofDays(30);
    private static final Set<String> ANALYSIS_INCLUDED_MODULES = Set.of(
            "education",
            "skill",
            "experience",
            "project",
            "personalStrengths",
            "award",
            "portfolio",
            "other"
    );
    private static final Set<String> BASIC_ANALYSIS_IGNORED_FIELDS = Set.of(
            "avatar",
            "avatarAlign",
            "themeColor",
            "_richFontSize",
            "_richFontFamily",
            "_richLineHeight"
    );

    private final ResumeMapper resumeMapper;
    private final ResumeContentMapper resumeContentMapper;
    private final ResumeEmailLogMapper resumeEmailLogMapper;
    private final UserMapper userMapper;
    private final EmailService emailService;
    private final DeepSeekClient deepSeekClient;
    private final ObjectMapper objectMapper;
    private final ResumeRecallProperties resumeRecallProperties;
    private final RedissonClient redissonClient;

    @Override
    public List<NoticeVO> listNotices(Long userId) {
        QueryWrapper<ResumeNotice> wrapper = new QueryWrapper<>();
        wrapper.eq("userId", userId).orderByDesc("createTime");
        List<ResumeNotice> notices = this.baseMapper.selectList(wrapper);

        List<NoticeVO> vos = new ArrayList<>();
        for (ResumeNotice notice : notices) {
            NoticeVO vo = new NoticeVO();
            BeanUtils.copyProperties(notice, vo);
            if (notice.getResumeId() != null) {
                Resume resume = resumeMapper.selectById(notice.getResumeId());
                vo.setResumeTitle(resume != null ? resume.getTitle() : "已删除的简历");
            }
            vos.add(vo);
        }
        return vos;
    }

    @Override
    public long getUnreadCount(Long userId) {
        QueryWrapper<ResumeNotice> wrapper = new QueryWrapper<>();
        wrapper.eq("userId", userId).eq("isRead", 0);
        return this.baseMapper.selectCount(wrapper);
    }

    @Override
    public void markRead(Long noticeId, Long userId) {
        ResumeNotice notice = this.baseMapper.selectById(noticeId);
        if (notice == null || !notice.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "通知不存在");
        }
        notice.setIsRead(1);
        this.baseMapper.updateById(notice);
    }

    @Override
    public void markAllRead(Long userId) {
        QueryWrapper<ResumeNotice> wrapper = new QueryWrapper<>();
        wrapper.eq("userId", userId).eq("isRead", 0);
        List<ResumeNotice> unreadList = this.baseMapper.selectList(wrapper);
        for (ResumeNotice notice : unreadList) {
            notice.setIsRead(1);
            this.baseMapper.updateById(notice);
        }
    }

    @Override
    public void analyzeAndNotify(Long resumeId, Long userId, Integer resumeVersionNum) {
        Resume resume = resumeMapper.selectById(resumeId);
        if (resume == null || !resume.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "简历不存在");
        }

        String analysisHash = buildAnalysisHash(resume);
        if (analysisHash == null || analysisHash.isBlank()) {
            return;
        }
        String hashKey = buildAnalysisHashKey(resumeId);
        Boolean analyzed = null;
        try {
            analyzed = getAnalysisHashSet(hashKey).contains(analysisHash);
        } catch (Throwable e) {
            log.warn("检查简历分析 hash 去重失败，将继续执行分析，resumeId={}, versionNum={}", resumeId, resumeVersionNum, e);
        }
        if (Boolean.TRUE.equals(analyzed)) {
            log.info("跳过重复简历分析，resumeId={}, versionNum={}, hash={}", resumeId, resumeVersionNum, analysisHash);
            return;
        }

        AnalysisResult analysisResult = analyzeResume(resume);
        if (analysisResult == null) {
            return;
        }

        // 生成完整度检查通知
        if (analysisResult.completenessContent() != null && !analysisResult.completenessContent().isBlank()) {
            ResumeNotice notice = new ResumeNotice();
            notice.setUserId(userId);
            notice.setResumeId(resumeId);
            notice.setResumeVersionNum(resumeVersionNum);
            notice.setType("completeness_check");
            notice.setTitle("完整度检查");
            notice.setContent(analysisResult.completenessContent());
            notice.setIsRead(0);
            this.baseMapper.insert(notice);
        }

        // 生成优化建议通知
        if (analysisResult.optimizeContent() != null && !analysisResult.optimizeContent().isBlank()) {
            ResumeNotice notice = new ResumeNotice();
            notice.setUserId(userId);
            notice.setResumeId(resumeId);
            notice.setResumeVersionNum(resumeVersionNum);
            notice.setType("optimize_suggest");
            notice.setTitle("优化建议");
            notice.setContent(analysisResult.optimizeContent());
            notice.setIsRead(0);
            this.baseMapper.insert(notice);
        }

        try {
            getAnalysisHashSet(hashKey).add(analysisHash, ANALYSIS_HASH_TTL.getSeconds(), TimeUnit.SECONDS);
        } catch (Throwable e) {
            log.warn("写入简历分析 hash 去重缓存失败，不影响本次通知结果，resumeId={}, versionNum={}", resumeId, resumeVersionNum, e);
        }
    }

    @Override
    public void scanAndSendRecallEmails() {
        if (!resumeRecallProperties.isEnabled()) {
            return;
        }

        QueryWrapper<User> userWrapper = new QueryWrapper<>();
        userWrapper.eq("noticeEnabled", 1).isNotNull("email").ne("email", "");
        List<User> users = userMapper.selectList(userWrapper);
        if (users.isEmpty()) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime staleDeadline = now.minusMinutes(resumeRecallProperties.getStaleThresholdMinutes());
        LocalDateTime cooldownDeadline = now.minusMinutes(resumeRecallProperties.getCooldownMinutes());

        for (User user : users) {
            try {
                handleRecallForUser(user, staleDeadline, cooldownDeadline, now);
            } catch (Exception e) {
                log.error("处理简历召回邮件失败，userId={}", user.getId(), e);
            }
        }
    }

    private void handleRecallForUser(User user,
                                     LocalDateTime staleDeadline,
                                     LocalDateTime cooldownDeadline,
                                     LocalDateTime now) {
        Resume latestResume = findLatestResume(user.getId());
        if (latestResume == null || latestResume.getUpdateTime() == null) {
            return;
        }
        if (latestResume.getUpdateTime().isAfter(staleDeadline)) {
            return;
        }
        if (hasRecentRecallEmail(user.getId(), cooldownDeadline)) {
            return;
        }

        AnalysisResult analysisResult = analyzeResume(latestResume);
        if (analysisResult == null) {
            return;
        }

        String email = user.getEmail();
        String resumeTitle = latestResume.getTitle();
        Long resumeId = latestResume.getId();
        String subject = "[智能简历] 优化建议 - " + resumeTitle;
        String mergedContent = buildRecallEmailContent(analysisResult);
        boolean sent = emailService.send(email, subject, mergedContent, resumeId, resumeTitle);

        if (sent || mergedContent != null) {
            ResumeEmailLog logEntity = new ResumeEmailLog();
            logEntity.setUserId(user.getId());
            logEntity.setResumeId(resumeId);
            logEntity.setType(RECALL_EMAIL_TYPE);
            logEntity.setSubject(subject);
            logEntity.setStatus(sent ? 1 : 0);
            logEntity.setErrorMsg(sent ? null : "简历召回邮件发送失败");
            logEntity.setCreateTime(now);
            resumeEmailLogMapper.insert(logEntity);
        }
    }

    private Resume findLatestResume(Long userId) {
        QueryWrapper<Resume> wrapper = new QueryWrapper<>();
        wrapper.eq("userId", userId).orderByDesc("updateTime").last("LIMIT 1");
        return resumeMapper.selectOne(wrapper);
    }

    private boolean hasRecentRecallEmail(Long userId, LocalDateTime cooldownDeadline) {
        QueryWrapper<ResumeEmailLog> wrapper = new QueryWrapper<>();
        wrapper.eq("userId", userId)
                .eq("type", RECALL_EMAIL_TYPE)
                .eq("status", 1)
                .ge("createTime", cooldownDeadline)
                .last("LIMIT 1");
        return resumeEmailLogMapper.selectOne(wrapper) != null;
    }

    private AnalysisResult analyzeResume(Resume resume) {
        String resumeText = buildResumeText(resume.getId());
        if (resumeText.isBlank()) {
            return null;
        }
        Map<String, String> aiResult = callAiAnalysis(resumeText);
        if (aiResult == null) {
            return null;
        }
        return new AnalysisResult(aiResult.get("completenessContent"), aiResult.get("optimizeContent"));
    }

    private String buildAnalysisHashKey(Long resumeId) {
        return "resume:analysis:hashes:" + resumeId;
    }

    private RSetCache<String> getAnalysisHashSet(String hashKey) {
        var keyType = redissonClient.getKeys().getType(hashKey);
        String typeName = keyType == null ? "" : String.valueOf(keyType);
        if (!typeName.isBlank() && !"ZSET".equals(typeName) && !"NONE".equals(typeName)) {
            redissonClient.getKeys().delete(hashKey);
        }
        return redissonClient.getSetCache(hashKey);
    }

    private String buildAnalysisHash(Resume resume) {
        try {
            QueryWrapper<ResumeContent> wrapper = new QueryWrapper<>();
            wrapper.eq("resumeId", resume.getId()).orderByAsc("sortOrder");
            List<ResumeContent> contents = resumeContentMapper.selectList(wrapper).stream()
                    .filter(c -> ANALYSIS_INCLUDED_MODULES.contains(c.getModuleType()))
                    .toList();

            List<Map<String, Object>> contentList = contents.stream().map(c -> {
                Map<String, Object> map = new java.util.LinkedHashMap<>();
                map.put("moduleType", c.getModuleType());
                map.put("contentText", buildAnalysisContentText(c));
                map.put("sortOrder", c.getSortOrder());
                return map;
            }).toList();

            Map<String, Object> snapshot = new java.util.LinkedHashMap<>();
            snapshot.put("contents", contentList);

            String snapshotJson = objectMapper.writeValueAsString(snapshot);
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(snapshotJson.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(bytes.length * 2);
            for (byte b : bytes) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (Exception e) {
            log.warn("构建简历分析 hash 失败，resumeId={}", resume.getId(), e);
            return null;
        }
    }

    private String buildRecallEmailContent(AnalysisResult analysisResult) {
        String completenessContent = analysisResult.completenessContent();
        String optimizeContent = analysisResult.optimizeContent();
        if ((completenessContent == null || completenessContent.isBlank())
                && (optimizeContent == null || optimizeContent.isBlank())) {
            return null;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("<p>这份简历已经有一段时间没有更新了，我们结合当前内容，为您整理了一份完整度检查和优化建议，供您参考。</p>");

        if (completenessContent != null && !completenessContent.isBlank()) {
            sb.append("<h3 style=\"font-size:18px;color:#4f46e5;margin:18px 0 10px;font-weight:600;\">完整度检查</h3>");
            sb.append(completenessContent);
        }

        if (optimizeContent != null && !optimizeContent.isBlank()) {
            sb.append("<h3 style=\"font-size:18px;color:#4f46e5;margin:18px 0 10px;font-weight:600;\">优化建议</h3>");
            sb.append(optimizeContent);
        }

        return sb.toString();
    }

    private String buildResumeText(Long resumeId) {
        QueryWrapper<ResumeContent> wrapper = new QueryWrapper<>();
        wrapper.eq("resumeId", resumeId).orderByAsc("sortOrder");
        List<ResumeContent> contents = resumeContentMapper.selectList(wrapper).stream()
                .filter(c -> ANALYSIS_INCLUDED_MODULES.contains(c.getModuleType()))
                .toList();

        if (contents.isEmpty()) return "";

        Map<String, String> moduleLabels = Map.of(
                "basic", "基本信息", "education", "教育经历", "experience", "工作经历",
                "project", "项目经历", "skill", "专业技能", "personalStrengths", "个人优势",
                "award", "荣誉奖项", "portfolio", "作品集", "other", "其他经历"
        );

        StringBuilder sb = new StringBuilder();
        for (ResumeContent c : contents) {
            String label = moduleLabels.getOrDefault(c.getModuleType(), c.getModuleType());
            String text = buildAnalysisContentText(c);
            sb.append("【").append(label).append("】\n").append(text).append("\n\n");
        }
        return sb.toString();
    }

    private String buildAnalysisContentText(ResumeContent content) {
        if (content == null) {
            return "(空)";
        }
        Set<String> ignoredFields = "basic".equals(content.getModuleType())
                ? BASIC_ANALYSIS_IGNORED_FIELDS
                : Set.of();
        return stripHtml(content.getContentJson(), ignoredFields);
    }

    private String stripHtml(String json) {
        return stripHtml(json, Set.of());
    }

    private String stripHtml(String json, Set<String> ignoredFields) {
        if (json == null || json.isBlank()) return "(空)";
        try {
            StringBuilder sb = new StringBuilder();
            Object node = objectMapper.readValue(json, Object.class);
            appendNodeValues(sb, node, ignoredFields);
            String text = sb.toString().trim();
            return text.isBlank() ? "(空)" : text;
        } catch (Exception e) {
            return json.replaceAll("<[^>]+>", " ").replaceAll("\\s+", " ").trim();
        }
    }

    @SuppressWarnings("unchecked")
    private void appendNodeValues(StringBuilder sb, Object node, Set<String> ignoredFields) {
        if (node == null) {
            return;
        }
        if (node instanceof Map<?, ?> map) {
            appendMapValues(sb, (Map<String, Object>) map, ignoredFields);
            return;
        }
        if (node instanceof List<?> list) {
            for (Object item : list) {
                appendNodeValues(sb, item, ignoredFields);
            }
            return;
        }
        String text = node.toString().replaceAll("<[^>]+>", " ").replaceAll("\\s+", " ").trim();
        if (!text.isEmpty() && !text.equals("{}") && !text.equals("[]")) {
            sb.append(text).append("\n");
        }
    }

    @SuppressWarnings("unchecked")
    private void appendMapValues(StringBuilder sb, Map<String, Object> map, Set<String> ignoredFields) {
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            if (value == null || ignoredFields.contains(key)) continue;
            if (value instanceof Map) {
                sb.append(key).append(": ");
                appendMapValues(sb, (Map<String, Object>) value, ignoredFields);
                sb.append("\n");
            } else if (value instanceof List) {
                for (Object item : (List<?>) value) {
                    appendNodeValues(sb, item, ignoredFields);
                }
            } else {
                String text = value.toString().replaceAll("<[^>]+>", " ").replaceAll("\\s+", " ").trim();
                if (!text.isEmpty() && !text.equals("{}") && !text.equals("[]")) {
                    sb.append(key).append(": ").append(text).append("\n");
                }
            }
        }
    }

    private Map<String, String> callAiAnalysis(String resumeText) {
        String systemPrompt = """
                你是一个专业的简历顾问。请分析用户简历，按要求输出结果。

                严格按以下 JSON 格式输出，不要输出任何其他内容：
                {
                  "completenessContent": "<p>总体说明</p><ol><li>要点1</li><li>要点2</li></ol>",
                  "optimizeContent": "<p>总体说明</p><ol><li>建议1</li><li>建议2</li></ol>"
                }

                任务要求：
                1. 完整度检查：先用一句<p>概述整体情况，再用<ol><li>逐条列出缺失或过于简单的模块。共200字以内。
                2. 优化建议：先用一句<p>概述优化方向，再用<ol><li>逐条列出具体改进建议（措辞、量化成果、技术栈补充等）。每条建议控制在30字左右，至少列出3条。共300字以内。
                3. 重点：<ol><li>列表必须使用 HTML 标签，便于前端渲染，不要用 Markdown。
                """;

        String userPrompt = "简历内容：\n" + resumeText;

        try {
            List<DeepSeekMessage> messages = List.of(
                    DeepSeekMessage.system(systemPrompt),
                    DeepSeekMessage.user(userPrompt)
            );
            String aiResponse = callDeepSeek(messages, 1000);
            String json = aiResponse.trim();
            if (json.startsWith("```")) {
                json = json.replaceAll("^```\\w*\\n?", "").replaceAll("\\n?```$", "");
            }
            return objectMapper.readValue(json, Map.class);
        } catch (Exception e) {
            log.error("AI 通知分析失败", e);
            return null;
        }
    }

    private record AnalysisResult(String completenessContent, String optimizeContent) {
    }

    private String callDeepSeek(List<DeepSeekMessage> messages, int maxTokens) {
        return deepSeekClient.chatOnce(messages, "deepseek-v4-flash", true, maxTokens);
    }
}
