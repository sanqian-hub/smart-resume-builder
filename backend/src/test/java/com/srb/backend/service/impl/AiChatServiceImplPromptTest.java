package com.srb.backend.service.impl;

import com.srb.backend.ai.DeepSeekClient;
import com.srb.backend.mapper.ResumeChatMapper;
import com.srb.backend.mapper.ResumeContentMapper;
import com.srb.backend.mapper.ResumeMapper;
import com.srb.backend.mapper.UserMapper;
import com.srb.backend.mapper.UserMemoryMapper;
import com.srb.backend.model.entity.UserMemory;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AiChatServiceImplPromptTest {

    @Test
    void modifyPromptShouldStayCompactWhileKeepingProtocol() throws Exception {
        UserMemoryMapper userMemoryMapper = mock(UserMemoryMapper.class);
        when(userMemoryMapper.selectList(org.mockito.ArgumentMatchers.any())).thenReturn(List.of());

        AiChatServiceImpl service = new AiChatServiceImpl(
                mock(DeepSeekClient.class),
                mock(ResumeChatMapper.class),
                mock(ResumeMapper.class),
                mock(ResumeContentMapper.class),
                mock(UserMapper.class),
                userMemoryMapper
        );

        Map<String, String> moduleData = new HashMap<>();
        moduleData.put("basic", "{\"name\":\"张三\"}");
        moduleData.put("project", "[{\"name\":\"项目一\"}]");
        moduleData.put("skill", "[{\"name\":\"技能\"}]");

        Method method = AiChatServiceImpl.class.getDeclaredMethod(
                "buildSystemPrompt", String.class, String.class, Map.class, Long.class);
        method.setAccessible(true);

        Object result = method.invoke(service, "modify", "整体优化一下项目经历和个人优势", moduleData, 1L);
        Method systemPromptMethod = result.getClass().getDeclaredMethod("systemPrompt");
        systemPromptMethod.setAccessible(true);
        String systemPrompt = (String) systemPromptMethod.invoke(result);

        assertThat(systemPrompt).contains("<!--RESUME_MODIFY-->");
        assertThat(systemPrompt).contains("moduleType:");
        assertThat(systemPrompt).doesNotContain("不要在说明之后才输出指令");
        assertThat(systemPrompt.length()).isLessThanOrEqualTo(2200);
    }

    @Test
    void chatPromptShouldStayCompactAndHideModifyProtocol() throws Exception {
        UserMemoryMapper userMemoryMapper = mock(UserMemoryMapper.class);
        when(userMemoryMapper.selectList(org.mockito.ArgumentMatchers.any())).thenReturn(List.of());

        AiChatServiceImpl service = new AiChatServiceImpl(
                mock(DeepSeekClient.class),
                mock(ResumeChatMapper.class),
                mock(ResumeMapper.class),
                mock(ResumeContentMapper.class),
                mock(UserMapper.class),
                userMemoryMapper
        );

        Method method = AiChatServiceImpl.class.getDeclaredMethod(
                "buildSystemPrompt", String.class, String.class, Map.class, Long.class);
        method.setAccessible(true);

        Object result = method.invoke(service, "chat", "怎么优化项目经历", Map.of("basic", "{\"name\":\"张三\"}"), 1L);
        Method systemPromptMethod = result.getClass().getDeclaredMethod("systemPrompt");
        systemPromptMethod.setAccessible(true);
        String systemPrompt = (String) systemPromptMethod.invoke(result);

        assertThat(systemPrompt).doesNotContain("<!--RESUME_MODIFY-->");
        assertThat(systemPrompt).doesNotContain("moduleType:");
        assertThat(systemPrompt.length()).isLessThanOrEqualTo(240);
    }

    @Test
    void chatModeMaxTokensShouldBeHigherThanModifyMode() throws Exception {
        var chatField = AiChatServiceImpl.class.getDeclaredField("CHAT_MODE_MAX_TOKENS");
        chatField.setAccessible(true);
        var modifyField = AiChatServiceImpl.class.getDeclaredField("MODIFY_MODE_MAX_TOKENS");
        modifyField.setAccessible(true);

        assertThat(chatField.getInt(null)).isEqualTo(1200);
        assertThat(chatField.getInt(null)).isLessThan(modifyField.getInt(null));
    }

    @Test
    void modifyPromptShouldRouteToTargetModulesAndTrimBasicContext() throws Exception {
        UserMemoryMapper userMemoryMapper = mock(UserMemoryMapper.class);
        when(userMemoryMapper.selectList(org.mockito.ArgumentMatchers.any())).thenReturn(List.of());

        AiChatServiceImpl service = new AiChatServiceImpl(
                mock(DeepSeekClient.class),
                mock(ResumeChatMapper.class),
                mock(ResumeMapper.class),
                mock(ResumeContentMapper.class),
                mock(UserMapper.class),
                userMemoryMapper
        );

        Method method = AiChatServiceImpl.class.getDeclaredMethod(
                "buildSystemPrompt", String.class, String.class, Map.class, Long.class);
        method.setAccessible(true);

        Map<String, String> moduleData = new HashMap<>();
        moduleData.put("basic", """
                {"name":"夜神月","phone":"13910421987","email":"light.yagami@to-oh.ac.jp","avatar":"https://avatar.example.com/a.png","status":"在校/应届生","jobTitle":"前端开发工程师","location":"北京","salary":"5000-10000元","education":"本科","website":"https://portfolio.example.com","github":"https://github.com/light-yagami","wechat":"yagami_light_24","age":"24","workYears":"3年","gender":"男"}
                """.trim());
        moduleData.put("education", """
                [{"school":"东应大学","major":"信息科学","degree":"本科","startDate":"2004/04","endDate":"2008/03","description":"<p>主修课程很多很多</p>"}]
                """.trim());
        moduleData.put("skill", """
                [{"name":"前端开发","content":"<ul><li>Vue 3</li></ul>"}]
                """.trim());
        moduleData.put("project", """
                [{"name":"智能简历","content":"<ul><li>项目内容</li></ul>"}]
                """.trim());
        moduleData.put("personalStrengths", "{\"content\":\"<p>个人优势</p>\"}");

        Object result = method.invoke(service, "modify", "帮我修改专业技能", moduleData, 1L);
        Method systemPromptMethod = result.getClass().getDeclaredMethod("systemPrompt");
        systemPromptMethod.setAccessible(true);
        String systemPrompt = (String) systemPromptMethod.invoke(result);
        String basicSection = systemPrompt.substring(
                systemPrompt.indexOf("【基本信息(basic)】"),
                systemPrompt.indexOf("【专业技能(skill)】"));

        assertThat(systemPrompt).contains("【基本信息(basic)】");
        assertThat(systemPrompt).contains("【专业技能(skill)】");
        assertThat(systemPrompt).doesNotContain("【教育经历(education)】");
        assertThat(systemPrompt).doesNotContain("【项目经历(project)】");
        assertThat(basicSection).contains("\"name\":\"夜神月\"");
        assertThat(basicSection).contains("\"jobTitle\":\"前端开发工程师\"");
        assertThat(basicSection).doesNotContain("\"avatar\"");
        assertThat(basicSection).doesNotContain("\"website\"");
        assertThat(basicSection).doesNotContain("\"github\"");
    }

    @Test
    void modifyPromptShouldLoadAllModulesForGlobalIntent() throws Exception {
        UserMemoryMapper userMemoryMapper = mock(UserMemoryMapper.class);
        when(userMemoryMapper.selectList(org.mockito.ArgumentMatchers.any())).thenReturn(List.of());

        AiChatServiceImpl service = new AiChatServiceImpl(
                mock(DeepSeekClient.class),
                mock(ResumeChatMapper.class),
                mock(ResumeMapper.class),
                mock(ResumeContentMapper.class),
                mock(UserMapper.class),
                userMemoryMapper
        );

        Method method = AiChatServiceImpl.class.getDeclaredMethod(
                "buildSystemPrompt", String.class, String.class, Map.class, Long.class);
        method.setAccessible(true);

        Map<String, String> moduleData = new HashMap<>();
        moduleData.put("basic", "{\"name\":\"夜神月\"}");
        moduleData.put("education", "[{\"school\":\"东应大学\"}]");
        moduleData.put("experience", "[{\"company\":\"某科技\"}]");
        moduleData.put("project", "[{\"name\":\"智能简历\"}]");
        moduleData.put("skill", "[{\"name\":\"前端开发\"}]");
        moduleData.put("personalStrengths", "{\"content\":\"<p>个人优势</p>\"}");

        Object result = method.invoke(service, "modify", "整体优化整份简历", moduleData, 1L);
        Method systemPromptMethod = result.getClass().getDeclaredMethod("systemPrompt");
        systemPromptMethod.setAccessible(true);
        String systemPrompt = (String) systemPromptMethod.invoke(result);

        assertThat(systemPrompt).contains("【基本信息(basic)】");
        assertThat(systemPrompt).contains("【教育经历(education)】");
        assertThat(systemPrompt).contains("【工作经历(experience)】");
        assertThat(systemPrompt).contains("【项目经历(project)】");
        assertThat(systemPrompt).contains("【专业技能(skill)】");
        assertThat(systemPrompt).contains("【个人优势(personalStrengths)】");
    }

    @Test
    void modifyPromptShouldDescribeOptionalItemIndexForArrayModules() throws Exception {
        UserMemoryMapper userMemoryMapper = mock(UserMemoryMapper.class);
        when(userMemoryMapper.selectList(org.mockito.ArgumentMatchers.any())).thenReturn(List.of());

        AiChatServiceImpl service = new AiChatServiceImpl(
                mock(DeepSeekClient.class),
                mock(ResumeChatMapper.class),
                mock(ResumeMapper.class),
                mock(ResumeContentMapper.class),
                mock(UserMapper.class),
                userMemoryMapper
        );

        Method method = AiChatServiceImpl.class.getDeclaredMethod(
                "buildSystemPrompt", String.class, String.class, Map.class, Long.class);
        method.setAccessible(true);

        Map<String, String> moduleData = new HashMap<>();
        moduleData.put("basic", "{\"name\":\"夜神月\"}");
        moduleData.put("experience", """
                [{"company":"第一家公司","content":"<p>第一段</p>"},{"company":"第二家公司","content":"<p>第二段</p>"}]
                """.trim());

        Object result = method.invoke(service, "modify", "优化第二段工作经历", moduleData, 1L);
        Method systemPromptMethod = result.getClass().getDeclaredMethod("systemPrompt");
        systemPromptMethod.setAccessible(true);
        String systemPrompt = (String) systemPromptMethod.invoke(result);

        assertThat(systemPrompt).contains("itemIndex:");
        assertThat(systemPrompt).contains("数组模块");
    }

    @Test
    void extractModifyCommandsShouldPreserveOptionalItemIndexForArrayModules() throws Exception {
        UserMemoryMapper userMemoryMapper = mock(UserMemoryMapper.class);
        when(userMemoryMapper.selectList(org.mockito.ArgumentMatchers.any())).thenReturn(List.of());

        AiChatServiceImpl service = new AiChatServiceImpl(
                mock(DeepSeekClient.class),
                mock(ResumeChatMapper.class),
                mock(ResumeMapper.class),
                mock(ResumeContentMapper.class),
                mock(UserMapper.class),
                userMemoryMapper
        );

        Method method = AiChatServiceImpl.class.getDeclaredMethod("extractModifyCommands", String.class);
        method.setAccessible(true);

        @SuppressWarnings("unchecked")
        List<String> commands = (List<String>) method.invoke(service, """
                <!--RESUME_MODIFY-->
                moduleType: experience
                itemIndex: 1
                {"company":"第二家公司","content":"<p>优化后的第二段</p>"}
                <!--/RESUME_MODIFY-->
                """);

        assertThat(commands).hasSize(1);
        assertThat(commands.getFirst()).contains("\"itemIndex\":1");
    }

    @Test
    void modifyPromptShouldLimitUserMemoriesToThree() throws Exception {
        UserMemoryMapper userMemoryMapper = mock(UserMemoryMapper.class);
        AtomicReference<Object> queryRef = new AtomicReference<>();
        when(userMemoryMapper.selectList(org.mockito.ArgumentMatchers.any())).thenAnswer(invocation -> {
            queryRef.set(invocation.getArgument(0));
            return List.of(
                    memory("skill", "熟练 Vue 3"),
                    memory("skill", "熟练 Pinia"),
                    memory("career", "目标前端开发"),
                    memory("habit", "喜欢一次性修改多个模块"),
                    memory("preference", "偏好简洁风格")
            );
        });

        AiChatServiceImpl service = new AiChatServiceImpl(
                mock(DeepSeekClient.class),
                mock(ResumeChatMapper.class),
                mock(ResumeMapper.class),
                mock(ResumeContentMapper.class),
                mock(UserMapper.class),
                userMemoryMapper
        );

        Method method = AiChatServiceImpl.class.getDeclaredMethod(
                "buildSystemPrompt", String.class, String.class, Map.class, Long.class);
        method.setAccessible(true);

        Map<String, String> moduleData = Map.of(
                "basic", "{\"name\":\"夜神月\"}",
                "skill", "[{\"name\":\"前端开发\",\"content\":\"<ul><li>Vue 3</li></ul>\"}]"
        );

        Object result = method.invoke(service, "modify", "修改专业技能", moduleData, 1L);
        Method systemPromptMethod = result.getClass().getDeclaredMethod("systemPrompt");
        systemPromptMethod.setAccessible(true);
        systemPromptMethod.invoke(result);

        Object queryWrapper = queryRef.get();
        assertThat(queryWrapper).isNotNull();
        assertThat(readField(queryWrapper, "lastSql")).contains("LIMIT 3");
    }

    private static UserMemory memory(String category, String content) {
        UserMemory memory = new UserMemory();
        memory.setCategory(category);
        memory.setContent(content);
        return memory;
    }

    private static String readField(Object target, String fieldName) throws Exception {
        Class<?> type = target.getClass();
        while (type != null) {
            try {
                var field = type.getDeclaredField(fieldName);
                field.setAccessible(true);
                Object value = field.get(target);
                return value == null ? null : String.valueOf(value);
            } catch (NoSuchFieldException ignored) {
                type = type.getSuperclass();
            }
        }
        throw new NoSuchFieldException(fieldName);
    }
}
