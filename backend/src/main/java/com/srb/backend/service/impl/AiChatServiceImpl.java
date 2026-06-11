package com.srb.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.srb.backend.ai.DeepSeekClient;
import com.srb.backend.ai.DeepSeekMessage;
import com.srb.backend.common.BusinessException;
import com.srb.backend.common.ErrorCode;
import com.srb.backend.common.SessionUtils;
import com.srb.backend.mapper.ResumeChatMapper;
import com.srb.backend.mapper.ResumeContentMapper;
import com.srb.backend.mapper.ResumeMapper;
import com.srb.backend.mapper.UserMapper;
import com.srb.backend.mapper.UserMemoryMapper;
import com.srb.backend.model.entity.Resume;
import com.srb.backend.model.entity.ResumeChat;
import com.srb.backend.model.entity.ResumeContent;
import com.srb.backend.model.entity.User;
import com.srb.backend.model.entity.UserMemory;
import com.srb.backend.service.AiChatService;
import com.srb.backend.support.AiChatTelemetry;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiChatServiceImpl implements AiChatService {

    private static final String CHAT_MODE = "chat";
    private static final String MODIFY_MODE = "modify";
    private static final String DEEPSEEK_MODEL = "deepseek-v4-flash";
    private static final int CHAT_MODE_MAX_TOKENS = 1200;
    private static final int MODIFY_MODE_MAX_TOKENS = 1600;
    private static final int AUXILIARY_MAX_TOKENS = 1200;
    private static final int MEMORY_COMPRESS_MAX_TOKENS = 1600;
    private static final int MAX_SKILL_MEMORIES_PER_EXTRACTION = 1;
    private static final int MEMORY_DUPLICATE_CHECK_LIMIT = 10;
    private static final double MEMORY_KEYWORD_SIMILARITY_THRESHOLD = 0.6d;
    private static final double SKILL_MEMORY_KEYWORD_SIMILARITY_THRESHOLD = 0.75d;
    private static final int MIN_SKILL_MEMORY_LENGTH = 8;
    private static final int MAX_SKILL_MEMORIES_PER_USER = 5;
    private static final int MEMORY_COMPRESS_TRIGGER_THRESHOLD = 20;
    private static final Pattern MEMORY_TOKEN_SPLIT_PATTERN = Pattern.compile("[^\\p{IsHan}a-z0-9+#./-]+");
    private static final Set<String> SKILL_SOFT_TRAIT_BLACKLIST = Set.of(
            "自驱力", "学习能力", "产品体验", "工程质量", "细节优化", "需求拆解", "独立拆解需求", "组件实现", "功能开发"
    );

    private final DeepSeekClient deepSeekClient;
    private final ResumeChatMapper resumeChatMapper;
    private final ResumeMapper resumeMapper;
    private final ResumeContentMapper resumeContentMapper;
    private final UserMapper userMapper;
    private final UserMemoryMapper userMemoryMapper;

    private final ExecutorService executor = Executors.newCachedThreadPool();

    @Override
    public void chatStream(Long resumeId, String userMessage, String mode, Map<String, String> moduleData, SseEmitter emitter, HttpServletRequest request) {
        executor.submit(() -> {
            try {
                User loginUser = SessionUtils.getLoginUser(request, userMapper);
                String normalizedMode = normalizeChatMode(mode);
                boolean modifyMode = isModifyMode(normalizedMode);

                // resumeId 为空时跳过简历校验和对话历史持久化（新简历未保存场景）
                final boolean persist = resumeId != null;
                AiChatTelemetry telemetry = new AiChatTelemetry(
                        resumeId,
                        loginUser.getId(),
                        persist,
                        normalizedMode,
                        userMessage == null ? 0 : userMessage.length(),
                        moduleData == null ? 0 : moduleData.size()
                );
                log.info(telemetry.requestReceivedLog());
                Resume resume = null;
                if (persist) {
                    resume = resumeMapper.selectById(resumeId);
                    if (resume == null) {
                        sendError(emitter, "简历不存在");
                        return;
                    }
                    if (!resume.getUserId().equals(loginUser.getId())) {
                        sendError(emitter, "无权操作此简历");
                        return;
                    }

                    // 保存用户消息
                    ResumeChat userChat = new ResumeChat();
                    userChat.setResumeId(resumeId);
                    userChat.setUserId(loginUser.getId());
                    userChat.setRole("user");
                    userChat.setContent(userMessage);
                    userChat.setCreateTime(LocalDateTime.now());
                    resumeChatMapper.insert(userChat);
                }

                // 构建消息列表（使用前端实时 moduleData 作为上下文）
                List<DeepSeekMessage> messages = new ArrayList<>();
                PromptBuildResult promptBuildResult = buildSystemPrompt(normalizedMode, userMessage, moduleData, loginUser.getId());
                String systemPrompt = promptBuildResult.systemPrompt();
                messages.add(DeepSeekMessage.system(systemPrompt));
                List<DeepSeekMessage> history = loadHistoryMessages(resumeId, persist);
                messages.addAll(history);
                messages.add(DeepSeekMessage.user(userMessage));
                log.info(telemetry.promptBuiltLog(
                        systemPrompt.length(),
                        promptBuildResult.filledModuleCount(),
                        promptBuildResult.memoryCount(),
                        history.size(),
                        messages.size()
                ));

                log.info("AI 请求上下文大小: systemPrompt={}字符, history={}条, userMessage={}字符, 总消息数={}",
                        systemPrompt.length(), history.size(), userMessage.length(), messages.size());
                log.info("系统提示词内容:\n{}", systemPrompt);

                // 调用 AI（真流式）
                long startTime = System.currentTimeMillis();
                StringBuilder fullContent = new StringBuilder();
                boolean[] done = {false};
                boolean[] retried = {false};
                Flux<String> flux = streamDeepSeek(messages, normalizedMode);
                flux.doOnNext(text -> {
                    try {
                        if (text != null && !text.isEmpty()) {
                            if (fullContent.isEmpty()) {
                                telemetry.markFirstToken();
                                log.info(telemetry.firstTokenLog());
                            }
                            fullContent.append(text);
                            if (!modifyMode) {
                                emitter.send(SseEmitter.event()
                                        .data("{\"content\":\"" + escapeJson(text) + "\"}"));
                            }
                        }
                    } catch (Exception e) {
                        done[0] = true;
                    }
                }).doOnComplete(() -> {
                    if (done[0]) return;
                    done[0] = true;
                    String rawContent = fullContent.toString();

                    List<String> suggestList = new ArrayList<>();

                    if (modifyMode) {
                        // 解析修改指令（支持多模块）
                        suggestList = extractModifyCommands(rawContent);

                        // 如果 AI 暗示做了修改但没有输出 RESUME_MODIFY 标记，补充调用一次
                        if (suggestList.isEmpty() && shouldRetryForModify(rawContent)) {
                            retried[0] = true;
                            long retryStartAt = System.currentTimeMillis();
                            log.info(telemetry.retryTriggeredLog("missing_resume_modify"));
                            log.info("AI 暗示修改了简历但未输出 RESUME_MODIFY，尝试补充调用...");
                            suggestList = retryExtractModify(moduleData, loginUser.getId(), rawContent, userMessage);
                            log.info(telemetry.retryCompletedLog(
                                    System.currentTimeMillis() - retryStartAt,
                                    !suggestList.isEmpty(),
                                    suggestList.size()
                            ));
                        }
                    }

                    // 保存 AI 回复（去掉指令标签）
                    String displayContent = rawContent.replaceAll("(?s)<!--RESUME_MODIFY-->.*?<!--/RESUME_MODIFY-->", "").trim();
                    if (modifyMode && displayContent.isEmpty() && !suggestList.isEmpty()) {
                        displayContent = buildModifySummary(suggestList);
                    }
                    if (persist) {
                        ResumeChat assistantChat = new ResumeChat();
                        assistantChat.setResumeId(resumeId);
                        assistantChat.setUserId(loginUser.getId());
                        assistantChat.setRole("assistant");
                        assistantChat.setContent(displayContent);
                        assistantChat.setCreateTime(LocalDateTime.now());
                        resumeChatMapper.insert(assistantChat);
                    }

                    try {
                        if (modifyMode && !displayContent.isEmpty()) {
                            emitter.send(SseEmitter.event()
                                    .data("{\"content\":\"" + escapeJson(displayContent) + "\"}"));
                        }
                        for (String suggestData : suggestList) {
                            emitter.send(SseEmitter.event().data(suggestData));
                        }
                        log.info(telemetry.streamDoneLog(displayContent.length(), suggestList.size(), retried[0]));
                        emitter.send(SseEmitter.event().data("[DONE]"));
                        emitter.complete();
                        scheduleMemoryExtraction(loginUser.getId(), userMessage, fullContent.toString(), telemetry);
                    } catch (IOException e) {
                        emitter.completeWithError(e);
                    }
                }).doOnError(e -> {
                    if (done[0]) return;
                    done[0] = true;
                    log.warn("AI 流式异常（可能客户端断开）: {}", e.getMessage());
                    try { emitter.completeWithError(e); } catch (Exception ignored) {}
                }).subscribe();

            } catch (Exception e) {
                log.error("AI 对话异常", e);
                sendError(emitter, "AI 服务异常: " + e.getMessage());
            }
        });
    }

    private void scheduleMemoryExtraction(Long userId, String userMessage, String assistantReply, AiChatTelemetry telemetry) {
        executor.submit(() -> {
            try {
                long memoryStartAt = System.currentTimeMillis();
                MemoryExtractResult memoryExtractResult = extractMemory(userId, userMessage, assistantReply);
                long memoryLatency = System.currentTimeMillis() - memoryStartAt;
                log.info(telemetry.memoryExtractDoneLog(
                        memoryLatency,
                        memoryExtractResult.insertedCount(),
                        memoryExtractResult.dedupedCount(),
                        memoryExtractResult.compressResult() != null,
                        memoryExtractResult.totalBeforeCompress(),
                        memoryExtractResult.totalAfterCompress()
                ));
                if (memoryExtractResult.compressResult() != null) {
                    log.info(telemetry.memoryCompressDoneLog(
                            memoryExtractResult.compressResult().latencyMs(),
                            memoryExtractResult.compressResult().totalBeforeCompress(),
                            memoryExtractResult.compressResult().totalAfterCompress()
                    ));
                }
            } catch (Exception e) {
                log.warn("异步提取用户记忆失败: userId={}, error={}", userId, e.getMessage());
            }
        });
    }

    @Override
    public List<ResumeChat> getChatHistory(Long resumeId, HttpServletRequest request) {
        SessionUtils.getLoginUser(request, userMapper);
        QueryWrapper<ResumeChat> qw = new QueryWrapper<>();
        qw.eq("resumeId", resumeId);
        qw.orderByAsc("createTime");
        return resumeChatMapper.selectList(qw);
    }

    @Override
    public void clearChatHistory(Long resumeId, HttpServletRequest request) {
        User loginUser = SessionUtils.getLoginUser(request, userMapper);
        Resume resume = resumeMapper.selectById(resumeId);
        if (resume == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "简历不存在");
        }
        if (!resume.getUserId().equals(loginUser.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权操作此简历");
        }
        QueryWrapper<ResumeChat> qw = new QueryWrapper<>();
        qw.eq("resumeId", resumeId);
        resumeChatMapper.delete(qw);
    }

    @Override
    public List<UserMemory> getMemoryList(HttpServletRequest request) {
        User loginUser = SessionUtils.getLoginUser(request, userMapper);
        QueryWrapper<UserMemory> qw = new QueryWrapper<>();
        qw.eq("userId", loginUser.getId());
        qw.orderByDesc("updateTime");
        return userMemoryMapper.selectList(qw);
    }

    @Override
    public void deleteMemory(Long memoryId, HttpServletRequest request) {
        User loginUser = SessionUtils.getLoginUser(request, userMapper);
        UserMemory mem = userMemoryMapper.selectById(memoryId);
        if (mem == null) throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "记忆不存在");
        if (!mem.getUserId().equals(loginUser.getId())) throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权操作");
        userMemoryMapper.deleteById(memoryId);
    }

    @Override
    public void clearMemory(HttpServletRequest request) {
        User loginUser = SessionUtils.getLoginUser(request, userMapper);
        QueryWrapper<UserMemory> qw = new QueryWrapper<>();
        qw.eq("userId", loginUser.getId());
        userMemoryMapper.delete(qw);
    }

    private void sendError(SseEmitter emitter, String message) {
        try {
            emitter.send(SseEmitter.event().data("{\"error\":\"" + escapeJson(message) + "\"}"));
            emitter.complete();
        } catch (IOException e) {
            emitter.completeWithError(e);
        }
    }

    private String escapeJson(String text) {
        return text.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    private static final java.util.regex.Pattern MODIFY_PATTERN =
            java.util.regex.Pattern.compile("<!--RESUME_MODIFY-->(.*?)<!--/RESUME_MODIFY-->", java.util.regex.Pattern.DOTALL);

    private static final com.fasterxml.jackson.databind.ObjectMapper OBJECT_MAPPER = new com.fasterxml.jackson.databind.ObjectMapper();

    private static final java.util.Set<String> ARRAY_MODULES = java.util.Set.of(
            "education", "experience", "project", "skill", "award", "portfolio", "other");

    private List<String> extractModifyCommands(String content) {
        List<String> results = new ArrayList<>();
        java.util.regex.Matcher m = MODIFY_PATTERN.matcher(content);
        if (!m.find()) {
            log.info("AI 回复中未检测到 RESUME_MODIFY 标记");
            return results;
        }
        do {
            String raw = m.group(1).trim();
            if (raw.isEmpty()) continue;
            try {
                String[] lines = raw.split("\\n");
                if (lines.length < 2) {
                    log.warn("修改指令格式错误（缺少换行分隔）: {}", raw.substring(0, Math.min(raw.length(), 100)));
                    continue;
                }
                String moduleTypeLine = lines[0].trim();
                if (!moduleTypeLine.startsWith("moduleType:")) {
                    log.warn("修改指令格式错误（首行缺少 moduleType:）: {}", moduleTypeLine);
                    continue;
                }
                String moduleType = moduleTypeLine.substring("moduleType:".length()).trim();
                Integer itemIndex = null;
                int contentStartLine = 1;
                if (lines[1].trim().startsWith("itemIndex:")) {
                    String itemIndexText = lines[1].trim().substring("itemIndex:".length()).trim();
                    try {
                        itemIndex = Integer.parseInt(itemIndexText);
                    } catch (NumberFormatException e) {
                        log.warn("修改指令 itemIndex 非法: moduleType={}, itemIndex={}", moduleType, itemIndexText);
                        continue;
                    }
                    if (itemIndex < 0) {
                        log.warn("修改指令 itemIndex 不能为负数: moduleType={}, itemIndex={}", moduleType, itemIndex);
                        continue;
                    }
                    contentStartLine = 2;
                }
                String moduleContent = String.join("\n", java.util.Arrays.copyOfRange(lines, contentStartLine, lines.length)).trim();

                if (moduleType.isEmpty() || moduleContent.isEmpty()) {
                    log.warn("修改指令 moduleType 或 content 为空: moduleType={}", moduleType);
                    continue;
                }

                // 验证 content 是有效 JSON
                try {
                    OBJECT_MAPPER.readTree(moduleContent);
                } catch (Exception e) {
                    log.warn("修改指令 content JSON 无效: moduleType={}, error={}", moduleType, e.getMessage());
                    log.info("【调试】无效 content 前200字符: {}", moduleContent.substring(0, Math.min(moduleContent.length(), 200)));
                    continue;
                }

                // 包装成该模块期望的 JSON 结构
                String wrapped;
                if ("personalStrengths".equals(moduleType)) {
                    if (moduleContent.trim().startsWith("{")) {
                        wrapped = moduleContent;
                    } else {
                        wrapped = "{\"content\":\"" + escapeJson(moduleContent) + "\"}";
                    }
                } else if ("basic".equals(moduleType)) {
                    wrapped = moduleContent;
                } else {
                    moduleContent = moduleContent.trim();
                    if (itemIndex != null) {
                        if (!moduleContent.startsWith("{") && !moduleContent.startsWith("[")) {
                            log.warn("AI 返回的 {} 模块定向内容不是合法 JSON 对象/数组，跳过", moduleType);
                            continue;
                        }
                        wrapped = moduleContent;
                    } else if (!moduleContent.startsWith("[")) {
                        log.warn("AI 返回的 {} 模块内容不是数组格式，跳过", moduleType);
                        continue;
                    } else {
                        wrapped = moduleContent;
                    }
                }

                String suggestData = "{\"type\":\"suggest\",\"moduleType\":\"" + escapeJson(moduleType) + "\",\"content\":\"" + escapeJson(wrapped) + "\""
                        + (itemIndex != null ? ",\"itemIndex\":" + itemIndex : "")
                        + "}";
                log.info("解析修改指令成功: moduleType={}, itemIndex={}, contentLength={}", moduleType, itemIndex, moduleContent.length());
                results.add(suggestData);
            } catch (Exception e) {
                log.warn("解析修改指令失败: {}", e.getMessage());
            }
        } while (m.find());
        return results;
    }

    private boolean shouldRetryForModify(String content) {
        String lower = content.toLowerCase();
        String[] hints = {"已为您生成", "已为您修改", "已修改", "已生成", "修改指令如下", "已更新", "已优化", "已润色"};
        for (String hint : hints) {
            if (lower.contains(hint)) return true;
        }
        return false;
    }

    private List<String> retryExtractModify(Map<String, String> moduleData, Long userId, String aiResponse, String userMessage) {
        List<String> results = new ArrayList<>();
        try {
            String retryPrompt = """
                你之前的回复提到了修改/生成简历内容，但没有按照要求的格式输出修改指令。
                用户原始请求：%s

                你之前的回复：
                %s

                请严格按照系统提示中的格式要求，直接输出修改指令（<!--RESUME_MODIFY-->...<!--/RESUME_MODIFY-->），格式为：
                第一行 moduleType: 模块类型
                如果是数组模块中的单条定向修改，第二行输出 itemIndex: 目标下标
                随后再输出 content JSON
                不要输出任何说明文字。
                """.formatted(userMessage, aiResponse);

            List<DeepSeekMessage> msgs = new ArrayList<>();
            String sysPrompt = buildSystemPrompt(MODIFY_MODE, userMessage, moduleData, userId).systemPrompt();
            msgs.add(DeepSeekMessage.system(sysPrompt));
            msgs.add(DeepSeekMessage.user(retryPrompt));
            String retryText = callDeepSeek(msgs, AUXILIARY_MAX_TOKENS);

            log.info("补充调用结果: {}", retryText);
            results = extractModifyCommands(retryText);
            if (results.isEmpty()) {
                log.warn("补充调用仍未提取到 RESUME_MODIFY 标记");
            }
        } catch (Exception e) {
            log.warn("补充调用失败: {}", e.getMessage());
        }
        return results;
    }

    private String fixUnescapedJson(String json) {
        // 修复 AI 返回的 JSON 中 content 字段内部特殊字符未转义的问题
        // 例如: {"moduleType":"personalStrengths","content":"{"content":"<p>...</p>"}"}
        // 修复为: {"moduleType":"personalStrengths","content":"{\"content\":\"<p>...</p>\"}"}
        java.util.regex.Matcher typeMatcher = java.util.regex.Pattern.compile("\"moduleType\"\\s*:\\s*\"(\\w+)\"").matcher(json);
        if (!typeMatcher.find()) return json;
        String moduleType = typeMatcher.group(1);

        // 使用 DOTALL 模式，让 . 匹配包括换行符在内的所有字符
        java.util.regex.Matcher contentMatcher = java.util.regex.Pattern.compile("\"content\"\\s*:\\s*\"(.*)\"\\s*\\}\\s*$", java.util.regex.Pattern.DOTALL).matcher(json);
        if (!contentMatcher.find()) return json;
        String content = contentMatcher.group(1);

        // 转义 content 中的反斜杠、双引号、换行符、回车符、制表符
        content = content.replace("\\", "\\\\")
                         .replace("\"", "\\\"")
                         .replace("\n", "\\n")
                         .replace("\r", "\\r")
                         .replace("\t", "\\t");
        return "{\"moduleType\":\"" + moduleType + "\",\"content\":\"" + content + "\"}";
    }

    private String fixTruncatedJson(String json) {
        // AI 输出被截断时，JSON 不完整。尝试补全闭合结构。
        if (json == null || json.length() < 10) return json;
        // 提取 moduleType
        java.util.regex.Matcher typeMatcher = java.util.regex.Pattern.compile("\"moduleType\"\\s*:\\s*\"(\\w+)\"").matcher(json);
        if (!typeMatcher.find()) return json;
        String moduleType = typeMatcher.group(1);
        // 找到 "content": 的位置，取其后的所有内容
        int contentIdx = json.indexOf("\"content\"");
        if (contentIdx < 0) return json;
        // 跳过 "content": 部分
        int colonIdx = json.indexOf(':', contentIdx);
        if (colonIdx < 0) return json;
        String rest = json.substring(colonIdx + 1).trim();
        if (!rest.startsWith("\"")) return json;
        // 去掉开头和可能的结尾引号，获取原始 content
        String rawContent;
        if (rest.endsWith("\"}")) {
            // 可能是完整的但最后被截断
            rawContent = rest.substring(1, rest.length() - 2);
        } else {
            rawContent = rest.substring(1);
        }
        // 对截断的 content 补全未闭合的结构
        int openBraces = 0;
        for (char c : rawContent.toCharArray()) {
            if (c == '{') openBraces++;
            else if (c == '}') openBraces--;
        }
        for (int i = 0; i < openBraces; i++) rawContent += "}";
        // 补全未闭合的引号
        int quoteCount = 0;
        for (char c : rawContent.toCharArray()) {
            if (c == '"' && (rawContent.indexOf(c) == 0 || rawContent.charAt(rawContent.indexOf(c) - 1) != '\\')) {
                quoteCount++;
            }
        }
        if (quoteCount % 2 != 0) rawContent += "\"";
        return "{\"moduleType\":\"" + moduleType + "\",\"content\":\"" + rawContent + "\"}";
    }

    private static final List<String> MODULE_ORDER = List.of(
            "basic", "education", "experience", "project", "skill", "personalStrengths", "award", "portfolio", "other");

    private static final List<String> DEFAULT_PROMPT_MODULES = List.of(
            "basic", "experience", "project", "skill", "personalStrengths");

    private static final List<String> ALWAYS_INCLUDED_MODULES = List.of("basic");

    private static final Set<String> GLOBAL_OPTIMIZE_KEYWORDS = Set.of(
            "整体", "整份", "全篇", "全部", "统一", "全面", "完整", "整体优化", "整体润色", "全局"
    );

    private static final Set<String> MODIFY_INTENT_KEYWORDS = Set.of(
            "修改", "优化", "润色", "改写", "重写", "补充", "完善", "扩写", "精简"
    );

    private static final Set<String> FUZZY_SCOPE_KEYWORDS = Set.of(
            "这块", "这段", "这部分", "这里", "简历", "内容"
    );

    private static final Map<String, String> MODULE_LABELS = Map.of(
            "basic", "基本信息",
            "education", "教育经历",
            "experience", "工作经历",
            "project", "项目经历",
            "skill", "专业技能",
            "personalStrengths", "个人优势",
            "award", "荣誉奖项",
            "portfolio", "作品集",
            "other", "其他经历"
    );

    private static final Map<String, Set<String>> EXACT_MODULE_KEYWORDS = Map.of(
            "basic", Set.of("基本信息", "个人信息", "联系方式", "求职意向"),
            "education", Set.of("教育经历", "学历", "学校", "学位"),
            "experience", Set.of("工作经历", "实习经历", "公司经历", "岗位经历", "职业经历"),
            "project", Set.of("项目经历", "项目经验", "项目亮点"),
            "skill", Set.of("专业技能", "技能", "技术栈", "技术能力", "前端技能", "后端技能"),
            "personalStrengths", Set.of("个人优势", "个人亮点", "自我介绍"),
            "award", Set.of("荣誉奖项", "奖项", "荣誉", "获奖"),
            "portfolio", Set.of("作品集", "个人作品", "博客", "github", "开源作品"),
            "other", Set.of("其他经历", "社团经历", "竞赛经历", "实践经历")
    );

    private PromptBuildResult buildSystemPrompt(String mode, String userMessage, Map<String, String> moduleData, Long userId) {
        return isModifyMode(mode)
                ? buildModifySystemPrompt(userMessage, moduleData, userId)
                : buildChatSystemPrompt(userMessage, moduleData, userId);
    }

    private PromptBuildResult buildModifySystemPrompt(String userMessage, Map<String, String> moduleData, Long userId) {
        StringBuilder sb = new StringBuilder();
        sb.append("你是简历编辑助手。只围绕简历内容改写；[空] 表示未填写。");
        int filledModuleCount = 0;
        ModuleSelection selection = selectRelevantModules(userMessage);

        for (String type : selection.modules()) {
            String label = MODULE_LABELS.getOrDefault(type, type);
            String value = moduleData != null ? moduleData.get(type) : null;
            if (value != null && !value.trim().isEmpty()) {
                filledModuleCount++;
                sb.append("\n【").append(label).append("(").append(type).append(")】")
                        .append(cleanHtml(compactModuleContent(type, value, selection)));
            } else {
                sb.append("\n【").append(label).append("(").append(type).append(")】[空]");
            }
        }

        int memoryCount = appendUserMemories(sb, userId);

        sb.append("""

规则：
1. 已填模块直接基于现有内容修改；[空] 模块可结合已知信息补全
2. 只要用户要改简历，就直接输出修改指令，不要先解释、反问或确认
3. 修改指令必须包在 <!--RESUME_MODIFY-->...<!--/RESUME_MODIFY--> 内，且块内第一行必须是 `moduleType: 模块类型`
4. 若只修改数组模块中的单条内容，可在第二行输出 `itemIndex: 0-based下标`，随后仅输出该条对象 JSON；否则直接输出完整 JSON
5. 仅纯咨询/闲聊才不输出修改指令
6. JSON 必须合法，内容需可直接应用

模块类型及字段结构：
- basic：{"name":"姓名","phone":"电话","email":"邮箱","status":"求职状态","jobTitle":"求职意向","location":"所在城市","avatar":"头像URL"}
- education：[{"school":"学校","major":"专业","degree":"学历","startDate":"开始时间","endDate":"结束时间","description":"<p>描述</p>"}]
- experience：[{"company":"公司","department":"部门","position":"职位","city":"城市","startDate":"开始时间","endDate":"结束时间","content":"<ul><li>描述</li></ul>"}]
- project：[{"name":"项目名","role":"角色","city":"城市","link":"链接","startDate":"开始时间","endDate":"结束时间","content":"<p>概述</p><ul><li>细节</li></ul>"}]
- skill：[{"name":"技能类别","content":"<ul><li>具体技能</li></ul>"}]
- personalStrengths：{"content":"<p>个人优势段落</p>"}
- award：[{"name":"奖项名","date":"日期","content":"描述"}]
- portfolio：[{"name":"作品名","link":"链接","image":"图片URL","content":"<p>描述</p>"}]
- other：[{"name":"名称","role":"角色","department":"部门","city":"城市","startDate":"开始时间","endDate":"结束时间","content":"<p>描述</p>"}
// 只保留协议和字段约束，避免冗长示例占用上下文。
修改指令格式：
<!--RESUME_MODIFY-->
moduleType: 模块类型
itemIndex: 数组项下标（仅数组模块定向修改时可选）
完整 JSON 内容
<!--/RESUME_MODIFY-->

content 格式要求：
- `basic` 输出完整 JSON 对象
- 数组模块默认输出完整 JSON 数组
- 若只修改数组模块中的单条内容，可带 `itemIndex` 并仅输出该条 JSON 对象
- 文本字段使用 HTML 标签
- JSON 必须合法、可直接应用
""");

        return new PromptBuildResult(sb.toString(), filledModuleCount, memoryCount);
    }

    private PromptBuildResult buildChatSystemPrompt(String userMessage, Map<String, String> moduleData, Long userId) {
        StringBuilder sb = new StringBuilder();
        sb.append("你是简历顾问。只基于下方相关简历内容给建议。");
        int filledModuleCount = 0;
        ModuleSelection selection = selectRelevantModules(userMessage);

        for (String type : selection.modules()) {
            String label = MODULE_LABELS.getOrDefault(type, type);
            String value = moduleData != null ? moduleData.get(type) : null;
            if (value != null && !value.trim().isEmpty()) {
                filledModuleCount++;
                sb.append("\n【").append(label).append("(").append(type).append(")】")
                        .append(cleanHtml(compactModuleContent(type, value, selection)));
            } else {
                sb.append("\n【").append(label).append("(").append(type).append(")】[空]");
            }
        }

        int memoryCount = appendUserMemories(sb, userId);

        sb.append("""

对话规则：
1. 优先结合当前简历给出具体建议，不要泛泛而谈
2. 默认做咨询、分析、比较、指出问题和优化方向
3. 除非用户明确要改写/生成，否则不要输出可直接应用的修改结果
4. 不要输出修改指令或可直接应用的 JSON
5. 信息不足时只指出缺口，不要重复索取已给内容
""");

        return new PromptBuildResult(sb.toString(), filledModuleCount, memoryCount);
    }

    private String buildModifySummary(List<String> suggestList) {
        LinkedHashSet<String> moduleLabels = new LinkedHashSet<>();
        for (String suggestData : suggestList) {
            java.util.regex.Matcher matcher = java.util.regex.Pattern
                    .compile("\"moduleType\"\\s*:\\s*\"([^\"]+)\"")
                    .matcher(suggestData);
            if (matcher.find()) {
                String moduleType = matcher.group(1);
                moduleLabels.add(MODULE_LABELS.getOrDefault(moduleType, moduleType));
            }
        }
        if (moduleLabels.isEmpty()) {
            return "已为您完成本次简历修改，相关模块内容已更新。";
        }
        return "已为您优化" + formatModuleLabels(moduleLabels) + "模块，相关内容已更新。";
    }

    private String formatModuleLabels(Set<String> moduleLabels) {
        List<String> labels = new ArrayList<>(moduleLabels);
        if (labels.size() == 1) {
            return "【" + labels.get(0) + "】";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < labels.size(); i++) {
            if (i > 0) {
                sb.append(i == labels.size() - 1 ? "和" : "、");
            }
            sb.append("【").append(labels.get(i)).append("】");
        }
        return sb.toString();
    }

    private int appendUserMemories(StringBuilder sb, Long userId) {
        if (userId == null) {
            return 0;
        }
        QueryWrapper<UserMemory> memQw = new QueryWrapper<>();
        memQw.eq("userId", userId);
        memQw.orderByDesc("updateTime");
        memQw.last("LIMIT 3");
        List<UserMemory> memories = userMemoryMapper.selectList(memQw);
        if (memories == null || memories.isEmpty()) {
            return 0;
        }
        sb.append("\n\n## 关于用户画像（以下为历史画像参考，若与上方简历当前内容矛盾，以简历内容为准）");
        for (UserMemory mem : memories) {
            String label = switch (mem.getCategory()) {
                case "preference" -> "偏好";
                case "skill" -> "技能";
                case "career" -> "职业";
                case "habit" -> "习惯";
                default -> mem.getCategory();
            };
            sb.append("\n- [").append(label).append("] ").append(mem.getContent());
        }
        return memories.size();
    }

    private ModuleSelection selectRelevantModules(String userMessage) {
        LinkedHashSet<String> selected = new LinkedHashSet<>(ALWAYS_INCLUDED_MODULES);
        LinkedHashSet<String> explicitTargets = new LinkedHashSet<>();
        String normalized = userMessage == null ? "" : userMessage.toLowerCase(Locale.ROOT);

        if (containsAny(normalized, GLOBAL_OPTIMIZE_KEYWORDS)) {
            selected.addAll(MODULE_ORDER);
            return new ModuleSelection(orderModules(selected), explicitTargets, true);
        }

        for (Map.Entry<String, Set<String>> entry : EXACT_MODULE_KEYWORDS.entrySet()) {
            if (containsAny(normalized, entry.getValue())) {
                explicitTargets.add(entry.getKey());
                selected.add(entry.getKey());
            }
        }

        if (explicitTargets.contains("project")) {
            selected.add("skill");
        }
        if (explicitTargets.contains("experience")) {
            selected.add("skill");
        }
        if (explicitTargets.contains("personalStrengths")) {
            selected.add("skill");
            selected.add("project");
        }

        if (selected.size() == ALWAYS_INCLUDED_MODULES.size()
                && containsAny(normalized, MODIFY_INTENT_KEYWORDS)
                && containsAny(normalized, FUZZY_SCOPE_KEYWORDS)) {
            selected.addAll(DEFAULT_PROMPT_MODULES);
        }

        if (selected.size() == ALWAYS_INCLUDED_MODULES.size()) {
            selected.addAll(DEFAULT_PROMPT_MODULES);
        }

        return new ModuleSelection(orderModules(selected), explicitTargets, false);
    }

    private String compactModuleContent(String type, String value, ModuleSelection selection) {
        if (value == null || value.isBlank()) {
            return value;
        }
        try {
            if ("basic".equals(type) && !selection.explicitTargets().contains("basic")) {
                JsonNode node = OBJECT_MAPPER.readTree(value);
                if (!node.isObject()) return value;
                ObjectNode compact = OBJECT_MAPPER.createObjectNode();
                copyIfPresent(node, compact, "name");
                copyIfPresent(node, compact, "status");
                copyIfPresent(node, compact, "jobTitle");
                copyIfPresent(node, compact, "location");
                copyIfPresent(node, compact, "education");
                return OBJECT_MAPPER.writeValueAsString(compact);
            }
            if ("education".equals(type) && !selection.explicitTargets().contains("education")) {
                JsonNode node = OBJECT_MAPPER.readTree(value);
                if (!node.isArray()) return value;
                ArrayNode compactArray = OBJECT_MAPPER.createArrayNode();
                for (JsonNode item : node) {
                    if (!item.isObject()) continue;
                    ObjectNode compact = OBJECT_MAPPER.createObjectNode();
                    copyIfPresent(item, compact, "school");
                    copyIfPresent(item, compact, "major");
                    copyIfPresent(item, compact, "degree");
                    copyIfPresent(item, compact, "startDate");
                    copyIfPresent(item, compact, "endDate");
                    compactArray.add(compact);
                }
                return OBJECT_MAPPER.writeValueAsString(compactArray);
            }
        } catch (Exception ignored) {
            return value;
        }
        return value;
    }

    private void copyIfPresent(JsonNode from, ObjectNode to, String field) {
        JsonNode value = from.get(field);
        if (value != null && !value.isNull()) {
            to.set(field, value);
        }
    }

    private boolean containsAny(String text, Set<String> keywords) {
        for (String keyword : keywords) {
            if (text.contains(keyword.toLowerCase(Locale.ROOT))) {
                return true;
            }
        }
        return false;
    }

    private List<String> orderModules(Set<String> selected) {
        List<String> ordered = new ArrayList<>();
        for (String type : MODULE_ORDER) {
            if (selected.contains(type)) {
                ordered.add(type);
            }
        }
        return ordered;
    }

    private String normalizeChatMode(String mode) {
        if (CHAT_MODE.equalsIgnoreCase(mode)) {
            return CHAT_MODE;
        }
        return MODIFY_MODE;
    }

    private boolean isModifyMode(String mode) {
        return MODIFY_MODE.equals(mode);
    }

    private Flux<String> streamDeepSeek(List<DeepSeekMessage> messages, String mode) {
        return deepSeekClient.streamChat(
                messages,
                DEEPSEEK_MODEL,
                true,
                isModifyMode(mode) ? MODIFY_MODE_MAX_TOKENS : CHAT_MODE_MAX_TOKENS
        );
    }

    private String callDeepSeek(List<DeepSeekMessage> messages, int maxTokens) {
        return deepSeekClient.chatOnce(messages, DEEPSEEK_MODEL, true, maxTokens);
    }

    private String cleanHtml(String text) {
        if (text == null) return "";
        return text
                .replaceAll("<style[^>]*>.*?</style>", "")
                .replaceAll("&nbsp;", " ")
                .replaceAll("\\s{2,}", " ")
                .trim();
    }

    private List<DeepSeekMessage> loadHistoryMessages(Long resumeId, boolean persist) {
        if (!persist) return List.of();
        QueryWrapper<ResumeChat> qw = new QueryWrapper<>();
        qw.eq("resumeId", resumeId);
        qw.orderByDesc("createTime");
        qw.last("LIMIT 4");
        List<ResumeChat> chats = resumeChatMapper.selectList(qw);
        chats.sort(Comparator.comparing(ResumeChat::getCreateTime));

        List<DeepSeekMessage> messages = new ArrayList<>();
        for (ResumeChat chat : chats) {
            if ("user".equals(chat.getRole())) {
                messages.add(DeepSeekMessage.user(chat.getContent()));
            } else if ("assistant".equals(chat.getRole())) {
                messages.add(DeepSeekMessage.assistant(chat.getContent()));
            }
        }
        return messages;
    }

    private MemoryExtractResult extractMemory(Long userId, String userMessage, String assistantReply) {
        int insertedCount = 0;
        int dedupedCount = 0;
        long totalBeforeCompress = 0;
        int totalAfterCompress = 0;
        MemoryCompressResult compressResult = null;
        try {
            if (userMessage == null || userMessage.trim().isEmpty()) {
                return new MemoryExtractResult(0, 0, 0, 0, null);
            }
            if (assistantReply == null || assistantReply.trim().isEmpty()) {
                return new MemoryExtractResult(0, 0, 0, 0, null);
            }

            String analysisPrompt = """
                    分析以下对话，提取关于用户的关键信息。如果发现以下类型的信息，请输出对应 JSON 数组。
                    如果没有发现有价值的信息，返回空数组 []。

                    类别说明：
                    - preference: 用户对简历的偏好（如喜欢简洁风格、偏好某些排版、目标岗位方向）
                    - skill: 用户具备的技能（从简历内容或对话中提取）
                    - career: 用户的职业目标、意向行业、目标公司
                    - habit: 用户的修改习惯（如经常修改某类内容、反复调整某模块）

                    输出格式（JSON 数组）：
                    [{"category": "preference", "content": "..."}, ...]

                    用户消息：%s

                    AI 回复：%s
                    """.formatted(userMessage, assistantReply);

            List<DeepSeekMessage> memMessages = List.of(
                    DeepSeekMessage.system("你是一个用户画像分析助手，只输出 JSON 数组，不要输出其他内容。"),
                    DeepSeekMessage.user(analysisPrompt)
            );
            String result = callDeepSeek(memMessages, AUXILIARY_MAX_TOKENS);

            if (result == null || result.trim().isEmpty()) {
                return new MemoryExtractResult(0, 0, 0, 0, null);
            }

            // 提取 JSON 数组部分
            String jsonStr = result.trim();
            int start = jsonStr.indexOf('[');
            int end = jsonStr.lastIndexOf(']');
            if (start < 0 || end < 0 || start >= end) {
                return new MemoryExtractResult(0, 0, 0, 0, null);
            }
            jsonStr = jsonStr.substring(start, end + 1);

            com.fasterxml.jackson.databind.JsonNode array = OBJECT_MAPPER.readTree(jsonStr);
            if (!array.isArray() || array.isEmpty()) {
                return new MemoryExtractResult(0, 0, 0, 0, null);
            }

            List<UserMemory> toInsert = new ArrayList<>();
            for (com.fasterxml.jackson.databind.JsonNode item : array) {
                String category = item.get("category").asText();
                String content = item.get("content").asText();
                if (category == null || category.isEmpty() || content == null || content.isEmpty()) continue;
            }

            List<MemoryCandidate> candidates = consolidateMemoryCandidates(array);
            for (MemoryCandidate candidate : candidates) {
                UserMemory existingMemory = findSemanticDuplicateMemory(userId, candidate);
                if (existingMemory != null) {
                    if (shouldReplaceMemory(existingMemory.getContent(), candidate.content())) {
                        existingMemory.setContent(candidate.content());
                    }
                    existingMemory.setUpdateTime(LocalDateTime.now());
                    userMemoryMapper.updateById(existingMemory);
                    dedupedCount++;
                    continue;
                }

                UserMemory mem = new UserMemory();
                mem.setUserId(userId);
                mem.setCategory(candidate.category());
                mem.setContent(candidate.content());
                mem.setSource("auto");
                mem.setCreateTime(LocalDateTime.now());
                mem.setUpdateTime(LocalDateTime.now());
                toInsert.add(mem);
            }

            for (UserMemory mem : toInsert) {
                userMemoryMapper.insert(mem);
            }
            insertedCount = toInsert.size();

            if (!toInsert.isEmpty()) {
                enforceSkillMemoryLimit(userId);
                log.info("为用户 {} 提取了 {} 条新记忆", userId, toInsert.size());
                compressResult = cleanupMemories(userId);
            }
            if (compressResult != null) {
                totalBeforeCompress = compressResult.totalBeforeCompress();
                totalAfterCompress = compressResult.totalAfterCompress();
            } else {
                QueryWrapper<UserMemory> countQw = new QueryWrapper<>();
                countQw.eq("userId", userId);
                Long total = userMemoryMapper.selectCount(countQw);
                totalBeforeCompress = total == null ? 0 : total;
                totalAfterCompress = total == null ? 0 : total.intValue();
            }
        } catch (Exception e) {
            log.warn("提取用户记忆失败: {}", e.getMessage());
            return new MemoryExtractResult(insertedCount, dedupedCount, totalBeforeCompress, totalAfterCompress, compressResult);
        }
        return new MemoryExtractResult(insertedCount, dedupedCount, totalBeforeCompress, totalAfterCompress, compressResult);
    }

    private List<MemoryCandidate> consolidateMemoryCandidates(com.fasterxml.jackson.databind.JsonNode array) {
        List<MemoryCandidate> consolidated = new ArrayList<>();
        for (com.fasterxml.jackson.databind.JsonNode item : array) {
            String category = item.path("category").asText("").trim();
            String content = item.path("content").asText("").trim();
            if (category.isEmpty() || content.isEmpty()) {
                continue;
            }

            MemoryCandidate candidate = new MemoryCandidate(category, compactMemoryContent(content));
            if (candidate.content().isEmpty()) {
                continue;
            }
            if ("skill".equals(candidate.category()) && !isValidSkillMemory(candidate.content())) {
                continue;
            }

            boolean merged = false;
            for (int i = 0; i < consolidated.size(); i++) {
                MemoryCandidate existing = consolidated.get(i);
                if (!existing.category().equals(candidate.category())) {
                    continue;
                }
                if (isSemanticDuplicate(existing.content(), candidate.content())) {
                    consolidated.set(i, shouldReplaceMemory(existing.content(), candidate.content()) ? candidate : existing);
                    merged = true;
                    break;
                }
            }
            if (!merged) {
                consolidated.add(candidate);
            }
        }

        List<MemoryCandidate> skillCandidates = consolidated.stream()
                .filter(candidate -> "skill".equals(candidate.category()))
                .sorted((left, right) -> Integer.compare(memoryScore(right.content()), memoryScore(left.content())))
                .limit(MAX_SKILL_MEMORIES_PER_EXTRACTION)
                .toList();

        List<MemoryCandidate> result = new ArrayList<>();
        boolean skillAdded = false;
        for (MemoryCandidate candidate : consolidated) {
            if (!"skill".equals(candidate.category())) {
                result.add(candidate);
                continue;
            }
            if (!skillAdded && skillCandidates.contains(candidate)) {
                result.add(candidate);
                skillAdded = true;
            }
        }
        return result;
    }

    private UserMemory findSemanticDuplicateMemory(Long userId, MemoryCandidate candidate) {
        QueryWrapper<UserMemory> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userId", userId);
        queryWrapper.eq("category", candidate.category());
        queryWrapper.orderByDesc("updateTime");
        queryWrapper.last("LIMIT " + MEMORY_DUPLICATE_CHECK_LIMIT);
        List<UserMemory> existingMemories = userMemoryMapper.selectList(queryWrapper);
        for (UserMemory memory : existingMemories) {
            if (isSemanticDuplicate(memory.getContent(), candidate.content())) {
                return memory;
            }
        }
        return null;
    }

    private boolean isSemanticDuplicate(String left, String right) {
        String normalizedLeft = normalizeMemoryContent(left);
        String normalizedRight = normalizeMemoryContent(right);
        if (normalizedLeft.isEmpty() || normalizedRight.isEmpty()) {
            return false;
        }
        if (normalizedLeft.equals(normalizedRight)) {
            return true;
        }
        if (normalizedLeft.contains(normalizedRight) || normalizedRight.contains(normalizedLeft)) {
            return true;
        }

        Set<String> leftKeywords = extractMemoryKeywords(normalizedLeft);
        Set<String> rightKeywords = extractMemoryKeywords(normalizedRight);
        if (leftKeywords.isEmpty() || rightKeywords.isEmpty()) {
            return false;
        }

        Set<String> intersection = new HashSet<>(leftKeywords);
        intersection.retainAll(rightKeywords);
        double similarity = (double) intersection.size() / Math.min(leftKeywords.size(), rightKeywords.size());
        return similarity >= keywordSimilarityThreshold(left, right);
    }

    private boolean shouldReplaceMemory(String existingContent, String candidateContent) {
        return memoryScore(candidateContent) > memoryScore(existingContent);
    }

    private double keywordSimilarityThreshold(String left, String right) {
        if (looksLikeSkillSummary(left) || looksLikeSkillSummary(right)) {
            return SKILL_MEMORY_KEYWORD_SIMILARITY_THRESHOLD;
        }
        return MEMORY_KEYWORD_SIMILARITY_THRESHOLD;
    }

    private int memoryScore(String content) {
        Set<String> keywords = extractMemoryKeywords(normalizeMemoryContent(content));
        return keywords.size() * 10 + compactMemoryContent(content).length();
    }

    private String compactMemoryContent(String content) {
        return content == null ? "" : content.trim().replaceAll("\\s+", " ");
    }

    private boolean isValidSkillMemory(String content) {
        String normalized = compactMemoryContent(content);
        if (normalized.length() < MIN_SKILL_MEMORY_LENGTH) {
            return false;
        }
        if (SKILL_SOFT_TRAIT_BLACKLIST.contains(normalized)) {
            return false;
        }
        if (!looksLikeAggregatedSkillMemory(normalized)) {
            return false;
        }
        return true;
    }

    private boolean looksLikeAggregatedSkillMemory(String content) {
        if (content.contains("、") || content.contains("，") || content.contains(",") || content.contains("；")
                || content.contains(";") || content.contains("及") || content.contains("以及") || content.contains("与")) {
            return true;
        }
        Set<String> keywords = extractMemoryKeywords(normalizeMemoryContent(content));
        return keywords.size() >= 3;
    }

    private boolean looksLikeSkillSummary(String content) {
        String normalized = compactMemoryContent(content);
        return normalized.length() >= MIN_SKILL_MEMORY_LENGTH && looksLikeAggregatedSkillMemory(normalized);
    }

    private void enforceSkillMemoryLimit(Long userId) {
        QueryWrapper<UserMemory> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userId", userId);
        queryWrapper.eq("category", "skill");
        queryWrapper.orderByDesc("updateTime");
        List<UserMemory> skillMemories = userMemoryMapper.selectList(queryWrapper);
        if (skillMemories.size() <= MAX_SKILL_MEMORIES_PER_USER) {
            return;
        }

        skillMemories.sort((left, right) -> {
            int scoreCompare = Integer.compare(memoryScore(right.getContent()), memoryScore(left.getContent()));
            if (scoreCompare != 0) {
                return scoreCompare;
            }
            return right.getUpdateTime().compareTo(left.getUpdateTime());
        });

        List<UserMemory> retained = skillMemories.subList(0, MAX_SKILL_MEMORIES_PER_USER);
        Set<Long> retainedIds = retained.stream().map(UserMemory::getId).collect(java.util.stream.Collectors.toSet());
        for (UserMemory memory : skillMemories) {
            if (!retainedIds.contains(memory.getId())) {
                userMemoryMapper.deleteById(memory.getId());
            }
        }
    }

    private String normalizeMemoryContent(String content) {
        String normalized = compactMemoryContent(content).toLowerCase(Locale.ROOT);
        normalized = normalized
                .replace('（', '(')
                .replace('）', ')')
                .replace('，', ',')
                .replace('。', '.')
                .replace('、', ' ')
                .replace('/', ' ')
                .replace('-', ' ');
        return normalized;
    }

    private Set<String> extractMemoryKeywords(String normalizedContent) {
        Set<String> keywords = new LinkedHashSet<>();
        for (String token : MEMORY_TOKEN_SPLIT_PATTERN.split(normalizedContent)) {
            String trimmed = token.trim();
            if (trimmed.length() < 2) {
                continue;
            }
            keywords.add(trimmed);
        }
        return keywords;
    }

    private MemoryCompressResult cleanupMemories(Long userId) {
        QueryWrapper<UserMemory> countQw = new QueryWrapper<>();
        countQw.eq("userId", userId);
        Long total = userMemoryMapper.selectCount(countQw);
        if (total == null || total <= MEMORY_COMPRESS_TRIGGER_THRESHOLD) return null;

        // 超出阈值后，用 AI 压缩合并
        long startAt = System.currentTimeMillis();
        QueryWrapper<UserMemory> allQw = new QueryWrapper<>();
        allQw.eq("userId", userId);
        allQw.orderByAsc("updateTime");
        List<UserMemory> allMemories = userMemoryMapper.selectList(allQw);

        StringBuilder memText = new StringBuilder();
        for (UserMemory m : allMemories) {
            memText.append("- [").append(m.getCategory()).append("] ").append(m.getContent()).append("\n");
        }

        String compressPrompt = """
                以下是关于一个用户的画像记忆列表（共 %d 条），请将它们压缩合并为 15 条以内。
                规则：
                1. 合并相似或重复的内容，保留最完整的信息
                2. 去除过时或矛盾的信息
                3. 保持每条信息的简洁（一句话描述）
                4. category 只能是：preference、skill、career、habit
                5. 只输出 JSON 数组，不要输出其他内容
                6. 格式：[{"category":"xxx","content":"xxx"}, ...]

                记忆列表：
                %s
                """.formatted(allMemories.size(), memText);

        try {
            List<DeepSeekMessage> messages = List.of(
                    DeepSeekMessage.system("你是一个数据压缩助手，只输出 JSON 数组。"),
                    DeepSeekMessage.user(compressPrompt)
            );
            String result = callDeepSeek(messages, MEMORY_COMPRESS_MAX_TOKENS);
            if (result == null || result.trim().isEmpty()) return null;

            String jsonStr = result.trim();
            int start = jsonStr.indexOf('[');
            int end = jsonStr.lastIndexOf(']');
            if (start < 0 || end < 0 || start >= end) return null;
            jsonStr = jsonStr.substring(start, end + 1);

            com.fasterxml.jackson.databind.JsonNode array = OBJECT_MAPPER.readTree(jsonStr);
            if (!array.isArray() || array.isEmpty()) return null;

            // 删除所有旧记忆
            QueryWrapper<UserMemory> delQw = new QueryWrapper<>();
            delQw.eq("userId", userId);
            userMemoryMapper.delete(delQw);

            // 插入压缩后的记忆
            LocalDateTime now = LocalDateTime.now();
            for (com.fasterxml.jackson.databind.JsonNode item : array) {
                String category = item.get("category").asText();
                String content = item.get("content").asText();
                if (category == null || category.isEmpty() || content == null || content.isEmpty()) continue;

                UserMemory mem = new UserMemory();
                mem.setUserId(userId);
                mem.setCategory(category);
                mem.setContent(content);
                mem.setSource("auto");
                mem.setCreateTime(now);
                mem.setUpdateTime(now);
                userMemoryMapper.insert(mem);
            }
            log.info("用户 {} 的记忆已从 {} 条压缩为 {} 条", userId, total, array.size());
            return new MemoryCompressResult(total, array.size(), System.currentTimeMillis() - startAt);
        } catch (Exception e) {
            log.warn("压缩用户记忆失败: {}", e.getMessage());
            return null;
        }
    }

    private record PromptBuildResult(String systemPrompt, int filledModuleCount, int memoryCount) {}
    private record ModuleSelection(List<String> modules, Set<String> explicitTargets, boolean global) {}

    private record MemoryCandidate(String category, String content) {}

    private record MemoryExtractResult(
            int insertedCount,
            int dedupedCount,
            long totalBeforeCompress,
            int totalAfterCompress,
            MemoryCompressResult compressResult) {}

    private record MemoryCompressResult(long totalBeforeCompress, int totalAfterCompress, long latencyMs) {}
}
