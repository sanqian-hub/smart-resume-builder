package com.srb.backend.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.srb.backend.mapper.ResumeContentMapper;
import com.srb.backend.mapper.ResumeMapper;
import com.srb.backend.mapper.ResumeShareMapper;
import com.srb.backend.mapper.ResumeVersionMapper;
import com.srb.backend.mapper.UserMapper;
import com.srb.backend.model.entity.Resume;
import com.srb.backend.model.entity.ResumeContent;
import com.srb.backend.model.entity.ResumeVersion;
import com.srb.backend.model.entity.User;
import com.srb.backend.model.vo.ResumeVersionSaveVO;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Map;

import static com.srb.backend.constant.UserConstant.USER_LOGIN_STATE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ResumeServiceImplSaveVersionTest {

    @Test
    void saveVersionShouldSkipNewHistoryWhenOnlyTitleChanges() throws Exception {
        ResumeMapper resumeMapper = mock(ResumeMapper.class);
        ResumeContentMapper resumeContentMapper = mock(ResumeContentMapper.class);
        ResumeVersionMapper resumeVersionMapper = mock(ResumeVersionMapper.class);
        UserMapper userMapper = mock(UserMapper.class);

        Long userId = 1001L;
        Long resumeId = 2001L;

        User user = new User();
        user.setId(userId);
        when(userMapper.selectById(userId)).thenReturn(user);

        Resume resume = new Resume();
        resume.setId(resumeId);
        resume.setUserId(userId);
        resume.setTitle("新的简历标题");
        resume.setCurrentTemplate("classic-1");
        resume.setStyleConfig("{\"themeColor\":\"#4672f2\"}");
        when(resumeMapper.selectById(resumeId)).thenReturn(resume);

        ResumeContent content = new ResumeContent();
        content.setResumeId(resumeId);
        content.setModuleType("basic");
        content.setContentJson("{\"name\":\"Alex Chen\"}");
        content.setSortOrder(0);
        when(resumeContentMapper.selectList(any())).thenReturn(List.of(content));

        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> snapshot = Map.of(
                "contents", List.of(Map.of(
                        "moduleType", "basic",
                        "contentJson", "{\"name\":\"Alex Chen\"}",
                        "sortOrder", 0
                )),
                "title", "旧的简历标题",
                "template", "classic-1",
                "styleConfig", "{\"themeColor\":\"#4672f2\"}"
        );

        ResumeVersion lastVersion = new ResumeVersion();
        lastVersion.setId(301L);
        lastVersion.setResumeId(resumeId);
        lastVersion.setUserId(userId);
        lastVersion.setVersionNum(3);
        lastVersion.setSnapshotJson(objectMapper.writeValueAsString(snapshot));
        when(resumeVersionMapper.selectOne(any())).thenReturn(lastVersion);

        ResumeServiceImpl service = new ResumeServiceImpl(
                resumeContentMapper,
                mock(ResumeShareMapper.class),
                resumeVersionMapper,
                userMapper,
                objectMapper,
                null
        );
        ReflectionTestUtils.setField(service, "baseMapper", resumeMapper);

        MockHttpServletRequest request = new MockHttpServletRequest();
        HttpSession session = request.getSession(true);
        session.setAttribute(USER_LOGIN_STATE, userId);

        ResumeVersionSaveVO result = service.saveVersion(request, resumeId, "手动保存");

        assertThat(result.getCreated()).isFalse();
        assertThat(result.getId()).isEqualTo(301L);
        assertThat(result.getVersionNum()).isEqualTo(3);
        verify(resumeVersionMapper, never()).insert(any(ResumeVersion.class));
    }

    @Test
    void saveVersionShouldCreateNewHistoryWhenContentChanges() throws Exception {
        ResumeMapper resumeMapper = mock(ResumeMapper.class);
        ResumeContentMapper resumeContentMapper = mock(ResumeContentMapper.class);
        ResumeVersionMapper resumeVersionMapper = mock(ResumeVersionMapper.class);
        UserMapper userMapper = mock(UserMapper.class);

        Long userId = 1002L;
        Long resumeId = 2002L;

        User user = new User();
        user.setId(userId);
        when(userMapper.selectById(userId)).thenReturn(user);

        Resume resume = new Resume();
        resume.setId(resumeId);
        resume.setUserId(userId);
        resume.setTitle("未命名简历");
        resume.setCurrentTemplate("classic-1");
        resume.setStyleConfig("{\"themeColor\":\"#4672f2\"}");
        when(resumeMapper.selectById(resumeId)).thenReturn(resume);

        ResumeContent content = new ResumeContent();
        content.setResumeId(resumeId);
        content.setModuleType("basic");
        content.setContentJson("{\"name\":\"Alex Chen\",\"jobTitle\":\"前端开发工程师\"}");
        content.setSortOrder(0);
        when(resumeContentMapper.selectList(any())).thenReturn(List.of(content));

        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> snapshot = Map.of(
                "contents", List.of(Map.of(
                        "moduleType", "basic",
                        "contentJson", "{\"name\":\"Alex Chen\"}",
                        "sortOrder", 0
                )),
                "title", "未命名简历",
                "template", "classic-1",
                "styleConfig", "{\"themeColor\":\"#4672f2\"}"
        );

        ResumeVersion lastVersion = new ResumeVersion();
        lastVersion.setId(302L);
        lastVersion.setResumeId(resumeId);
        lastVersion.setUserId(userId);
        lastVersion.setVersionNum(3);
        lastVersion.setSnapshotJson(objectMapper.writeValueAsString(snapshot));
        when(resumeVersionMapper.selectOne(any())).thenReturn(lastVersion);
        when(resumeVersionMapper.selectCount(any())).thenReturn(4L);
        when(resumeVersionMapper.insert(any(ResumeVersion.class))).thenAnswer(invocation -> {
            ResumeVersion version = invocation.getArgument(0);
            version.setId(401L);
            return 1;
        });

        ResumeServiceImpl service = new ResumeServiceImpl(
                resumeContentMapper,
                mock(ResumeShareMapper.class),
                resumeVersionMapper,
                userMapper,
                objectMapper,
                null
        );
        ReflectionTestUtils.setField(service, "baseMapper", resumeMapper);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.getSession(true).setAttribute(USER_LOGIN_STATE, userId);

        ResumeVersionSaveVO result = service.saveVersion(request, resumeId, "手动保存");

        assertThat(result.getCreated()).isTrue();
        assertThat(result.getId()).isEqualTo(401L);
        assertThat(result.getVersionNum()).isEqualTo(4);
        verify(resumeVersionMapper).insert(any(ResumeVersion.class));
    }
}
