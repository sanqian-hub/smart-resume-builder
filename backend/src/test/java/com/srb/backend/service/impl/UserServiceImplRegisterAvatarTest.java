package com.srb.backend.service.impl;

import com.srb.backend.mapper.UserMapper;
import com.srb.backend.model.dto.UserRegisterRequest;
import com.srb.backend.model.entity.User;
import com.srb.backend.service.CosService;
import com.srb.backend.common.BusinessException;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class UserServiceImplRegisterAvatarTest {

    @Test
    void registerShouldPersistRandomDefaultAvatarUrl() {
        UserMapper userMapper = mock(UserMapper.class);
        when(userMapper.selectCount(any())).thenReturn(0L);
        when(userMapper.insert(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(1001L);
            return 1;
        });

        UserServiceImpl service = new UserServiceImpl(mock(CosService.class));
        ReflectionTestUtils.setField(service, "baseMapper", userMapper);

        UserRegisterRequest request = new UserRegisterRequest();
        request.setUsername("test-user");
        request.setUserAccount("tester001");
        request.setUserPassword("password123");
        request.setPhone("13800000000");
        request.setEmail("test@example.com");

        long userId = service.register(request);

        assertThat(userId).isEqualTo(1001L);

        var userCaptor = org.mockito.ArgumentCaptor.forClass(User.class);
        verify(userMapper).insert(userCaptor.capture());
        User savedUser = userCaptor.getValue();

        assertThat(savedUser.getAvatarUrl()).isIn(
                "https://api.dicebear.com/9.x/lorelei/svg?eyebrows=variant01,variant05,variant06,variant07,variant08,variant09,variant12&seed=Sophia",
                "https://api.dicebear.com/9.x/lorelei/svg?eyebrows=variant01,variant05,variant06,variant07,variant08,variant09,variant12&seed=Mason",
                "https://api.dicebear.com/9.x/lorelei/svg?eyebrows=variant01,variant05,variant06,variant07,variant08,variant09,variant12&seed=George",
                "https://api.dicebear.com/9.x/lorelei/svg?eyebrows=variant01,variant05,variant06,variant07,variant08,variant09,variant12&seed=Aiden",
                "https://api.dicebear.com/9.x/lorelei/svg?eyebrows=variant01,variant05,variant06,variant07,variant08,variant09,variant12&seed=Riley",
                "https://api.dicebear.com/9.x/lorelei/svg?eyebrows=variant01,variant05,variant06,variant07,variant08,variant09,variant12&seed=Liliana"
        );
    }

    @Test
    void registerShouldRejectDuplicateAccountBeforeInsert() {
        UserMapper userMapper = mock(UserMapper.class);
        when(userMapper.selectCount(any())).thenReturn(1L);

        UserServiceImpl service = new UserServiceImpl(mock(CosService.class));
        ReflectionTestUtils.setField(service, "baseMapper", userMapper);

        UserRegisterRequest request = new UserRegisterRequest();
        request.setUsername("test-user");
        request.setUserAccount("tester001");
        request.setUserPassword("password123");

        assertThatThrownBy(() -> service.register(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("账号已存在");

        verify(userMapper, never()).insert(any(User.class));
    }
}
