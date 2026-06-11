package com.srb.backend.service.impl;

import com.srb.backend.mapper.RememberLoginTokenMapper;
import com.srb.backend.mapper.UserMapper;
import com.srb.backend.model.entity.RememberLoginToken;
import com.srb.backend.model.entity.User;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class RememberLoginTokenServiceTest {

    @Test
    void issueTokenShouldPersistRecordAndWriteRememberCookie() {
        RememberLoginTokenMapper mapper = mock(RememberLoginTokenMapper.class);
        UserMapper userMapper = mock(UserMapper.class);
        when(mapper.insert(any(RememberLoginToken.class))).thenAnswer(invocation -> {
            RememberLoginToken token = invocation.getArgument(0);
            token.setId(1L);
            return 1;
        });

        RememberLoginTokenServiceImpl service = new RememberLoginTokenServiceImpl(mapper, userMapper);
        ReflectionTestUtils.setField(service, "rememberMeMaxAgeSeconds", 30 * 24 * 60 * 60);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("127.0.0.1");
        request.addHeader("User-Agent", "JUnit Browser");
        MockHttpServletResponse response = new MockHttpServletResponse();

        service.issueTokenForCurrentDevice(1001L, request, response);

        ArgumentCaptor<RememberLoginToken> tokenCaptor = ArgumentCaptor.forClass(RememberLoginToken.class);
        verify(mapper).insert(tokenCaptor.capture());
        RememberLoginToken saved = tokenCaptor.getValue();

        assertThat(saved.getUserId()).isEqualTo(1001L);
        assertThat(saved.getSelector()).isNotBlank();
        assertThat(saved.getValidatorHash()).isNotBlank();
        assertThat(saved.getExpiresAt()).isNotNull();
        assertThat(saved.getRevoked()).isEqualTo(0);
        assertThat(saved.getUserAgent()).isEqualTo("JUnit Browser");
        assertThat(saved.getClientIp()).isEqualTo("127.0.0.1");

        Cookie rememberCookie = response.getCookie("remember_me");
        assertThat(rememberCookie).isNotNull();
        assertThat(rememberCookie.getValue()).contains(":");
        assertThat(rememberCookie.isHttpOnly()).isTrue();
        assertThat(rememberCookie.getPath()).isEqualTo("/");
        assertThat(rememberCookie.getMaxAge()).isEqualTo(30 * 24 * 60 * 60);
    }

    @Test
    void clearCurrentTokenShouldRevokeRecordAndExpireCookie() {
        RememberLoginTokenMapper mapper = mock(RememberLoginTokenMapper.class);
        UserMapper userMapper = mock(UserMapper.class);
        RememberLoginToken existing = new RememberLoginToken();
        existing.setId(10L);
        existing.setSelector("selector-1");
        existing.setRevoked(0);
        when(mapper.selectOne(any())).thenReturn(existing);

        RememberLoginTokenServiceImpl service = new RememberLoginTokenServiceImpl(mapper, userMapper);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setCookies(new Cookie("remember_me", "selector-1:validator-1"));
        MockHttpServletResponse response = new MockHttpServletResponse();

        service.clearCurrentDeviceToken(request, response);

        ArgumentCaptor<RememberLoginToken> tokenCaptor = ArgumentCaptor.forClass(RememberLoginToken.class);
        verify(mapper).updateById(tokenCaptor.capture());
        RememberLoginToken revoked = tokenCaptor.getValue();
        assertThat(revoked.getId()).isEqualTo(10L);
        assertThat(revoked.getRevoked()).isEqualTo(1);

        Cookie rememberCookie = response.getCookie("remember_me");
        assertThat(rememberCookie).isNotNull();
        assertThat(rememberCookie.getValue()).isEmpty();
        assertThat(rememberCookie.getMaxAge()).isZero();
        assertThat(rememberCookie.getPath()).isEqualTo("/");
    }

    @Test
    void restoreSessionIfPossibleShouldRebuildSessionRotateValidatorAndRewriteCookie() {
        RememberLoginTokenMapper mapper = mock(RememberLoginTokenMapper.class);
        UserMapper userMapper = mock(UserMapper.class);

        RememberLoginToken existing = new RememberLoginToken();
        existing.setId(15L);
        existing.setUserId(1001L);
        existing.setSelector("selector-1");
        existing.setValidatorHash("will-be-overwritten");
        existing.setExpiresAt(java.time.LocalDateTime.now().plusDays(7));
        existing.setRevoked(0);

        when(mapper.selectOne(any())).thenReturn(existing);
        User user = new User();
        user.setId(1001L);
        when(userMapper.selectById(1001L)).thenReturn(user);

        RememberLoginTokenServiceImpl service = new RememberLoginTokenServiceImpl(mapper, userMapper);
        ReflectionTestUtils.setField(service, "rememberMeMaxAgeSeconds", 30 * 24 * 60 * 60);
        String oldHash = ReflectionTestUtils.invokeMethod(service, "sha256Hex", "validator-1");
        ReflectionTestUtils.setField(existing, "validatorHash", oldHash);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setCookies(new Cookie("remember_me", "selector-1:validator-1"));
        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean restored = service.restoreSessionIfPossible(request, response);

        assertThat(restored).isTrue();
        assertThat(request.getSession(false)).isNotNull();
        assertThat(request.getSession(false).getAttribute("user_login_state")).isEqualTo(1001L);

        ArgumentCaptor<RememberLoginToken> tokenCaptor = ArgumentCaptor.forClass(RememberLoginToken.class);
        verify(mapper).updateById(tokenCaptor.capture());
        RememberLoginToken rotated = tokenCaptor.getValue();
        assertThat(rotated.getId()).isEqualTo(15L);
        assertThat(rotated.getSelector()).isEqualTo("selector-1");
        assertThat(rotated.getValidatorHash()).isNotEqualTo(oldHash);
        assertThat(rotated.getLastUsedAt()).isNotNull();

        Cookie rememberCookie = response.getCookie("remember_me");
        assertThat(rememberCookie).isNotNull();
        assertThat(rememberCookie.getValue()).startsWith("selector-1:");
        assertThat(rememberCookie.getValue()).isNotEqualTo("selector-1:validator-1");
    }

    @Test
    void restoreSessionIfPossibleShouldClearCookieForExpiredToken() {
        RememberLoginTokenMapper mapper = mock(RememberLoginTokenMapper.class);
        UserMapper userMapper = mock(UserMapper.class);

        RememberLoginToken existing = new RememberLoginToken();
        existing.setId(16L);
        existing.setUserId(1001L);
        existing.setSelector("selector-expired");
        existing.setValidatorHash("ignored");
        existing.setExpiresAt(java.time.LocalDateTime.now().minusMinutes(1));
        existing.setRevoked(0);
        when(mapper.selectOne(any())).thenReturn(existing);

        RememberLoginTokenServiceImpl service = new RememberLoginTokenServiceImpl(mapper, userMapper);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setCookies(new Cookie("remember_me", "selector-expired:validator-1"));
        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean restored = service.restoreSessionIfPossible(request, response);

        assertThat(restored).isFalse();
        assertThat(request.getSession(false)).isNull();
        Cookie rememberCookie = response.getCookie("remember_me");
        assertThat(rememberCookie).isNotNull();
        assertThat(rememberCookie.getValue()).isEmpty();
        assertThat(rememberCookie.getMaxAge()).isZero();
    }

    @Test
    void restoreSessionIfPossibleShouldClearCookieForRevokedToken() {
        RememberLoginTokenMapper mapper = mock(RememberLoginTokenMapper.class);
        UserMapper userMapper = mock(UserMapper.class);

        RememberLoginToken existing = new RememberLoginToken();
        existing.setId(17L);
        existing.setUserId(1001L);
        existing.setSelector("selector-revoked");
        existing.setValidatorHash("ignored");
        existing.setExpiresAt(java.time.LocalDateTime.now().plusDays(7));
        existing.setRevoked(1);
        when(mapper.selectOne(any())).thenReturn(existing);

        RememberLoginTokenServiceImpl service = new RememberLoginTokenServiceImpl(mapper, userMapper);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setCookies(new Cookie("remember_me", "selector-revoked:validator-1"));
        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean restored = service.restoreSessionIfPossible(request, response);

        assertThat(restored).isFalse();
        assertThat(request.getSession(false)).isNull();
        Cookie rememberCookie = response.getCookie("remember_me");
        assertThat(rememberCookie).isNotNull();
        assertThat(rememberCookie.getValue()).isEmpty();
        assertThat(rememberCookie.getMaxAge()).isZero();
    }

    @Test
    void restoreSessionIfPossibleShouldRevokeTokenAndClearCookieWhenValidatorMismatch() {
        RememberLoginTokenMapper mapper = mock(RememberLoginTokenMapper.class);
        UserMapper userMapper = mock(UserMapper.class);

        RememberLoginToken existing = new RememberLoginToken();
        existing.setId(18L);
        existing.setUserId(1001L);
        existing.setSelector("selector-mismatch");
        existing.setValidatorHash("another-hash");
        existing.setExpiresAt(java.time.LocalDateTime.now().plusDays(7));
        existing.setRevoked(0);
        when(mapper.selectOne(any())).thenReturn(existing);

        RememberLoginTokenServiceImpl service = new RememberLoginTokenServiceImpl(mapper, userMapper);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setCookies(new Cookie("remember_me", "selector-mismatch:validator-1"));
        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean restored = service.restoreSessionIfPossible(request, response);

        assertThat(restored).isFalse();
        ArgumentCaptor<RememberLoginToken> tokenCaptor = ArgumentCaptor.forClass(RememberLoginToken.class);
        verify(mapper).updateById(tokenCaptor.capture());
        assertThat(tokenCaptor.getValue().getRevoked()).isEqualTo(1);
        Cookie rememberCookie = response.getCookie("remember_me");
        assertThat(rememberCookie).isNotNull();
        assertThat(rememberCookie.getValue()).isEmpty();
        assertThat(rememberCookie.getMaxAge()).isZero();
    }

    @Test
    void restoreSessionIfPossibleShouldRevokeTokenAndClearCookieWhenUserMissing() {
        RememberLoginTokenMapper mapper = mock(RememberLoginTokenMapper.class);
        UserMapper userMapper = mock(UserMapper.class);

        RememberLoginToken existing = new RememberLoginToken();
        existing.setId(19L);
        existing.setUserId(1001L);
        existing.setSelector("selector-user-missing");
        existing.setExpiresAt(java.time.LocalDateTime.now().plusDays(7));
        existing.setRevoked(0);

        RememberLoginTokenServiceImpl service = new RememberLoginTokenServiceImpl(mapper, userMapper);
        String validHash = ReflectionTestUtils.invokeMethod(service, "sha256Hex", "validator-1");
        existing.setValidatorHash(validHash);
        when(mapper.selectOne(any())).thenReturn(existing);
        when(userMapper.selectById(1001L)).thenReturn(null);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setCookies(new Cookie("remember_me", "selector-user-missing:validator-1"));
        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean restored = service.restoreSessionIfPossible(request, response);

        assertThat(restored).isFalse();
        ArgumentCaptor<RememberLoginToken> tokenCaptor = ArgumentCaptor.forClass(RememberLoginToken.class);
        verify(mapper).updateById(tokenCaptor.capture());
        assertThat(tokenCaptor.getValue().getRevoked()).isEqualTo(1);
        Cookie rememberCookie = response.getCookie("remember_me");
        assertThat(rememberCookie).isNotNull();
        assertThat(rememberCookie.getValue()).isEmpty();
        assertThat(rememberCookie.getMaxAge()).isZero();
    }
}
