package com.srb.backend.service.impl;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.srb.backend.ai.DeepSeekClient;
import com.srb.backend.ai.DeepSeekMessage;
import com.srb.backend.common.BusinessException;
import com.srb.backend.mapper.ResumeContentMapper;
import com.srb.backend.mapper.ResumeMapper;
import com.srb.backend.mapper.ResumeShareMapper;
import com.srb.backend.mapper.ResumeVersionMapper;
import com.srb.backend.mapper.UserMapper;
import com.srb.backend.model.entity.User;
import com.srb.backend.model.vo.ResumeProofreadVO;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.slf4j.LoggerFactory;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static com.srb.backend.constant.UserConstant.USER_LOGIN_STATE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ResumeServiceImplProofreadLocationTest {

    @Test
    void proofreadShouldParsePreciseLocationFieldsFromAiResponse() {
        ResumeMapper resumeMapper = mock(ResumeMapper.class);
        ResumeContentMapper resumeContentMapper = mock(ResumeContentMapper.class);
        ResumeVersionMapper resumeVersionMapper = mock(ResumeVersionMapper.class);
        ResumeShareMapper resumeShareMapper = mock(ResumeShareMapper.class);
        UserMapper userMapper = mock(UserMapper.class);
        DeepSeekClient deepSeekClient = mock(DeepSeekClient.class);

        Long userId = 1001L;
        User user = new User();
        user.setId(userId);
        when(userMapper.selectById(userId)).thenReturn(user);

        when(deepSeekClient.chatOnce(anyList(), anyString(), anyBoolean(), anyInt(), anyDouble())).thenReturn("""
                {
                  "summary": "整体语法和表达基本规范，仅发现一处错别字和一处表达可优化。",
                  "items": [
                    {
                      "id": "proofread-1",
                      "moduleType": "project",
                      "itemIndex": 1,
                      "fieldPath": "description",
                      "occurrenceIndex": 2,
                      "fieldLabel": "项目经历",
                      "type": "typo",
                      "typeLabel": "错别字",
                      "original": "可感知与一直性",
                      "suggestion": "可感知与一致性",
                      "reason": "“一直性”应为“一致性”"
                    },
                    {
                      "id": "proofread-2",
                      "moduleType": "award",
                      "fieldLabel": "荣誉奖项",
                      "type": "style",
                      "original": "成绩长期名列前茅",
                      "suggestion": "成绩名列前茅",
                      "reason": "去掉重复表达"
                    }
                  ]
                }
                """);

        ResumeServiceImpl service = new ResumeServiceImpl(
                resumeContentMapper,
                resumeShareMapper,
                resumeVersionMapper,
                userMapper,
                new ObjectMapper(),
                deepSeekClient
        );
        ReflectionTestUtils.setField(service, "baseMapper", resumeMapper);

        MockHttpServletRequest request = new MockHttpServletRequest();
        HttpSession session = request.getSession(true);
        session.setAttribute(USER_LOGIN_STATE, userId);

        ResumeProofreadVO result = service.proofreadResume(request, null, Map.of(
                "project", """
                        [{"name":"智能简历生成系统","description":"提升多步骤操作过程的状态可感知与一直性。"}]
                        """,
                "award", """
                        [{"name":"一等奖学金","description":"成绩长期名列前茅"}]
                        """
        ));

        assertThat(result.getItems()).hasSize(2);

        ResumeProofreadVO.Item first = result.getItems().get(0);
        assertThat(first.getModuleType()).isEqualTo("project");
        assertThat(first.getItemIndex()).isEqualTo(1);
        assertThat(first.getFieldPath()).isEqualTo("description");
        assertThat(first.getOccurrenceIndex()).isEqualTo(2);
        assertThat(first.getOriginal()).isEqualTo("可感知与一直性");
        assertThat(first.getSuggestion()).isEqualTo("可感知与一致性");

        ResumeProofreadVO.Item second = result.getItems().get(1);
        assertThat(second.getModuleType()).isEqualTo("award");
        assertThat(second.getItemIndex()).isNull();
        assertThat(second.getFieldPath()).isNull();
        assertThat(second.getOccurrenceIndex()).isEqualTo(0);
    }

    @Test
    void proofreadShouldNormalizeBasicFieldPathAndIgnoreItemIndexForBasicModule() {
        ResumeMapper resumeMapper = mock(ResumeMapper.class);
        ResumeContentMapper resumeContentMapper = mock(ResumeContentMapper.class);
        ResumeVersionMapper resumeVersionMapper = mock(ResumeVersionMapper.class);
        ResumeShareMapper resumeShareMapper = mock(ResumeShareMapper.class);
        UserMapper userMapper = mock(UserMapper.class);
        DeepSeekClient deepSeekClient = mock(DeepSeekClient.class);

        Long userId = 1001L;
        User user = new User();
        user.setId(userId);
        when(userMapper.selectById(userId)).thenReturn(user);

        when(deepSeekClient.chatOnce(anyList(), anyString(), anyBoolean(), anyInt(), anyDouble())).thenReturn("""
                {
                  "summary": "已发现可优化内容",
                  "items": [
                    {
                      "id": "proofread-basic-1",
                      "moduleType": "basic",
                      "itemIndex": 0,
                      "fieldPath": "basic.location",
                      "occurrenceIndex": 0,
                      "fieldLabel": "基本信息",
                      "type": "typo",
                      "typeLabel": "错别字",
                      "original": "背景",
                      "suggestion": "北京",
                      "reason": "地点字段存在错别字"
                    }
                  ]
                }
                """);

        ResumeServiceImpl service = new ResumeServiceImpl(
                resumeContentMapper,
                resumeShareMapper,
                resumeVersionMapper,
                userMapper,
                new ObjectMapper(),
                deepSeekClient
        );
        ReflectionTestUtils.setField(service, "baseMapper", resumeMapper);

        MockHttpServletRequest request = new MockHttpServletRequest();
        HttpSession session = request.getSession(true);
        session.setAttribute(USER_LOGIN_STATE, userId);

        ResumeProofreadVO result = service.proofreadResume(request, null, Map.of(
                "basic", """
                        {"name":"Elovara","location":"背景"}
                        """
        ));

        assertThat(result.getItems()).hasSize(1);
        ResumeProofreadVO.Item first = result.getItems().get(0);
        assertThat(first.getModuleType()).isEqualTo("basic");
        assertThat(first.getItemIndex()).isNull();
        assertThat(first.getFieldPath()).isEqualTo("location");
        assertThat(first.getOccurrenceIndex()).isEqualTo(0);
    }

    @Test
    void proofreadShouldNormalizeOneBasedItemIndexWhenSingleCandidateMatches() {
        ResumeMapper resumeMapper = mock(ResumeMapper.class);
        ResumeContentMapper resumeContentMapper = mock(ResumeContentMapper.class);
        ResumeVersionMapper resumeVersionMapper = mock(ResumeVersionMapper.class);
        ResumeShareMapper resumeShareMapper = mock(ResumeShareMapper.class);
        UserMapper userMapper = mock(UserMapper.class);
        DeepSeekClient deepSeekClient = mock(DeepSeekClient.class);

        Long userId = 1001L;
        User user = new User();
        user.setId(userId);
        when(userMapper.selectById(userId)).thenReturn(user);

        when(deepSeekClient.chatOnce(anyList(), anyString(), anyBoolean(), anyInt(), anyDouble())).thenReturn("""
                {
                  "summary": "已发现可优化内容",
                  "items": [
                    {
                      "id": "proofread-edu-1",
                      "moduleType": "education",
                      "itemIndex": 1,
                      "fieldPath": "school",
                      "occurrenceIndex": 0,
                      "fieldLabel": "教育经历",
                      "type": "typo",
                      "typeLabel": "错别字",
                      "original": "东应大雪",
                      "suggestion": "东应大学",
                      "reason": "学校名称存在错别字"
                    }
                  ]
                }
                """);

        ResumeServiceImpl service = new ResumeServiceImpl(
                resumeContentMapper,
                resumeShareMapper,
                resumeVersionMapper,
                userMapper,
                new ObjectMapper(),
                deepSeekClient
        );
        ReflectionTestUtils.setField(service, "baseMapper", resumeMapper);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.getSession(true).setAttribute(USER_LOGIN_STATE, userId);

        ResumeProofreadVO result = service.proofreadResume(request, null, Map.of(
                "education", """
                        [{"school":"东应大雪","major":"信息科学"}]
                        """
        ));

        assertThat(result.getItems()).hasSize(1);
        assertThat(result.getItems().get(0).getItemIndex()).isEqualTo(0);
        assertThat(result.getItems().get(0).getFieldPath()).isEqualTo("school");
    }

    @Test
    void proofreadShouldExplicitlyAskToCheckBasicShortTextFieldsAndUseLowerTemperature() {
        ResumeMapper resumeMapper = mock(ResumeMapper.class);
        ResumeContentMapper resumeContentMapper = mock(ResumeContentMapper.class);
        ResumeVersionMapper resumeVersionMapper = mock(ResumeVersionMapper.class);
        ResumeShareMapper resumeShareMapper = mock(ResumeShareMapper.class);
        UserMapper userMapper = mock(UserMapper.class);
        DeepSeekClient deepSeekClient = mock(DeepSeekClient.class);

        Long userId = 1001L;
        User user = new User();
        user.setId(userId);
        when(userMapper.selectById(userId)).thenReturn(user);
        when(deepSeekClient.chatOnce(anyList(), anyString(), anyBoolean(), anyInt(), anyDouble())).thenReturn("""
                {
                  "summary": "已发现可优化内容",
                  "items": []
                }
                """);

        ResumeServiceImpl service = new ResumeServiceImpl(
                resumeContentMapper,
                resumeShareMapper,
                resumeVersionMapper,
                userMapper,
                new ObjectMapper(),
                deepSeekClient
        );
        ReflectionTestUtils.setField(service, "baseMapper", resumeMapper);

        MockHttpServletRequest request = new MockHttpServletRequest();
        HttpSession session = request.getSession(true);
        session.setAttribute(USER_LOGIN_STATE, userId);

        service.proofreadResume(request, null, Map.of(
                "basic", """
                        {"name":"Elovara","location":"背景","status":"在校/应届生"}
                        """
        ));

        ArgumentCaptor<List<DeepSeekMessage>> messagesCaptor = ArgumentCaptor.forClass(List.class);
        verify(deepSeekClient).chatOnce(messagesCaptor.capture(), anyString(), anyBoolean(), anyInt(), eq(0.1d));

        List<DeepSeekMessage> messages = messagesCaptor.getValue();
        assertThat(messages).hasSize(2);
        assertThat(messages.get(0).content()).contains("基本信息里的姓名、求职方向、地点、状态等短文本字段");
        assertThat(messages.get(0).content()).contains("即使是像“背景”这样只有两个字的短文本，也要逐项检查错别字和词语误用");
        assertThat(messages.get(0).content()).contains("itemIndex 必须使用 0-based 索引");
        assertThat(messages.get(1).content()).contains("## 候选纠错字段列表");
        assertThat(messages.get(1).content()).contains("\"candidateId\":\"c1\"");
        assertThat(messages.get(1).content()).contains("\"label\":\"基本信息-location\"");
        assertThat(messages.get(1).content()).contains("\"text\":\"背景\"");
        assertThat(messages.get(1).content()).contains("\"candidateId\":\"c2\"");
        assertThat(messages.get(1).content()).contains("\"label\":\"基本信息-status\"");
        assertThat(messages.get(1).content()).contains("\"text\":\"在校/应届生\"");
        assertThat(messages.get(1).content()).doesNotContain("\"moduleType\":");
        assertThat(messages.get(1).content()).doesNotContain("\"fieldPath\":");
    }

    @Test
    void proofreadShouldLogRawAiResponseWhenJsonParsingFails() {
        ResumeMapper resumeMapper = mock(ResumeMapper.class);
        ResumeContentMapper resumeContentMapper = mock(ResumeContentMapper.class);
        ResumeVersionMapper resumeVersionMapper = mock(ResumeVersionMapper.class);
        ResumeShareMapper resumeShareMapper = mock(ResumeShareMapper.class);
        UserMapper userMapper = mock(UserMapper.class);
        DeepSeekClient deepSeekClient = mock(DeepSeekClient.class);

        Long userId = 1001L;
        User user = new User();
        user.setId(userId);
        when(userMapper.selectById(userId)).thenReturn(user);
        when(deepSeekClient.chatOnce(anyList(), anyString(), anyBoolean(), anyInt(), anyDouble()))
                .thenReturn("这不是合法 JSON");

        ResumeServiceImpl service = new ResumeServiceImpl(
                resumeContentMapper,
                resumeShareMapper,
                resumeVersionMapper,
                userMapper,
                new ObjectMapper(),
                deepSeekClient
        );
        ReflectionTestUtils.setField(service, "baseMapper", resumeMapper);

        Logger logger = (Logger) LoggerFactory.getLogger(ResumeServiceImpl.class);
        ListAppender<ILoggingEvent> appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);

        MockHttpServletRequest request = new MockHttpServletRequest();
        HttpSession session = request.getSession(true);
        session.setAttribute(USER_LOGIN_STATE, userId);

        try {
            assertThatThrownBy(() -> service.proofreadResume(request, null, Map.of(
                    "basic", """
                            {"name":"Elovara","location":"背景"}
                            """
            )))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("AI 返回结果解析失败，请重试");

            assertThat(appender.list)
                    .anySatisfy(event -> {
                        assertThat(event.getFormattedMessage()).contains("语法纠错 AI 原始返回");
                        assertThat(event.getFormattedMessage()).contains("这不是合法 JSON");
                    });
        } finally {
            logger.detachAppender(appender);
            appender.stop();
        }
    }

    @Test
    void proofreadShouldConstrainOutputSizeAndAvoidNoOpSuggestions() {
        ResumeMapper resumeMapper = mock(ResumeMapper.class);
        ResumeContentMapper resumeContentMapper = mock(ResumeContentMapper.class);
        ResumeVersionMapper resumeVersionMapper = mock(ResumeVersionMapper.class);
        ResumeShareMapper resumeShareMapper = mock(ResumeShareMapper.class);
        UserMapper userMapper = mock(UserMapper.class);
        DeepSeekClient deepSeekClient = mock(DeepSeekClient.class);

        Long userId = 1001L;
        User user = new User();
        user.setId(userId);
        when(userMapper.selectById(userId)).thenReturn(user);
        when(deepSeekClient.chatOnce(anyList(), anyString(), anyBoolean(), anyInt(), anyDouble())).thenReturn("""
                {
                  "summary": "仅返回高价值纠错建议",
                  "items": []
                }
                """);

        ResumeServiceImpl service = new ResumeServiceImpl(
                resumeContentMapper,
                resumeShareMapper,
                resumeVersionMapper,
                userMapper,
                new ObjectMapper(),
                deepSeekClient
        );
        ReflectionTestUtils.setField(service, "baseMapper", resumeMapper);

        MockHttpServletRequest request = new MockHttpServletRequest();
        HttpSession session = request.getSession(true);
        session.setAttribute(USER_LOGIN_STATE, userId);

        service.proofreadResume(request, null, Map.of(
                "project", """
                        [{"name":"智能简历生成系统","content":"缓存优化： 为解决首次访问系统的用户主页加载过慢的问题，使用 Spring Scheduler 定时任务 来实现 缓存预热 。"}]
                        """
        ));

        ArgumentCaptor<List<DeepSeekMessage>> messagesCaptor = ArgumentCaptor.forClass(List.class);
        verify(deepSeekClient).chatOnce(messagesCaptor.capture(), anyString(), anyBoolean(), eq(2600), eq(0.1d));

        List<DeepSeekMessage> messages = messagesCaptor.getValue();
        assertThat(messages).hasSize(2);
        String systemPrompt = messages.get(0).content();
        assertThat(systemPrompt).contains("最多只返回 8 条最重要、最值得用户手动处理的建议");
        assertThat(systemPrompt).contains("如果 suggestion 与 original 完全相同，绝对不要返回这条建议");
        assertThat(systemPrompt).contains("优先返回错别字、明显语病、影响专业性的表达问题");
        assertThat(systemPrompt).doesNotContain("typeLabel 只能取：");
        assertThat(systemPrompt).doesNotContain("\"fieldLabel\": \"项目经历\"");
        assertThat(systemPrompt).doesNotContain("\"typeLabel\": \"表达优化\"");
    }

    @Test
    void proofreadShouldDropWhitespaceOnlySuggestions() {
        ResumeMapper resumeMapper = mock(ResumeMapper.class);
        ResumeContentMapper resumeContentMapper = mock(ResumeContentMapper.class);
        ResumeVersionMapper resumeVersionMapper = mock(ResumeVersionMapper.class);
        ResumeShareMapper resumeShareMapper = mock(ResumeShareMapper.class);
        UserMapper userMapper = mock(UserMapper.class);
        DeepSeekClient deepSeekClient = mock(DeepSeekClient.class);

        Long userId = 1001L;
        User user = new User();
        user.setId(userId);
        when(userMapper.selectById(userId)).thenReturn(user);
        when(deepSeekClient.chatOnce(anyList(), anyString(), anyBoolean(), anyInt(), anyDouble())).thenReturn("""
                {
                  "summary": "已发现可优化内容",
                  "items": [
                    {
                      "id": "proofread-space-1",
                      "moduleType": "skill",
                      "itemIndex": 0,
                      "fieldPath": "content",
                      "occurrenceIndex": 0,
                      "fieldLabel": "专业技能",
                      "type": "style",
                      "typeLabel": "表达优化",
                      "original": "开发工具： 熟练使用 Git、IDEA、Knife4j",
                      "suggestion": "开发工具：熟练使用 Git、IDEA、Knife4j",
                      "reason": "去掉多余空格"
                    },
                    {
                      "id": "proofread-typo-1",
                      "moduleType": "skill",
                      "itemIndex": 0,
                      "fieldPath": "content",
                      "occurrenceIndex": 1,
                      "fieldLabel": "专业技能",
                      "type": "typo",
                      "typeLabel": "错别字",
                      "original": "开发笑率",
                      "suggestion": "开发效率",
                      "reason": "“笑”应为“效”"
                    }
                  ]
                }
                """);

        ResumeServiceImpl service = new ResumeServiceImpl(
                resumeContentMapper,
                resumeShareMapper,
                resumeVersionMapper,
                userMapper,
                new ObjectMapper(),
                deepSeekClient
        );
        ReflectionTestUtils.setField(service, "baseMapper", resumeMapper);

        MockHttpServletRequest request = new MockHttpServletRequest();
        HttpSession session = request.getSession(true);
        session.setAttribute(USER_LOGIN_STATE, userId);

        ResumeProofreadVO result = service.proofreadResume(request, null, Map.of(
                "skill", """
                        [{"content":"开发工具： 熟练使用 Git、IDEA、Knife4j\\n开发笑率"}]
                        """
        ));

        assertThat(result.getItems()).hasSize(1);
        assertThat(result.getItems().get(0).getId()).isEqualTo("proofread-typo-1");

        ArgumentCaptor<List<DeepSeekMessage>> messagesCaptor = ArgumentCaptor.forClass(List.class);
        verify(deepSeekClient).chatOnce(messagesCaptor.capture(), anyString(), anyBoolean(), anyInt(), eq(0.1d));
        String systemPrompt = messagesCaptor.getValue().get(0).content();
        assertThat(systemPrompt).contains("不要仅因为空格、换行、缩进或纯排版习惯差异就返回建议");
    }

    @Test
    void proofreadShouldDropLargerStyleSuggestionWhenTypoSuggestionAlreadyCoversSameFragment() {
        ResumeMapper resumeMapper = mock(ResumeMapper.class);
        ResumeContentMapper resumeContentMapper = mock(ResumeContentMapper.class);
        ResumeVersionMapper resumeVersionMapper = mock(ResumeVersionMapper.class);
        ResumeShareMapper resumeShareMapper = mock(ResumeShareMapper.class);
        UserMapper userMapper = mock(UserMapper.class);
        DeepSeekClient deepSeekClient = mock(DeepSeekClient.class);

        Long userId = 1001L;
        User user = new User();
        user.setId(userId);
        when(userMapper.selectById(userId)).thenReturn(user);
        when(deepSeekClient.chatOnce(anyList(), anyString(), anyBoolean(), anyInt(), anyDouble())).thenReturn("""
                {
                  "summary": "已发现可优化内容",
                  "items": [
                    {
                      "id": "proofread-typo-1",
                      "candidateId": "c1",
                      "occurrenceIndex": 0,
                      "type": "typo",
                      "original": "一直性",
                      "suggestion": "一致性",
                      "reason": "错别字"
                    },
                    {
                      "id": "proofread-style-1",
                      "candidateId": "c1",
                      "occurrenceIndex": 0,
                      "type": "style",
                      "original": "优化复杂弹窗、加载反馈和边界交互，增强多步骤操作过程中的状态可感知性与一直性。",
                      "suggestion": "优化复杂弹窗、加载反馈和边界交互，增强多步骤操作过程中的状态可感知性与一致性",
                      "reason": "修正错别字并合并重复表达，使语句更顺专业。"
                    }
                  ]
                }
                """);

        ResumeServiceImpl service = new ResumeServiceImpl(
                resumeContentMapper,
                resumeShareMapper,
                resumeVersionMapper,
                userMapper,
                new ObjectMapper(),
                deepSeekClient
        );
        ReflectionTestUtils.setField(service, "baseMapper", resumeMapper);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.getSession(true).setAttribute(USER_LOGIN_STATE, userId);

        ResumeProofreadVO result = service.proofreadResume(request, null, Map.of(
                "project", """
                        [{"name":"智能简历生成系统","content":"优化复杂弹窗、加载反馈和边界交互，增强多步骤操作过程中的状态可感知性与一直性。"}]
                        """
        ));

        assertThat(result.getItems()).hasSize(1);
        assertThat(result.getItems().get(0).getId()).isEqualTo("proofread-typo-1");
        assertThat(result.getItems().get(0).getOriginal()).isEqualTo("一直性");
    }

    @Test
    void proofreadShouldReuseCacheWhenOnlyNonProofreadFieldsChange() {
        ResumeMapper resumeMapper = mock(ResumeMapper.class);
        ResumeContentMapper resumeContentMapper = mock(ResumeContentMapper.class);
        ResumeVersionMapper resumeVersionMapper = mock(ResumeVersionMapper.class);
        ResumeShareMapper resumeShareMapper = mock(ResumeShareMapper.class);
        UserMapper userMapper = mock(UserMapper.class);
        DeepSeekClient deepSeekClient = mock(DeepSeekClient.class);

        Long userId = 1001L;
        User user = new User();
        user.setId(userId);
        when(userMapper.selectById(userId)).thenReturn(user);
        when(deepSeekClient.chatOnce(anyList(), anyString(), anyBoolean(), anyInt(), anyDouble())).thenReturn("""
                {
                  "summary": "已发现可优化内容",
                  "items": [
                    {
                      "id": "proofread-basic-1",
                      "moduleType": "basic",
                      "fieldPath": "location",
                      "occurrenceIndex": 0,
                      "fieldLabel": "基本信息",
                      "type": "typo",
                      "typeLabel": "错别字",
                      "original": "背景",
                      "suggestion": "北京",
                      "reason": "地点字段存在错别字"
                    }
                  ]
                }
                """);

        ResumeServiceImpl service = new ResumeServiceImpl(
                resumeContentMapper,
                resumeShareMapper,
                resumeVersionMapper,
                userMapper,
                new ObjectMapper(),
                deepSeekClient
        );
        ReflectionTestUtils.setField(service, "baseMapper", resumeMapper);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.getSession(true).setAttribute(USER_LOGIN_STATE, userId);

        String firstBasic = """
                {
                  "name":"Elovara",
                  "location":"背景",
                  "phone":"13800000000",
                  "email":"a@example.com",
                  "salary":"5000-8000元"
                }
                """;
        String secondBasic = """
                {
                  "name":"Elovara",
                  "location":"背景",
                  "phone":"13911112222",
                  "email":"b@example.com",
                  "salary":"8000-10000元"
                }
                """;

        ResumeProofreadVO first = service.proofreadResume(request, null, Map.of("basic", firstBasic));
        ResumeProofreadVO second = service.proofreadResume(request, null, Map.of("basic", secondBasic));

        assertThat(first.getItems()).hasSize(1);
        assertThat(second.getItems()).hasSize(1);
        verify(deepSeekClient, times(1)).chatOnce(anyList(), anyString(), anyBoolean(), anyInt(), anyDouble());
    }

    @Test
    void proofreadShouldInvalidateCacheWhenProofreadFieldsChange() {
        ResumeMapper resumeMapper = mock(ResumeMapper.class);
        ResumeContentMapper resumeContentMapper = mock(ResumeContentMapper.class);
        ResumeVersionMapper resumeVersionMapper = mock(ResumeVersionMapper.class);
        ResumeShareMapper resumeShareMapper = mock(ResumeShareMapper.class);
        UserMapper userMapper = mock(UserMapper.class);
        DeepSeekClient deepSeekClient = mock(DeepSeekClient.class);

        Long userId = 1001L;
        User user = new User();
        user.setId(userId);
        when(userMapper.selectById(userId)).thenReturn(user);
        when(deepSeekClient.chatOnce(anyList(), anyString(), anyBoolean(), anyInt(), anyDouble())).thenReturn("""
                {
                  "summary": "已发现可优化内容",
                  "items": []
                }
                """);

        ResumeServiceImpl service = new ResumeServiceImpl(
                resumeContentMapper,
                resumeShareMapper,
                resumeVersionMapper,
                userMapper,
                new ObjectMapper(),
                deepSeekClient
        );
        ReflectionTestUtils.setField(service, "baseMapper", resumeMapper);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.getSession(true).setAttribute(USER_LOGIN_STATE, userId);

        service.proofreadResume(request, null, Map.of("basic", """
                {"name":"Elovara","location":"背景"}
                """));
        service.proofreadResume(request, null, Map.of("basic", """
                {"name":"Elovara","location":"北京"}
                """));

        verify(deepSeekClient, times(2)).chatOnce(anyList(), anyString(), anyBoolean(), anyInt(), anyDouble());
    }

    @Test
    void proofreadShouldFilterNonProofreadFieldsFromAiInput() {
        ResumeMapper resumeMapper = mock(ResumeMapper.class);
        ResumeContentMapper resumeContentMapper = mock(ResumeContentMapper.class);
        ResumeVersionMapper resumeVersionMapper = mock(ResumeVersionMapper.class);
        ResumeShareMapper resumeShareMapper = mock(ResumeShareMapper.class);
        UserMapper userMapper = mock(UserMapper.class);
        DeepSeekClient deepSeekClient = mock(DeepSeekClient.class);

        Long userId = 1001L;
        User user = new User();
        user.setId(userId);
        when(userMapper.selectById(userId)).thenReturn(user);
        when(deepSeekClient.chatOnce(anyList(), anyString(), anyBoolean(), anyInt(), anyDouble())).thenReturn("""
                {
                  "summary": "已发现可优化内容",
                  "items": []
                }
                """);

        ResumeServiceImpl service = new ResumeServiceImpl(
                resumeContentMapper,
                resumeShareMapper,
                resumeVersionMapper,
                userMapper,
                new ObjectMapper(),
                deepSeekClient
        );
        ReflectionTestUtils.setField(service, "baseMapper", resumeMapper);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.getSession(true).setAttribute(USER_LOGIN_STATE, userId);

        service.proofreadResume(request, null, Map.of(
                "basic", """
                        {
                          "name":"Elovara",
                          "jobTitle":"前端开发工程师",
                          "location":"背景",
                          "status":"在校/应届生",
                          "phone":"13800000000",
                          "email":"your.email@example.com",
                          "github":"https://github.com/example",
                          "salary":"5000-10000元"
                        }
                        """,
                "project", """
                        [{
                          "name":"智能简历生成系统",
                          "role":"前端负责人",
                          "content":"提升页面可用型与性能",
                          "link":"https://example.com/project"
                        }]
                        """
        ));

        ArgumentCaptor<List<DeepSeekMessage>> messagesCaptor = ArgumentCaptor.forClass(List.class);
        verify(deepSeekClient).chatOnce(messagesCaptor.capture(), anyString(), anyBoolean(), anyInt(), eq(0.1d));
        String systemPrompt = messagesCaptor.getValue().get(0).content();
        String userPrompt = messagesCaptor.getValue().get(1).content();
        assertThat(systemPrompt).contains("最多只返回 8 条最重要、最值得用户手动处理的建议");
        assertThat(systemPrompt).contains("candidateId");
        assertThat(userPrompt).contains("## 候选纠错字段列表");
        assertThat(userPrompt).contains("\"candidateId\":\"");
        assertThat(userPrompt).contains("\"label\":\"基本信息-location\"");
        assertThat(userPrompt).contains("\"text\":\"背景\"");
        assertThat(userPrompt).contains("\"label\":\"基本信息-status\"");
        assertThat(userPrompt).contains("\"text\":\"在校/应届生\"");
        assertThat(userPrompt).contains("\"label\":\"基本信息-jobTitle\"");
        assertThat(userPrompt).contains("\"text\":\"前端开发工程师\"");
        assertThat(userPrompt).contains("\"label\":\"项目经历-第1条-content\"");
        assertThat(userPrompt).contains("\"text\":\"提升页面可用型与性能\"");
        assertThat(userPrompt).doesNotContain("\"moduleType\":");
        assertThat(userPrompt).doesNotContain("\"itemIndex\":");
        assertThat(userPrompt).doesNotContain("\"fieldPath\":");
        assertThat(userPrompt).doesNotContain("\"fieldLabel\":");
        assertThat(userPrompt).doesNotContain("13800000000");
        assertThat(userPrompt).doesNotContain("your.email@example.com");
        assertThat(userPrompt).doesNotContain("https://github.com/example");
        assertThat(userPrompt).doesNotContain("5000-10000元");
        assertThat(userPrompt).doesNotContain("https://example.com/project");
    }

    @Test
    void proofreadShouldDeduplicateInFlightRequestsByProofreadHash() throws Exception {
        ResumeMapper resumeMapper = mock(ResumeMapper.class);
        ResumeContentMapper resumeContentMapper = mock(ResumeContentMapper.class);
        ResumeVersionMapper resumeVersionMapper = mock(ResumeVersionMapper.class);
        ResumeShareMapper resumeShareMapper = mock(ResumeShareMapper.class);
        UserMapper userMapper = mock(UserMapper.class);
        DeepSeekClient deepSeekClient = mock(DeepSeekClient.class);

        Long userId = 1001L;
        User user = new User();
        user.setId(userId);
        when(userMapper.selectById(userId)).thenReturn(user);

        AtomicInteger callCount = new AtomicInteger();
        CountDownLatch enteredLatch = new CountDownLatch(1);
        CountDownLatch releaseLatch = new CountDownLatch(1);
        when(deepSeekClient.chatOnce(anyList(), anyString(), anyBoolean(), anyInt(), anyDouble())).thenAnswer(invocation -> {
            callCount.incrementAndGet();
            enteredLatch.countDown();
            if (!releaseLatch.await(2, TimeUnit.SECONDS)) {
                throw new IllegalStateException("release timeout");
            }
            return """
                    {
                      "summary": "已发现可优化内容",
                      "items": []
                    }
                    """;
        });

        ResumeServiceImpl service = new ResumeServiceImpl(
                resumeContentMapper,
                resumeShareMapper,
                resumeVersionMapper,
                userMapper,
                new ObjectMapper(),
                deepSeekClient
        );
        ReflectionTestUtils.setField(service, "baseMapper", resumeMapper);

        MockHttpServletRequest requestA = new MockHttpServletRequest();
        requestA.getSession(true).setAttribute(USER_LOGIN_STATE, userId);
        MockHttpServletRequest requestB = new MockHttpServletRequest();
        requestB.getSession(true).setAttribute(USER_LOGIN_STATE, userId);

        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            CompletableFuture<ResumeProofreadVO> first = CompletableFuture.supplyAsync(() -> service.proofreadResume(requestA, null, Map.of(
                    "basic", """
                            {"name":"Elovara","location":"背景"}
                            """
            )), executor);
            assertThat(enteredLatch.await(1, TimeUnit.SECONDS)).isTrue();

            CompletableFuture<ResumeProofreadVO> second = CompletableFuture.supplyAsync(() -> service.proofreadResume(requestB, null, Map.of(
                    "basic", """
                            {"name":"Elovara","location":"背景"}
                            """
            )), executor);

            releaseLatch.countDown();

            assertThat(first.get(2, TimeUnit.SECONDS).getItems()).isEmpty();
            assertThat(second.get(2, TimeUnit.SECONDS).getItems()).isEmpty();
            assertThat(callCount.get()).isEqualTo(1);
        } finally {
            executor.shutdownNow();
        }
    }

    @Test
    void proofreadShouldRestoreMetadataFromCandidateIdWhenAiOnlyReturnsCompressedItems() {
        ResumeMapper resumeMapper = mock(ResumeMapper.class);
        ResumeContentMapper resumeContentMapper = mock(ResumeContentMapper.class);
        ResumeVersionMapper resumeVersionMapper = mock(ResumeVersionMapper.class);
        ResumeShareMapper resumeShareMapper = mock(ResumeShareMapper.class);
        UserMapper userMapper = mock(UserMapper.class);
        DeepSeekClient deepSeekClient = mock(DeepSeekClient.class);

        Long userId = 1001L;
        User user = new User();
        user.setId(userId);
        when(userMapper.selectById(userId)).thenReturn(user);
        when(deepSeekClient.chatOnce(anyList(), anyString(), anyBoolean(), anyInt(), anyDouble())).thenReturn("""
                {
                  "summary": "已发现可优化内容",
                  "items": [
                    {
                      "id": "proofread-basic-1",
                      "candidateId": "c1",
                      "occurrenceIndex": 0,
                      "type": "typo",
                      "original": "背景",
                      "suggestion": "北京",
                      "reason": "地点字段存在错别字"
                    }
                  ]
                }
                """);

        ResumeServiceImpl service = new ResumeServiceImpl(
                resumeContentMapper,
                resumeShareMapper,
                resumeVersionMapper,
                userMapper,
                new ObjectMapper(),
                deepSeekClient
        );
        ReflectionTestUtils.setField(service, "baseMapper", resumeMapper);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.getSession(true).setAttribute(USER_LOGIN_STATE, userId);

        ResumeProofreadVO result = service.proofreadResume(request, null, Map.of(
                "basic", """
                        {"name":"Elovara","location":"背景"}
                        """
        ));

        assertThat(result.getItems()).hasSize(1);
        ResumeProofreadVO.Item first = result.getItems().get(0);
        assertThat(first.getModuleType()).isEqualTo("basic");
        assertThat(first.getItemIndex()).isNull();
        assertThat(first.getFieldPath()).isEqualTo("location");
        assertThat(first.getFieldLabel()).isEqualTo("基本信息");
        assertThat(first.getType()).isEqualTo("typo");
        assertThat(first.getTypeLabel()).isEqualTo("错别字");
        assertThat(first.getOriginal()).isEqualTo("背景");
        assertThat(first.getSuggestion()).isEqualTo("北京");
    }

    @Test
    void proofreadShouldFilterCrossItemMergedSuggestionsWhenSmallerItemsAlreadyExist() {
        ResumeMapper resumeMapper = mock(ResumeMapper.class);
        ResumeContentMapper resumeContentMapper = mock(ResumeContentMapper.class);
        ResumeVersionMapper resumeVersionMapper = mock(ResumeVersionMapper.class);
        ResumeShareMapper resumeShareMapper = mock(ResumeShareMapper.class);
        UserMapper userMapper = mock(UserMapper.class);
        DeepSeekClient deepSeekClient = mock(DeepSeekClient.class);

        Long userId = 1001L;
        User user = new User();
        user.setId(userId);
        when(userMapper.selectById(userId)).thenReturn(user);
        when(deepSeekClient.chatOnce(anyList(), anyString(), anyBoolean(), anyInt(), anyDouble())).thenReturn("""
                {
                  "summary": "已发现可优化内容",
                  "items": [
                    {
                      "id": "proofread-1",
                      "moduleType": "experience",
                      "itemIndex": 0,
                      "fieldPath": "content",
                      "occurrenceIndex": 0,
                      "fieldLabel": "工作经历",
                      "type": "typo",
                      "typeLabel": "错别字",
                      "original": "开阀",
                      "suggestion": "开发",
                      "reason": "“开阀”应为“开发”。"
                    },
                    {
                      "id": "proofread-2",
                      "moduleType": "experience",
                      "itemIndex": 0,
                      "fieldPath": "content",
                      "occurrenceIndex": 1,
                      "fieldLabel": "工作经历",
                      "type": "typo",
                      "typeLabel": "错别字",
                      "original": "愈发",
                      "suggestion": "语法",
                      "reason": "“愈发”应为“语法”。"
                    },
                    {
                      "id": "proofread-3",
                      "moduleType": "experience",
                      "itemIndex": 0,
                      "fieldPath": "content",
                      "occurrenceIndex": 0,
                      "fieldLabel": "工作经历",
                      "type": "style",
                      "typeLabel": "表达优化",
                      "original": "熟练使用Vue3加速页面开阀 熟悉使用Vue3愈发",
                      "suggestion": "熟练使用 Vue 3 加速页面开发，熟悉使用 Vue 3 语法",
                      "reason": "修正错别字并合并重复表达，使语句更顺畅专业。"
                    }
                  ]
                }
                """);

        ResumeServiceImpl service = new ResumeServiceImpl(
                resumeContentMapper,
                resumeShareMapper,
                resumeVersionMapper,
                userMapper,
                new ObjectMapper(),
                deepSeekClient
        );
        ReflectionTestUtils.setField(service, "baseMapper", resumeMapper);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.getSession(true).setAttribute(USER_LOGIN_STATE, userId);

        ResumeProofreadVO result = service.proofreadResume(request, null, Map.of(
                "experience", """
                        [{"company":"某科技有限公司","content":"<ol><li>熟练使用Vue3加速页面开阀</li><li>熟悉使用Vue3愈发</li></ol>"}]
                        """
        ));

        assertThat(result.getItems()).hasSize(2);
        assertThat(result.getItems()).extracting(ResumeProofreadVO.Item::getOriginal)
                .containsExactly("开阀", "愈发");

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<DeepSeekMessage>> messagesCaptor = ArgumentCaptor.forClass(List.class);
        verify(deepSeekClient, times(1)).chatOnce(messagesCaptor.capture(), anyString(), anyBoolean(), anyInt(), anyDouble());
        assertThat(messagesCaptor.getValue().get(0).content())
                .contains("不要跨多个列表项、编号点或段落合并一条建议")
                .contains("如果两个列表项各自有问题，必须拆成两条建议");
    }
}
