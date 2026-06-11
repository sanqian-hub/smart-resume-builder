package com.srb.backend.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.srb.backend.ai.DeepSeekClient;
import com.srb.backend.ai.DeepSeekMessage;
import com.srb.backend.common.BusinessException;
import com.srb.backend.common.ErrorCode;
import com.srb.backend.common.SessionUtils;
import com.srb.backend.mapper.ResumeContentMapper;
import com.srb.backend.mapper.ResumeMapper;
import com.srb.backend.mapper.ResumeShareMapper;
import com.srb.backend.mapper.ResumeVersionMapper;
import com.srb.backend.mapper.UserMapper;
import com.srb.backend.model.dto.ResumeAddRequest;
import com.srb.backend.model.dto.ResumeContentDTO;
import com.srb.backend.model.dto.ResumeUpdateRequest;
import com.srb.backend.model.entity.Resume;
import com.srb.backend.model.entity.ResumeContent;
import com.srb.backend.model.entity.ResumeShare;
import com.srb.backend.model.entity.ResumeVersion;
import com.srb.backend.model.entity.User;
import com.srb.backend.model.vo.ResumePublicShareVO;
import com.srb.backend.model.vo.ResumeProofreadVO;
import com.srb.backend.model.vo.ResumeScoreVO;
import com.srb.backend.model.vo.ResumeSelfIntroVO;
import com.srb.backend.model.vo.ResumeVersionSaveVO;
import com.srb.backend.model.vo.ResumeVO;
import com.srb.backend.model.vo.ResumeShareVO;
import com.srb.backend.service.ResumeService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.time.LocalDateTime;
import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Slf4j
public class ResumeServiceImpl extends ServiceImpl<ResumeMapper, Resume> implements ResumeService {
    private static final Duration PROOFREAD_CACHE_TTL = Duration.ofMinutes(10);
    private static final List<String> PROOFREAD_ORDERED_MODULES = List.of("basic", "education", "experience", "project", "skill", "personalStrengths", "award", "portfolio", "other");
    private static final Map<String, String> PROOFREAD_MODULE_LABELS = Map.of(
            "basic", "基本信息", "education", "教育经历", "experience", "工作经历",
            "project", "项目经历", "skill", "专业技能", "personalStrengths", "个人优势",
            "award", "荣誉奖项", "portfolio", "个人作品", "other", "其他经历"
    );
    private static final Set<String> BASIC_PROOFREAD_FIELDS = Set.of("name", "jobTitle", "location", "status");
    private static final Set<String> EDUCATION_PROOFREAD_FIELDS = Set.of("school", "major", "description");
    private static final Set<String> EXPERIENCE_PROOFREAD_FIELDS = Set.of("company", "department", "position", "content");
    private static final Set<String> PROJECT_PROOFREAD_FIELDS = Set.of("name", "role", "content");
    private static final Set<String> SKILL_PROOFREAD_FIELDS = Set.of("content");
    private static final Set<String> PERSONAL_STRENGTHS_PROOFREAD_FIELDS = Set.of("content");
    private static final Set<String> AWARD_PROOFREAD_FIELDS = Set.of("name", "content");
    private static final Set<String> PORTFOLIO_PROOFREAD_FIELDS = Set.of("name", "content");
    private static final Set<String> OTHER_PROOFREAD_FIELDS = Set.of("name", "role", "department", "content");

    private final Map<String, CachedProofreadResult> proofreadCache = new ConcurrentHashMap<>();
    private final Map<String, CompletableFuture<ResumeProofreadVO>> proofreadInflight = new ConcurrentHashMap<>();

    private final ResumeContentMapper resumeContentMapper;
    private final ResumeShareMapper resumeShareMapper;
    private final ResumeVersionMapper resumeVersionMapper;
    private final UserMapper userMapper;
    private final ObjectMapper objectMapper;
    private final DeepSeekClient deepSeekClient;

    @Override
    public Long addResume(HttpServletRequest request, ResumeAddRequest resumeRequest) {
        User user = SessionUtils.getLoginUser(request, userMapper);
        Resume resume = new Resume();
        resume.setUserId(user.getId());
        String title = resumeRequest.getTitle();
        resume.setTitle((title != null && !title.isBlank()) ? title : "未命名简历");
        resume.setCurrentTemplate(resumeRequest.getCurrentTemplate() != null ? resumeRequest.getCurrentTemplate() : "classic");
        resume.setStyleConfig(resumeRequest.getStyleConfig());
        resume.setStatus(0);
        this.baseMapper.insert(resume);
        return resume.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateResume(HttpServletRequest request, ResumeUpdateRequest resumeRequest) {
        User user = SessionUtils.getLoginUser(request, userMapper);
        Long resumeId = resumeRequest.getId();
        Resume resume = this.baseMapper.selectById(resumeId);
        if (resume == null || !resume.getUserId().equals(user.getId())) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "简历不存在");
        }
        if (resumeRequest.getTitle() != null) {
            resume.setTitle(resumeRequest.getTitle());
        }
        if (resumeRequest.getStatus() != null) {
            resume.setStatus(resumeRequest.getStatus());
        }
        if (resumeRequest.getCurrentTemplate() != null) {
            resume.setCurrentTemplate(resumeRequest.getCurrentTemplate());
        }
        if (resumeRequest.getStyleConfig() != null) {
            resume.setStyleConfig(resumeRequest.getStyleConfig());
        }
        this.baseMapper.updateById(resume);

