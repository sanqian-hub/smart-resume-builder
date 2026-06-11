package com.srb.backend.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.srb.backend.mapper.ResumeContentMapper;
import com.srb.backend.mapper.ResumeEmailLogMapper;
import com.srb.backend.mapper.ResumeMapper;
import com.srb.backend.mapper.ResumeNoticeMapper;
import com.srb.backend.mapper.UserMapper;
import com.srb.backend.model.entity.Resume;
import com.srb.backend.model.entity.ResumeContent;
import com.srb.backend.service.EmailService;
import com.srb.backend.config.ResumeRecallProperties;
import org.junit.jupiter.api.Test;
import org.redisson.api.RedissonClient;

import java.lang.reflect.Method;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class NoticeServiceImplAnalysisHashTest {

    @Test
    void buildAnalysisHashShouldIgnoreResumeTitle() throws Exception {
        ResumeMapper resumeMapper = mock(ResumeMapper.class);
        ResumeContentMapper resumeContentMapper = mock(ResumeContentMapper.class);
        ResumeEmailLogMapper resumeEmailLogMapper = mock(ResumeEmailLogMapper.class);
        UserMapper userMapper = mock(UserMapper.class);
        EmailService emailService = mock(EmailService.class);
        com.srb.backend.ai.DeepSeekClient deepSeekClient = mock(com.srb.backend.ai.DeepSeekClient.class);
        ObjectMapper objectMapper = new ObjectMapper();
        ResumeRecallProperties resumeRecallProperties = mock(ResumeRecallProperties.class);
        RedissonClient redissonClient = mock(RedissonClient.class);

        ResumeContent content = new ResumeContent();
        content.setResumeId(1001L);
        content.setModuleType("basic");
        content.setContentJson("{\"name\":\"Alex Chen\"}");
        content.setSortOrder(0);
        when(resumeContentMapper.selectList(any())).thenReturn(List.of(content));

        NoticeServiceImpl service = new NoticeServiceImpl(
                resumeMapper,
                resumeContentMapper,
                resumeEmailLogMapper,
                userMapper,
                emailService,
                deepSeekClient,
                objectMapper,
                resumeRecallProperties,
                redissonClient
        );

        Method buildAnalysisHash = NoticeServiceImpl.class.getDeclaredMethod("buildAnalysisHash", Resume.class);
        buildAnalysisHash.setAccessible(true);

        Resume first = new Resume();
        first.setId(1001L);
        first.setTitle("第一次标题");
        first.setUserId(1L);

        Resume second = new Resume();
        second.setId(1001L);
        second.setTitle("第二次标题");
        second.setUserId(1L);

        String firstHash = (String) buildAnalysisHash.invoke(service, first);
        String secondHash = (String) buildAnalysisHash.invoke(service, second);

        assertThat(firstHash).isEqualTo(secondHash);
    }
}