        if (!CollectionUtils.isEmpty(resumeRequest.getContents())) {
            syncResumeContents(resumeId, resumeRequest.getContents());
        }
    }

    @Override
    public void deleteResume(HttpServletRequest request, Long resumeId) {
        User user = SessionUtils.getLoginUser(request, userMapper);
        Resume resume = this.baseMapper.selectById(resumeId);
        if (resume == null || !resume.getUserId().equals(user.getId())) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "简历不存在");
        }
        this.baseMapper.deleteById(resumeId);
    }

    @Override
    public ResumeVO getResume(HttpServletRequest request, Long resumeId) {
        User user = SessionUtils.getLoginUser(request, userMapper);
        Resume resume = this.baseMapper.selectById(resumeId);
        if (resume == null || !resume.getUserId().equals(user.getId())) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "简历不存在");
        }
        return toResumeVO(resume);
    }

    @Override
    public Page<ResumeVO> pageResume(HttpServletRequest request, int current, int pageSize) {
        User user = SessionUtils.getLoginUser(request, userMapper);
        Page<Resume> page = new Page<>(current, pageSize);
        QueryWrapper<Resume> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userId", user.getId()).orderByDesc("updateTime");
        Page<Resume> resumePage = this.baseMapper.selectPage(page, queryWrapper);

        Page<ResumeVO> voPage = new Page<>(current, pageSize, resumePage.getTotal());
        List<ResumeVO> voList = resumePage.getRecords().stream().map(this::toResumeVO).toList();
        voPage.setRecords(voList);
        return voPage;
    }

    @Override
    public List<ResumeVO> myList(HttpServletRequest request) {
        User user = SessionUtils.getLoginUser(request, userMapper);
        QueryWrapper<Resume> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userId", user.getId()).orderByDesc("updateTime");
        List<Resume> resumes = this.baseMapper.selectList(queryWrapper);
        return resumes.stream().map(this::toResumeVO).toList();
    }

    private ResumeVO toResumeVO(Resume resume) {
        ResumeVO vo = new ResumeVO();
        BeanUtils.copyProperties(resume, vo);

        QueryWrapper<ResumeContent> contentWrapper = new QueryWrapper<>();
        contentWrapper.eq("resumeId", resume.getId()).orderByAsc("sortOrder");
        List<ResumeContent> contents = resumeContentMapper.selectList(contentWrapper);

        List<ResumeVO.ResumeContentVO> contentVOList = contents.stream().map(c -> {
            ResumeVO.ResumeContentVO cvo = new ResumeVO.ResumeContentVO();
            BeanUtils.copyProperties(c, cvo);
            return cvo;
        }).toList();
        vo.setContents(contentVOList);
        vo.setStyleConfig(resolveStyleConfig(resume.getStyleConfig(), contents));

        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResumeVersionSaveVO saveVersion(HttpServletRequest request, Long resumeId, String remark) {
        User user = SessionUtils.getLoginUser(request, userMapper);
        Resume resume = this.baseMapper.selectById(resumeId);
        if (resume == null || !resume.getUserId().equals(user.getId())) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "简历不存在");
        }

        // 查询当前简历所有内容
        QueryWrapper<ResumeContent> contentWrapper = new QueryWrapper<>();
        contentWrapper.eq("resumeId", resumeId).orderByAsc("sortOrder");
        List<ResumeContent> contents = resumeContentMapper.selectList(contentWrapper);

        // 构建 Map → JSON 快照
        List<Map<String, Object>> contentList = contents.stream().map(c -> {
            Map<String, Object> map = new java.util.LinkedHashMap<>();
            map.put("moduleType", c.getModuleType());
            map.put("contentJson", c.getContentJson());
            map.put("sortOrder", c.getSortOrder());
            return map;
        }).toList();

        Map<String, Object> snapshot = new java.util.LinkedHashMap<>();
        snapshot.put("contents", contentList);
        snapshot.put("title", resume.getTitle());
        snapshot.put("template", resume.getCurrentTemplate());
        snapshot.put("styleConfig", resolveStyleConfig(resume.getStyleConfig(), contents));
        String snapshotJson;
        try {
            snapshotJson = objectMapper.writeValueAsString(snapshot);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "快照序列化失败");
        }

        String comparableSnapshotJson = buildComparableSnapshotJson(snapshot);

        // 计算版本号
        QueryWrapper<ResumeVersion> maxWrapper = new QueryWrapper<>();
        maxWrapper.eq("resumeId", resumeId).orderByDesc("versionNum").last("LIMIT 1");
        ResumeVersion lastVersion = resumeVersionMapper.selectOne(maxWrapper);
        if (lastVersion != null && comparableSnapshotJson.equals(buildComparableSnapshotJson(lastVersion.getSnapshotJson()))) {
            ResumeVersionSaveVO vo = new ResumeVersionSaveVO();
            vo.setId(lastVersion.getId());
            vo.setVersionNum(lastVersion.getVersionNum());
            vo.setCreated(false);
            return vo;
        }
        int nextNum = (lastVersion != null ? lastVersion.getVersionNum() : 0) + 1;

        ResumeVersion version = new ResumeVersion();
        version.setResumeId(resumeId);
        version.setUserId(user.getId());
        version.setVersionNum(nextNum);
        version.setSnapshotJson(snapshotJson);
        version.setRemark(remark);
        resumeVersionMapper.insert(version);

        // 超出上限清理最旧版本
        QueryWrapper<ResumeVersion> countWrapper = new QueryWrapper<>();
        countWrapper.eq("resumeId", resumeId);
        Long count = resumeVersionMapper.selectCount(countWrapper);
        int maxVersions = 30;
        if (count > maxVersions) {
            QueryWrapper<ResumeVersion> deleteWrapper = new QueryWrapper<>();
            deleteWrapper.eq("resumeId", resumeId)
                    .orderByAsc("createTime")
                    .last("LIMIT " + (count - maxVersions));
            resumeVersionMapper.delete(deleteWrapper);
        }

        ResumeVersionSaveVO vo = new ResumeVersionSaveVO();
        vo.setId(version.getId());
        vo.setVersionNum(version.getVersionNum());
        vo.setCreated(true);
        return vo;
    }

    @Override
    public List<ResumeVersion> listVersions(HttpServletRequest request, Long resumeId) {
        User user = SessionUtils.getLoginUser(request, userMapper);
        Resume resume = this.baseMapper.selectById(resumeId);
        if (resume == null || !resume.getUserId().equals(user.getId())) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "简历不存在");
        }
        QueryWrapper<ResumeVersion> wrapper = new QueryWrapper<>();
        wrapper.eq("resumeId", resumeId).orderByDesc("versionNum");
        return resumeVersionMapper.selectList(wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @SuppressWarnings("unchecked")
    public void rollbackVersion(HttpServletRequest request, Long versionId) {
        User user = SessionUtils.getLoginUser(request, userMapper);
        ResumeVersion version = resumeVersionMapper.selectById(versionId);
        if (version == null || !version.getUserId().equals(user.getId())) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "版本不存在");
        }

        Resume resume = this.baseMapper.selectById(version.getResumeId());
        if (resume == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "简历不存在");
        }

        // 解析快照
        try {
            Map<String, Object> snapshot = objectMapper.readValue(version.getSnapshotJson(), Map.class);

            // 恢复简历标题和模板
            if (snapshot.containsKey("title")) {
                resume.setTitle((String) snapshot.get("title"));
            }
            if (snapshot.containsKey("template")) {
                resume.setCurrentTemplate((String) snapshot.get("template"));
            }
            resume.setStyleConfig(resolveSnapshotStyleConfig(snapshot));
            this.baseMapper.updateById(resume);

            List<Map<String, Object>> contents = (List<Map<String, Object>>) snapshot.get("contents");
            if (contents != null) {
                List<ResumeContentDTO> contentDTOs = contents.stream().map(c -> {
                    ResumeContentDTO dto = new ResumeContentDTO();
                    dto.setModuleType((String) c.get("moduleType"));
                    dto.setContentJson((String) c.get("contentJson"));
                    dto.setSortOrder(c.get("sortOrder") != null ? ((Number) c.get("sortOrder")).intValue() : 0);
                    return dto;
                }).toList();
                syncResumeContents(resume.getId(), contentDTOs);
            }
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "版本快照解析失败");
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Object> matchAnalysis(HttpServletRequest request, Long resumeId, String jobDescription, Map<String, String> moduleData) {
        User user = SessionUtils.getLoginUser(request, userMapper);
        String normalizedJobDescription = jobDescription == null ? "" : jobDescription.trim();

        // resumeId 仅用于权限校验（有则验，无则跳过）
        if (resumeId != null) {
            Resume resume = this.baseMapper.selectById(resumeId);
            if (resume == null || !resume.getUserId().equals(user.getId())) {
                throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "简历不存在");
            }
        }

        // 统一使用前端传来的 moduleData 构建简历文本
        StringBuilder resumeText = new StringBuilder();
        Map<String, String> moduleLabels = Map.of(
                "basic", "基本信息", "education", "教育经历", "experience", "工作经历",
                "project", "项目经历", "skill", "专业技能", "personalStrengths", "个人优势",
                "award", "荣誉奖项", "portfolio", "作品集", "other", "其他经历"
        );

        if (moduleData != null) {
            List.of("basic", "education", "experience", "project", "skill", "personalStrengths", "award", "portfolio", "other").forEach(type -> {
                String json = moduleData.get(type);
                if (json != null && !json.isBlank()) {
                    String text = stripHtml(json);
                    if (text != null && !text.isBlank()) {
                        resumeText.append("【").append(moduleLabels.getOrDefault(type, type)).append("】\n").append(text).append("\n\n");
                    }
                }
            });
        }

        if (resumeText.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "简历内容为空，请先填写简历");
        }
        validateJobDescriptionForMatch(normalizedJobDescription);

        String systemPrompt = """
                你是一个专业的简历评估专家。请分析简历与目标岗位的匹配度。

                在开始评分前，先判断“目标岗位 JD”是否是一段有效的岗位描述。
                如果 JD 明显无效、过短、只有寒暄或无法识别岗位职责/要求，请不要正常评分，
                必须严格返回以下 JSON：
                {
                  "invalidJobDescription": true,
                  "message": "岗位描述过短或无效，请输入完整的岗位职责和任职要求后再试"
                }

                请从以下维度评分（0-100）并给出分析：
                1. 技能匹配度：简历中的技能与JD要求的匹配程度
                2. 经验匹配度：工作/项目经历与岗位要求的契合度
                3. 教育匹配度：学历、专业与岗位要求的匹配程度

                严格按以下 JSON 格式输出，不要输出任何其他内容：
                {
                  "overallScore": 78,
                  "dimensions": [
                    {"name": "技能匹配度", "score": 85, "analysis": "分析文字", "suggestion": "改进建议"},
                    {"name": "经验匹配度", "score": 72, "analysis": "分析文字", "suggestion": "改进建议"},
                    {"name": "教育匹配度", "score": 70, "analysis": "分析文字", "suggestion": "改进建议"}
                  ],
                  "missingSkills": ["Kubernetes", "微服务架构"],
                  "highlights": ["3年Java开发经验", "有分布式项目经验"],
                  "summary": "整体评价段落"
                }
                """;

        String userPrompt = "## 简历内容\n" + resumeText + "\n## 目标岗位 JD\n" + normalizedJobDescription;

        List<DeepSeekMessage> messages = List.of(
                DeepSeekMessage.system(systemPrompt),
                DeepSeekMessage.user(userPrompt)
        );

        String aiResponse = callDeepSeek(messages, 1400);

        // 清理可能的 markdown 代码块包裹
        String json = aiResponse.trim();
        if (json.startsWith("```")) {
            json = json.replaceAll("^```\\w*\\n?", "").replaceAll("\\n?```$", "");
        }

        try {
            return objectMapper.readValue(json, Map.class);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "AI 返回结果解析失败，请重试");
        }
    }

    @Override
    public ResumeScoreVO scoreResume(HttpServletRequest request, Long resumeId, Map<String, String> moduleData) {
        User user = SessionUtils.getLoginUser(request, userMapper);

        if (resumeId != null) {
            Resume resume = this.baseMapper.selectById(resumeId);
            if (resume == null || !resume.getUserId().equals(user.getId())) {
                throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "简历不存在");
            }
        }

        String resumeText = buildResumeText(moduleData, resumeId);
        if (resumeText.isBlank()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "简历内容为空，请先填写简历");
        }

        String systemPrompt = """
                你是一名专业的中文简历评审顾问。你的任务是对候选人的简历做通用打分，不参考任何岗位描述。

                请严格遵守以下要求：
                1. 只基于给定简历内容分析，不要编造不存在的经历、项目、数据、奖项或技能。
                2. 总分范围是 0 到 100。
                3. 必须严格按以下四个维度评分：
                   - 内容完整度
                   - 表达清晰度
                   - 岗位相关性
                   - 亮点竞争力
                4. 每个维度都要给出简短分析和一条改进建议。
                5. summary 用 2 到 4 句中文总结整体水平。
                6. suggestions 给出 3 到 5 条具体、可执行的优化建议。
                7. highlights 给出 2 到 4 条当前简历已有的亮点。
                8. 不要输出任何 markdown 代码块。

                严格按以下 JSON 输出，不要输出任何其他内容：
                {
                  "totalScore": 82,
                  "summary": "整体评价",
                  "highlights": ["亮点1", "亮点2"],
                  "suggestions": ["建议1", "建议2", "建议3"],
                  "dimensions": [
                    {"name": "内容完整度", "score": 84, "analysis": "分析文字", "suggestion": "改进建议"},
                    {"name": "表达清晰度", "score": 79, "analysis": "分析文字", "suggestion": "改进建议"},
                    {"name": "岗位相关性", "score": 81, "analysis": "分析文字", "suggestion": "改进建议"},
                    {"name": "亮点竞争力", "score": 76, "analysis": "分析文字", "suggestion": "改进建议"}
                  ]
                }
                """;

        String userPrompt = "## 候选纠错字段列表（JSON）\n" + resumeText;
        List<DeepSeekMessage> messages = List.of(
                DeepSeekMessage.system(systemPrompt),
                DeepSeekMessage.user(userPrompt)
        );

        String json = cleanupAiJson(callDeepSeek(messages, 1400));

        try {
            Map<String, Object> parsed = objectMapper.readValue(json, Map.class);
            ResumeScoreVO vo = new ResumeScoreVO();
            vo.setTotalScore(parseScore(parsed.get("totalScore")));
            vo.setSummary(stringValue(parsed.get("summary")));
            vo.setHighlights(toStringList(parsed.get("highlights")));
            vo.setSuggestions(toStringList(parsed.get("suggestions")));
            vo.setDimensions(toScoreDimensions(parsed.get("dimensions")));
            if (vo.getTotalScore() == null) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "AI 返回评分为空，请重试");
            }
            return vo;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "AI 返回结果解析失败，请重试");
        }
    }

    @Override
    public ResumeProofreadVO proofreadResume(HttpServletRequest request, Long resumeId, Map<String, String> moduleData) {
        User user = SessionUtils.getLoginUser(request, userMapper);

        if (resumeId != null) {
            Resume resume = this.baseMapper.selectById(resumeId);
            if (resume == null || !resume.getUserId().equals(user.getId())) {
                throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "简历不存在");
            }
        }

        ProofreadInputPayload proofreadInputPayload = buildProofreadInputPayload(moduleData, resumeId);
        String resumeText = buildProofreadResumeText(proofreadInputPayload);
        if (resumeText.isBlank()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "简历内容为空，请先填写简历");
        }
        String cacheKey = buildProofreadCacheKey(user.getId(), proofreadInputPayload);
        ResumeProofreadVO cachedResult = getCachedProofreadResult(cacheKey);
        if (cachedResult != null) {
            return cachedResult;
        }
        CompletableFuture<ResumeProofreadVO> inFlight = proofreadInflight.get(cacheKey);
        if (inFlight != null) {
            return joinInflightProofread(inFlight);
        }
        CompletableFuture<ResumeProofreadVO> currentFuture = new CompletableFuture<>();
        CompletableFuture<ResumeProofreadVO> existingFuture = proofreadInflight.putIfAbsent(cacheKey, currentFuture);
        if (existingFuture != null) {
            return joinInflightProofread(existingFuture);
        }
        try {
            ResumeProofreadVO result = executeProofread(resumeText, proofreadInputPayload);
            cacheProofreadResult(cacheKey, result);
            currentFuture.complete(result);
            return result;
        } catch (RuntimeException e) {
            currentFuture.completeExceptionally(e);
            throw e;
        } finally {
            proofreadInflight.remove(cacheKey, currentFuture);
        }
    }

    private ResumeProofreadVO executeProofread(String resumeText, ProofreadInputPayload proofreadInputPayload) {

        String systemPrompt = """
                你是一名专业的中文简历语法纠错顾问。你的任务是对候选人的简历内容给出保守、可执行的语法和表达优化建议。

                请严格遵守以下要求：
                1. 只基于给定简历内容分析，不要编造不存在的经历、数据、项目、奖项或技能。
                2. 你只做以下四类问题识别：错别字、语法问题、表达优化、表达澄清。
                3. 不要重写整段内容，只给出局部、可替换的修改建议。
                4. 必须保持保守，不要擅自修改以下内容：
                   - 项目名、作品名、平台名、品牌名、产品名、专有名词
                   - 链接、邮箱、手机号、时间、数字编号
                   - 疑似谐音、自定义命名（例如“小红薯”）如果不能确定，就不要改
                5. 个人作品模块只允许修改描述文本，不允许改标题和链接。
                6. 荣誉奖项模块允许对奖项描述做纠错，但不要改奖项名称本身。
                7. 如果没有发现明确需要修正的问题，也要返回合法 JSON，items 为空数组。
                8. 不要输出 markdown 代码块。
                9. 基本信息里的姓名、求职方向、地点、状态等短文本字段也要逐项检查，不能只关注长段落描述。
                10. 即使是像“背景”这样只有两个字的短文本，也要逐项检查错别字和词语误用，必要时再补充明显不自然的表达问题。
                11. 最多只返回 8 条最重要、最值得用户手动处理的建议；如果问题很多，优先返回错别字、明显语病、影响专业性的表达问题。
                12. 如果 suggestion 与 original 完全相同，绝对不要返回这条建议；只改空格、标点但没有实际可见变化的建议也不要返回。
                13. 如果同一模块里有多条相近问题，优先合并成更少的高价值建议，避免堆砌低价值格式问题。
                14. 不要仅因为空格、换行、缩进或纯排版习惯差异就返回建议；如果内容语义没变，只是展示用留白不同，不属于语法纠错范围。
                15. 像“冒号后多一个空格”“中英文之间空一格/少一格”“为了排版保留的展示空格”这类情况，一律不要作为建议返回。
                16. 不要为了统一格式而改写用户的展示风格；只有当错别字、语病、歧义或明显不专业表达存在时，才返回建议。
                17. 一条建议只能针对单个列表项、单个编号点、单个段落或一个最小连续文本片段；不要跨多个列表项、编号点或段落合并一条建议。
                18. 如果两个列表项各自有问题，必须拆成两条建议，绝对不要把它们拼成一条更长的 original。
                19. itemIndex 必须使用 0-based 索引：第一条是 0，第二条是 1，绝对不要返回 1-based 索引。

                支持的 moduleType 只有：
                basic, education, experience, project, skill, personalStrengths, award, portfolio, other

                type 只能取：
                typo, grammar, style, clarity

                严格按以下 JSON 输出，不要输出任何其他内容：
                {
                  "summary": "整体说明",
                  "items": [
                    {
                      "id": "proofread-1",
                      "candidateId": "c1",
                      "occurrenceIndex": 0,
                      "type": "style",
                      "original": "负责项目开发和设计",
                      "suggestion": "负责项目设计与开发",
                      "reason": "语序更自然，表达更紧凑"
                    }
                  ]
                }
                """;

                String userPrompt = """
                        ## 候选纠错字段列表（JSON）
                        你会收到若干候选纠错字段，每项包含：
                        - candidateId: 候选字段唯一标识，必须原样带回
                        - label: 仅供你理解上下文的简短标签
                        - text: 待检查文本

                        返回 items 时：
                        - 必须保留 candidateId
                        - 不要返回 moduleType / itemIndex / fieldPath / fieldLabel / typeLabel
                        - occurrenceIndex 仍使用 0-based
                        
                        """ + resumeText;
        List<DeepSeekMessage> messages = List.of(
                DeepSeekMessage.system(systemPrompt),
                DeepSeekMessage.user(userPrompt)
        );

        String json = cleanupAiJson(callDeepSeek(messages, 2600));
        if (!StringUtils.hasText(json)) {
            log.warn("语法纠错 AI 原始返回为空");
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "AI 返回结果解析失败，请重试");
        }

        try {
            Map<String, Object> parsed = objectMapper.readValue(json, Map.class);
            ResumeProofreadVO vo = new ResumeProofreadVO();
            vo.setSummary(stringValue(parsed.get("summary")));
            vo.setItems(toProofreadItems(parsed.get("items"), proofreadInputPayload));
            if (vo.getSummary() == null || vo.getSummary().isBlank()) {
                vo.setSummary(vo.getItems().isEmpty() ? "当前未发现明显的语法或表达问题" : "已发现可手动应用的语法纠错建议");
            }
            return vo;
        } catch (Exception e) {
            log.warn("语法纠错 AI 原始返回解析失败: {}", json, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "AI 返回结果解析失败，请重试");
        }
    }

    private ResumeProofreadVO getCachedProofreadResult(String cacheKey) {
        CachedProofreadResult cached = proofreadCache.get(cacheKey);
        if (cached == null) {
            return null;
        }
        if (cached.expiresAt().isBefore(Instant.now())) {
            proofreadCache.remove(cacheKey, cached);
            return null;
        }
        return cached.value();
    }

    private void cacheProofreadResult(String cacheKey, ResumeProofreadVO value) {
        proofreadCache.put(cacheKey, new CachedProofreadResult(value, Instant.now().plus(PROOFREAD_CACHE_TTL)));
    }

    private ResumeProofreadVO joinInflightProofread(CompletableFuture<ResumeProofreadVO> future) {
        try {
            return future.join();
        } catch (RuntimeException e) {
            Throwable cause = e.getCause();
            if (cause instanceof RuntimeException runtimeException) {
                throw runtimeException;
            }
            throw e;
        }
    }

    private void validateJobDescriptionForMatch(String jobDescription) {
        String compact = jobDescription == null ? "" : jobDescription.replaceAll("\\s+", "");
        if (compact.length() < 20) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "岗位描述过短，请补充岗位职责和任职要求后再试");
        }
        if (compact.matches("^你好[啊呀吗嘛哈]*$") || compact.matches("^测试[一下啊呀吗嘛]*$") || compact.matches("^哈{2,}$") || compact.matches("^在吗$")) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请输入有效的岗位 JD 后再分析");
        }
        boolean hasKeyword = Stream.of("岗位", "职责", "要求", "负责", "熟悉", "经验", "能力", "任职", "开发", "设计", "产品", "运营", "测试", "架构", "技术", "学历", "本科")
                .anyMatch(compact::contains);
        if (!hasKeyword) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请输入更完整的岗位职责、要求或技能关键词后再分析");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String createShare(HttpServletRequest request, Long resumeId, Long versionId, String password, Integer expireDays) {
        User user = SessionUtils.getLoginUser(request, userMapper);
        Resume resume = this.baseMapper.selectById(resumeId);
        if (resume == null || !resume.getUserId().equals(user.getId())) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "简历不存在");
        }
        validateSharePassword(password);
        validateExpireDays(expireDays);

        String normalizedPassword = (password != null && !password.isBlank()) ? password : null;
        int normalizedExpireDays = normalizeExpireDays(expireDays);
        String sourceType;
        Long sourceVersionId = null;
        Integer sourceVersionNum = null;
        String snapshotJson;

        if (versionId != null) {
            ResumeVersion version = resumeVersionMapper.selectById(versionId);
            if (version == null || !version.getUserId().equals(user.getId()) || !version.getResumeId().equals(resumeId)) {
                throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "历史版本不存在");
            }
            sourceType = "version";
            sourceVersionId = version.getId();
            sourceVersionNum = version.getVersionNum();
            snapshotJson = version.getSnapshotJson();
        } else {
            sourceType = "current";
            ResumeVersion latestVersion = findLatestVersion(resumeId);
            if (latestVersion != null) {
                sourceVersionId = latestVersion.getId();
                sourceVersionNum = latestVersion.getVersionNum();
            }
            snapshotJson = buildCurrentSnapshotJson(resume);
        }

        ResumeShare reusableShare = findReusableShare(
                user.getId(),
                resumeId,
                sourceType,
                sourceVersionId,
                snapshotJson,
                normalizedPassword,
                normalizedExpireDays
        );
        if (reusableShare != null) {
            return reusableShare.getShareKey();
        }

        ResumeShare share = new ResumeShare();
        share.setResumeId(resumeId);
        share.setUserId(user.getId());
        share.setShareKey(UUID.randomUUID().toString().replace("-", ""));
        share.setPassword(normalizedPassword);
        share.setExpireTime(toExpireTime(normalizedExpireDays));
        share.setExpireDays(normalizedExpireDays);
        share.setViewCount(0);
        share.setStatus(1);
        share.setSourceType(sourceType);
        share.setSourceVersionId(sourceVersionId);
        share.setSourceVersionNum(sourceVersionNum);
        share.setSnapshotJson(snapshotJson);

        resumeShareMapper.insert(share);
        return share.getShareKey();
    }

    @Override
    public List<ResumeShareVO> listShares(HttpServletRequest request, Long resumeId) {
        User user = SessionUtils.getLoginUser(request, userMapper);
        Resume resume = this.baseMapper.selectById(resumeId);
        if (resume == null || !resume.getUserId().equals(user.getId())) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "简历不存在");
        }

        QueryWrapper<ResumeShare> wrapper = new QueryWrapper<>();
        wrapper.eq("resumeId", resumeId)
                .eq("userId", user.getId())
                .orderByDesc("createTime")
                .orderByDesc("id");
        return resumeShareMapper.selectList(wrapper).stream().map(this::toResumeShareVO).toList();
    }

    @Override
    public void closeShare(HttpServletRequest request, Long shareId) {
        User user = SessionUtils.getLoginUser(request, userMapper);
        ResumeShare share = getOwnedShare(user.getId(), shareId);
        share.setStatus(0);
        resumeShareMapper.updateById(share);
    }

    @Override
    public ResumeShareVO updateSharePassword(HttpServletRequest request, Long shareId, String password) {
        User user = SessionUtils.getLoginUser(request, userMapper);
        ResumeShare share = getOwnedShare(user.getId(), shareId);
        validateSharePassword(password);
        String normalizedPassword = (password != null && !password.isBlank()) ? password : null;
        if (Objects.equals(share.getPassword(), normalizedPassword)) {
            return toResumeShareVO(share);
        }
        LambdaUpdateWrapper<ResumeShare> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(ResumeShare::getId, share.getId())
                .set(ResumeShare::getPassword, normalizedPassword);
        resumeShareMapper.update(null, wrapper);
        share.setPassword(normalizedPassword);
        return toResumeShareVO(share);
    }

    @Override
    public ResumeShareVO updateShareExpire(HttpServletRequest request, Long shareId, Integer expireDays) {
        User user = SessionUtils.getLoginUser(request, userMapper);
        ResumeShare share = getOwnedShare(user.getId(), shareId);
        validateExpireDays(expireDays);
        int normalizedExpireDays = normalizeExpireDays(expireDays);
        if (Objects.equals(normalizeExpireDays(share.getExpireDays()), normalizedExpireDays)) {
            return toResumeShareVO(share);
        }
        LocalDateTime expireTime = toExpireTime(normalizedExpireDays);
        LambdaUpdateWrapper<ResumeShare> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(ResumeShare::getId, share.getId())
                .set(ResumeShare::getExpireTime, expireTime)
                .set(ResumeShare::getExpireDays, normalizedExpireDays);
        resumeShareMapper.update(null, wrapper);
        share.setExpireTime(expireTime);
        share.setExpireDays(normalizedExpireDays);
        return toResumeShareVO(share);
    }

    @Override
    public ResumePublicShareVO getPublicShare(HttpServletRequest request, String shareKey) {
        ResumeShare share = getActiveShareByKey(shareKey);
        ResumePublicShareVO vo = new ResumePublicShareVO();
        boolean expired = isShareExpired(share);
        Long currentUserId = SessionUtils.getLoginUserIdIfPresent(request);
        boolean ownerViewing = currentUserId != null && currentUserId.equals(share.getUserId());
        vo.setExpired(expired);
        vo.setNeedPassword(hasPassword(share) && !ownerViewing);
        if (expired) {
            return vo;
        }
        if (Boolean.TRUE.equals(vo.getNeedPassword())) {
            return vo;
        }
        if (!ownerViewing) {
            incrementShareViewCount(share);
        }
        vo.setResume(snapshotToResumeVO(share));
        return vo;
    }

    @Override
    public ResumeVO verifyPublicShare(String shareKey, String password) {
        ResumeShare share = getActiveShareByKey(shareKey);
        if (isShareExpired(share)) {
            throw new BusinessException(ErrorCode.FORBIDDEN_ERROR, "分享链接已过期");
        }
        if (!hasPassword(share)) {
            incrementShareViewCount(share);
            return snapshotToResumeVO(share);
        }
        if (password == null || !password.equals(share.getPassword())) {
            throw new BusinessException(ErrorCode.FORBIDDEN_ERROR, "访问密码错误");
        }
        incrementShareViewCount(share);
        return snapshotToResumeVO(share);
    }

    @Override
    @SuppressWarnings("unchecked")
    public ResumeSelfIntroVO generateSelfIntro(HttpServletRequest request,
                                               Long resumeId,
                                               Integer durationSeconds,
                                               String style,
                                               String jobDescription,
                                               Map<String, String> moduleData) {
        User user = SessionUtils.getLoginUser(request, userMapper);
        validateSelfIntroArgs(durationSeconds, style);

        if (resumeId != null) {
            Resume resume = this.baseMapper.selectById(resumeId);
            if (resume == null || !resume.getUserId().equals(user.getId())) {
                throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "简历不存在");
            }
        }

        String resumeText = buildResumeText(moduleData, resumeId);
        if (resumeText.isBlank()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "简历内容为空，请先填写简历");
        }

        String normalizedStyle = normalizeSelfIntroStyle(style);
        String normalizedJobDescription = jobDescription == null ? "" : jobDescription.trim();
        String durationGuide = switch (durationSeconds) {
            case 30 -> "控制在 110~150 字，适合 30 秒左右口播。";
            case 60 -> "控制在 220~300 字，适合 60 秒左右口播。";
            case 90 -> "控制在 330~450 字，适合 90 秒左右口播。";
            default -> "控制在合适口播长度。";
        };

        String systemPrompt = """
                你是一名资深求职教练，擅长根据候选人的真实简历内容生成可直接开口表达的面试自我介绍。

                请严格遵守以下要求：
                1. 只基于给定简历内容生成，不要编造不存在的经历、项目、公司、数据或奖项。
                2. 输出必须适合口语表达，语气自然流畅，不要写成简历条目，也不要分点。
                3. 必须贴合指定时长与风格。
                4. 如果提供了岗位描述，要适度向岗位重点靠拢，但仍以候选人真实经历为核心。
                5. 不要输出任何 markdown 代码块。

                严格按以下 JSON 输出，不要输出任何其他内容：
                {
                  "title": "60秒面试自我介绍",
                  "content": "完整口播稿",
                  "highlights": ["亮点1", "亮点2", "亮点3"]
                }
                """;

        StringBuilder userPrompt = new StringBuilder()
                .append("## 目标\n")
                .append("请生成一段 ").append(durationSeconds).append(" 秒面试自我介绍。\n")
                .append("风格：").append(normalizedStyle).append("\n")
                .append("时长要求：").append(durationGuide).append("\n\n")
                .append("## 当前简历内容\n")
                .append(resumeText)
                .append("\n");
        if (!normalizedJobDescription.isBlank()) {
            userPrompt.append("\n## 目标岗位描述\n").append(normalizedJobDescription).append("\n");
        }

        List<DeepSeekMessage> messages = List.of(
                DeepSeekMessage.system(systemPrompt),
                DeepSeekMessage.user(userPrompt.toString())
        );

        String aiResponse = callDeepSeek(messages, 1200);
        String json = cleanupAiJson(aiResponse);

        try {
            Map<String, Object> parsed = objectMapper.readValue(json, Map.class);
            ResumeSelfIntroVO vo = new ResumeSelfIntroVO();
            vo.setTitle(stringValue(parsed.get("title")));
            vo.setContent(stringValue(parsed.get("content")));
            vo.setHighlights(toStringList(parsed.get("highlights")));
            if (vo.getContent() == null || vo.getContent().isBlank()) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "AI 生成结果为空，请重试");
            }
            if (vo.getTitle() == null || vo.getTitle().isBlank()) {
                vo.setTitle(durationSeconds + "秒面试自我介绍");
            }
            return vo;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "AI 返回结果解析失败，请重试");
        }
    }

    private ResumeShareVO toResumeShareVO(ResumeShare share) {
        ResumeShareVO vo = new ResumeShareVO();
        BeanUtils.copyProperties(share, vo);
        vo.setHasPassword(hasPassword(share));
        vo.setExpired(isShareExpired(share));
        return vo;
    }

    private String callDeepSeek(List<DeepSeekMessage> messages, int maxTokens) {
        return deepSeekClient.chatOnce(messages, "deepseek-v4-flash", true, maxTokens, 0.1d);
    }

    private ResumeShare getOwnedShare(Long userId, Long shareId) {
        ResumeShare share = resumeShareMapper.selectById(shareId);
        if (share == null || !share.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "分享记录不存在");
        }
        return share;
    }

    private ResumeShare getActiveShareByKey(String shareKey) {
        QueryWrapper<ResumeShare> wrapper = new QueryWrapper<>();
        wrapper.eq("shareKey", shareKey)
                .eq("status", 1)
                .last("LIMIT 1");
        ResumeShare share = resumeShareMapper.selectOne(wrapper);
        if (share == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "分享链接不存在");
        }
        return share;
    }

    private boolean hasPassword(ResumeShare share) {
        return share.getPassword() != null && !share.getPassword().isBlank();
    }

    private boolean isShareExpired(ResumeShare share) {
        return share.getExpireTime() != null && share.getExpireTime().isBefore(LocalDateTime.now());
    }

    private void incrementShareViewCount(ResumeShare share) {
        share.setViewCount((share.getViewCount() == null ? 0 : share.getViewCount()) + 1);
        resumeShareMapper.updateById(share);
    }

    private void validateSharePassword(String password) {
        if (password == null || password.isBlank()) {
            return;
        }
        if (!password.matches("\\d{6}")) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "访问密码必须为 6 位数字");
        }
    }

    private void validateExpireDays(Integer expireDays) {
        if (expireDays == null) {
            return;
        }
        if (expireDays < 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "有效期参数错误");
        }
        if (expireDays > 3650) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "有效期不能超过 3650 天");
        }
    }

    private LocalDateTime toExpireTime(Integer expireDays) {
        if (expireDays == null || expireDays == 0) {
            return null;
        }
        return LocalDateTime.now().plusDays(expireDays);
    }

    private int normalizeExpireDays(Integer expireDays) {
        return expireDays == null ? 0 : expireDays;
    }

    private ResumeVersion findLatestVersion(Long resumeId) {
        QueryWrapper<ResumeVersion> wrapper = new QueryWrapper<>();
        wrapper.eq("resumeId", resumeId)
                .orderByDesc("versionNum")
                .last("LIMIT 1");
        return resumeVersionMapper.selectOne(wrapper);
    }

    private ResumeShare findReusableShare(Long userId,
                                          Long resumeId,
                                          String sourceType,
                                          Long sourceVersionId,
                                          String snapshotJson,
                                          String password,
                                          Integer expireDays) {
        QueryWrapper<ResumeShare> wrapper = new QueryWrapper<>();
        wrapper.eq("userId", userId)
                .eq("resumeId", resumeId)
                .eq("status", 1)
                .orderByDesc("createTime")
                .orderByDesc("id");
        List<ResumeShare> shares = resumeShareMapper.selectList(wrapper);
        for (ResumeShare share : shares) {
            if (isShareExpired(share)) {
                continue;
            }
            if (!Objects.equals(share.getSourceType(), sourceType)) {
                continue;
            }
            if (!Objects.equals(share.getSourceVersionId(), sourceVersionId)) {
                continue;
            }
            if (!Objects.equals(share.getSnapshotJson(), snapshotJson)) {
                continue;
            }
            if (!Objects.equals(share.getPassword(), password)) {
                continue;
            }
            if (!Objects.equals(normalizeExpireDays(share.getExpireDays()), expireDays)) {
                continue;
            }
            return share;
        }
        return null;
    }

    private String buildCurrentSnapshotJson(Resume resume) {
        QueryWrapper<ResumeContent> contentWrapper = new QueryWrapper<>();
        contentWrapper.eq("resumeId", resume.getId()).orderByAsc("sortOrder");
        List<ResumeContent> contents = resumeContentMapper.selectList(contentWrapper);

        List<Map<String, Object>> contentList = contents.stream().map(c -> {
            Map<String, Object> map = new java.util.LinkedHashMap<>();
            map.put("moduleType", c.getModuleType());
            map.put("contentJson", c.getContentJson());
            map.put("sortOrder", c.getSortOrder());
            return map;
        }).toList();

        Map<String, Object> snapshot = new java.util.LinkedHashMap<>();
        snapshot.put("contents", contentList);
        snapshot.put("title", resume.getTitle());
        snapshot.put("template", resume.getCurrentTemplate());
        snapshot.put("styleConfig", resolveStyleConfig(resume.getStyleConfig(), contents));
        try {
            return objectMapper.writeValueAsString(snapshot);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "分享快照生成失败");
        }
    }

    private String buildComparableSnapshotJson(String snapshotJson) {
        if (!StringUtils.hasText(snapshotJson)) {
            return "";
        }
        try {
            Map<String, Object> snapshot = objectMapper.readValue(snapshotJson, Map.class);
            return buildComparableSnapshotJson(snapshot);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "版本快照解析失败");
        }
    }

    private String buildComparableSnapshotJson(Map<String, Object> snapshot) {
        Map<String, Object> comparableSnapshot = new java.util.LinkedHashMap<>();
        comparableSnapshot.put("contents", normalizeComparableContents(snapshot.get("contents")));
        comparableSnapshot.put("template", snapshot.get("template"));
        comparableSnapshot.put("styleConfig", resolveSnapshotStyleConfig(snapshot));
        try {
            return objectMapper.writeValueAsString(comparableSnapshot);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "版本快照序列化失败");
        }
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> normalizeComparableContents(Object rawContents) {
        if (!(rawContents instanceof List<?> contents)) {
            return List.of();
        }
        List<Map<String, Object>> normalizedContents = new ArrayList<>();
        for (Object item : contents) {
            if (!(item instanceof Map<?, ?> contentMap)) {
                continue;
            }
            Map<String, Object> normalizedContent = new java.util.LinkedHashMap<>();
            normalizedContent.put("moduleType", contentMap.get("moduleType"));
            normalizedContent.put("contentJson", contentMap.get("contentJson"));
            normalizedContent.put("sortOrder", contentMap.get("sortOrder"));
            normalizedContents.add(normalizedContent);
        }
        return normalizedContents;
    }

    private String buildResumeText(Map<String, String> moduleData, Long resumeId) {
        StringBuilder resumeText = new StringBuilder();
        if (moduleData != null && !moduleData.isEmpty()) {
            PROOFREAD_ORDERED_MODULES.forEach(type -> appendResumeText(resumeText, PROOFREAD_MODULE_LABELS, type, moduleData.get(type)));
        } else if (resumeId != null) {
            QueryWrapper<ResumeContent> wrapper = new QueryWrapper<>();
            wrapper.eq("resumeId", resumeId).orderByAsc("sortOrder");
            List<ResumeContent> contents = resumeContentMapper.selectList(wrapper);
            contents.forEach(content -> appendResumeText(resumeText, PROOFREAD_MODULE_LABELS, content.getModuleType(), content.getContentJson()));
        }
        return resumeText.toString().trim();
    }

    private ProofreadInputPayload buildProofreadInputPayload(Map<String, String> moduleData, Long resumeId) {
        List<ProofreadModulePayload> modules = new ArrayList<>();
        Map<String, String> rawModuleData = resolveProofreadModuleData(moduleData, resumeId);
        for (String moduleType : PROOFREAD_ORDERED_MODULES) {
            List<ProofreadFieldPayload> fields = extractProofreadFields(moduleType, rawModuleData.get(moduleType));
            if (!fields.isEmpty()) {
                modules.add(new ProofreadModulePayload(moduleType, moduleLabelForProofread(moduleType), fields));
            }
        }
        return assignProofreadCandidateIds(modules);
    }

    private Map<String, String> resolveProofreadModuleData(Map<String, String> moduleData, Long resumeId) {
        if (moduleData != null && !moduleData.isEmpty()) {
            return moduleData;
        }
        Map<String, String> resolved = new HashMap<>();
        if (resumeId == null) {
            return resolved;
        }
        QueryWrapper<ResumeContent> wrapper = new QueryWrapper<>();
        wrapper.eq("resumeId", resumeId).orderByAsc("sortOrder");
        List<ResumeContent> contents = resumeContentMapper.selectList(wrapper);
        for (ResumeContent content : contents) {
            resolved.put(content.getModuleType(), content.getContentJson());
        }
        return resolved;
    }

    private List<ProofreadFieldPayload> extractProofreadFields(String moduleType, String json) {
        if (!StringUtils.hasText(json)) {
            return List.of();
        }
        try {
            Object parsed = objectMapper.readValue(json, Object.class);
            if (parsed instanceof Map<?, ?> rawMap) {
                return extractProofreadFieldsFromMap(moduleType, null, castObjectMap(rawMap));
            }
            if (parsed instanceof List<?> rawList) {
                List<ProofreadFieldPayload> fields = new ArrayList<>();
                for (int index = 0; index < rawList.size(); index++) {
                    Object item = rawList.get(index);
                    if (item instanceof Map<?, ?> itemMap) {
                        fields.addAll(extractProofreadFieldsFromMap(moduleType, index, castObjectMap(itemMap)));
                    }
                }
                return fields;
            }
        } catch (Exception ignored) {
            String text = normalizeProofreadFieldText(json);
            if (shouldIncludeProofreadText(text)) {
                return List.of(new ProofreadFieldPayload(null, proofreadFieldLabel(moduleType, null, null), moduleType, null, null, text));
            }
        }
        return List.of();
    }

    private List<ProofreadFieldPayload> extractProofreadFieldsFromMap(String moduleType, Integer itemIndex, Map<String, Object> map) {
        Set<String> allowedFields = allowedProofreadFields(moduleType);
        if (allowedFields.isEmpty()) {
            return List.of();
        }
        List<ProofreadFieldPayload> fields = new ArrayList<>();
        for (String fieldPath : allowedFields) {
            Object value = map.get(fieldPath);
            String text = normalizeProofreadFieldValue(fieldPath, value);
            if (!shouldIncludeProofreadText(text)) {
                continue;
            }
            fields.add(new ProofreadFieldPayload(
                    null,
                    proofreadFieldLabel(moduleType, itemIndex, fieldPath),
                    moduleType,
                    itemIndex,
                    fieldPath,
                    text
            ));
        }
        return fields;
    }

    private String normalizeProofreadFieldValue(String fieldPath, Object value) {
        if (value == null) {
            return null;
        }
        String text = value.toString();
        if (isRichProofreadField(fieldPath)) {
            text = stripHtml(text);
        }
        return normalizeProofreadFieldText(text);
    }

    private String normalizeProofreadFieldText(String text) {
        if (text == null) {
            return null;
        }
        String normalized = text.replace('\u00A0', ' ').replaceAll("\\s+", " ").trim();
        return normalized.isEmpty() ? null : normalized;
    }

    private boolean shouldIncludeProofreadText(String text) {
        if (!StringUtils.hasText(text)) {
            return false;
        }
        String compact = text.replaceAll("\\s+", "");
        if (!StringUtils.hasText(compact)) {
            return false;
        }
        if (compact.matches("https?://\\S+")) {
            return false;
        }
        if (compact.matches("[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}")) {
            return false;
        }
        if (compact.matches("\\+?[0-9()\\-]{6,}")) {
            return false;
        }
        if (compact.matches("[0-9/\\-.年月日:：~至]+")) {
            return false;
        }
        return !compact.matches("[0-9]+");
    }

    private boolean isRichProofreadField(String fieldPath) {
        return "content".equals(fieldPath) || "description".equals(fieldPath) || "summary".equals(fieldPath);
    }

    private Set<String> allowedProofreadFields(String moduleType) {
        return switch (moduleType) {
            case "basic" -> BASIC_PROOFREAD_FIELDS;
            case "education" -> EDUCATION_PROOFREAD_FIELDS;
            case "experience" -> EXPERIENCE_PROOFREAD_FIELDS;
            case "project" -> PROJECT_PROOFREAD_FIELDS;
            case "skill" -> SKILL_PROOFREAD_FIELDS;
            case "personalStrengths" -> PERSONAL_STRENGTHS_PROOFREAD_FIELDS;
            case "award" -> AWARD_PROOFREAD_FIELDS;
            case "portfolio" -> PORTFOLIO_PROOFREAD_FIELDS;
            case "other" -> OTHER_PROOFREAD_FIELDS;
            default -> Set.of();
        };
    }

    private Map<String, Object> castObjectMap(Map<?, ?> rawMap) {
        Map<String, Object> result = new LinkedHashMap<>();
        rawMap.forEach((key, value) -> {
            if (key != null) {
                result.put(key.toString(), value);
            }
        });
        return result;
    }

    private ProofreadInputPayload assignProofreadCandidateIds(List<ProofreadModulePayload> modules) {
        List<ProofreadModulePayload> assignedModules = new ArrayList<>();
        int candidateIndex = 1;
        for (ProofreadModulePayload module : modules) {
            List<ProofreadFieldPayload> assignedFields = new ArrayList<>();
            for (ProofreadFieldPayload field : module.fields()) {
                assignedFields.add(new ProofreadFieldPayload(
                        "c" + candidateIndex++,
                        field.label(),
                        field.moduleType(),
                        field.itemIndex(),
                        field.fieldPath(),
                        field.text()
                ));
            }
            assignedModules.add(new ProofreadModulePayload(module.moduleType(), module.fieldLabel(), assignedFields));
        }
        return new ProofreadInputPayload(assignedModules);
    }

    private String buildProofreadResumeText(ProofreadInputPayload payload) {
        List<Map<String, Object>> candidates = new ArrayList<>();
        for (ProofreadModulePayload module : payload.modules()) {
            for (ProofreadFieldPayload field : module.fields()) {
                Map<String, Object> candidate = new LinkedHashMap<>();
                candidate.put("candidateId", field.candidateId());
                candidate.put("label", field.label());
                candidate.put("text", field.text());
                candidates.add(candidate);
            }
        }
        try {
            return objectMapper.writeValueAsString(candidates);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "语法纠错候选字段序列化失败");
        }
    }

    private String buildProofreadCacheKey(Long userId, ProofreadInputPayload payload) {
        return userId + ":" + sha256(objectToStableJson(payload));
    }

    private String objectToStableJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "语法纠错输入序列化失败");
        }
    }

    private String sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder(bytes.length * 2);
            for (byte current : bytes) {
                builder.append(String.format("%02x", current));
            }
            return builder.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "语法纠错摘要计算失败");
        }
    }

    private void appendResumeText(StringBuilder resumeText, Map<String, String> moduleLabels, String type, String json) {
        if (json == null || json.isBlank()) {
            return;
        }
        String text = stripHtml(json);
        if (!text.isBlank()) {
            resumeText.append("【")
                    .append(moduleLabels.getOrDefault(type, type))
                    .append("】\n")
                    .append(text)
                    .append("\n\n");
        }
    }

    @SuppressWarnings("unchecked")
    private ResumeVO snapshotToResumeVO(ResumeShare share) {
        String snapshotJson = share.getSnapshotJson();
        if (snapshotJson == null || snapshotJson.isBlank()) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "分享快照不存在");
        }

        try {
            Map<String, Object> snapshot = objectMapper.readValue(snapshotJson, Map.class);
            ResumeVO vo = new ResumeVO();
            vo.setId(share.getResumeId());
            vo.setTitle((String) snapshot.getOrDefault("title", "分享简历"));
            vo.setCurrentTemplate((String) snapshot.getOrDefault("template", "classic"));
            vo.setStatus(1);
            vo.setStyleConfig(resolveSnapshotStyleConfig(snapshot));

            List<Map<String, Object>> contents = (List<Map<String, Object>>) snapshot.get("contents");
            if (contents != null) {
                List<ResumeVO.ResumeContentVO> contentVOList = contents.stream().map(c -> {
                    ResumeVO.ResumeContentVO cvo = new ResumeVO.ResumeContentVO();
                    cvo.setModuleType((String) c.get("moduleType"));
                    cvo.setContentJson((String) c.get("contentJson"));
                    Object sortOrder = c.get("sortOrder");
                    cvo.setSortOrder(sortOrder != null ? ((Number) sortOrder).intValue() : 0);
                    return cvo;
                }).toList();
                vo.setContents(contentVOList);
            }
            return vo;
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "分享快照解析失败");
        }
    }

    private String stripHtml(String json) {
        if (json == null || json.isBlank()) return "";
        try {
            Map<String, Object> map = objectMapper.readValue(json, Map.class);
            StringBuilder sb = new StringBuilder();
            appendMapValues(sb, map);
            return sb.toString();
        } catch (Exception e) {
            return json.replaceAll("<[^>]+>", " ").replaceAll("\\s+", " ").trim();
        }
    }

    private void validateSelfIntroArgs(Integer durationSeconds, String style) {
        if (durationSeconds == null || List.of(30, 60, 90).stream().noneMatch(v -> v.equals(durationSeconds))) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "时长参数错误");
        }
        if (style == null || style.isBlank()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "风格不能为空");
        }
    }

    private String normalizeSelfIntroStyle(String style) {
        return switch (style.trim()) {
            case "formal" -> "正式稳重";
            case "natural" -> "自然真诚";
            case "jobFocused" -> "偏岗位导向";
            case "正式稳重", "自然真诚", "偏岗位导向" -> style.trim();
            default -> "自然真诚";
        };
    }

    private String cleanupAiJson(String text) {
        String json = text == null ? "" : text.trim();
        if (json.startsWith("```")) {
            json = json.replaceAll("^```\\w*\\n?", "").replaceAll("\\n?```$", "");
        }
        return json.trim();
    }

    private String stringValue(Object value) {
        return value == null ? null : value.toString().trim();
    }

    private List<String> toStringList(Object value) {
        List<String> result = new ArrayList<>();
        if (value instanceof List<?> list) {
            for (Object item : list) {
                if (item != null) {
                    String text = item.toString().trim();
                    if (!text.isBlank()) {
                        result.add(text);
                    }
                }
            }
        }
        return result;
    }

    private Integer parseScore(Object value) {
        if (value instanceof Number number) {
            return Math.max(0, Math.min(100, number.intValue()));
        }
        if (value != null) {
            try {
                return Math.max(0, Math.min(100, (int) Math.round(Double.parseDouble(value.toString().trim()))));
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        return null;
    }

    private List<ResumeScoreVO.Dimension> toScoreDimensions(Object value) {
        List<ResumeScoreVO.Dimension> result = new ArrayList<>();
        if (!(value instanceof List<?> list)) {
            return result;
        }
        for (Object item : list) {
            if (!(item instanceof Map<?, ?> rawMap)) {
                continue;
            }
            ResumeScoreVO.Dimension dimension = new ResumeScoreVO.Dimension();
            dimension.setName(stringValue(rawMap.get("name")));
            dimension.setScore(parseScore(rawMap.get("score")));
            dimension.setAnalysis(stringValue(rawMap.get("analysis")));
            dimension.setSuggestion(stringValue(rawMap.get("suggestion")));
            if (dimension.getName() != null && dimension.getScore() != null) {
                result.add(dimension);
            }
        }
        return result;
    }

    private List<ResumeProofreadVO.Item> toProofreadItems(Object value, ProofreadInputPayload proofreadInputPayload) {
        List<ResumeProofreadVO.Item> result = new ArrayList<>();
        if (!(value instanceof List<?> list)) {
            return result;
        }
        int index = 1;
        for (Object item : list) {
            if (!(item instanceof Map<?, ?> rawMap)) {
                continue;
            }
            ResumeProofreadVO.Item issue = new ResumeProofreadVO.Item();
            issue.setId(stringValue(rawMap.get("id")));
            String candidateId = stringValue(rawMap.get("candidateId"));
            ProofreadFieldPayload matchedField = findProofreadCandidateById(proofreadInputPayload, candidateId);
            issue.setModuleType(normalizeProofreadModuleType(stringValue(rawMap.get("moduleType"))));
            if (matchedField != null && issue.getModuleType() == null) {
                issue.setModuleType(matchedField.moduleType());
            }
            issue.setFieldPath(normalizeProofreadFieldPath(stringValue(rawMap.get("fieldPath"))));
            if (matchedField != null && (issue.getFieldPath() == null || issue.getFieldPath().isBlank())) {
                issue.setFieldPath(normalizeProofreadFieldPath(matchedField.fieldPath()));
            }
            issue.setItemIndex(resolveProofreadItemIndex(
                    issue.getModuleType(),
                    matchedField != null && parseOptionalNonNegativeInt(rawMap.get("itemIndex")) == null
                            ? matchedField.itemIndex()
                            : parseOptionalNonNegativeInt(rawMap.get("itemIndex")),
                    issue.getFieldPath(),
                    stringValue(rawMap.get("original")),
                    proofreadInputPayload
            ));
            issue.setOccurrenceIndex(parseOptionalNonNegativeInt(rawMap.get("occurrenceIndex")));
            issue.setFieldLabel(normalizeProofreadFieldLabel(
                    matchedField != null ? matchedField.moduleLabel() : stringValue(rawMap.get("fieldLabel")),
                    issue.getModuleType()
            ));
            issue.setType(normalizeProofreadType(stringValue(rawMap.get("type"))));
            issue.setTypeLabel(normalizeProofreadTypeLabel(stringValue(rawMap.get("typeLabel")), issue.getType()));
            issue.setOriginal(stringValue(rawMap.get("original")));
            issue.setSuggestion(stringValue(rawMap.get("suggestion")));
            issue.setReason(stringValue(rawMap.get("reason")));
            if (issue.getId() == null || issue.getId().isBlank()) {
                issue.setId("proofread-" + index);
            }
            if (issue.getOriginal() == null || issue.getOriginal().isBlank() || issue.getSuggestion() == null || issue.getSuggestion().isBlank()) {
                continue;
            }
            if (issue.getOriginal().equals(issue.getSuggestion())) {
                continue;
            }
            if (isWhitespaceOnlyProofreadChange(issue.getOriginal(), issue.getSuggestion())) {
                continue;
            }
            if (shouldDropLowValueStyleProofread(issue)) {
                continue;
            }
            if (issue.getModuleType() == null) {
                issue.setModuleType("other");
            }
            if (issue.getFieldLabel() == null || issue.getFieldLabel().isBlank()) {
                issue.setFieldLabel(moduleLabelForProofread(issue.getModuleType()));
            }
            if (issue.getType() == null) {
                issue.setType("style");
            }
            if (issue.getTypeLabel() == null || issue.getTypeLabel().isBlank()) {
                issue.setTypeLabel(normalizeProofreadTypeLabel(null, issue.getType()));
            }
            if (issue.getOccurrenceIndex() == null) {
                issue.setOccurrenceIndex(0);
            }
            result.add(issue);
            index++;
        }
        return filterCrossItemMergedProofreadItems(result);
    }

    private String normalizeProofreadModuleType(String moduleType) {
        if (moduleType == null || moduleType.isBlank()) {
            return null;
        }
        return switch (moduleType.trim()) {
            case "basic", "education", "experience", "project", "skill", "personalStrengths", "award", "portfolio", "other" -> moduleType.trim();
            default -> null;
        };
    }

    private boolean isWhitespaceOnlyProofreadChange(String original, String suggestion) {
        return stripWhitespaceForProofread(original).equals(stripWhitespaceForProofread(suggestion));
    }

    private String stripWhitespaceForProofread(String text) {
        return text == null ? "" : text.replaceAll("\\s+", "");
    }

    private boolean shouldDropLowValueStyleProofread(ResumeProofreadVO.Item issue) {
        if (!"style".equals(issue.getType())) {
            return false;
        }
        String reason = issue.getReason();
        if (!StringUtils.hasText(reason)) {
            return false;
        }
        return reason.contains("空格")
                || reason.contains("换行")
                || reason.contains("缩进")
                || reason.contains("排版")
                || reason.contains("格式不一致")
                || reason.contains("统一格式");
    }

    private List<ResumeProofreadVO.Item> filterCrossItemMergedProofreadItems(List<ResumeProofreadVO.Item> issues) {
        if (issues.size() < 3) {
            return filterOverlappingProofreadItems(issues);
        }
        List<ResumeProofreadVO.Item> filtered = new ArrayList<>();
        for (ResumeProofreadVO.Item current : issues) {
            if (shouldDropCrossItemMergedProofreadItem(current, issues)) {
                continue;
            }
            filtered.add(current);
        }
        return filterOverlappingProofreadItems(filtered);
    }

    private boolean shouldDropCrossItemMergedProofreadItem(ResumeProofreadVO.Item current,
                                                           List<ResumeProofreadVO.Item> issues) {
        if (!"style".equals(current.getType()) && !"clarity".equals(current.getType())) {
            return false;
        }
        if (!StringUtils.hasText(current.getOriginal())) {
            return false;
        }
        String currentOriginal = current.getOriginal().trim();
        Set<String> containedDistinctOriginals = new LinkedHashSet<>();
        for (ResumeProofreadVO.Item other : issues) {
            if (other == current) {
                continue;
            }
            if (!Objects.equals(current.getModuleType(), other.getModuleType())
                    || !Objects.equals(current.getItemIndex(), other.getItemIndex())
                    || !Objects.equals(normalizeProofreadFieldPath(current.getFieldPath()), normalizeProofreadFieldPath(other.getFieldPath()))) {
                continue;
            }
            if (!StringUtils.hasText(other.getOriginal())) {
                continue;
            }
            String otherOriginal = other.getOriginal().trim();
            if (otherOriginal.equals(currentOriginal) || otherOriginal.length() < 2) {
                continue;
            }
            if (currentOriginal.contains(otherOriginal)) {
                containedDistinctOriginals.add(otherOriginal);
            }
        }
        return containedDistinctOriginals.size() >= 2;
    }

    private List<ResumeProofreadVO.Item> filterOverlappingProofreadItems(List<ResumeProofreadVO.Item> issues) {
        if (issues.size() < 2) {
            return issues;
        }
        List<ResumeProofreadVO.Item> filtered = new ArrayList<>();
        for (ResumeProofreadVO.Item current : issues) {
            if (shouldDropOverlappingProofreadItem(current, issues)) {
                continue;
            }
            filtered.add(current);
        }
        return filtered;
    }

    private boolean shouldDropOverlappingProofreadItem(ResumeProofreadVO.Item current,
                                                       List<ResumeProofreadVO.Item> issues) {
        if (!"style".equals(current.getType()) && !"clarity".equals(current.getType())) {
            return false;
        }
        if (!StringUtils.hasText(current.getOriginal()) || !StringUtils.hasText(current.getSuggestion())) {
            return false;
        }
        String currentOriginal = current.getOriginal().trim();
        String currentSuggestion = current.getSuggestion().trim();
        for (ResumeProofreadVO.Item other : issues) {
            if (other == current) {
                continue;
            }
            if (!"typo".equals(other.getType()) && !"grammar".equals(other.getType())) {
                continue;
            }
            if (!Objects.equals(current.getModuleType(), other.getModuleType())
                    || !Objects.equals(current.getItemIndex(), other.getItemIndex())
                    || !Objects.equals(normalizeProofreadFieldPath(current.getFieldPath()), normalizeProofreadFieldPath(other.getFieldPath()))) {
                continue;
            }
            if (!StringUtils.hasText(other.getOriginal()) || !StringUtils.hasText(other.getSuggestion())) {
                continue;
            }
            String otherOriginal = other.getOriginal().trim();
            String otherSuggestion = other.getSuggestion().trim();
            if (otherOriginal.length() < 2 || otherSuggestion.length() < 2) {
                continue;
            }
            if (currentOriginal.contains(otherOriginal) && currentSuggestion.contains(otherSuggestion)) {
                return true;
            }
        }
        return false;
    }

    private String normalizeProofreadType(String type) {
        if (type == null || type.isBlank()) {
            return null;
        }
        return switch (type.trim()) {
            case "typo", "grammar", "style", "clarity" -> type.trim();
            default -> null;
        };
    }

    private String normalizeProofreadTypeLabel(String typeLabel, String type) {
        if (typeLabel != null && !typeLabel.isBlank()) {
            return typeLabel.trim();
        }
        if (type == null) {
            return "纠错建议";
        }
        return switch (type) {
            case "typo" -> "错别字";
            case "grammar" -> "语法问题";
            case "style" -> "表达优化";
            case "clarity" -> "表达澄清";
            default -> "纠错建议";
        };
    }

    private String normalizeProofreadFieldLabel(String fieldLabel, String moduleType) {
        if (fieldLabel != null && !fieldLabel.isBlank()) {
            return fieldLabel.trim();
        }
        return moduleLabelForProofread(moduleType);
    }

    private ProofreadFieldPayload findProofreadCandidateById(ProofreadInputPayload proofreadInputPayload, String candidateId) {
        if (proofreadInputPayload == null || !StringUtils.hasText(candidateId)) {
            return null;
        }
        for (ProofreadModulePayload module : proofreadInputPayload.modules()) {
            for (ProofreadFieldPayload field : module.fields()) {
                if (Objects.equals(field.candidateId(), candidateId)) {
                    return field;
                }
            }
        }
        return null;
    }

    private String normalizeProofreadFieldPath(String fieldPath) {
        if (fieldPath == null) {
            return null;
        }
        String normalized = fieldPath.trim();
        if (normalized.isEmpty()) {
            return null;
        }
        int lastDot = normalized.lastIndexOf('.');
        return lastDot >= 0 && lastDot < normalized.length() - 1 ? normalized.substring(lastDot + 1) : normalized;
    }

    private Integer parseOptionalNonNegativeInt(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number number) {
            return Math.max(0, number.intValue());
        }
        String text = value.toString().trim();
        if (text.isEmpty()) {
            return null;
        }
        try {
            return Math.max(0, Integer.parseInt(text));
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private Integer normalizeProofreadItemIndex(String moduleType, Integer itemIndex) {
        if ("basic".equals(moduleType) || "personalStrengths".equals(moduleType)) {
            return null;
        }
        return itemIndex;
    }

    private Integer resolveProofreadItemIndex(String moduleType,
                                              Integer itemIndex,
                                              String fieldPath,
                                              String original,
                                              ProofreadInputPayload proofreadInputPayload) {
        Integer normalizedItemIndex = normalizeProofreadItemIndex(moduleType, itemIndex);
        if (normalizedItemIndex == null || proofreadInputPayload == null || !StringUtils.hasText(original)) {
            return normalizedItemIndex;
        }

        Set<Integer> exactFieldCandidates = findProofreadCandidateItemIndexes(proofreadInputPayload, moduleType, fieldPath, original);
        Integer resolvedFromExactField = resolveProofreadItemIndexFromCandidates(normalizedItemIndex, exactFieldCandidates);
        if (resolvedFromExactField != null) {
            return resolvedFromExactField;
        }

        Set<Integer> looseFieldCandidates = findProofreadCandidateItemIndexes(proofreadInputPayload, moduleType, null, original);
        Integer resolvedFromLooseField = resolveProofreadItemIndexFromCandidates(normalizedItemIndex, looseFieldCandidates);
        if (resolvedFromLooseField != null) {
            return resolvedFromLooseField;
        }

        return normalizedItemIndex;
    }

    private Integer resolveProofreadItemIndexFromCandidates(Integer itemIndex, Set<Integer> candidates) {
        if (candidates.isEmpty()) {
            return null;
        }
        if (candidates.contains(itemIndex)) {
            return itemIndex;
        }
        if (itemIndex > 0 && candidates.contains(itemIndex - 1) && !candidates.contains(itemIndex)) {
            return itemIndex - 1;
        }
        return candidates.size() == 1 ? candidates.iterator().next() : null;
    }

    private Set<Integer> findProofreadCandidateItemIndexes(ProofreadInputPayload proofreadInputPayload,
                                                           String moduleType,
                                                           String fieldPath,
                                                           String original) {
        Set<Integer> candidates = new LinkedHashSet<>();
        String normalizedFieldPath = normalizeProofreadFieldPath(fieldPath);
        for (ProofreadModulePayload module : proofreadInputPayload.modules()) {
            if (!Objects.equals(module.moduleType(), moduleType)) {
                continue;
            }
            for (ProofreadFieldPayload field : module.fields()) {
                if (field.itemIndex() == null || !StringUtils.hasText(field.text())) {
                    continue;
                }
                if (normalizedFieldPath != null && !Objects.equals(normalizeProofreadFieldPath(field.fieldPath()), normalizedFieldPath)) {
                    continue;
                }
                if (!field.text().contains(original)) {
                    continue;
                }
                candidates.add(field.itemIndex());
            }
        }
        return candidates;
    }

    private String moduleLabelForProofread(String moduleType) {
        if (moduleType == null) {
            return "简历内容";
        }
        return switch (moduleType) {
            case "basic" -> "基本信息";
            case "education" -> "教育经历";
            case "experience" -> "工作经历";
            case "project" -> "项目经历";
            case "skill" -> "专业技能";
            case "personalStrengths" -> "个人优势";
            case "award" -> "荣誉奖项";
            case "portfolio" -> "个人作品";
            case "other" -> "其他经历";
            default -> "简历内容";
        };
    }

    private String proofreadFieldLabel(String moduleType, Integer itemIndex, String fieldPath) {
        String moduleLabel = moduleLabelForProofread(moduleType);
        if (itemIndex == null) {
            return moduleLabel + (StringUtils.hasText(fieldPath) ? "-" + fieldPath : "");
        }
        return moduleLabel + "-第" + (itemIndex + 1) + "条" + (StringUtils.hasText(fieldPath) ? "-" + fieldPath : "");
    }

    @SuppressWarnings("unchecked")
    private void appendMapValues(StringBuilder sb, Map<String, Object> map) {
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            if (value == null) continue;
            if (value instanceof Map) {
                sb.append(key).append(": ");
                appendMapValues(sb, (Map<String, Object>) value);
                sb.append("\n");
            } else if (value instanceof List) {
                for (Object item : (List<?>) value) {
                    if (item instanceof Map) {
                        appendMapValues(sb, (Map<String, Object>) item);
                    } else if (item != null) {
                        sb.append(item).append("\n");
                    }
                }
            } else {
                String text = value.toString().replaceAll("<[^>]+>", " ").replaceAll("\\s+", " ").trim();
                if (!text.isEmpty() && !text.equals("{}") && !text.equals("[]")) {
                    sb.append(key).append(": ").append(text).append("\n");
                }
            }
        }
    }

    private String resolveStyleConfig(String styleConfigJson, List<ResumeContent> contents) {
        if (StringUtils.hasText(styleConfigJson)) {
            return styleConfigJson;
        }
        if (CollectionUtils.isEmpty(contents)) {
            return null;
        }
        for (ResumeContent content : contents) {
            if ("basic".equals(content.getModuleType())) {
                return extractLegacyStyleConfig(content.getContentJson());
            }
        }
        return null;
    }

    private void syncResumeContents(Long resumeId, List<ResumeContentDTO> contentDTOs) {
        QueryWrapper<ResumeContent> contentWrapper = new QueryWrapper<>();
        contentWrapper.eq("resumeId", resumeId);
        List<ResumeContent> existingContents = resumeContentMapper.selectList(contentWrapper);

        Map<String, ResumeContent> existingByModuleType = new HashMap<>();
        for (ResumeContent existingContent : existingContents) {
            existingByModuleType.put(existingContent.getModuleType(), existingContent);
        }

        Set<String> incomingModuleTypes = new HashSet<>();
        for (ResumeContentDTO dto : contentDTOs) {
            String moduleType = dto.getModuleType();
            incomingModuleTypes.add(moduleType);

            ResumeContent existingContent = existingByModuleType.get(moduleType);
            if (existingContent != null) {
                existingContent.setContentJson(dto.getContentJson());
                existingContent.setSortOrder(dto.getSortOrder() != null ? dto.getSortOrder() : 0);
                resumeContentMapper.updateById(existingContent);
                continue;
            }

            ResumeContent newContent = new ResumeContent();
            newContent.setResumeId(resumeId);
            newContent.setModuleType(moduleType);
            newContent.setContentJson(dto.getContentJson());
            newContent.setSortOrder(dto.getSortOrder() != null ? dto.getSortOrder() : 0);
            resumeContentMapper.insert(newContent);
        }

        for (ResumeContent existingContent : existingContents) {
            if (!incomingModuleTypes.contains(existingContent.getModuleType())) {
                resumeContentMapper.deleteById(existingContent.getId());
            }
        }
    }

    @SuppressWarnings("unchecked")
    private String resolveSnapshotStyleConfig(Map<String, Object> snapshot) {
        Object styleConfig = snapshot.get("styleConfig");
        if (styleConfig instanceof String styleConfigJson && StringUtils.hasText(styleConfigJson)) {
            return styleConfigJson;
        }
        Object rawContents = snapshot.get("contents");
        if (!(rawContents instanceof List<?> contents)) {
            return null;
        }
        for (Object item : contents) {
            if (!(item instanceof Map<?, ?> contentMap)) {
                continue;
            }
            Object moduleType = contentMap.get("moduleType");
            if (!"basic".equals(moduleType)) {
                continue;
            }
            Object basicContentJson = contentMap.get("contentJson");
            if (basicContentJson instanceof String json) {
                return extractLegacyStyleConfig(json);
            }
        }
        return null;
    }

    private String extractLegacyStyleConfig(String basicContentJson) {
        if (!StringUtils.hasText(basicContentJson)) {
            return null;
        }
        try {
            Map<String, Object> basic = objectMapper.readValue(basicContentJson, Map.class);
            Map<String, Object> styleConfig = new java.util.LinkedHashMap<>();
            putStyleValue(styleConfig, "themeColor", basic.get("themeColor"));
            putStyleValue(styleConfig, "richFontSize", basic.get("_richFontSize"));
            putStyleValue(styleConfig, "richFontFamily", basic.get("_richFontFamily"));
            putStyleValue(styleConfig, "richLineHeight", basic.get("_richLineHeight"));
            if (styleConfig.isEmpty()) {
                return null;
            }
            return objectMapper.writeValueAsString(styleConfig);
        } catch (Exception e) {
            return null;
        }
    }

    private void putStyleValue(Map<String, Object> styleConfig, String key, Object value) {
        if (value == null) {
            return;
        }
        if (value instanceof String text && !StringUtils.hasText(text)) {
            return;
        }
        styleConfig.put(key, value);
    }

    private record ProofreadInputPayload(List<ProofreadModulePayload> modules) {
    }

    private record ProofreadModulePayload(String moduleType, String fieldLabel, List<ProofreadFieldPayload> fields) {
    }

    private record ProofreadFieldPayload(String candidateId,
                                         String label,
                                         String moduleType,
                                         Integer itemIndex,
                                         String fieldPath,
                                         String text) {
        private String moduleLabel() {
            int lastDash = label == null ? -1 : label.indexOf('-');
            return lastDash > 0 ? label.substring(0, lastDash) : label;
        }
    }

    private record CachedProofreadResult(ResumeProofreadVO value, Instant expiresAt) {
    }
}
